package com.bankingsystem.controller;

import com.bankingsystem.model.User;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // Add data to the model
                model.addAttribute("user", user);
                model.addAttribute("accounts", Collections.emptyList());
                model.addAttribute("recentTransactions", Collections.emptyList());
                model.addAttribute("unreadNotifications", 0);
                model.addAttribute("totalBalance", BigDecimal.ZERO);
                
                return "dashboard/index";
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/profile")
    public String profile(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                model.addAttribute("user", user);
                return "dashboard/profile";
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard/notifications")
    public String dashboardNotifications(Model model) {
        // Redirect to the main notifications controller
        return "redirect:/notifications";
    }
}
