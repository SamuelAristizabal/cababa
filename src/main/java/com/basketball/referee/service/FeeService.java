package com.basketball.referee.service;

import com.basketball.referee.model.*;
import com.basketball.referee.repository.FeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeeService {

    @Autowired
    private FeeRepository feeRepository;

    public List<Fee> findAll() {
        return feeRepository.findAll();
    }

    public List<Fee> findActive() {
        return feeRepository.findByActiveTrue();
    }

    public Optional<Fee> findById(Long id) {
        return feeRepository.findById(id);
    }

    public List<Fee> findByTournament(Long tournamentId) {
        return feeRepository.findByTournamentId(tournamentId);
    }

    public List<Fee> findByRank(Referee.Rank rank) {
        return feeRepository.findByRank(rank);
    }

    public Optional<Fee> findByTournamentRankAndRol(Long tournamentId, Referee.Rank rank, MatchAssignment.RefereeRole role) {
        return feeRepository.findByTournamentIdAndRankAndRole(tournamentId, rank, role);
    }

    public Fee save(Fee fee) {
        return feeRepository.save(fee);
    }

    public Fee create(Fee fee, BigDecimal amountTournament) {
        fee.setActive(true);
        BigDecimal amountBase;
        if (fee.getRank() == Referee.Rank.FIBA){ amountBase = new BigDecimal("1500000.00"); }
        else if (fee.getRank() == Referee.Rank.FIRST){amountBase = new BigDecimal("800000.00");}
        else if (fee.getRank() == Referee.Rank.SECOND){amountBase = new BigDecimal("500000.00");}
        else if (fee.getRank() == Referee.Rank.THIRD){amountBase = new BigDecimal("300000.00");}
        else if (fee.getRank() == Referee.Rank.FORMATION){amountBase = new BigDecimal("150000.00");}
        else {amountBase = new BigDecimal("100000.00");}
        fee.setAmount(amountBase.add(amountTournament));
        return feeRepository.save(fee);
    }

    public Fee update(Long id, Fee feeDetails) {
        Optional<Fee> feeOpt = feeRepository.findById(id);
        if (feeOpt.isPresent()) {
            Fee fee = feeOpt.get();
            fee.setTournament(feeDetails.getTournament());
            fee.setRank(feeDetails.getRank());
            fee.setRole(feeDetails.getRole());
            fee.setAmount(feeDetails.getAmount());
            fee.setDescription(feeDetails.getDescription());
            return feeRepository.save(fee);
        }
        throw new RuntimeException("Fee no encontrada");
    }

    public void toggleStatus(Long id) {
        Optional<Fee> feeOpt = feeRepository.findById(id);
        if (feeOpt.isPresent()) {
            Fee fee = feeOpt.get();
            fee.setActive(!fee.isActive());
            feeRepository.save(fee);
        }
    }

    public void deleteById(Long id) {
        feeRepository.deleteById(id);
    }

    public BigDecimal calculatePayment(MatchAssignment assignment) {
        Optional<Fee> feeOpt = findByTournamentRankAndRol(
            assignment.getMatch().getTournament().getId(),
            assignment.getReferee().getRank(),
            assignment.getRole()
        );
        
        return feeOpt.map(Fee::getAmount).orElse(BigDecimal.ZERO);
    }

    public List<Fee> findByFilters(String search, String tournament, String rank) {
        List<Fee> fees = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            fees = fees.stream()
                .filter(t -> (t.getDescription() != null && t.getDescription().toLowerCase().contains(search.toLowerCase())) ||
                           t.getTournament().getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        if (tournament != null && !tournament.trim().isEmpty()) {
            Long tournamentId = Long.parseLong(tournament);
            fees = fees.stream()
                .filter(t -> t.getTournament().getId().equals(tournamentId))
                .toList();
        }
        
        if (rank != null && !rank.trim().isEmpty()) {
            Referee.Rank rankEnum = Referee.Rank.valueOf(rank);
            fees = fees.stream()
                .filter(t -> t.getRank().equals(rankEnum))
                .toList();
        }
        
        return fees;
    }
}
