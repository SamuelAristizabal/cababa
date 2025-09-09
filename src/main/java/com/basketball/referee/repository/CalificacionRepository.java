package com.basketball.referee.repository;

import com.basketball.referee.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    
    List<Calificacion> findByArbitroId(Long arbitroId);
    
    List<Calificacion> findByAsignacionId(Long asignacionId);
    
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.arbitro.id = :arbitroId")
    Double findAverageRatingByArbitro(@Param("arbitroId") Long arbitroId);
    
    @Query("SELECT c FROM Calificacion c WHERE c.arbitro.id = :arbitroId ORDER BY c.createdAt DESC")
    List<Calificacion> findByArbitroOrderByCreatedAtDesc(@Param("arbitroId") Long arbitroId);
    
    boolean existsByAsignacionId(Long asignacionId);
}
