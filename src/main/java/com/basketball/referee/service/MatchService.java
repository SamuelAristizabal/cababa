package com.basketball.referee.service;

import com.basketball.referee.model.Match;
import com.basketball.referee.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    public List<Match> findAll() {
        return matchRepository.findAll();
    }

    public Optional<Match> findById(Long id) {
        return matchRepository.findById(id);
    }

    public List<Match> findByTournament(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    public List<Match> findByCourt(Long courtId) {
        return matchRepository.findByCourtId(courtId);
    }

    public List<Match> findByState(Match.MatchState state) {
        return matchRepository.findByState(state);
    }

    public List<Match> findUpcomingMatches() {
        return matchRepository.findUpcomingMatches(LocalDateTime.now());
    }

    public List<Match> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return matchRepository.findByDateHourBetween(start, end);
    }

    public List<Match> findByReferee(Long refereeId) {
        return matchRepository.findByRefereeId(refereeId);
    }

    public Match save(Match match) {
        return matchRepository.save(match);
    }

    public Match create(Match match) {
        match.setState(Match.MatchState.PROGRAMMED);
        return matchRepository.save(match);
    }

    public Match update(Long id, Match matchDetails) {
        Optional<Match> matchOpt = matchRepository.findById(id);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setTournament(matchDetails.getTournament());
            match.setCourt(matchDetails.getCourt());
            match.setLocalTeam(matchDetails.getLocalTeam());
            match.setVisitorTeam(matchDetails.getVisitorTeam());
            match.setDateHour(matchDetails.getDateHour());
            match.setObservations(matchDetails.getObservations());
            return matchRepository.save(match);
        }
        throw new RuntimeException("Match no encontrado");
    }

    public void updateState(Long id, Match.MatchState nuevoState) {
        Optional<Match> matchOpt = matchRepository.findById(id);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setState(nuevoState);
            matchRepository.save(match);
        }
    }

    public void updateResultado(Long id, Integer localResult, Integer visitorResult) {
        Optional<Match> matchOpt = matchRepository.findById(id);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setLocalResult(localResult);
            match.setVisitorResult(visitorResult);
            match.setState(Match.MatchState.FINISHED);
            matchRepository.save(match);
        }
    }

    public void deleteById(Long id) {
        matchRepository.deleteById(id);
    }

    public long countByState(Match.MatchState state) {
        return matchRepository.countByState(state);
    }

    public List<Match> findByFilters(String search, String tournament, String state, String date) {
        // This would typically use Criteria API or custom repository methods
        // For now, implementing basic filtering
        List<Match> matches = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            matches = matches.stream()
                .filter(p -> p.getLocalTeam().toLowerCase().contains(search.toLowerCase()) ||
                           p.getVisitorTeam().toLowerCase().contains(search.toLowerCase()) ||
                           p.getTournament().getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        if (tournament != null && !tournament.trim().isEmpty()) {
            Long tournamentId = Long.parseLong(tournament);
            matches = matches.stream()
                .filter(p -> p.getTournament().getId().equals(tournamentId))
                .toList();
        }
        
        if (state != null && !state.trim().isEmpty()) {
            Match.MatchState stateEnum = Match.MatchState.valueOf(state);
            matches = matches.stream()
                .filter(p -> p.getState().equals(stateEnum))
                .toList();
        }
        
        if (date != null && !date.trim().isEmpty()) {
            LocalDateTime dateFiltro = LocalDateTime.parse(date + "T00:00:00");
            LocalDateTime endDate = dateFiltro.plusDays(1);
            matches = matches.stream()
                .filter(p -> p.getDateHour().isAfter(dateFiltro) && p.getDateHour().isBefore(endDate))
                .toList();
        }
        
        return matches;
    }
}
