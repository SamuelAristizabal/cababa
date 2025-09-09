package com.basketball.referee.controller;

import com.basketball.referee.entity.*;
import com.basketball.referee.service.*;
import com.basketball.referee.service.LiquidacionService.LiquidacionData;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/financial")
@PreAuthorize("hasRole('ADMIN')")
public class FinancialController {

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private LiquidacionService liquidacionService;

//  @Autowired
//  private PdfService pdfService;

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private ArbitroService arbitroService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard Financiero");
        return "admin/financial/dashboard";
    }

    // Tarifas CRUD
    @GetMapping("/tarifas")
    public String listTarifas(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String torneo,
                             @RequestParam(required = false) String escalafon,
                             Model model) {
        List<Tarifa> tarifas;
        
        if (search != null || torneo != null || escalafon != null) {
            tarifas = tarifaService.findByFilters(search, torneo, escalafon);
        } else {
            tarifas = tarifaService.findAll();
        }
        
        List<Torneo> torneos = torneoService.findAll();
        
        model.addAttribute("tarifas", tarifas);
        model.addAttribute("torneos", torneos);
        model.addAttribute("escalafones", Arbitro.Escalafon.values());
        model.addAttribute("title", "Gestión de Tarifas");
        return "admin/financial/tarifas/list";
    }

    @GetMapping("/tarifas/new")
    public String newTarifa(Model model) {
        model.addAttribute("tarifa", new Tarifa());
        model.addAttribute("torneos", torneoService.findActive());
        model.addAttribute("escalafones", Arbitro.Escalafon.values());
        model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
        model.addAttribute("title", "Nueva Tarifa");
        return "admin/financial/tarifas/form";
    }

    @PostMapping("/tarifas")
    public String createTarifa(@Valid Tarifa tarifa,@RequestParam BigDecimal montoTorneo,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
            model.addAttribute("title", "Nueva Tarifa");
            return "admin/financial/tarifas/form";
        }

        try {
            tarifaService.create(tarifa, montoTorneo);
            redirectAttributes.addFlashAttribute("successMessage", "Tarifa creada exitosamente");
            return "redirect:/admin/financial/tarifas";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear tarifa: " + e.getMessage());
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
            model.addAttribute("title", "Nueva Tarifa");
            return "admin/financial/tarifas/form";
        }
    }

    @GetMapping("/tarifas/{id}/edit")
    public String editTarifa(@PathVariable Long id, Model model) {
        Optional<Tarifa> tarifaOpt = tarifaService.findById(id);
        if (tarifaOpt.isEmpty()) {
            return "redirect:/admin/financial/tarifas";
        }

        model.addAttribute("tarifa", tarifaOpt.get());
        model.addAttribute("torneos", torneoService.findActive());
        model.addAttribute("escalafones", Arbitro.Escalafon.values());
        model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
        model.addAttribute("title", "Editar Tarifa");
        return "admin/financial/tarifas/form";
    }

    @PostMapping("/tarifas/{id}")
    public String updateTarifa(@PathVariable Long id,
                              @Valid Tarifa tarifa,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
            model.addAttribute("title", "Editar Tarifa");
            return "admin/financial/tarifas/form";
        }

        try {
            tarifaService.update(id, tarifa);
            redirectAttributes.addFlashAttribute("successMessage", "Tarifa actualizada exitosamente");
            return "redirect:/admin/financial/tarifas";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar tarifa: " + e.getMessage());
            model.addAttribute("torneos", torneoService.findActive());
            model.addAttribute("escalafones", Arbitro.Escalafon.values());
            model.addAttribute("roles", AsignacionPartido.RolArbitro.values());
            model.addAttribute("title", "Editar Tarifa");
            return "admin/financial/tarifas/form";
        }
    }

    // Liquidaciones
    @GetMapping("/liquidaciones")
    public String liquidaciones(Model model) {
        List<Arbitro> arbitros = arbitroService.findAllActive();
        model.addAttribute("arbitros", arbitros);
        model.addAttribute("title", "Liquidaciones");
        return "admin/financial/liquidaciones/index";
    }

    @GetMapping("/liquidaciones/arbitro/{arbitroId}")
    public String liquidacionArbitro(@PathVariable Long arbitroId,
                                   @RequestParam(required = false) String month,
                                   @RequestParam(required = false) String year,
                                   Model model) {
        
        YearMonth yearMonth = YearMonth.now();
        if (month != null && year != null) {
            yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        }

        Optional<Arbitro> arbitroOpt = arbitroService.findById(arbitroId);
        if (arbitroOpt.isEmpty()) {
            return "redirect:/admin/financial/liquidaciones";
        }

        LiquidacionData liquidacion = liquidacionService.calculateLiquidacion(arbitroId, yearMonth);
        
        model.addAttribute("arbitro", arbitroOpt.get());
        model.addAttribute("liquidacion", liquidacion);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("title", "Liquidación de " + arbitroOpt.get().getUser().getFullName());
        return "admin/financial/liquidaciones/detail";
    }
}