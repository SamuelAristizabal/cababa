package com.basketball.referee.repository;

import com.basketball.referee.entity.Arbitro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArbitroRepository extends JpaRepository<Arbitro, Long> {
    
    Optional<Arbitro> findByUserId(Long userId);
    
    Optional<Arbitro> findByDocumento(String documento);

    @Query("""
        SELECT a FROM Arbitro a
        WHERE (:search IS NULL OR 
               LOWER(a.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.documento) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:escalafon IS NULL OR a.escalafon = :escalafon)
          AND (:especialidad IS NULL OR a.especialidad = :especialidad)
          AND (:activo IS NULL OR a.activo = :activo)
    """)
    List<Arbitro> findByFilters(String search, String escalafon, String especialidad, Boolean activo);
    
    List<Arbitro> findByActivoTrue();
    
    List<Arbitro> findByEspecialidad(Arbitro.Especialidad especialidad);
    
    List<Arbitro> findByEscalafon(Arbitro.Escalafon escalafon);
    
    @Query("SELECT a FROM Arbitro a WHERE a.activo = true AND (a.especialidad = :especialidad OR a.especialidad = 'AMBOS')")
    List<Arbitro> findByEspecialidadOrAmbos(@Param("especialidad") Arbitro.Especialidad especialidad);
    
    @Query("SELECT a FROM Arbitro a JOIN a.asignaciones asig WHERE asig.estado = 'ACEPTADA' GROUP BY a ORDER BY COUNT(asig) DESC")
    List<Arbitro> findMostActiveReferees();
    
    boolean existsByDocumento(String documento);
}
