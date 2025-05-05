package com.bankingsystem.controller;

import com.bankingsystem.controller.form.LoanForm;
import com.bankingsystem.model.Loan;
import com.bankingsystem.model.Loan.LoanType;
import com.bankingsystem.model.LoanPayment;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public String showLoanCalculator(Model model) {
        if (!model.containsAttribute("loanForm")) {
            model.addAttribute("loanForm", new LoanForm());
        }
        return "loans/calculator";
    }
    
    @PostMapping("/calculate")
    public String calculateLoan(
            @Valid @ModelAttribute("loanForm") LoanForm loanForm,
            BindingResult bindingResult,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            return "loans/calculator";
        }
        
        BigDecimal amount = loanForm.getAmount();
        BigDecimal annualRate = loanForm.getInterestRate();
        int termMonths = loanForm.getTermMonths();
        
        // Calculate monthly interest rate
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        
        // Calculate EMI using the formula: P * r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal temp = monthlyRate.add(BigDecimal.ONE).pow(termMonths);
        BigDecimal emi = amount.multiply(monthlyRate)
                .multiply(temp)
                .divide(temp.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        
        // Calculate total payment and total interest
        BigDecimal totalPayment = emi.multiply(BigDecimal.valueOf(termMonths));
        BigDecimal totalInterest = totalPayment.subtract(amount);
        
        // Generate amortization schedule
        List<LoanPayment> amortizationSchedule = generateAmortizationSchedule(amount, annualRate, termMonths, emi);
        
        // Add results to the model
        model.addAttribute("emi", emi);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("totalInterest", totalInterest);
        model.addAttribute("amortizationSchedule", amortizationSchedule);
        
        return "loans/calculator";
    }
    
    private List<LoanPayment> generateAmortizationSchedule(
            BigDecimal principal, 
            BigDecimal annualRate, 
            int termMonths, 
            BigDecimal emi) {
        
        List<LoanPayment> schedule = new ArrayList<>();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = principal;
        
        for (int i = 1; i <= termMonths; i++) {
            BigDecimal interestForMonth = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalForMonth = emi.subtract(interestForMonth);
            
            // Adjust for the last payment to account for any rounding differences
            if (i == termMonths) {
                principalForMonth = remainingBalance;
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(principalForMonth);
            }
            
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }
            
            schedule.add(new LoanPayment(
                i,
                emi,
                principalForMonth,
                interestForMonth,
                remainingBalance
            ));
        }
        
        return schedule;
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
