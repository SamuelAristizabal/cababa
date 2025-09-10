package com.basketball.referee.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.basketball.referee.model.Grade;
import com.basketball.referee.model.Referee;
import com.basketball.referee.service.GradeService;
import com.basketball.referee.service.RefereeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/referees/{refereeId}/grade")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private RefereeService refereeService;

    public GradeController(GradeService gradeService, RefereeService refereeService) {
        this.gradeService = gradeService;
        this.refereeService = refereeService;
    }

    /**
     * Formulario para calificar un árbitro específico
     */
    @GetMapping
    public String newGrade(@PathVariable Long refereeId, Model model) {
        Optional<Referee> refereeOpt = refereeService.findById(refereeId);

        if (refereeOpt.isEmpty()) {
            return "redirect:/admin/referees";
        }

        Referee referee = refereeOpt.get();

        Grade grade = new Grade();
        grade.setReferee(referee);

        model.addAttribute("grade", grade);
        model.addAttribute("referee", referee);
        model.addAttribute("title", "Calificar Árbitro");

        return "admin/grades/form";
    }

    /**
     * Guardar la calificación
     */
    @PostMapping
    public String saveGrade(@PathVariable Long refereeId,
                            @Valid @ModelAttribute("grade") Grade grade,
                            BindingResult result,
                            Model model) {

        Optional<Referee> refereeOpt = refereeService.findById(refereeId);

        if (refereeOpt.isEmpty()) {
            return "redirect:/admin/referees";
        }

        Referee referee = refereeOpt.get();
        grade.setReferee(referee);

        if (result.hasErrors()) {
            model.addAttribute("referee", referee);
            model.addAttribute("title", "Calificar Árbitro");
            return "admin/grades/form";
        }

        gradeService.save(grade);
        return "redirect:/admin/referees";
    }
}
