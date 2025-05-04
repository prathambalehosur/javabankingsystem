package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.service.NotificationService;
import com.bankingsystem.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get user's accounts
        List<Account> accounts = accountService.getUserActiveAccounts(user);
        
        // Get recent transactions
        List<Transaction> recentTransactions = transactionService.getRecentTransactions(user, 5);
        
        // Get unread notification count
        Long unreadNotifications = notificationService.countUnreadNotifications(user);
        
        // Calculate total balance across all accounts
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Add data to the model
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("totalBalance", totalBalance);
        
        return "dashboard/index";
    }
    
    @GetMapping("/profile")
    public String profile(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        model.addAttribute("user", user);
        return "dashboard/profile";
    }
    
    @GetMapping("/settings")
    public String settings(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        model.addAttribute("user", user);
        return "dashboard/settings";
    }
    
    @GetMapping("/notifications")
    public String notifications(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get user's notifications
        model.addAttribute("notifications", notificationService.getAllNotifications(user));
        
        // Mark all as read
        notificationService.markAllAsRead(user);
        
        return "dashboard/notifications";
    }
}
