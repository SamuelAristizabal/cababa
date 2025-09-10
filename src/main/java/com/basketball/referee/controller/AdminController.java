package com.basketball.referee.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.basketball.referee.model.Court;
import com.basketball.referee.model.Match;
import com.basketball.referee.model.MatchAssignment;
import com.basketball.referee.model.Referee;
import com.basketball.referee.model.Tournament;
import com.basketball.referee.model.User;
import com.basketball.referee.service.CourtService;
import com.basketball.referee.service.GradeService;
import com.basketball.referee.service.MatchAssignmentService;
import com.basketball.referee.service.MatchService;
import com.basketball.referee.service.RefereeService;
import com.basketball.referee.service.TournamentService;
import com.basketball.referee.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RefereeService refereeService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private CourtService courtService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchAssignmentService assignmentsService;
    
    @Autowired
    private GradeService gradeService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Statistics
        long totalReferees = refereeService.countActive();
        long totalTournaments = tournamentService.countActive();
        long totalCourts = courtService.countActive();
        long matchesProgrammed = matchService.countByState(Match.MatchState.PROGRAMMED);
        long assignmentsPending = assignmentsService.countByState(MatchAssignment.AssignmentState.PENDING);
        long assignmentsAceptadas = assignmentsService.countByState(MatchAssignment.AssignmentState.ACCEPTED);
        long assignmentsRechazadas = assignmentsService.countByState(MatchAssignment.AssignmentState.REJECTED);
        long assignmentsCompletadas = assignmentsService.countByState(MatchAssignment.AssignmentState.COMPLETED);

        // Recent data
        List<Referee> recentReferees = refereeService.findAll().stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .limit(5)
                .toList();

        List<Tournament> currentTournaments = tournamentService.findCurrentTournaments();
        List<Referee> topReferees = refereeService.findTopRefereesByAverageScore(5);

        model.addAttribute("topReferees", topReferees);
        model.addAttribute("totalReferees", totalReferees);
        model.addAttribute("totalTournaments", totalTournaments);
        model.addAttribute("totalCourts", totalCourts);
        model.addAttribute("matchesProgramados", matchesProgrammed);
        model.addAttribute("assignmentsPendientes", assignmentsPending);
        model.addAttribute("assignmentsAceptadas", assignmentsAceptadas);
        model.addAttribute("assignmentsRechazadas", assignmentsRechazadas);
        model.addAttribute("assignmentsCompletadas", assignmentsCompletadas);
        model.addAttribute("recentReferees", recentReferees);
        model.addAttribute("currentTournaments", currentTournaments);
        model.addAttribute("title", "Dashboard Administrativo");

        return "admin/dashboard";
    }

    // CRUD referees
    @GetMapping("/referees")
    public String listReferees(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String rank,
                              @RequestParam(required = false) String specialty,
                              @RequestParam(required = false) String active,
                              Model model) {
        List<Referee> referees;
        
        if (search != null || rank != null || specialty != null || active != null) {
            referees = refereeService.findByFilters(search, rank, specialty, active);
        } else {
            referees = refereeService.findAll();
        }
        
        Map<Long, Double> averages = referees.stream()
                .filter(r -> r.getId() != null) // evita referees sin ID
                .collect(Collectors.toMap(
                    Referee::getId,
                    r -> Optional.ofNullable(gradeService.findAverageByReferee(r.getId()))
                                .orElse(0.0) // valor por defecto si no hay calificaciones
                ));

        model.addAttribute("referees", referees);
        model.addAttribute("averages", averages);
        model.addAttribute("title", "Gestión de Árbitros");
        return "admin/referees/list";
    }

    @GetMapping("/referees/new")
    public String newReferee(Model model) {
        Referee referee = new Referee();
        referee.setUser(new User()); // Initialize user object
        model.addAttribute("referee", referee);
        model.addAttribute("title", "Crear Árbitro");
        return "admin/referees/form";
    }

    @PostMapping("/referees")
    public String createReferee(@Valid @ModelAttribute Referee referee,
                               BindingResult result,
                               @RequestParam(required = false) MultipartFile foto,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(error -> "⚠️ " + error.getDefaultMessage())
                    .toList();

            model.addAttribute("errorMessages", errorMessages);
            model.addAttribute("title", "Crear Árbitro");
            return "admin/referees/form";
        }

        if (userService.existsByEmail(referee.getUser().getEmail())) {
            result.rejectValue("user.email", "error.user", "El email ya existe");
            model.addAttribute("errorMessages", List.of("⚠️ El email ya existe"));
            model.addAttribute("title", "Crear Árbitro");
            return "admin/referees/form";
        }

        try {
            refereeService.createReferee(referee, foto);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitro creado exitosamente");
            return "redirect:/admin/referees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear árbitro: " + e.getMessage());
            model.addAttribute("title", "Crear Árbitro");
            return "admin/referees/form";
        }
    }

    @GetMapping("/referees/{id}")
    public String viewReferee(@PathVariable Long id, Model model) {
        Optional<Referee> refereeOpt = refereeService.findById(id);
        if (refereeOpt.isEmpty()) {
            return "redirect:/admin/referees";
        }

        Referee referee = refereeOpt.get();
        model.addAttribute("referee", referee);
        model.addAttribute("user", referee.getUser()); 
        model.addAttribute("title", "Detalles del Árbitro");

        return "admin/referees/view";
    }

    @GetMapping("/referees/{id}/edit")
    public String editReferee(@PathVariable Long id, Model model) {
        Optional<Referee> refereeOpt = refereeService.findById(id);
        if (refereeOpt.isEmpty()) {
            return "redirect:/admin/referees";
        }

        model.addAttribute("referee", refereeOpt.get());
        model.addAttribute("title", "Editar Árbitro");
        return "admin/referees/form"; // Use same form template
    }

    @PostMapping("/referees/{id}")
    public String updateReferee(@PathVariable Long id,
                               @Valid @ModelAttribute Referee referee,
                               BindingResult result,
                               @RequestParam(required = false) MultipartFile foto,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("errorMessages", result.getAllErrors().stream()
                    .map(error -> "⚠️ " + error.getDefaultMessage())
                    .toList());
            model.addAttribute("title", "Editar Árbitro");
            return "admin/referees/form";
        }

        try {
            refereeService.updateReferee(id, referee, foto);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitro actualizado exitosamente");
            return "redirect:/admin/referees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar árbitro: " + e.getMessage());
            model.addAttribute("title", "Editar Árbitro");
            return "admin/referees/form";
        }
    }

    @PostMapping("/referees/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleRefereeStatus(@PathVariable Long id) {
        try {
            refereeService.toggleStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar state: " + e.getMessage());
        }
    }

    @GetMapping("/assignments")
    public String listAssignments(Model model) {
        List<MatchAssignment> assignments = assignmentsService.findAll();
        model.addAttribute("assignments", assignments);
        model.addAttribute("states", MatchAssignment.AssignmentState.values());
        model.addAttribute("title", "Gestión de Assignments");
        return "admin/assignments/list";
    }

    @PostMapping("/assignments/{assignmentId}/update-state")
    public String updateAssignmentState(@PathVariable Long assignmentId,
                                        @RequestParam("state") MatchAssignment.AssignmentState state,
                                        RedirectAttributes redirectAttributes) {
        if (state == MatchAssignment.AssignmentState.COMPLETED) {
            assignmentsService.completeAssignment(assignmentId);
            redirectAttributes.addFlashAttribute("successMessage", "State actualizado correctamente");
            return "redirect:/admin/assignments";

        } else if((state == MatchAssignment.AssignmentState.REJECTED)){
            assignmentsService.rejectAssignment(assignmentId);
            redirectAttributes.addFlashAttribute("successMessage", "State actualizado correctamente");
            return "redirect:/admin/assignments";
        } else if((state == MatchAssignment.AssignmentState.ACCEPTED)){
            assignmentsService.acceptAssignment(assignmentId);
            redirectAttributes.addFlashAttribute("successMessage", "State actualizado correctamente");
            return "redirect:/admin/assignments";
        }
        return "redirect:/admin/assignments";
    }

    // CRUD Tournaments
    @GetMapping("/tournaments")
    public String listTournaments(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String state,
                             @RequestParam(required = false) String year,
                             Model model) {
        List<Tournament> tournaments;
        
        if (search != null || state != null || year != null) {
            tournaments = tournamentService.findByFilters(search, state, year);
        } else {
            tournaments = tournamentService.findAll();
        }
        
        List<Integer> years = tournamentService.getAvailableYears();
        
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("years", years);
        model.addAttribute("title", "Gestión de Tournaments");
        return "admin/tournaments/list";
    }

    @GetMapping("/tournaments/new")
    public String newTournament(Model model) {
        model.addAttribute("tournament", new Tournament());
        model.addAttribute("states", Tournament.TournamentState.values());
        model.addAttribute("title", "Crear Tournament");
        return "admin/tournaments/form";
    }

    @PostMapping("/tournaments")
    public String createTournament(@Valid Tournament tournament,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("states", Tournament.TournamentState.values());
            model.addAttribute("title", "Crear Tournament");
            return "admin/tournaments/form";
        }

        try {
            tournamentService.create(tournament);
            redirectAttributes.addFlashAttribute("successMessage", "Tournament creado exitosamente");
            return "redirect:/admin/tournaments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear tournament: " + e.getMessage());
            model.addAttribute("states", Tournament.TournamentState.values());
            model.addAttribute("title", "Crear Tournament");
            return "admin/tournaments/form";
        }
    }

    @GetMapping("/tournaments/{id}")
    public String viewTournament(@PathVariable Long id, Model model) {
        Optional<Tournament> tournamentOpt = tournamentService.findById(id);
        if (tournamentOpt.isEmpty()) {
            return "redirect:/admin/tournaments";
        }

        model.addAttribute("tournament", tournamentOpt.get());
        model.addAttribute("title", "Detalles del Tournament");
        return "admin/tournaments/view";
    }

    @GetMapping("/tournaments/{id}/edit")
    public String editTournament(@PathVariable Long id, Model model) {
        Optional<Tournament> tournamentOpt = tournamentService.findById(id);
        if (tournamentOpt.isEmpty()) {
            return "redirect:/admin/tournaments";
        }

        model.addAttribute("tournament", tournamentOpt.get());
        model.addAttribute("title", "Editar Tournament");
        return "admin/tournaments/form"; // Use same form template
    }

    @PostMapping("/tournaments/{id}")
    public String updateTournament(@PathVariable Long id,
                              @Valid Tournament tournament,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("states", Tournament.TournamentState.values());
            model.addAttribute("title", "Editar Tournament");
            return "admin/tournaments/edit";
        }

        try {
            tournamentService.update(id, tournament);
            redirectAttributes.addFlashAttribute("successMessage", "Tournament actualizado exitosamente");
            return "redirect:/admin/tournaments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar tournament: " + e.getMessage());
            model.addAttribute("states", Tournament.TournamentState.values());
            model.addAttribute("title", "Editar Tournament");
            return "admin/tournaments/edit";
        }
    }

    @PostMapping("/tournaments/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleTournamentStatus(@PathVariable Long id) {
        try {
            tournamentService.toggleStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar state: " + e.getMessage());
        }
    }

    @DeleteMapping("/tournaments/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTournament(@PathVariable Long id) {
        try {
            tournamentService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar tournament: " + e.getMessage());
        }
    }

    // CRUD Courts
    @GetMapping("/courts")
    public String listCourts(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String active,
                             Model model) {
        List<Court> courts;
        
        if (search != null || active != null) {
            courts = courtService.findByFilters(search, active);
        } else {
            courts = courtService.findAll();
        }
        
        model.addAttribute("courts", courts);
        model.addAttribute("title", "Gestión de Courts");
        return "admin/courts/list";
    }

    @GetMapping("/courts/new")
    public String newCourt(Model model) {
        model.addAttribute("court", new Court());
        model.addAttribute("title", "Crear Court");
        return "admin/courts/form";
    }

    @PostMapping("/courts")
    public String createCourt(@Valid Court court,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Crear Court");
            return "admin/courts/form";
        }

        try {
            courtService.create(court);
            redirectAttributes.addFlashAttribute("successMessage", "Court creada exitosamente");
            return "redirect:/admin/courts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear court: " + e.getMessage());
            model.addAttribute("title", "Crear Court");
            return "admin/courts/form";
        }
    }

    @GetMapping("/courts/{id}/edit")
    public String editCourt(@PathVariable Long id, Model model) {
        Optional<Court> courtOpt = courtService.findById(id);
        if (courtOpt.isEmpty()) {
            return "redirect:/admin/courts";
        }

        model.addAttribute("court", courtOpt.get());
        model.addAttribute("title", "Editar Court");
        return "admin/courts/form"; // Use same form template
    }

    @PostMapping("/courts/{id}")
    public String updateCourt(@PathVariable Long id,
                              @Valid Court court,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Editar Court");
            return "admin/courts/edit";
        }

        try {
            courtService.update(id, court);
            redirectAttributes.addFlashAttribute("successMessage", "Court actualizada exitosamente");
            return "redirect:/admin/courts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar court: " + e.getMessage());
            model.addAttribute("title", "Editar Court");
            return "admin/courts/edit";
        }
    }

    @PostMapping("/courts/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleCourtStatus(@PathVariable Long id) {
        try {
            courtService.toggleStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar state: " + e.getMessage());
        }
    }
}
