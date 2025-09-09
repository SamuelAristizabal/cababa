package com.basketball.referee.controller;

import com.basketball.referee.entity.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArbitroService arbitroService;

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private CanchaService canchaService;

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private AsignacionPartidoService asignacionService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Statistics
        long totalArbitros = arbitroService.countActive();
        long totalTorneos = torneoService.countActive();
        long totalCanchas = canchaService.countActive();
        long partidosProgramados = partidoService.countByEstado(Partido.EstadoPartido.PROGRAMADO);
        long asignacionesPendientes = asignacionService.countByEstado(AsignacionPartido.EstadoAsignacion.PENDIENTE);
        long asignacionesAceptadas = asignacionService.countByEstado(AsignacionPartido.EstadoAsignacion.ACEPTADA);
        long asignacionesRechazadas = asignacionService.countByEstado(AsignacionPartido.EstadoAsignacion.RECHAZADA);
        long asignacionesCompletadas = asignacionService.countByEstado(AsignacionPartido.EstadoAsignacion.COMPLETADA);

        // Recent data
        List<Arbitro> recentArbitros = arbitroService.findAll().stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .limit(5)
                .toList();

        List<Torneo> currentTournaments = torneoService.findCurrentTournaments();
        List<Arbitro> mostActiveReferees = arbitroService.findMostActiveReferees().stream().limit(5).toList();

        model.addAttribute("totalArbitros", totalArbitros);
        model.addAttribute("totalTorneos", totalTorneos);
        model.addAttribute("totalCanchas", totalCanchas);
        model.addAttribute("partidosProgramados", partidosProgramados);
        model.addAttribute("asignacionesPendientes", asignacionesPendientes);
        model.addAttribute("asignacionesAceptadas", asignacionesAceptadas);
        model.addAttribute("asignacionesRechazadas", asignacionesRechazadas);
        model.addAttribute("asignacionesCompletadas", asignacionesCompletadas);
        model.addAttribute("recentArbitros", recentArbitros);
        model.addAttribute("currentTournaments", currentTournaments);
        model.addAttribute("mostActiveReferees", mostActiveReferees);
        model.addAttribute("title", "Dashboard Administrativo");

        return "admin/dashboard";
    }

    // CRUD Árbitros
    @GetMapping("/arbitros")
    public String listArbitros(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String escalafon,
                              @RequestParam(required = false) String especialidad,
                              @RequestParam(required = false) String activo,
                              Model model) {
        List<Arbitro> arbitros;
        
        if (search != null || escalafon != null || especialidad != null || activo != null) {
            arbitros = arbitroService.findByFilters(search, escalafon, especialidad, activo);
        } else {
            arbitros = arbitroService.findAll();
        }
        
        model.addAttribute("arbitros", arbitros);
        model.addAttribute("title", "Gestión de Árbitros");
        return "admin/arbitros/list";
    }

    @GetMapping("/arbitros/new")
    public String newArbitro(Model model) {
        Arbitro arbitro = new Arbitro();
        arbitro.setUser(new User()); // Initialize user object
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("title", "Crear Árbitro");
        return "admin/arbitros/form";
    }

    @PostMapping("/arbitros")
    public String createArbitro(@Valid @ModelAttribute Arbitro arbitro,
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
            return "admin/arbitros/form";
        }

        if (userService.existsByEmail(arbitro.getUser().getEmail())) {
            result.rejectValue("user.email", "error.user", "El email ya existe");
            model.addAttribute("errorMessages", List.of("⚠️ El email ya existe"));
            model.addAttribute("title", "Crear Árbitro");
            return "admin/arbitros/form";
        }

        try {
            arbitroService.createArbitro(arbitro, foto);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitro creado exitosamente");
            return "redirect:/admin/arbitros";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear árbitro: " + e.getMessage());
            model.addAttribute("title", "Crear Árbitro");
            return "admin/arbitros/form";
        }
    }

    @GetMapping("/arbitros/{id}")
    public String viewArbitro(@PathVariable Long id, Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findById(id);
        if (arbitroOpt.isEmpty()) {
            return "redirect:/admin/arbitros";
        }

        model.addAttribute("arbitro", arbitroOpt.get());
        model.addAttribute("title", "Detalles del Árbitro");
        return "admin/arbitros/view";
    }

    @GetMapping("/arbitros/{id}/edit")
    public String editArbitro(@PathVariable Long id, Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findById(id);
        if (arbitroOpt.isEmpty()) {
            return "redirect:/admin/arbitros";
        }

        model.addAttribute("arbitro", arbitroOpt.get());
        model.addAttribute("title", "Editar Árbitro");
        return "admin/arbitros/form"; // Use same form template
    }

    @PostMapping("/arbitros/{id}")
    public String updateArbitro(@PathVariable Long id,
                               @Valid @ModelAttribute Arbitro arbitro,
                               BindingResult result,
                               @RequestParam(required = false) MultipartFile foto,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("errorMessages", result.getAllErrors().stream()
                    .map(error -> "⚠️ " + error.getDefaultMessage())
                    .toList());
            model.addAttribute("title", "Editar Árbitro");
            return "admin/arbitros/form";
        }

        try {
            arbitroService.updateArbitro(id, arbitro, foto);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitro actualizado exitosamente");
            return "redirect:/admin/arbitros";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar árbitro: " + e.getMessage());
            model.addAttribute("title", "Editar Árbitro");
            return "admin/arbitros/form";
        }
    }

    @PostMapping("/arbitros/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleArbitroStatus(@PathVariable Long id) {
        try {
            arbitroService.toggleStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    @GetMapping("/asignaciones")
    public String listAsignaciones(Model model) {
        List<AsignacionPartido> asignaciones = asignacionService.findAll();
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("estados", AsignacionPartido.EstadoAsignacion.values());
        model.addAttribute("title", "Gestión de Asignaciones");
        return "admin/asignaciones/list";
    }

    @PostMapping("/asignaciones/{asignacionId}/update-estado")
    public String updateAsignacionEstado(@PathVariable Long asignacionId,
                                        @RequestParam("estado") AsignacionPartido.EstadoAsignacion estado,
                                        RedirectAttributes redirectAttributes) {
        if (estado == AsignacionPartido.EstadoAsignacion.COMPLETADA) {
            asignacionService.completeAssignment(asignacionId);
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
            return "redirect:/admin/asignaciones";

        } else if((estado == AsignacionPartido.EstadoAsignacion.RECHAZADA)){
            asignacionService.rejectAssignment(asignacionId);
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
            return "redirect:/admin/asignaciones";
        } else if((estado == AsignacionPartido.EstadoAsignacion.ACEPTADA)){
            asignacionService.acceptAssignment(asignacionId);
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
            return "redirect:/admin/asignaciones";
        }
        return "redirect:/admin/asignaciones";
    }

    // CRUD Torneos
    @GetMapping("/torneos")
    public String listTorneos(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String estado,
                             @RequestParam(required = false) String year,
                             Model model) {
        List<Torneo> torneos;
        
        if (search != null || estado != null || year != null) {
            torneos = torneoService.findByFilters(search, estado, year);
        } else {
            torneos = torneoService.findAll();
        }
        
        List<Integer> years = torneoService.getAvailableYears();
        
        model.addAttribute("torneos", torneos);
        model.addAttribute("years", years);
        model.addAttribute("title", "Gestión de Torneos");
        return "admin/torneos/list";
    }

    @GetMapping("/torneos/new")
    public String newTorneo(Model model) {
        model.addAttribute("torneo", new Torneo());
        model.addAttribute("estados", Torneo.EstadoTorneo.values());
        model.addAttribute("title", "Crear Torneo");
        return "admin/torneos/form";
    }

    @PostMapping("/torneos")
    public String createTorneo(@Valid Torneo torneo,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("estados", Torneo.EstadoTorneo.values());
            model.addAttribute("title", "Crear Torneo");
            return "admin/torneos/form";
        }

        try {
            torneoService.create(torneo);
            redirectAttributes.addFlashAttribute("successMessage", "Torneo creado exitosamente");
            return "redirect:/admin/torneos";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear torneo: " + e.getMessage());
            model.addAttribute("estados", Torneo.EstadoTorneo.values());
            model.addAttribute("title", "Crear Torneo");
            return "admin/torneos/form";
        }
    }

    @GetMapping("/torneos/{id}")
    public String viewTorneo(@PathVariable Long id, Model model) {
        Optional<Torneo> torneoOpt = torneoService.findById(id);
        if (torneoOpt.isEmpty()) {
            return "redirect:/admin/torneos";
        }

        model.addAttribute("torneo", torneoOpt.get());
        model.addAttribute("title", "Detalles del Torneo");
        return "admin/torneos/view";
    }

    @GetMapping("/torneos/{id}/edit")
    public String editTorneo(@PathVariable Long id, Model model) {
        Optional<Torneo> torneoOpt = torneoService.findById(id);
        if (torneoOpt.isEmpty()) {
            return "redirect:/admin/torneos";
        }

        model.addAttribute("torneo", torneoOpt.get());
        model.addAttribute("title", "Editar Torneo");
        return "admin/torneos/form"; // Use same form template
    }

    @PostMapping("/torneos/{id}")
    public String updateTorneo(@PathVariable Long id,
                              @Valid Torneo torneo,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("estados", Torneo.EstadoTorneo.values());
            model.addAttribute("title", "Editar Torneo");
            return "admin/torneos/edit";
        }

        try {
            torneoService.update(id, torneo);
            redirectAttributes.addFlashAttribute("successMessage", "Torneo actualizado exitosamente");
            return "redirect:/admin/torneos";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar torneo: " + e.getMessage());
            model.addAttribute("estados", Torneo.EstadoTorneo.values());
            model.addAttribute("title", "Editar Torneo");
            return "admin/torneos/edit";
        }
    }

    @PostMapping("/torneos/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleTorneoStatus(@PathVariable Long id) {
        try {
            torneoService.toggleStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    @DeleteMapping("/torneos/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTorneo(@PathVariable Long id) {
        try {
            torneoService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar torneo: " + e.getMessage());
        }
    }

    // CRUD Canchas
    @GetMapping("/canchas")
    public String listCanchas(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String activa,
                             Model model) {
        List<Cancha> canchas;
        
        if (search != null || activa != null) {
            canchas = canchaService.findByFilters(search, activa);
        } else {
            canchas = canchaService.findAll();
        }
        
        model.addAttribute("canchas", canchas);
        model.addAttribute("title", "Gestión de Canchas");
        return "admin/canchas/list";
    }

    @GetMapping("/canchas/new")
    public String newCancha(Model model) {
        model.addAttribute("cancha", new Cancha());
        model.addAttribute("title", "Crear Cancha");
        return "admin/canchas/form";
    }

    @PostMapping("/canchas")
    public String createCancha(@Valid Cancha cancha,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Crear Cancha");
            return "admin/canchas/form";
        }

        try {
            canchaService.create(cancha);
            redirectAttributes.addFlashAttribute("successMessage", "Cancha creada exitosamente");
            return "redirect:/admin/canchas";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear cancha: " + e.getMessage());
            model.addAttribute("title", "Crear Cancha");
            return "admin/canchas/form";
        }
    }

    @GetMapping("/canchas/{id}/edit")
    public String editCancha(@PathVariable Long id, Model model) {
        Optional<Cancha> canchaOpt = canchaService.findById(id);
        if (canchaOpt.isEmpty()) {
            return "redirect:/admin/canchas";
        }

        model.addAttribute("cancha", canchaOpt.get());
        model.addAttribute("title", "Editar Cancha");
        return "admin/canchas/form"; // Use same form template
    }

    @PostMapping("/canchas/{id}")
    public String updateCancha(@PathVariable Long id,
                              @Valid Cancha cancha,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Editar Cancha");
            return "admin/canchas/edit";
        }

        try {
            canchaService.update(id, cancha);
            redirectAttributes.addFlashAttribute("successMessage", "Cancha actualizada exitosamente");
            return "redirect:/admin/canchas";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar cancha: " + e.getMessage());
            model.addAttribute("title", "Editar Cancha");
            return "admin/canchas/edit";
        }
    }

    @PostMapping("/canchas/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleCanchaStatus(@PathVariable Long id) {
        try {
            canchaService.toggleStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }
}
