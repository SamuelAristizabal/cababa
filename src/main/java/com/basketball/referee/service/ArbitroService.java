package com.basketball.referee.service;

import com.basketball.referee.entity.Arbitro;
import com.basketball.referee.entity.User;
import com.basketball.referee.service.UserService;
import com.basketball.referee.repository.ArbitroRepository;
import com.basketball.referee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ArbitroService {

    @Autowired
    private ArbitroRepository arbitroRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final String uploadDir = "uploads/photos/";

    public List<Arbitro> findAll() {
        return arbitroRepository.findAll();
    }

    public List<Arbitro> findAllActive() {
        return arbitroRepository.findByActivoTrue();
    }

    public Optional<Arbitro> findById(Long id) {
        return arbitroRepository.findById(id);
    }

    public Optional<Arbitro> findByUserId(Long userId) {
        return arbitroRepository.findByUserId(userId);
    }

    public Optional<Arbitro> findByDocumento(String documento) {
        return arbitroRepository.findByDocumento(documento);
    }

    public List<Arbitro> findByFilters(String search, String escalafon, String especialidad, String activo) {
        return arbitroRepository.findByFilters(
            (search != null && !search.trim().isEmpty()) ? search : null,
            (escalafon != null && !escalafon.trim().isEmpty()) ? escalafon : null,
            (especialidad != null && !especialidad.trim().isEmpty()) ? especialidad : null,
            (activo != null && !activo.trim().isEmpty()) ? Boolean.parseBoolean(activo) : null
        );
    }

    public List<Arbitro> findByEspecialidad(Arbitro.Especialidad especialidad) {
        return arbitroRepository.findByEspecialidadOrAmbos(especialidad);
    }

    public List<Arbitro> findByEscalafon(Arbitro.Escalafon escalafon) {
        return arbitroRepository.findByEscalafon(escalafon);
    }

    public List<Arbitro> findMostActiveReferees() {
        return arbitroRepository.findMostActiveReferees();
    }

    public Arbitro save(Arbitro arbitro) {
        return arbitroRepository.save(arbitro);
    }

    public Arbitro createArbitro(Arbitro arbitro, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            arbitro.setUser(userOpt.get());
            arbitro.setActivo(true);
            return arbitroRepository.save(arbitro);
        }
        throw new RuntimeException("Usuario no encontrado");
    }

    public Arbitro createArbitro(Arbitro arbitro, MultipartFile foto) {
        try {
            String photoUrl = uploadPhoto(foto);
            arbitro.setFotoUrl(photoUrl);
            arbitro.setActivo(true);
            userService.createReferee(arbitro.getUser());
            return arbitroRepository.save(arbitro);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    public Arbitro updateArbitro(Long id, Arbitro arbitroDetails, MultipartFile photo) throws IOException {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(id);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            arbitro.getUser().setFirstName(arbitroDetails.getUser().getFirstName());
            arbitro.getUser().setLastName(arbitroDetails.getUser().getLastName());
            arbitro.getUser().setEmail(arbitroDetails.getUser().getEmail());
            userRepository.save(arbitro.getUser());
            arbitro.setDocumento(arbitroDetails.getDocumento());
            arbitro.setTelefono(arbitroDetails.getTelefono());
            arbitro.setDireccion(arbitroDetails.getDireccion());
            arbitro.setFechaNacimiento(arbitroDetails.getFechaNacimiento());
            arbitro.setEspecialidad(arbitroDetails.getEspecialidad());
            arbitro.setEscalafon(arbitroDetails.getEscalafon());
            arbitro.setObservaciones(arbitroDetails.getObservaciones());
            arbitro.setFotoUrl(uploadPhoto(photo));
            return arbitroRepository.save(arbitro);
        }
        throw new RuntimeException("Árbitro no encontrado");
    }

    public Arbitro updateArbitro(Long id, Arbitro arbitroDetails) throws IOException {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(id);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            arbitro.setDocumento(arbitroDetails.getDocumento());
            arbitro.setTelefono(arbitroDetails.getTelefono());
            arbitro.setDireccion(arbitroDetails.getDireccion());
            arbitro.setFechaNacimiento(arbitroDetails.getFechaNacimiento());
            arbitro.setEspecialidad(arbitroDetails.getEspecialidad());
            arbitro.setEscalafon(arbitroDetails.getEscalafon());
            arbitro.setObservaciones(arbitroDetails.getObservaciones());
            return arbitroRepository.save(arbitro);
        }
        throw new RuntimeException("Árbitro no encontrado");
    }

    public String uploadPhoto(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return "/uploads/photos/" + filename;
    }

    public void updatePhoto(Long arbitroId, MultipartFile file) throws IOException {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            String photoUrl = uploadPhoto(file);
            if (photoUrl != null) {
                arbitro.setFotoUrl(photoUrl);
                arbitroRepository.save(arbitro);
            }
        }
    }

    public void toggleStatus(Long id) {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(id);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            arbitro.setActivo(!arbitro.isActivo());
            arbitroRepository.save(arbitro);
        }
    }

    public void deleteById(Long id) {
        arbitroRepository.deleteById(id);
    }

    public boolean existsByDocumento(String documento) {
        return arbitroRepository.existsByDocumento(documento);
    }

    public long countActive() {
        return arbitroRepository.findByActivoTrue().size();
    }
}
