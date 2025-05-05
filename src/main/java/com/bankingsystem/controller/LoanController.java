package com.bankingsystem.controller;

import com.bankingsystem.model.Loan;
import com.bankingsystem.model.Loan.LoanType;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final UserRepository userRepository;

    @GetMapping
    public String listLoans(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                model.addAttribute("loans", Collections.emptyList());
                return "loans/list";
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/{loanNumber}")
    public String viewLoan(@PathVariable String loanNumber, Model model) {
        // Simplified implementation
        model.addAttribute("loan", null);
        model.addAttribute("error", "Loan service is currently unavailable");
        return "loans/view";
    }
    
    @GetMapping("/apply")
    public String showLoanApplicationForm(Model model) {
        model.addAttribute("loanTypes", LoanType.values());
        return "loans/apply";
    }
    
    @PostMapping("/apply")
    public String applyForLoan(
            @RequestParam LoanType loanType,
            @RequestParam BigDecimal amount,
            @RequestParam Integer termMonths,
            RedirectAttributes redirectAttributes) {
        
        // Simplified implementation
        redirectAttributes.addFlashAttribute("message", "Loan application service is currently unavailable");
        return "redirect:/loans";
    }
    
    @GetMapping("/calculator")
    public String showLoanCalculator() {
        return "loans/calculator";
    }
    
    @PostMapping("/calculate")
    @ResponseBody
    public String calculateLoan(
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal rate,
            @RequestParam Integer termMonths) {
        
        // Simple EMI calculation formula
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal emi = amount.multiply(monthlyRate.multiply(
                BigDecimal.ONE.add(monthlyRate).pow(termMonths)
        )).divide(
                BigDecimal.ONE.add(monthlyRate).pow(termMonths).subtract(BigDecimal.ONE),
                2, RoundingMode.HALF_UP
        );
        
        return emi.toString();
    }
    
    @GetMapping("/eligibility")
    public String showEligibilityCalculator() {
        return "loans/eligibility";
    }
    
    @PostMapping("/check-eligibility")
    @ResponseBody
    public String checkEligibility(
            @RequestParam BigDecimal income,
            @RequestParam BigDecimal existingEmi,
            @RequestParam LoanType loanType) {
        
        // Simple eligibility calculation
        BigDecimal maxEmi = income.multiply(BigDecimal.valueOf(0.5));
        BigDecimal availableEmi = maxEmi.subtract(existingEmi);
        
        if (availableEmi.compareTo(BigDecimal.ZERO) <= 0) {
            return "0";
        }
        
        // Rough estimate based on loan type
        BigDecimal rate;
        int termMonths;
        
        switch (loanType) {
            case HOME:
                rate = BigDecimal.valueOf(7.5);
                termMonths = 240;
                break;
            case CAR:
                rate = BigDecimal.valueOf(9.0);
                termMonths = 60;
                break;
            case PERSONAL:
                rate = BigDecimal.valueOf(12.0);
                termMonths = 36;
                break;
            case EDUCATION:
                rate = BigDecimal.valueOf(8.0);
                termMonths = 84;
                break;
            default:
                rate = BigDecimal.valueOf(10.0);
                termMonths = 60;
        }
        
        // P = EMI * ((1+r)^n - 1) / (r * (1+r)^n)
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(monthlyRate).pow(termMonths);
        BigDecimal eligibleAmount = availableEmi.multiply(
                factor.subtract(BigDecimal.ONE)
        ).divide(
                monthlyRate.multiply(factor),
                2, RoundingMode.HALF_UP
        );
        
        return eligibleAmount.toString();
    }
}
