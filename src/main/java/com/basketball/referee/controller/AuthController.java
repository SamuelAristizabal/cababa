package com.basketball.referee.controller;

import com.basketball.referee.entity.User;
import com.basketball.referee.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        // If user is already authenticated, redirect to dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Ha cerrado sesión exitosamente");
        }

        model.addAttribute("title", "Iniciar Sesión");
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "Registro de Usuario");
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid User user, 
                                 BindingResult result, 
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Registro de Usuario");
            return "auth/register";
        }

        // Check if username already exists
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "El nombre de usuario ya existe");
            model.addAttribute("title", "Registro de Usuario");
            return "auth/register";
        }

        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "El email ya está registrado");
            model.addAttribute("title", "Registro de Usuario");
            return "auth/register";
        }

        try {
            userService.createReferee(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registro exitoso. Puede iniciar sesión con sus credenciales.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al registrar usuario: " + e.getMessage());
            model.addAttribute("title", "Registro de Usuario");
            return "auth/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        // Check user role and redirect accordingly
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/referee/dashboard";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("title", "Acceso Denegado");
        return "error/access-denied";
    }
}
