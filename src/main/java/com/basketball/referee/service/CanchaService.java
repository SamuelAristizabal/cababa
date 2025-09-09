package com.basketball.referee.service;

import com.basketball.referee.entity.Cancha;
import com.basketball.referee.repository.CanchaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CanchaService {

    @Autowired
    private CanchaRepository canchaRepository;

    public List<Cancha> findAll() {
        return canchaRepository.findAll();
    }

    public List<Cancha> findAllActive() {
        return canchaRepository.findByActivaTrue();
    }

    public List<Cancha> findActive() {
        return findAllActive();
    }

    public List<Cancha> findAllActiveOrdered() {
        return canchaRepository.findByActivaTrueOrderByNombre();
    }

    public Optional<Cancha> findById(Long id) {
        return canchaRepository.findById(id);
    }

    public List<Cancha> findByCiudad(String ciudad) {
        return canchaRepository.findByCiudadIgnoreCase(ciudad);
    }

    public List<Cancha> searchByName(String nombre) {
        return canchaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Cancha save(Cancha cancha) {
        return canchaRepository.save(cancha);
    }

    public Cancha create(Cancha cancha) {
        cancha.setActiva(true);
        return canchaRepository.save(cancha);
    }

    public Cancha update(Long id, Cancha canchaDetails) {
        Optional<Cancha> canchaOpt = canchaRepository.findById(id);
        if (canchaOpt.isPresent()) {
            Cancha cancha = canchaOpt.get();
            cancha.setNombre(canchaDetails.getNombre());
            cancha.setCiudad(canchaDetails.getCiudad()); // Fixed field name to match entity
            cancha.setDireccion(canchaDetails.getDireccion()); // Added capacity field
            return canchaRepository.save(cancha);
        }
        throw new RuntimeException("Cancha no encontrada");
    }

    public void toggleStatus(Long id) {
        Optional<Cancha> canchaOpt = canchaRepository.findById(id);
        if (canchaOpt.isPresent()) {
            Cancha cancha = canchaOpt.get();
            cancha.setActiva(!cancha.isActiva());
            canchaRepository.save(cancha);
        }
    }

    public void deleteById(Long id) {
        canchaRepository.deleteById(id);
    }

    public long countActive() {
        return canchaRepository.findByActivaTrue().size();
    }

    public List<Cancha> findByFilters(String search, String activa) {
        List<Cancha> canchas = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            canchas = canchas.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (activa != null && !activa.trim().isEmpty()) {
            boolean isActive = Boolean.parseBoolean(activa);
            canchas = canchas.stream()
                .filter(c -> c.isActiva() == isActive)
                .collect(Collectors.toList());
        }
        
        return canchas;
    }
}
