package com.basketball.referee.repository;

import com.basketball.referee.entity.Tarifa;
import com.basketball.referee.entity.Arbitro;
import com.basketball.referee.entity.AsignacionPartido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    
    List<Tarifa> findByTorneoId(Long torneoId);
    
    List<Tarifa> findByActivaTrue();
    
    List<Tarifa> findByTorneoIdAndActivaTrue(Long torneoId);

    @Query("SELECT t FROM Tarifa t WHERE t.escalafon = :escalafon")
    List<Tarifa> findByEscalafon(Arbitro.Escalafon escalafon);
    
    Optional<Tarifa> findByTorneoIdAndEscalafonAndRol(Long torneoId, 
                                                      Arbitro.Escalafon escalafon, 
                                                      AsignacionPartido.RolArbitro rol);
    
    @Query("SELECT t FROM Tarifa t WHERE t.torneo.id = :torneoId AND t.escalafon = :escalafon AND t.rol = :rol AND t.activa = true")
    Optional<Tarifa> findActiveTarifa(@Param("torneoId") Long torneoId, 
                                      @Param("escalafon") Arbitro.Escalafon escalafon, 
                                      @Param("rol") AsignacionPartido.RolArbitro rol);
}
