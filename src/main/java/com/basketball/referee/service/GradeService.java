package com.basketball.referee.service;

import com.basketball.referee.model.Grade;
import com.basketball.referee.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }

    public List<Grade> findByReferee(Long refereeId) {
        return gradeRepository.findByRefereeId(refereeId);
    }

    public Double findAverageByReferee(Long refereeId) {
        return gradeRepository.findAverageRatingByReferee(refereeId);
    }

    public Grade save(Grade grade) {
        return gradeRepository.save(grade);
    }

    public void delete(Long id) {
        gradeRepository.deleteById(id);
    }

}
