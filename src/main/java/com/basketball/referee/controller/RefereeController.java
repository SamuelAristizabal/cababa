package com.basketball.referee.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.basketball.referee.model.Referee;
import com.basketball.referee.model.MatchAssignment;
import com.basketball.referee.model.User;
import com.basketball.referee.service.RefereeService;
import com.basketball.referee.service.MatchAssignmentService;
import com.basketball.referee.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/referee")
public class RefereeController {

    @Autowired
    private UserService userService;

    @Autowired
    private RefereeService refereeService;

    @Autowired
    private MatchAssignmentService assignmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> refereeOpt = refereeService.findByUserId(user.getId());
        
        if (refereeOpt.isEmpty()) {
            // Redirect to complete profile if referee profile doesn't exist
            return "redirect:/referee/complete-profile";
        }

        Referee referee = refereeOpt.get();
        
        // Get statistics
        long pendingAssignments = assignmentService.countPendingByReferee(referee.getId());
        long acceptedAssignments = assignmentService.countAcceptedByReferee(referee.getId());
        List<MatchAssignment> recentAssignments = assignmentService.findByRefereeId(referee.getId())
                .stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .limit(5)
                .toList();

        // Get upcoming matches
        List<MatchAssignment> upcomingMatches = assignmentService.findAcceptedByReferee(referee.getId())
                .stream()
                .filter(a -> a.getMatch().getDateHour().isAfter(LocalDate.now().atStartOfDay()))
                .sorted((a1, a2) -> a1.getMatch().getDateHour().compareTo(a2.getMatch().getDateHour()))
                .limit(3)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("referee", referee);
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("acceptedAssignments", acceptedAssignments);
        model.addAttribute("recentAssignments", recentAssignments);
        model.addAttribute("upcomingMatches", upcomingMatches);
        model.addAttribute("title", "Dashboard - Árbitro");

        return "referee/dashboard";
    }

    @GetMapping("/complete-profile")
    public String completeProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("referee", new Referee());
        model.addAttribute("specialtyes", Referee.Specialty.values());
        model.addAttribute("rankes", Referee.Rank.values());
        model.addAttribute("title", "Completar Perfil");

        return "referee/complete-profile";
    }

    @PostMapping("/complete-profile")
    public String processCompleteProfile(@Valid Referee referee,
                                       BindingResult result,
                                       Authentication authentication,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("specialtyes", Referee.Specialty.values());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("title", "Completar Perfil");
            return "referee/complete-profile";
        }

        // Check if document already exists
        if (refereeService.existsByDocument(referee.getDocument())) {
            result.rejectValue("document", "error.referee", "El document ya está registrado");
            model.addAttribute("user", user);
            model.addAttribute("specialtyes", Referee.Specialty.values());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("title", "Completar Perfil");
            return "referee/complete-profile";
        }

        try {
            refereeService.createReferee(referee, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Perfil completado exitosamente");
            return "redirect:/referee/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al completar perfil: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("specialtyes", Referee.Specialty.values());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("title", "Completar Perfil");
            return "referee/complete-profile";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> refereeOpt = refereeService.findByUserId(user.getId());
        if (refereeOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("referee", refereeOpt.get());
        model.addAttribute("title", "Mi Perfil");

        return "referee/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> refereeOpt = refereeService.findByUserId(user.getId());
        if (refereeOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("referee", refereeOpt.get());
        model.addAttribute("specialtyes", Referee.Specialty.values());
        model.addAttribute("rankes", Referee.Rank.values());
        model.addAttribute("title", "Editar Perfil");

        return "referee/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String processEditProfile(@Valid Referee referee,
                                   BindingResult result,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> existingRefereeOpt = refereeService.findByUserId(user.getId());
        if (existingRefereeOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("specialtyes", Referee.Specialty.values());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("title", "Editar Perfil");
            return "referee/edit-profile";
        }

        try {
            refereeService.updateReferee(existingRefereeOpt.get().getId(), referee);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente");
            return "redirect:/referee/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("specialtyes", Referee.Specialty.values());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("title", "Editar Perfil");
            return "referee/edit-profile";
        }
    }

    @PostMapping("/profile/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> refereeOpt = refereeService.findByUserId(user.getId());
        if (refereeOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        try {
            refereeService.updatePhoto(refereeOpt.get().getId(), file);
            redirectAttributes.addFlashAttribute("successMessage", "Foto actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al subir foto: " + e.getMessage());
        }

        return "redirect:/referee/profile";
    }

    @GetMapping("/assignments")
    public String assignments(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> refereeOpt = refereeService.findByUserId(user.getId());
        if (refereeOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        Referee referee = refereeOpt.get();
        List<MatchAssignment> allAssignments = assignmentService.findByRefereeId(referee.getId());
        
        // Separate by status
        List<MatchAssignment> pendingAssignments = allAssignments.stream()
                .filter(a -> a.getState() == MatchAssignment.AssignmentState.PENDING)
                .toList();
        
        List<MatchAssignment> acceptedAssignments = allAssignments.stream()
                .filter(a -> a.getState() == MatchAssignment.AssignmentState.ACCEPTED)
                .toList();
        
        List<MatchAssignment> completedAssignments = allAssignments.stream()
                .filter(a -> a.getState() == MatchAssignment.AssignmentState.COMPLETED)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("referee", referee);
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("acceptedAssignments", acceptedAssignments);
        model.addAttribute("completedAssignments", completedAssignments);
        model.addAttribute("title", "Mis Assignments");

        return "referee/assignments";
    }

    @PostMapping("/assignments/{id}/accept")
    public String acceptAssignment(@PathVariable Long id,
                                 @RequestParam(required = false) String comments,
                                 RedirectAttributes redirectAttributes) {
        try {
            assignmentService.acceptAssignment(id, comments);
            redirectAttributes.addFlashAttribute("successMessage", "Asignación aceptada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar asignación: " + e.getMessage());
        }

        return "redirect:/referee/assignments";
    }

    @PostMapping("/assignments/{id}/reject")
    public String rejectAssignment(@PathVariable Long id,
                                 @RequestParam(required = false) String comments,
                                 RedirectAttributes redirectAttributes) {
        try {
            assignmentService.rejectAssignment(id, comments);
            redirectAttributes.addFlashAttribute("successMessage", "Asignación rechazada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar asignación: " + e.getMessage());
        }

        return "redirect:/referee/assignments";
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) String month,
                         Model model, 
                         Authentication authentication) {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Referee> refereeOpt = refereeService.findByUserId(user.getId());
        if (refereeOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        Referee referee = refereeOpt.get();
        
        // Parse month parameter or use current month
        YearMonth yearMonth;
        if (month != null && !month.isEmpty()) {
            yearMonth = YearMonth.parse(month);
        } else {
            yearMonth = YearMonth.now();
        }

        List<MatchAssignment> monthAssignments = assignmentService.findByRefereeAndMonth(referee.getId(), yearMonth);
        
        Map<String, List<MatchAssignment>> assignmentsByDate = monthAssignments.stream()
            .collect(Collectors.groupingBy(a -> a.getMatch().getDateHour().toLocalDate().toString()));

        model.addAttribute("user", user);
        model.addAttribute("referee", referee);
        model.addAttribute("currentMonth", yearMonth);
        model.addAttribute("assignmentsByDate", assignmentsByDate);
        model.addAttribute("title", "Calendario - " + yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        System.out.println("currentMonth: " + yearMonth);
        System.out.println("Total assignments del mes: " + monthAssignments.size());
        assignmentsByDate.forEach((date, assignments) ->
            System.out.println(" -> " + date + ": " + assignments.size())
        );

        return "referee/calendar";
    }
}
