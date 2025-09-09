package com.basketball.referee.service;

import com.basketball.referee.entity.Torneo;
import com.basketball.referee.repository.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepository;

    public List<Torneo> findAll() {
        return torneoRepository.findAll();
    }

    public List<Torneo> findAllActive() {
        return torneoRepository.findByActivoTrue();
    }

    public List<Torneo> findActive() {
        return findAllActive();
    }

    public Optional<Torneo> findById(Long id) {
        return torneoRepository.findById(id);
    }

    public List<Torneo> findByEstado(Torneo.EstadoTorneo estado) {
        return torneoRepository.findByEstado(estado);
    }

    public List<Torneo> findCurrentTournaments() {
        return torneoRepository.findCurrentTournaments();
    }

    public List<Torneo> findActiveByDate(LocalDate fecha) {
        return torneoRepository.findActiveByDate(fecha);
    }

    public List<Torneo> searchByName(String nombre) {
        return torneoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Torneo save(Torneo torneo) {
        return torneoRepository.save(torneo);
    }

    public Torneo create(Torneo torneo) {
        torneo.setActivo(true);
        torneo.setEstado(torneo.getEstado());
        return torneoRepository.save(torneo);
    }

    public Torneo update(Long id, Torneo torneoDetails) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(id);
        if (torneoOpt.isPresent()) {
            Torneo torneo = torneoOpt.get();
            torneo.setNombre(torneoDetails.getNombre());
            torneo.setDescripcion(torneoDetails.getDescripcion());
            torneo.setFechaInicio(torneoDetails.getFechaInicio());
            torneo.setFechaFin(torneoDetails.getFechaFin());
            torneo.setEstado(torneoDetails.getEstado());
            return torneoRepository.save(torneo);
        }
        throw new RuntimeException("Torneo no encontrado");
    }

    public void toggleStatus(Long id) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(id);
        if (torneoOpt.isPresent()) {
            Torneo torneo = torneoOpt.get();
            torneo.setActivo(!torneo.isActivo());
            torneoRepository.save(torneo);
        }
    }

    public void updateEstado(Long id, Torneo.EstadoTorneo nuevoEstado) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(id);
        if (torneoOpt.isPresent()) {
            Torneo torneo = torneoOpt.get();
            torneo.setEstado(nuevoEstado);
            torneoRepository.save(torneo);
        }
    }

    public void delete(Long id) {
        deleteById(id);
    }

    public void deleteById(Long id) {
        torneoRepository.deleteById(id);
    }

    public long countActive() {
        return torneoRepository.findByActivoTrue().size();
    }

    public long countByEstado(Torneo.EstadoTorneo estado) {
        return torneoRepository.findByEstado(estado).size();
    }

    public List<Torneo> findByFilters(String search, String estado, String year) {
        List<Torneo> torneos = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            torneos = torneos.stream()
                .filter(t -> t.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                           (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
        }
        
        if (estado != null && !estado.trim().isEmpty()) {
            Torneo.EstadoTorneo estadoEnum = Torneo.EstadoTorneo.valueOf(estado);
            torneos = torneos.stream()
                .filter(t -> t.getEstado().equals(estadoEnum))
                .collect(Collectors.toList());
        }
        
        if (year != null && !year.trim().isEmpty()) {
            int yearInt = Integer.parseInt(year);
            torneos = torneos.stream()
                .filter(t -> t.getFechaInicio().getYear() == yearInt)
                .collect(Collectors.toList());
        }
        
        return torneos;
    }

    public List<Integer> getAvailableYears() {
        return findAll().stream()
            .map(t -> t.getFechaInicio().getYear())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
