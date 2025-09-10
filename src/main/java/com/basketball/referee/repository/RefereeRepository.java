package com.basketball.referee.repository;

import com.basketball.referee.model.Referee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Long> {
    
    Optional<Referee> findByUserId(Long userId);
    
    Optional<Referee> findByDocument(String document);

    @Query("""
        SELECT a FROM Referee a
        WHERE (:search IS NULL OR 
               LOWER(a.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.document) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:rank IS NULL OR a.rank = :rank)
          AND (:specialty IS NULL OR a.specialty = :specialty)
          AND (:active IS NULL OR a.active = :active)
    """)
    List<Referee> findByFilters(String search, String rank, String specialty, Boolean active);
    
    List<Referee> findByActiveTrue();
    
    List<Referee> findBySpecialty(Referee.Specialty specialty);
    
    List<Referee> findByRank(Referee.Rank rank);
    
    @Query("SELECT a FROM Referee a WHERE a.active = true AND (a.specialty = :specialty OR a.specialty = 'BOTH')")
    List<Referee> findBySpecialtyOrBoth(@Param("specialty") Referee.Specialty specialty);
    
    @Query("SELECT a FROM Referee a JOIN a.assignments asig WHERE asig.state = 'ACCEPTED' GROUP BY a ORDER BY COUNT(asig) DESC")
    List<Referee> findMostActiveReferees();
    
    boolean existsByDocument(String document);
}
