package com.basketball.referee.repository;

import com.basketball.referee.model.Fee;
import com.basketball.referee.model.Referee;
import com.basketball.referee.model.MatchAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    
    List<Fee> findByTournamentId(Long tournamentId);
    
    List<Fee> findByActiveTrue();
    
    List<Fee> findByTournamentIdAndActiveTrue(Long tournamentId);

    @Query("SELECT t FROM Fee t WHERE t.rank = :rank")
    List<Fee> findByRank(Referee.Rank rank);
    
    Optional<Fee> findByTournamentIdAndRankAndRole(Long tournamentId, 
                                                      Referee.Rank rank, 
                                                      MatchAssignment.RefereeRole role);
    
    @Query("SELECT t FROM Fee t WHERE t.tournament.id = :tournamentId AND t.rank = :rank AND t.role = :role AND t.active = true")
    Optional<Fee> findActiveFee(@Param("tournamentId") Long tournamentId, 
                                      @Param("rank") Referee.Rank rank, 
                                      @Param("role") MatchAssignment.RefereeRole role);
}
