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

import com.basketball.referee.entity.Arbitro;
import com.basketball.referee.entity.AsignacionPartido;
import com.basketball.referee.entity.User;
import com.basketball.referee.service.ArbitroService;
import com.basketball.referee.service.AsignacionPartidoService;
import com.basketball.referee.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/referee")
public class RefereeController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArbitroService arbitroService;

    @Autowired
    private AsignacionPartidoService asignacionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Arbitro> arbitroOpt = arbitroService.findByUserId(user.getId());
        
        if (arbitroOpt.isEmpty()) {
            // Redirect to complete profile if arbitro profile doesn't exist
            return "redirect:/referee/complete-profile";
        }

        Arbitro arbitro = arbitroOpt.get();
        
        // Get statistics
        long pendingAssignments = asignacionService.countPendingByArbitro(arbitro.getId());
        long acceptedAssignments = asignacionService.countAcceptedByArbitro(arbitro.getId());
        List<AsignacionPartido> recentAssignments = asignacionService.findByArbitroId(arbitro.getId())
                .stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .limit(5)
                .toList();

        // Get upcoming matches
        List<AsignacionPartido> upcomingMatches = asignacionService.findAcceptedByArbitro(arbitro.getId())
                .stream()
                .filter(a -> a.getPartido().getFechaHora().isAfter(LocalDate.now().atStartOfDay()))
                .sorted((a1, a2) -> a1.getPartido().getFechaHora().compareTo(a2.getPartido().getFechaHora()))
                .limit(3)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("arbitro", arbitro);
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
        model.addAttribute("arbitro", new Arbitro());
        model.addAttribute("especialidades", Arbitro.Especialidad.values());
        model.addAttribute("escalafones", Arbitro.Escalafon.values());
        model.addAttribute("title", "Completar Perfil");

        return "referee/complete-profile";
    }

    @PostMapping("/complete-profile")
    public String processCompleteProfile(@Valid Arbitro arbitro,
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
            model.addAttribute("especialidades", Arbitro.Especialidad.values());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("title", "Completar Perfil");
            return "referee/complete-profile";
        }

        // Check if documento already exists
        if (arbitroService.existsByDocumento(arbitro.getDocumento())) {
            result.rejectValue("documento", "error.arbitro", "El documento ya está registrado");
            model.addAttribute("user", user);
            model.addAttribute("especialidades", Arbitro.Especialidad.values());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("title", "Completar Perfil");
            return "referee/complete-profile";
        }

        try {
            arbitroService.createArbitro(arbitro, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Perfil completado exitosamente");
            return "redirect:/referee/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al completar perfil: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("especialidades", Arbitro.Especialidad.values());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
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

        Optional<Arbitro> arbitroOpt = arbitroService.findByUserId(user.getId());
        if (arbitroOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("arbitro", arbitroOpt.get());
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

        Optional<Arbitro> arbitroOpt = arbitroService.findByUserId(user.getId());
        if (arbitroOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("arbitro", arbitroOpt.get());
        model.addAttribute("especialidades", Arbitro.Especialidad.values());
        model.addAttribute("escalafones", Arbitro.Escalafon.values());
        model.addAttribute("title", "Editar Perfil");

        return "referee/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String processEditProfile(@Valid Arbitro arbitro,
                                   BindingResult result,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Arbitro> existingArbitroOpt = arbitroService.findByUserId(user.getId());
        if (existingArbitroOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("especialidades", Arbitro.Especialidad.values());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("title", "Editar Perfil");
            return "referee/edit-profile";
        }

        try {
            arbitroService.updateArbitro(existingArbitroOpt.get().getId(), arbitro);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente");
            return "redirect:/referee/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("especialidades", Arbitro.Especialidad.values());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
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

        Optional<Arbitro> arbitroOpt = arbitroService.findByUserId(user.getId());
        if (arbitroOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        try {
            arbitroService.updatePhoto(arbitroOpt.get().getId(), file);
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

        Optional<Arbitro> arbitroOpt = arbitroService.findByUserId(user.getId());
        if (arbitroOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        Arbitro arbitro = arbitroOpt.get();
        List<AsignacionPartido> allAssignments = asignacionService.findByArbitroId(arbitro.getId());
        
        // Separate by status
        List<AsignacionPartido> pendingAssignments = allAssignments.stream()
                .filter(a -> a.getEstado() == AsignacionPartido.EstadoAsignacion.PENDIENTE)
                .toList();
        
        List<AsignacionPartido> acceptedAssignments = allAssignments.stream()
                .filter(a -> a.getEstado() == AsignacionPartido.EstadoAsignacion.ACEPTADA)
                .toList();
        
        List<AsignacionPartido> completedAssignments = allAssignments.stream()
                .filter(a -> a.getEstado() == AsignacionPartido.EstadoAsignacion.COMPLETADA)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("acceptedAssignments", acceptedAssignments);
        model.addAttribute("completedAssignments", completedAssignments);
        model.addAttribute("title", "Mis Asignaciones");

        return "referee/assignments";
    }

    @PostMapping("/assignments/{id}/accept")
    public String acceptAssignment(@PathVariable Long id,
                                 @RequestParam(required = false) String comentarios,
                                 RedirectAttributes redirectAttributes) {
        try {
            asignacionService.acceptAssignment(id, comentarios);
            redirectAttributes.addFlashAttribute("successMessage", "Asignación aceptada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar asignación: " + e.getMessage());
        }

        return "redirect:/referee/assignments";
    }

    @PostMapping("/assignments/{id}/reject")
    public String rejectAssignment(@PathVariable Long id,
                                 @RequestParam(required = false) String comentarios,
                                 RedirectAttributes redirectAttributes) {
        try {
            asignacionService.rejectAssignment(id, comentarios);
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

        Optional<Arbitro> arbitroOpt = arbitroService.findByUserId(user.getId());
        if (arbitroOpt.isEmpty()) {
            return "redirect:/referee/complete-profile";
        }

        Arbitro arbitro = arbitroOpt.get();
        
        // Parse month parameter or use current month
        YearMonth yearMonth;
        if (month != null && !month.isEmpty()) {
            yearMonth = YearMonth.parse(month);
        } else {
            yearMonth = YearMonth.now();
        }

        List<AsignacionPartido> monthAssignments = asignacionService.findByArbitroAndMonth(arbitro.getId(), yearMonth);
        
        Map<String, List<AsignacionPartido>> assignmentsByDate = monthAssignments.stream()
            .collect(Collectors.groupingBy(a -> a.getPartido().getFechaHora().toLocalDate().toString()));

        model.addAttribute("user", user);
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("currentMonth", yearMonth);
        model.addAttribute("assignmentsByDate", assignmentsByDate);
        model.addAttribute("title", "Calendario - " + yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        System.out.println("currentMonth: " + yearMonth);
        System.out.println("Total asignaciones del mes: " + monthAssignments.size());
        assignmentsByDate.forEach((fecha, asignaciones) ->
            System.out.println(" -> " + fecha + ": " + asignaciones.size())
        );

        return "referee/calendar";
    }
}
