package com.basketball.referee.repository;

import com.basketball.referee.model.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    
    List<Court> findByActiveTrue();
    
    List<Court> findByCityIgnoreCase(String city);
    
    List<Court> findByNameContainingIgnoreCase(String name);
    
    List<Court> findByActiveTrueOrderByName();
}
