package com.basketball.referee.controller;

import com.basketball.referee.model.*;
import com.basketball.referee.service.*;
import com.basketball.referee.service.SettlementService.SettlementData;
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
    private FeeService feeService;

    @Autowired
    private SettlementService settlementService;

//  @Autowired
//  private PdfService pdfService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private RefereeService refereeService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard Financiero");
        return "admin/financial/dashboard";
    }

    // Fees CRUD
    @GetMapping("/fees")
    public String listFees(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String tournament,
                             @RequestParam(required = false) String rank,
                             Model model) {
        List<Fee> fees;
        
        if (search != null || tournament != null || rank != null) {
            fees = feeService.findByFilters(search, tournament, rank);
        } else {
            fees = feeService.findAll();
        }
        
        List<Tournament> tournaments = tournamentService.findAll();
        
        model.addAttribute("fees", fees);
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("rankes", Referee.Rank.values());
        model.addAttribute("title", "Gestión de Fees");
        return "admin/financial/fees/list";
    }

    @GetMapping("/fees/new")
    public String newFee(Model model) {
        model.addAttribute("fee", new Fee());
        model.addAttribute("tournaments", tournamentService.findActive());
        model.addAttribute("rankes", Referee.Rank.values());
        model.addAttribute("roles", MatchAssignment.RefereeRole.values());
        model.addAttribute("title", "Nueva Fee");
        return "admin/financial/fees/form";
    }

    @PostMapping("/fees")
    public String createFee(@Valid Fee fee,@RequestParam BigDecimal amountTournament,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("roles", MatchAssignment.RefereeRole.values());
            model.addAttribute("title", "Nueva Fee");
            return "admin/financial/fees/form";
        }

        try {
            feeService.create(fee, amountTournament);
            redirectAttributes.addFlashAttribute("successMessage", "Fee creada exitosamente");
            return "redirect:/admin/financial/fees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear fee: " + e.getMessage());
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("roles", MatchAssignment.RefereeRole.values());
            model.addAttribute("title", "Nueva Fee");
            return "admin/financial/fees/form";
        }
    }

    @GetMapping("/fees/{id}/edit")
    public String editFee(@PathVariable Long id, Model model) {
        Optional<Fee> feeOpt = feeService.findById(id);
        if (feeOpt.isEmpty()) {
            return "redirect:/admin/financial/fees";
        }

        model.addAttribute("fee", feeOpt.get());
        model.addAttribute("tournaments", tournamentService.findActive());
        model.addAttribute("rankes", Referee.Rank.values());
        model.addAttribute("roles", MatchAssignment.RefereeRole.values());
        model.addAttribute("title", "Editar Fee");
        return "admin/financial/fees/form";
    }

    @PostMapping("/fees/{id}")
    public String updateFee(@PathVariable Long id,
                              @Valid Fee fee,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("roles", MatchAssignment.RefereeRole.values());
            model.addAttribute("title", "Editar Fee");
            return "admin/financial/fees/form";
        }

        try {
            feeService.update(id, fee);
            redirectAttributes.addFlashAttribute("successMessage", "Fee actualizada exitosamente");
            return "redirect:/admin/financial/fees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar fee: " + e.getMessage());
            model.addAttribute("tournaments", tournamentService.findActive());
            model.addAttribute("rankes", Referee.Rank.values());
            model.addAttribute("roles", MatchAssignment.RefereeRole.values());
            model.addAttribute("title", "Editar Fee");
            return "admin/financial/fees/form";
        }
    }

    // Settlements
    @GetMapping("/settlements")
    public String settlements(Model model) {
        List<Referee> referees = refereeService.findAllActive();
        model.addAttribute("referees", referees);
        model.addAttribute("title", "Settlements");
        return "admin/financial/settlements/index";
    }

    @GetMapping("/settlements/referee/{refereeId}")
    public String settlementReferee(@PathVariable Long refereeId,
                                   @RequestParam(required = false) String month,
                                   @RequestParam(required = false) String year,
                                   Model model) {
        
        YearMonth yearMonth = YearMonth.now();
        if (month != null && year != null) {
            yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        }

        Optional<Referee> refereeOpt = refereeService.findById(refereeId);
        if (refereeOpt.isEmpty()) {
            return "redirect:/admin/financial/settlements";
        }

        SettlementData settlement = settlementService.calculateSettlement(refereeId, yearMonth);
        
        model.addAttribute("referee", refereeOpt.get());
        model.addAttribute("settlement", settlement);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("title", "Liquidación de " + refereeOpt.get().getUser().getFullName());
        return "admin/financial/settlements/detail";
    }
}