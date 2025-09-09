package com.basketball.referee.repository;

import com.basketball.referee.entity.AsignacionPartido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsignacionPartidoRepository extends JpaRepository<AsignacionPartido, Long> {
    
    List<AsignacionPartido> findByArbitroId(Long arbitroId);
    
    List<AsignacionPartido> findByPartidoId(Long partidoId);
    
    List<AsignacionPartido> findByEstado(AsignacionPartido.EstadoAsignacion estado);
    
    List<AsignacionPartido> findByArbitroIdAndEstado(Long arbitroId, AsignacionPartido.EstadoAsignacion estado);
    
    @Query("SELECT a FROM AsignacionPartido a WHERE a.arbitro.id = :arbitroId AND a.partido.fechaHora BETWEEN :inicio AND :fin")
    List<AsignacionPartido> findByArbitroAndDateRange(@Param("arbitroId") Long arbitroId, 
                                                      @Param("inicio") LocalDateTime inicio, 
                                                      @Param("fin") LocalDateTime fin);
    
    @Query("SELECT COUNT(a) FROM AsignacionPartido a WHERE a.estado = :estado")
    Long countByEstado(@Param("estado") AsignacionPartido.EstadoAsignacion estado);
    
    @Query("SELECT a FROM AsignacionPartido a WHERE a.arbitro.id = :arbitroId AND a.estado = 'ACEPTADA' AND a.partido.estado = 'FINALIZADO'")
    List<AsignacionPartido> findCompletedAssignmentsByArbitro(@Param("arbitroId") Long arbitroId);
}
