package com.basketball.referee.service;

import com.basketball.referee.model.Referee;
import com.basketball.referee.model.User;
import com.basketball.referee.service.UserService;
import com.basketball.referee.repository.RefereeRepository;
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
public class RefereeService {

    @Autowired
    private RefereeRepository refereeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final String uploadDir = "uploads/photos/";

    public List<Referee> findAll() {
        return refereeRepository.findAll();
    }

    public List<Referee> findAllActive() {
        return refereeRepository.findByActiveTrue();
    }

    public Optional<Referee> findById(Long id) {
        return refereeRepository.findById(id);
    }

    public Optional<Referee> findByUserId(Long userId) {
        return refereeRepository.findByUserId(userId);
    }

    public Optional<Referee> findByDocument(String document) {
        return refereeRepository.findByDocument(document);
    }

    public List<Referee> findByFilters(String search, String rank, String specialty, String active) {
        return refereeRepository.findByFilters(
            (search != null && !search.trim().isEmpty()) ? search : null,
            (rank != null && !rank.trim().isEmpty()) ? rank : null,
            (specialty != null && !specialty.trim().isEmpty()) ? specialty : null,
            (active != null && !active.trim().isEmpty()) ? Boolean.parseBoolean(active) : null
        );
    }

    public List<Referee> findBySpecialty(Referee.Specialty specialty) {
        return refereeRepository.findBySpecialtyOrBoth(specialty);
    }

    public List<Referee> findByRank(Referee.Rank rank) {
        return refereeRepository.findByRank(rank);
    }

    public List<Referee> findMostActiveReferees() {
        return refereeRepository.findMostActiveReferees();
    }

    public Referee save(Referee referee) {
        return refereeRepository.save(referee);
    }

    public Referee createReferee(Referee referee, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            referee.setUser(userOpt.get());
            referee.setActive(true);
            return refereeRepository.save(referee);
        }
        throw new RuntimeException("Usuario no encontrado");
    }

    public Referee createReferee(Referee referee, MultipartFile foto) {
        try {
            String photoUrl = uploadPhoto(foto);
            referee.setFotoUrl(photoUrl);
            referee.setActive(true);
            userService.createReferee(referee.getUser());
            return refereeRepository.save(referee);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    public Referee updateReferee(Long id, Referee refereeDetails, MultipartFile photo) throws IOException {
        Optional<Referee> refereeOpt = refereeRepository.findById(id);
        if (refereeOpt.isPresent()) {
            Referee referee = refereeOpt.get();
            referee.getUser().setFirstName(refereeDetails.getUser().getFirstName());
            referee.getUser().setLastName(refereeDetails.getUser().getLastName());
            referee.getUser().setEmail(refereeDetails.getUser().getEmail());
            userRepository.save(referee.getUser());
            referee.setDocument(refereeDetails.getDocument());
            referee.setPhone(refereeDetails.getPhone());
            referee.setAddress(refereeDetails.getAddress());
            referee.setBirthDate(refereeDetails.getBirthDate());
            referee.setSpecialty(refereeDetails.getSpecialty());
            referee.setRank(refereeDetails.getRank());
            referee.setObservations(refereeDetails.getObservations());
            referee.setFotoUrl(uploadPhoto(photo));
            return refereeRepository.save(referee);
        }
        throw new RuntimeException("Árbitro no encontrado");
    }

    public Referee updateReferee(Long id, Referee refereeDetails) throws IOException {
        Optional<Referee> refereeOpt = refereeRepository.findById(id);
        if (refereeOpt.isPresent()) {
            Referee referee = refereeOpt.get();
            referee.setDocument(refereeDetails.getDocument());
            referee.setPhone(refereeDetails.getPhone());
            referee.setAddress(refereeDetails.getAddress());
            referee.setBirthDate(refereeDetails.getBirthDate());
            referee.setSpecialty(refereeDetails.getSpecialty());
            referee.setRank(refereeDetails.getRank());
            referee.setObservations(refereeDetails.getObservations());
            return refereeRepository.save(referee);
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

    public void updatePhoto(Long refereeId, MultipartFile file) throws IOException {
        Optional<Referee> refereeOpt = refereeRepository.findById(refereeId);
        if (refereeOpt.isPresent()) {
            Referee referee = refereeOpt.get();
            String photoUrl = uploadPhoto(file);
            if (photoUrl != null) {
                referee.setFotoUrl(photoUrl);
                refereeRepository.save(referee);
            }
        }
    }

    public void toggleStatus(Long id) {
        Optional<Referee> refereeOpt = refereeRepository.findById(id);
        if (refereeOpt.isPresent()) {
            Referee referee = refereeOpt.get();
            referee.setActive(!referee.isActive());
            refereeRepository.save(referee);
        }
    }

    public void deleteById(Long id) {
        refereeRepository.deleteById(id);
    }

    public boolean existsByDocument(String document) {
        return refereeRepository.existsByDocument(document);
    }

    public long countActive() {
        return refereeRepository.findByActiveTrue().size();
    }
}
