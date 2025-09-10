package com.basketball.referee.repository;

import com.basketball.referee.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    List<Match> findByTournamentId(Long tournamentId);
    
    List<Match> findByCourtId(Long courtId);
    
    List<Match> findByState(Match.MatchState state);
    
    @Query("SELECT p FROM Match p WHERE p.dateHour BETWEEN :start AND :end ORDER BY p.dateHour")
    List<Match> findByDateHourBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT p FROM Match p WHERE p.dateHour >= :date ORDER BY p.dateHour")
    List<Match> findUpcomingMatches(@Param("date") LocalDateTime date);
    
    @Query("SELECT p FROM Match p JOIN p.assignments a WHERE a.referee.id = :refereeId ORDER BY p.dateHour DESC")
    List<Match> findByRefereeId(@Param("refereeId") Long refereeId);
    
    @Query("SELECT COUNT(p) FROM Match p WHERE p.state = :state")
    Long countByState(@Param("state") Match.MatchState state);
}
