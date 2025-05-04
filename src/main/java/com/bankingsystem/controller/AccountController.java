package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Account.AccountType;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.service.TransactionService;
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
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public String listAccounts(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get user's accounts
        List<Account> accounts = accountService.getUserAccounts(user);
        
        model.addAttribute("accounts", accounts);
        return "accounts/list";
    }
    
    @GetMapping("/{accountNumber}")
    public String viewAccount(@PathVariable String accountNumber, Model model) {
        // Get the account
        Account account = accountService.getAccountByNumber(accountNumber);
        
        // Get account transactions
        List<Transaction> transactions = transactionService.getAccountTransactions(account);
        
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        
        return "accounts/view";
    }
    
    @GetMapping("/create")
    public String showCreateAccountForm(Model model) {
        model.addAttribute("accountTypes", AccountType.values());
        return "accounts/create";
    }
    
    @PostMapping("/create")
    public String createAccount(
            @RequestParam("accountType") AccountType accountType,
            @RequestParam("initialDeposit") BigDecimal initialDeposit,
            RedirectAttributes redirectAttributes) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        try {
            Account account = accountService.createAccount(user, accountType, initialDeposit);
            redirectAttributes.addFlashAttribute("success", 
                    "Account created successfully with account number: " + account.getAccountNumber());
            return "redirect:/accounts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/create";
        }
    }
    
    @GetMapping("/deposit")
    public String showDepositForm() {
        return "accounts/deposit";
    }
    
    @PostMapping("/deposit")
    public String deposit(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        
        try {
            accountService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", 
                    "Deposit of $" + amount + " successful");
            return "redirect:/accounts/" + accountNumber;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/deposit";
        }
    }
    
    @GetMapping("/withdraw")
    public String showWithdrawForm() {
        return "accounts/withdraw";
    }
    
    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        
        try {
            accountService.withdraw(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", 
                    "Withdrawal of $" + amount + " successful");
            return "redirect:/accounts/" + accountNumber;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/withdraw";
        }
    }
    
    @GetMapping("/transfer")
    public String showTransferForm(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        // Get user's accounts for source account dropdown
        List<Account> accounts = accountService.getUserActiveAccounts(user);
        
        model.addAttribute("accounts", accounts);
        return "accounts/transfer";
    }
    
    @PostMapping("/transfer")
    public String transfer(
            @RequestParam("fromAccountNumber") String fromAccountNumber,
            @RequestParam("toAccountNumber") String toAccountNumber,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        
        try {
            accountService.transfer(fromAccountNumber, toAccountNumber, amount);
            redirectAttributes.addFlashAttribute("success", 
                    "Transfer of $" + amount + " successful");
            return "redirect:/accounts/" + fromAccountNumber;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/transfer";
        }
    }
    
    @GetMapping("/{accountNumber}/statement")
    public String generateStatement(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String format,
            Model model) {
        
        // Get the account
        Account account = accountService.getAccountByNumber(accountNumber);
        
        // Get all transactions for statement
        List<Transaction> transactions = transactionService.getAccountTransactions(account);
        
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        
        // If format is specified, generate and return the appropriate format
        if (format != null) {
            if (format.equals("pdf")) {
                // Logic to generate PDF statement
                return "accounts/statement-pdf";
            } else if (format.equals("excel")) {
                // Logic to generate Excel statement
                return "accounts/statement-excel";
            }
        }
        
        // Default to HTML view
        return "accounts/statement";
    }
    
    @PostMapping("/{accountNumber}/freeze")
    public String freezeAccount(
            @PathVariable String accountNumber,
            RedirectAttributes redirectAttributes) {
        
        try {
            accountService.setAccountStatus(accountNumber, false);
            redirectAttributes.addFlashAttribute("success", "Account frozen successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/accounts";
    }
    
    @PostMapping("/{accountNumber}/unfreeze")
    public String unfreezeAccount(
            @PathVariable String accountNumber,
            RedirectAttributes redirectAttributes) {
        
        try {
            accountService.setAccountStatus(accountNumber, true);
            redirectAttributes.addFlashAttribute("success", "Account unfrozen successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/accounts";
    }
}
