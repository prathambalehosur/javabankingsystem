package com.bankingsystem.controller;

import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public String listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get paginated transactions
        Page<Transaction> transactions = transactionService.getUserTransactions(
                user, 
                PageRequest.of(page, size, Sort.by("transactionDate").descending())
        );
        
        model.addAttribute("transactions", transactions);
        return "transactions/list";
    }
    
    @GetMapping("/{transactionNumber}")
    public String viewTransaction(@PathVariable String transactionNumber, Model model) {
        Transaction transaction = transactionService.getTransactionByNumber(transactionNumber);
        model.addAttribute("transaction", transaction);
        return "transactions/view";
    }
    
    @GetMapping("/search")
    public String searchTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String accountNumber,
            Model model) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Convert LocalDate to LocalDateTime for filtering
        LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        // Search transactions based on criteria
        List<Transaction> transactions = transactionService.searchTransactions(
                user, type, startDateTime, endDateTime, accountNumber);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("type", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("accountNumber", accountNumber);
        
        return "transactions/search";
    }
    
    @GetMapping("/recurring")
    public String listRecurringTransactions(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get recurring transactions
        List<Transaction> recurringTransactions = transactionService.getRecurringTransactions(user);
        
        model.addAttribute("recurringTransactions", recurringTransactions);
        return "transactions/recurring";
    }
    
    @GetMapping("/scheduled")
    public String listScheduledTransactions(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get scheduled transactions
        List<Transaction> scheduledTransactions = transactionService.getScheduledTransactions(user);
        
        model.addAttribute("scheduledTransactions", scheduledTransactions);
        return "transactions/scheduled";
    }
    
    @PostMapping("/scheduled/{transactionNumber}/cancel")
    public String cancelScheduledTransaction(
            @PathVariable String transactionNumber,
            Model model) {
        
        transactionService.cancelScheduledTransaction(transactionNumber);
        return "redirect:/transactions/scheduled";
    }
    
    @GetMapping("/export")
    public String exportTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String format,
            Model model) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Convert LocalDate to LocalDateTime for filtering
        LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        // Get transactions for export
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                user, startDateTime, endDateTime);
        
        model.addAttribute("transactions", transactions);
        
        // If format is specified, generate and return the appropriate format
        if (format != null) {
            if (format.equals("pdf")) {
                // Logic to generate PDF export
                return "transactions/export-pdf";
            } else if (format.equals("excel")) {
                // Logic to generate Excel export
                return "transactions/export-excel";
            } else if (format.equals("csv")) {
                // Logic to generate CSV export
                return "transactions/export-csv";
            }
        }
        
        // Default to HTML view
        return "transactions/export";
    }
}
