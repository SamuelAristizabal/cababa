package com.basketball.referee.repository;

import com.basketball.referee.entity.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    
    List<Torneo> findByActivoTrue();
    
    List<Torneo> findByEstado(Torneo.EstadoTorneo estado);
    
    List<Torneo> findByActivoTrueAndEstado(Torneo.EstadoTorneo estado);
    
    @Query("SELECT t FROM Torneo t WHERE t.activo = true AND t.fechaInicio <= :fecha AND t.fechaFin >= :fecha")
    List<Torneo> findActiveByDate(LocalDate fecha);
    
    @Query("SELECT t FROM Torneo t WHERE t.activo = true AND t.estado = 'EN_CURSO'")
    List<Torneo> findCurrentTournaments();
    
    List<Torneo> findByNombreContainingIgnoreCase(String nombre);
}
