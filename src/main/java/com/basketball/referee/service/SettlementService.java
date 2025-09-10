package com.basketball.referee.service;

import com.basketball.referee.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SettlementService {

    @Autowired
    private MatchAssignmentService assignmentService;

    @Autowired
    private FeeService feeService;

    public SettlementData calculateSettlement(Long refereeId, YearMonth yearMonth) {
        List<MatchAssignment> assignments = assignmentService.findByRefereeAndMonth(refereeId, yearMonth);
        
        // Filter only completed assignments
        List<MatchAssignment> completedAssignments = assignments.stream()
            .filter(a -> a.getState() == MatchAssignment.AssignmentState.COMPLETED)
            .collect(Collectors.toList());

        SettlementData settlement = new SettlementData();
        settlement.setRefereeId(refereeId);
        settlement.setYearMonth(yearMonth);
        settlement.setAssignments(completedAssignments);

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, Integer> matchesByRole = new HashMap<>();
        Map<String, BigDecimal> amountsByRole = new HashMap<>();

        for (MatchAssignment assignment : completedAssignments) {
            BigDecimal payment = feeService.calculatePayment(assignment);
            totalAmount = totalAmount.add(payment);

            String role = assignment.getRole().getDisplayName();
            matchesByRole.put(role, matchesByRole.getOrDefault(role, 0) + 1);
            amountsByRole.put(role, amountsByRole.getOrDefault(role, BigDecimal.ZERO).add(payment));
        }

        settlement.setTotalAmount(totalAmount);
        settlement.setMatchesByRole(matchesByRole);
        settlement.setAmountsByRole(amountsByRole);
        settlement.setTotalMatches(completedAssignments.size());

        return settlement;
    }

    public List<SettlementSummary> generateMonthlySummary(YearMonth yearMonth) {
        // This would typically be implemented with a custom repository query
        // For now, we'll use a simplified approach
        List<SettlementSummary> summaries = new ArrayList<>();
        
        // This is a placeholder - in a real implementation, you'd query all referees
        // and calculate their liquidations
        
        return summaries;
    }

    // Inner classes for data transfer
    public static class SettlementData {
        private Long refereeId;
        private YearMonth yearMonth;
        private List<MatchAssignment> assignments;
        private BigDecimal totalAmount;
        private Map<String, Integer> matchesByRole;
        private Map<String, BigDecimal> amountsByRole;
        private int totalMatches;

        // Getters and setters
        public Long getRefereeId() { return refereeId; }
        public void setRefereeId(Long refereeId) { this.refereeId = refereeId; }
        
        public YearMonth getYearMonth() { return yearMonth; }
        public void setYearMonth(YearMonth yearMonth) { this.yearMonth = yearMonth; }
        
        public List<MatchAssignment> getAssignments() { return assignments; }
        public void setAssignments(List<MatchAssignment> assignments) { this.assignments = assignments; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Map<String, Integer> getMatchesByRole() { return matchesByRole; }
        public void setMatchesByRole(Map<String, Integer> matchesByRole) { this.matchesByRole = matchesByRole; }
        
        public Map<String, BigDecimal> getAmountsByRole() { return amountsByRole; }
        public void setAmountsByRole(Map<String, BigDecimal> amountsByRole) { this.amountsByRole = amountsByRole; }
        
        public int getTotalMatches() { return totalMatches; }
        public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }
    }

    public static class SettlementSummary {
        private Long refereeId;
        private String refereeName;
        private int totalMatches;
        private BigDecimal totalAmount;

        // Getters and setters
        public Long getRefereeId() { return refereeId; }
        public void setRefereeId(Long refereeId) { this.refereeId = refereeId; }
        
        public String getRefereeName() { return refereeName; }
        public void setRefereeName(String refereeName) { this.refereeName = refereeName; }
        
        public int getTotalMatches() { return totalMatches; }
        public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}
