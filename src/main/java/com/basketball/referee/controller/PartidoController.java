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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/partidos")
@PreAuthorize("hasRole('ADMIN')")
public class PartidoController {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private CanchaService canchaService;

    @Autowired
    private ArbitroService arbitroService;

    @Autowired
    private AsignacionPartidoService asignacionService;

    @GetMapping
    public String listPartidos(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String torneo,
                              @RequestParam(required = false) String estado,
                              @RequestParam(required = false) String fecha,
                              Model model) {
        List<Partido> partidos;
        
        if (search != null || torneo != null || estado != null || fecha != null) {
            partidos = partidoService.findByFilters(search, torneo, estado, fecha);
        } else {
            partidos = partidoService.findAll();
        }
        
        List<Torneo> torneos = torneoService.findAll();
        
        model.addAttribute("partidos", partidos);
        model.addAttribute("torneos", torneos);
        model.addAttribute("estados", Partido.EstadoPartido.values());
        model.addAttribute("title", "Gestión de Partidos");
        return "admin/partidos/list";
    }

    @GetMapping("/new")
    public String newPartido(Model model) {
        model.addAttribute("partido", new Partido());
        model.addAttribute("torneos", torneoService.findActive());
        model.addAttribute("canchas", canchaService.findActive());
        model.addAttribute("title", "Programar Partido");
        return "admin/partidos/form";
    }

    @PostMapping
    public String createPartido(@Valid Partido partido,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
           model.addAttribute("errorMessages", result.getAllErrors().stream()
                    .map(error -> "⚠️ " + error.getDefaultMessage())
                    .toList()); 
            
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("canchas", canchaService.findActive());
            model.addAttribute("title", "Programar Partido");
            return "admin/partidos/form";
        }
        try {
            Partido savedPartido = partidoService.create(partido);
            redirectAttributes.addFlashAttribute("successMessage", "Partido programado exitosamente");
            return "redirect:/admin/partidos/" + savedPartido.getId() + "/assign";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al programar partido: " + e.getMessage());
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("canchas", canchaService.findActive());
            model.addAttribute("title", "Programar Partido");
            return "admin/partidos/form";
        }
    }

    @GetMapping("/{id}")
    public String viewPartido(@PathVariable Long id, Model model) {
        Optional<Partido> partidoOpt = partidoService.findById(id);
        if (partidoOpt.isEmpty()) {
            return "redirect:/admin/partidos";
        }

        Partido partido = partidoOpt.get();
        List<AsignacionPartido> asignaciones = asignacionService.findByPartido(id);

        model.addAttribute("partido", partido);
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("title", "Detalles del Partido");
        return "admin/partidos/view";
    }

    @GetMapping("/{id}/edit")
    public String editPartido(@PathVariable Long id, Model model) {
        Optional<Partido> partidoOpt = partidoService.findById(id);
        if (partidoOpt.isEmpty()) {
            return "redirect:/admin/partidos";
        }

        model.addAttribute("partido", partidoOpt.get());
        model.addAttribute("torneos", torneoService.findActive());
        model.addAttribute("canchas", canchaService.findActive());
        model.addAttribute("title", "Editar Partido");
        return "admin/partidos/form";
    }

    @PostMapping("/{id}")
    public String updatePartido(@PathVariable Long id,
                               @Valid Partido partido,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("canchas", canchaService.findActive());
            model.addAttribute("title", "Editar Partido");
            return "admin/partidos/form";
        }

        try {
            partidoService.update(id, partido);
            redirectAttributes.addFlashAttribute("successMessage", "Partido actualizado exitosamente");
            return "redirect:/admin/partidos/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar partido: " + e.getMessage());
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("canchas", canchaService.findActive());
            model.addAttribute("title", "Editar Partido");
            return "admin/partidos/form";
        }
    }

    @GetMapping("/{id}/assign")
    public String assignReferees(@PathVariable Long id, Model model) {
        Optional<Partido> partidoOpt = partidoService.findById(id);
        if (partidoOpt.isEmpty()) {
            return "redirect:/admin/partidos";
        }

        Partido partido = partidoOpt.get();
        List<Arbitro> arbitrosCampo = arbitroService.findByEspecialidad(Arbitro.Especialidad.CAMPO);
        List<Arbitro> arbitrosMesa = arbitroService.findByEspecialidad(Arbitro.Especialidad.MESA);
        List<AsignacionPartido> asignacionesExistentes = asignacionService.findByPartido(id);

        model.addAttribute("partido", partido);
        model.addAttribute("arbitrosCampo", arbitrosCampo);
        model.addAttribute("arbitrosMesa", arbitrosMesa);
        model.addAttribute("asignacionesExistentes", asignacionesExistentes);
        model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
        model.addAttribute("title", "Asignar Árbitros");
        return "admin/partidos/assign";
    }

    @PostMapping("/{id}/assign")
    public String processAssignments(@PathVariable Long id,
                                   @RequestParam List<Long> arbitroIds,
                                   @RequestParam List<String> roles,
                                   RedirectAttributes redirectAttributes) {
        try {
            asignacionService.assignReferees(id, arbitroIds, roles);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitros asignados exitosamente");
            return "redirect:/admin/partidos/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar árbitros: " + e.getMessage());
            return "redirect:/admin/partidos/" + id + "/assign";
        }
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelPartido(@PathVariable Long id) {
        try {
            partidoService.updateEstado(id, Partido.EstadoPartido.CANCELADO);
            asignacionService.cancelAllAssignments(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cancelar partido: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public String completePartido(@PathVariable Long id,
                                 @RequestParam Integer resultadoLocal,
                                 @RequestParam Integer resultadoVisitante,
                                 RedirectAttributes redirectAttributes) {
        try {
            partidoService.updateResultado(id, resultadoLocal, resultadoVisitante);
            redirectAttributes.addFlashAttribute("successMessage", "Partido finalizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al finalizar partido: " + e.getMessage());
        }
        return "redirect:/admin/partidos/" + id;
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
        
        List<Partido> partidos = partidoService.findByDateRange(startOfMonth, endOfMonth);

        model.addAttribute("partidos", partidos);
        model.addAttribute("currentDate", date);
        model.addAttribute("title", "Calendario de Partidos");
        return "admin/partidos/calendar";
    }
}
