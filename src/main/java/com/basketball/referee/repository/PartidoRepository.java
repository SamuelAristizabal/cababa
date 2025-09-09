package com.basketball.referee.repository;

import com.basketball.referee.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    
    List<Partido> findByTorneoId(Long torneoId);
    
    List<Partido> findByCanchaId(Long canchaId);
    
    List<Partido> findByEstado(Partido.EstadoPartido estado);
    
    @Query("SELECT p FROM Partido p WHERE p.fechaHora BETWEEN :inicio AND :fin ORDER BY p.fechaHora")
    List<Partido> findByFechaHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT p FROM Partido p WHERE p.fechaHora >= :fecha ORDER BY p.fechaHora")
    List<Partido> findUpcomingMatches(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT p FROM Partido p JOIN p.asignaciones a WHERE a.arbitro.id = :arbitroId ORDER BY p.fechaHora DESC")
    List<Partido> findByArbitroId(@Param("arbitroId") Long arbitroId);
    
    @Query("SELECT COUNT(p) FROM Partido p WHERE p.estado = :estado")
    Long countByEstado(@Param("estado") Partido.EstadoPartido estado);
}
