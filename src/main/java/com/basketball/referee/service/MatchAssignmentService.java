package com.basketball.referee.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.basketball.referee.model.Referee;
import com.basketball.referee.model.MatchAssignment;
import com.basketball.referee.model.Match;
import com.basketball.referee.repository.MatchAssignmentRepository;

@Service
@Transactional
public class MatchAssignmentService {

    @Autowired
    private MatchAssignmentRepository assignmentRepository;

    public List<MatchAssignment> findAll() {
        return assignmentRepository.findAll();
    }

    public Optional<MatchAssignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }

    public List<MatchAssignment> findByRefereeId(Long refereeId) {
        return assignmentRepository.findByRefereeId(refereeId);
    }

    public List<MatchAssignment> findByMatchId(Long matchId) {
        return assignmentRepository.findByMatchId(matchId);
    }

    public List<MatchAssignment> findByRefereeAndState(Long refereeId, MatchAssignment.AssignmentState state) {
        return assignmentRepository.findByRefereeIdAndState(refereeId, state);
    }

    public List<MatchAssignment> findPendingByReferee(Long refereeId) {
        return assignmentRepository.findByRefereeIdAndState(refereeId, MatchAssignment.AssignmentState.PENDING);
    }

    public List<MatchAssignment> findAcceptedByReferee(Long refereeId) {
        return assignmentRepository.findByRefereeIdAndState(refereeId, MatchAssignment.AssignmentState.ACCEPTED);
    }

    public List<MatchAssignment> findByRefereeAndMonth(Long refereeId, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return assignmentRepository.findByRefereeAndDateRange(refereeId, startOfMonth, endOfMonth);
    }

    public List<MatchAssignment> findCompletedByReferee(Long refereeId) {
        return assignmentRepository.findCompletedAssignmentsByReferee(refereeId);
    }

    public MatchAssignment save(MatchAssignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public MatchAssignment createAssignment(Match match, Referee referee, MatchAssignment.RefereeRole role) {
        MatchAssignment assignment = new MatchAssignment();
        assignment.setMatch(match);
        assignment.setReferee(referee);
        assignment.setRole(role);
        assignment.setState(MatchAssignment.AssignmentState.PENDING);
        return assignmentRepository.save(assignment);
    }

    public void acceptAssignment(Long assignmentId) {
        Optional<MatchAssignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isPresent()) {
            MatchAssignment assignment = assignmentOpt.get();
            assignment.setState(MatchAssignment.AssignmentState.ACCEPTED);
            assignment.setResponseDate(LocalDateTime.now());
            assignmentRepository.save(assignment);
        }
    }

    public void acceptAssignment(Long assignmentId, String comments) {
        Optional<MatchAssignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isPresent()) {
            MatchAssignment assignment = assignmentOpt.get();
            assignment.setState(MatchAssignment.AssignmentState.ACCEPTED);
            assignment.setResponseDate(LocalDateTime.now());
            assignment.setComments(comments);
            assignmentRepository.save(assignment);
        }
    }

    public void rejectAssignment(Long assignmentId) {
        Optional<MatchAssignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isPresent()) {
            MatchAssignment assignment = assignmentOpt.get();
            assignment.setState(MatchAssignment.AssignmentState.REJECTED);
            assignment.setResponseDate(LocalDateTime.now());
            assignmentRepository.save(assignment);
        }
    }
    public void rejectAssignment(Long assignmentId, String comments) {
        Optional<MatchAssignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isPresent()) {
            MatchAssignment assignment = assignmentOpt.get();
            assignment.setState(MatchAssignment.AssignmentState.REJECTED);
            assignment.setResponseDate(LocalDateTime.now());
            assignment.setComments(comments);
            assignmentRepository.save(assignment);
        }
    }

    public void completeAssignment(Long assignmentId) {
        Optional<MatchAssignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isPresent()) {
            MatchAssignment assignment = assignmentOpt.get();
            assignment.setState(MatchAssignment.AssignmentState.COMPLETED);
            assignmentRepository.save(assignment);
        }
    }

    public long countByState(MatchAssignment.AssignmentState state) {
        return assignmentRepository.countByState(state);
    }

    public long countPendingByReferee(Long refereeId) {
        return assignmentRepository.findByRefereeIdAndState(refereeId, MatchAssignment.AssignmentState.PENDING).size();
    }

    public long countAcceptedByReferee(Long refereeId) {
        return assignmentRepository.findByRefereeIdAndState(refereeId, MatchAssignment.AssignmentState.ACCEPTED).size();
    }

    public void deleteById(Long id) {
        assignmentRepository.deleteById(id);
    }

    public List<MatchAssignment> findByMatch(Long matchId) {
        return assignmentRepository.findByMatchId(matchId);
    }

    public void assignReferees(Long matchId, List<Long> refereeIds, List<String> roles) {
        // First, remove existing assignments for this match
        List<MatchAssignment> existingAssignments = findByMatch(matchId);
        existingAssignments.forEach(assignment -> deleteById(assignment.getId()));

        // Create new assignments
        for (int i = 0; i < refereeIds.size(); i++) {
            MatchAssignment assignment = new MatchAssignment();
            assignment.setMatch(new Match()); // Will be set by repository
            assignment.getMatch().setId(matchId);
            assignment.setReferee(new Referee());
            assignment.getReferee().setId(refereeIds.get(i));
            assignment.setRole(MatchAssignment.RefereeRole.valueOf(roles.get(i)));
            assignment.setState(MatchAssignment.AssignmentState.PENDING);
            assignmentRepository.save(assignment);
        }
    }

    public void cancelAllAssignments(Long matchId) {
        List<MatchAssignment> assignments = findByMatch(matchId);
        assignments.forEach(assignment -> {
            assignment.setState(MatchAssignment.AssignmentState.REJECTED);
            assignmentRepository.save(assignment);
        });
    }
}
