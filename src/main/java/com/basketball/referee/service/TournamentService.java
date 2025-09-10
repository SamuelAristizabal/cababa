package com.basketball.referee.service;

import com.basketball.referee.model.Tournament;
import com.basketball.referee.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public List<Tournament> findAllActive() {
        return tournamentRepository.findByActiveTrue();
    }

    public List<Tournament> findActive() {
        return findAllActive();
    }

    public Optional<Tournament> findById(Long id) {
        return tournamentRepository.findById(id);
    }

    public List<Tournament> findByState(Tournament.TournamentState state) {
        return tournamentRepository.findByState(state);
    }

    public List<Tournament> findCurrentTournaments() {
        return tournamentRepository.findCurrentTournaments();
    }

    public List<Tournament> findActiveByDate(LocalDate date) {
        return tournamentRepository.findActiveByDate(date);
    }

    public List<Tournament> searchByName(String name) {
        return tournamentRepository.findByNameContainingIgnoreCase(name);
    }

    public Tournament save(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public Tournament create(Tournament tournament) {
        tournament.setActive(true);
        tournament.setState(tournament.getState());
        return tournamentRepository.save(tournament);
    }

    public Tournament update(Long id, Tournament tournamentDetails) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isPresent()) {
            Tournament tournament = tournamentOpt.get();
            tournament.setName(tournamentDetails.getName());
            tournament.setDescription(tournamentDetails.getDescription());
            tournament.setStartDate(tournamentDetails.getStartDate());
            tournament.setEndDate(tournamentDetails.getEndDate());
            tournament.setState(tournamentDetails.getState());
            return tournamentRepository.save(tournament);
        }
        throw new RuntimeException("Tournament no encontrado");
    }

    public void toggleStatus(Long id) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isPresent()) {
            Tournament tournament = tournamentOpt.get();
            tournament.setActive(!tournament.isActive());
            tournamentRepository.save(tournament);
        }
    }

    public void updateState(Long id, Tournament.TournamentState nuevoState) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isPresent()) {
            Tournament tournament = tournamentOpt.get();
            tournament.setState(nuevoState);
            tournamentRepository.save(tournament);
        }
    }

    public void delete(Long id) {
        deleteById(id);
    }

    public void deleteById(Long id) {
        tournamentRepository.deleteById(id);
    }

    public long countActive() {
        return tournamentRepository.findByActiveTrue().size();
    }

    public long countByState(Tournament.TournamentState state) {
        return tournamentRepository.findByState(state).size();
    }

    public List<Tournament> findByFilters(String search, String state, String year) {
        List<Tournament> tournaments = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            tournaments = tournaments.stream()
                .filter(t -> t.getName().toLowerCase().contains(search.toLowerCase()) ||
                           (t.getDescription() != null && t.getDescription().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
        }
        
        if (state != null && !state.trim().isEmpty()) {
            Tournament.TournamentState stateEnum = Tournament.TournamentState.valueOf(state);
            tournaments = tournaments.stream()
                .filter(t -> t.getState().equals(stateEnum))
                .collect(Collectors.toList());
        }
        
        if (year != null && !year.trim().isEmpty()) {
            int yearInt = Integer.parseInt(year);
            tournaments = tournaments.stream()
                .filter(t -> t.getStartDate().getYear() == yearInt)
                .collect(Collectors.toList());
        }
        
        return tournaments;
    }

    public List<Integer> getAvailableYears() {
        return findAll().stream()
            .map(t -> t.getStartDate().getYear())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
