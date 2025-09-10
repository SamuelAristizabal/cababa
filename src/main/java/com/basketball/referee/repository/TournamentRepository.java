package com.basketball.referee.repository;

import com.basketball.referee.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    
    List<Tournament> findByActiveTrue();
    
    List<Tournament> findByState(Tournament.TournamentState state);
    
    List<Tournament> findByActiveTrueAndState(Tournament.TournamentState state);
    
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND t.startDate <= :date AND t.endDate >= :date")
    List<Tournament> findActiveByDate(LocalDate date);
    
    @Query("SELECT t FROM Tournament t WHERE t.active = true AND t.state = 'IN_PROGRESS'")
    List<Tournament> findCurrentTournaments();
    
    List<Tournament> findByNameContainingIgnoreCase(String name);
}
