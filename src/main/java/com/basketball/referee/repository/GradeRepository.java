package com.basketball.referee.repository;

import com.basketball.referee.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    List<Grade> findByRefereeId(Long refereeId);
    
    List<Grade> findByAssignmentId(Long assignmentId);
    
    @Query("SELECT AVG(c.score) FROM Grade c WHERE c.referee.id = :refereeId")
    Double findAverageRatingByReferee(@Param("refereeId") Long refereeId);
    
    @Query("SELECT c FROM Grade c WHERE c.referee.id = :refereeId ORDER BY c.createdAt DESC")
    List<Grade> findByRefereeOrderByCreatedAtDesc(@Param("refereeId") Long refereeId);
    
    boolean existsByAssignmentId(Long assignmentId);
}
