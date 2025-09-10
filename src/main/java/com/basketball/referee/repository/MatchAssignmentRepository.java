package com.basketball.referee.repository;

import com.basketball.referee.model.MatchAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchAssignmentRepository extends JpaRepository<MatchAssignment, Long> {
    
    List<MatchAssignment> findByRefereeId(Long refereeId);
    
    List<MatchAssignment> findByMatchId(Long matchId);
    
    List<MatchAssignment> findByState(MatchAssignment.AssignmentState state);
    
    List<MatchAssignment> findByRefereeIdAndState(Long refereeId, MatchAssignment.AssignmentState state);
    
    @Query("SELECT a FROM MatchAssignment a WHERE a.referee.id = :refereeId AND a.match.dateHour BETWEEN :start AND :end")
    List<MatchAssignment> findByRefereeAndDateRange(@Param("refereeId") Long refereeId, 
                                                      @Param("start") LocalDateTime start, 
                                                      @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(a) FROM MatchAssignment a WHERE a.state = :state")
    Long countByState(@Param("state") MatchAssignment.AssignmentState state);
    
    @Query("SELECT a FROM MatchAssignment a WHERE a.referee.id = :refereeId AND a.state = 'ACCEPTED' AND a.match.state = 'FINISHED'")
    List<MatchAssignment> findCompletedAssignmentsByReferee(@Param("refereeId") Long refereeId);
}
