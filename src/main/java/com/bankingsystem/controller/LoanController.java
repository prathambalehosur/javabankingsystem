package com.bankingsystem.controller;

import com.bankingsystem.model.Loan;
import com.bankingsystem.model.Loan.LoanType;
import com.bankingsystem.model.User;
import com.bankingsystem.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public String listLoans(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get user's loans
        List<Loan> loans = loanService.getUserLoans(user);
        
        model.addAttribute("loans", loans);
        return "loans/list";
    }
    
    @GetMapping("/{loanNumber}")
    public String viewLoan(@PathVariable String loanNumber, Model model) {
        // Get the loan
        Loan loan = loanService.getLoanByNumber(loanNumber);
        
        model.addAttribute("loan", loan);
        return "loans/view";
    }
    
    @GetMapping("/apply")
    public String showLoanApplicationForm(Model model) {
        model.addAttribute("loanTypes", LoanType.values());
        return "loans/apply";
    }
    
    @PostMapping("/apply")
    public String applyForLoan(
            @RequestParam("loanType") LoanType loanType,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("termMonths") Integer termMonths,
            @RequestParam("purpose") String purpose,
            RedirectAttributes redirectAttributes) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        try {
            Loan loan = loanService.applyForLoan(user, loanType, amount, termMonths, purpose);
            redirectAttributes.addFlashAttribute("success", 
                    "Loan application submitted successfully with loan number: " + loan.getLoanNumber());
            return "redirect:/loans";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loans/apply";
        }
    }
    
    @GetMapping("/calculator")
    public String showLoanCalculator() {
        return "loans/calculator";
    }
    
    @PostMapping("/calculator")
    public String calculateLoan(
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("interestRate") BigDecimal interestRate,
            @RequestParam("termMonths") Integer termMonths,
            Model model) {
        
        BigDecimal emi = loanService.calculateEMI(amount, interestRate, termMonths);
        BigDecimal totalAmount = emi.multiply(new BigDecimal(termMonths));
        BigDecimal totalInterest = totalAmount.subtract(amount);
        
        model.addAttribute("amount", amount);
        model.addAttribute("interestRate", interestRate);
        model.addAttribute("termMonths", termMonths);
        model.addAttribute("emi", emi);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalInterest", totalInterest);
        
        // Generate amortization schedule
        List<Object[]> amortizationSchedule = loanService.generateAmortizationSchedule(
                amount, interestRate, termMonths);
        model.addAttribute("amortizationSchedule", amortizationSchedule);
        
        return "loans/calculator-results";
    }
    
    @GetMapping("/eligibility")
    public String showEligibilityCalculator() {
        return "loans/eligibility";
    }
    
    @PostMapping("/eligibility")
    public String checkEligibility(
            @RequestParam("monthlyIncome") BigDecimal monthlyIncome,
            @RequestParam("existingEMI") BigDecimal existingEMI,
            @RequestParam("loanType") LoanType loanType,
            Model model) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        BigDecimal eligibleAmount = loanService.calculateEligibleAmount(user, monthlyIncome, existingEMI, loanType);
        
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("existingEMI", existingEMI);
        model.addAttribute("loanType", loanType);
        model.addAttribute("eligibleAmount", eligibleAmount);
        
        return "loans/eligibility-results";
    }
    
    @GetMapping("/{loanNumber}/repayment")
    public String showRepaymentSchedule(@PathVariable String loanNumber, Model model) {
        // Get the loan
        Loan loan = loanService.getLoanByNumber(loanNumber);
        
        // Get repayment schedule
        List<Object[]> repaymentSchedule = loanService.getRepaymentSchedule(loan);
        
        model.addAttribute("loan", loan);
        model.addAttribute("repaymentSchedule", repaymentSchedule);
        
        return "loans/repayment-schedule";
    }
    
    @PostMapping("/{loanNumber}/pay-emi")
    public String payEMI(
            @PathVariable String loanNumber,
            @RequestParam("accountNumber") String accountNumber,
            RedirectAttributes redirectAttributes) {
        
        try {
            loanService.payEMI(loanNumber, accountNumber);
            redirectAttributes.addFlashAttribute("success", "EMI payment successful");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/loans/" + loanNumber;
    }
}
