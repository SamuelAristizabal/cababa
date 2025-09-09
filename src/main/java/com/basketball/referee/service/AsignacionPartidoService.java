package com.basketball.referee.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.basketball.referee.entity.Arbitro;
import com.basketball.referee.entity.AsignacionPartido;
import com.basketball.referee.entity.Partido;
import com.basketball.referee.repository.AsignacionPartidoRepository;

@Service
@Transactional
public class AsignacionPartidoService {

    @Autowired
    private AsignacionPartidoRepository asignacionRepository;

    public List<AsignacionPartido> findAll() {
        return asignacionRepository.findAll();
    }

    public Optional<AsignacionPartido> findById(Long id) {
        return asignacionRepository.findById(id);
    }

    public List<AsignacionPartido> findByArbitroId(Long arbitroId) {
        return asignacionRepository.findByArbitroId(arbitroId);
    }

    public List<AsignacionPartido> findByPartidoId(Long partidoId) {
        return asignacionRepository.findByPartidoId(partidoId);
    }

    public List<AsignacionPartido> findByArbitroAndEstado(Long arbitroId, AsignacionPartido.EstadoAsignacion estado) {
        return asignacionRepository.findByArbitroIdAndEstado(arbitroId, estado);
    }

    public List<AsignacionPartido> findPendingByArbitro(Long arbitroId) {
        return asignacionRepository.findByArbitroIdAndEstado(arbitroId, AsignacionPartido.EstadoAsignacion.PENDIENTE);
    }

    public List<AsignacionPartido> findAcceptedByArbitro(Long arbitroId) {
        return asignacionRepository.findByArbitroIdAndEstado(arbitroId, AsignacionPartido.EstadoAsignacion.ACEPTADA);
    }

    public List<AsignacionPartido> findByArbitroAndMonth(Long arbitroId, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return asignacionRepository.findByArbitroAndDateRange(arbitroId, startOfMonth, endOfMonth);
    }

    public List<AsignacionPartido> findCompletedByArbitro(Long arbitroId) {
        return asignacionRepository.findCompletedAssignmentsByArbitro(arbitroId);
    }

    public AsignacionPartido save(AsignacionPartido asignacion) {
        return asignacionRepository.save(asignacion);
    }

    public AsignacionPartido createAsignacion(Partido partido, Arbitro arbitro, AsignacionPartido.RolArbitro rol) {
        AsignacionPartido asignacion = new AsignacionPartido();
        asignacion.setPartido(partido);
        asignacion.setArbitro(arbitro);
        asignacion.setRol(rol);
        asignacion.setEstado(AsignacionPartido.EstadoAsignacion.PENDIENTE);
        return asignacionRepository.save(asignacion);
    }

    public void acceptAssignment(Long asignacionId) {
        Optional<AsignacionPartido> asignacionOpt = asignacionRepository.findById(asignacionId);
        if (asignacionOpt.isPresent()) {
            AsignacionPartido asignacion = asignacionOpt.get();
            asignacion.setEstado(AsignacionPartido.EstadoAsignacion.ACEPTADA);
            asignacion.setFechaRespuesta(LocalDateTime.now());
            asignacionRepository.save(asignacion);
        }
    }

    public void rejectAssignment(Long asignacionId) {
        Optional<AsignacionPartido> asignacionOpt = asignacionRepository.findById(asignacionId);
        if (asignacionOpt.isPresent()) {
            AsignacionPartido asignacion = asignacionOpt.get();
            asignacion.setEstado(AsignacionPartido.EstadoAsignacion.RECHAZADA);
            asignacion.setFechaRespuesta(LocalDateTime.now());
            asignacionRepository.save(asignacion);
        }
    }

    public void completeAssignment(Long asignacionId) {
        Optional<AsignacionPartido> asignacionOpt = asignacionRepository.findById(asignacionId);
        if (asignacionOpt.isPresent()) {
            AsignacionPartido asignacion = asignacionOpt.get();
            asignacion.setEstado(AsignacionPartido.EstadoAsignacion.COMPLETADA);
            asignacionRepository.save(asignacion);
        }
    }

    public long countByEstado(AsignacionPartido.EstadoAsignacion estado) {
        return asignacionRepository.countByEstado(estado);
    }

    public long countPendingByArbitro(Long arbitroId) {
        return asignacionRepository.findByArbitroIdAndEstado(arbitroId, AsignacionPartido.EstadoAsignacion.PENDIENTE).size();
    }

    public long countAcceptedByArbitro(Long arbitroId) {
        return asignacionRepository.findByArbitroIdAndEstado(arbitroId, AsignacionPartido.EstadoAsignacion.ACEPTADA).size();
    }

    public void deleteById(Long id) {
        asignacionRepository.deleteById(id);
    }

    public List<AsignacionPartido> findByPartido(Long partidoId) {
        return asignacionRepository.findByPartidoId(partidoId);
    }

    public void assignReferees(Long partidoId, List<Long> arbitroIds, List<String> roles) {
        // First, remove existing assignments for this match
        List<AsignacionPartido> existingAssignments = findByPartido(partidoId);
        existingAssignments.forEach(assignment -> deleteById(assignment.getId()));

        // Create new assignments
        for (int i = 0; i < arbitroIds.size(); i++) {
            AsignacionPartido asignacion = new AsignacionPartido();
            asignacion.setPartido(new Partido()); // Will be set by repository
            asignacion.getPartido().setId(partidoId);
            asignacion.setArbitro(new Arbitro());
            asignacion.getArbitro().setId(arbitroIds.get(i));
            asignacion.setRol(AsignacionPartido.RolArbitro.valueOf(roles.get(i)));
            asignacion.setEstado(AsignacionPartido.EstadoAsignacion.PENDIENTE);
            asignacionRepository.save(asignacion);
        }
    }

    public void cancelAllAssignments(Long partidoId) {
        List<AsignacionPartido> assignments = findByPartido(partidoId);
        assignments.forEach(assignment -> {
            assignment.setEstado(AsignacionPartido.EstadoAsignacion.RECHAZADA);
            asignacionRepository.save(assignment);
        });
    }
}
