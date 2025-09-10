package com.basketball.referee.controller;

import com.basketball.referee.model.*;
import com.basketball.referee.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/matches")
@PreAuthorize("hasRole('ADMIN')")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private CourtService courtService;

    @Autowired
    private RefereeService refereeService;

    @Autowired
    private MatchAssignmentService assignmentService;

    @GetMapping
    public String listMatches(@RequestParam(required = false) String search,
            @RequestParam(required = false) String tournament,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String date,
            Model model) {
        List<Match> matches;

        if (search != null || tournament != null || state != null || date != null) {
            matches = matchService.findByFilters(search, tournament, state, date);
        } else {
            matches = matchService.findAll();
        }

        List<Tournament> tournaments = tournamentService.findAll();

        model.addAttribute("matches", matches);
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("states", Match.MatchState.values());
        model.addAttribute("title", "Gestión de Matches");
        return "admin/matches/list";
    }

    @GetMapping("/new")
    public String newMatch(Model model) {
        model.addAttribute("match", new Match());
        model.addAttribute("tournaments", tournamentService.findActive());
        model.addAttribute("courts", courtService.findActive());
        model.addAttribute("title", "Programar Match");
        return "admin/matches/form";
    }

    @PostMapping
    public String createMatch(@Valid Match match,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("errorMessages", result.getAllErrors().stream()
                    .map(error -> "⚠️ " + error.getDefaultMessage())
                    .toList());

            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("courts", courtService.findActive());
            model.addAttribute("title", "Programar Match");
            return "admin/matches/form";
        }
        try {
            Match savedMatch = matchService.create(match);
            redirectAttributes.addFlashAttribute("successMessage", "Match programado exitosamente");
            return "redirect:/admin/matches/" + savedMatch.getId() + "/assign";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al programar match: " + e.getMessage());
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("courts", courtService.findActive());
            model.addAttribute("title", "Programar Match");
            return "admin/matches/form";
        }
    }

    @GetMapping("/{id}")
    public String viewMatch(@PathVariable Long id, Model model) {
        Optional<Match> matchOpt = matchService.findById(id);
        if (matchOpt.isEmpty()) {
            return "redirect:/admin/matches";
        }

        Match match = matchOpt.get();
        List<MatchAssignment> assignments = assignmentService.findByMatch(id);

        model.addAttribute("match", match);
        model.addAttribute("assignments", assignments);
        model.addAttribute("title", "Detalles del Match");
        return "admin/matches/view";
    }

    @GetMapping("/{id}/edit")
    public String editMatch(@PathVariable Long id, Model model) {
        Optional<Match> matchOpt = matchService.findById(id);
        if (matchOpt.isEmpty()) {
            return "redirect:/admin/matches";
        }

        model.addAttribute("match", matchOpt.get());
        model.addAttribute("tournaments", tournamentService.findActive());
        model.addAttribute("courts", courtService.findActive());
        model.addAttribute("title", "Editar Match");
        return "admin/matches/form";
    }

    @PostMapping("/{id}")
    public String updateMatch(@PathVariable Long id,
            @Valid Match match,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("courts", courtService.findActive());
            model.addAttribute("title", "Editar Match");
            return "admin/matches/form";
        }

        try {
            matchService.update(id, match);
            redirectAttributes.addFlashAttribute("successMessage", "Match actualizado exitosamente");
            return "redirect:/admin/matches/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar match: " + e.getMessage());
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("courts", courtService.findActive());
            model.addAttribute("title", "Editar Match");
            return "admin/matches/form";
        }
    }

    @GetMapping("/{id}/assign")
    public String assignReferees(@PathVariable Long id, Model model) {
        Optional<Match> matchOpt = matchService.findById(id);
        if (matchOpt.isEmpty()) {
            return "redirect:/admin/matches";
        }

        Match match = matchOpt.get();
        List<Referee> refereesField = refereeService.findBySpecialty(Referee.Specialty.FIELD);
        List<Referee> refereesTable = refereeService.findBySpecialty(Referee.Specialty.TABLE);
        List<MatchAssignment> assignmentsExistentes = assignmentService.findByMatch(id);

        model.addAttribute("match", match);
        model.addAttribute("refereesField", refereesField);
        model.addAttribute("refereesTable", refereesTable);
        model.addAttribute("assignmentsExistentes", assignmentsExistentes);
        model.addAttribute("roles", MatchAssignment.RefereeRole.values());
        model.addAttribute("title", "Asignar Árbitros");
        return "admin/matches/assign";
    }

    @PostMapping("/{id}/assign")
    public String processAssignments(@PathVariable Long id,
            @RequestParam List<Long> refereeIds,
            @RequestParam List<String> roles,
            RedirectAttributes redirectAttributes) {
        try {
            assignmentService.assignReferees(id, refereeIds, roles);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitros asignados exitosamente");
            return "redirect:/admin/matches/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar árbitros: " + e.getMessage());
            return "redirect:/admin/matches/" + id + "/assign";
        }
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelMatch(@PathVariable Long id) {
        try {
            matchService.updateState(id, Match.MatchState.CANCELED);
            assignmentService.cancelAllAssignments(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cancelar match: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public String completeMatch(@PathVariable Long id,
            @RequestParam Integer localResult,
            @RequestParam Integer visitorResult,
            RedirectAttributes redirectAttributes) {
        try {
            matchService.updateResultado(id, localResult, visitorResult);
            redirectAttributes.addFlashAttribute("successMessage", "Match finalizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al finalizar match: " + e.getMessage());
        }
        return "redirect:/admin/matches/" + id;
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) String month,
            @RequestParam(required = false) String year,
            Model model) {
        LocalDate date = LocalDate.now();
        if (month != null && year != null) {
            date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
        }

        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);

        List<Match> matches = matchService.findByDateRange(startOfMonth, endOfMonth);

        Map<String, List<Match>> matchesByDate = matches.stream()
                .collect(Collectors.groupingBy(m -> m.getDateHour().toLocalDate().toString()));
        model.addAttribute("matchesByDate", matchesByDate);

        model.addAttribute("matches", matches);
        model.addAttribute("currentDate", date);
        model.addAttribute("matchesByDate", matchesByDate);
        model.addAttribute("title", "Calendario de Matches");
        return "admin/matches/calendar";
    }
}
