package com.basketball.referee.repository;

import com.basketball.referee.entity.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanchaRepository extends JpaRepository<Cancha, Long> {
    
    List<Cancha> findByActivaTrue();
    
    List<Cancha> findByCiudadIgnoreCase(String ciudad);
    
    List<Cancha> findByNombreContainingIgnoreCase(String nombre);
    
    List<Cancha> findByActivaTrueOrderByNombre();
}
