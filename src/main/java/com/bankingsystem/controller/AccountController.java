package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @GetMapping
    public String listAccounts(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                try {
                    // Get active accounts for the user
                    List<Account> accounts = accountService.getUserActiveAccounts(user);
                    
                    if (accounts.isEmpty()) {
                        // If no accounts exist, create sample accounts for the user
                        accounts = createSampleAccounts();
                        // Save the sample accounts to the database
                        for (Account account : accounts) {
                            account.setUser(user);
                            accountRepository.save(account);
                        }
                    }
                    
                    // Calculate total balance
                    BigDecimal totalBalance = accounts.stream()
                            .map(Account::getBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    model.addAttribute("user", user);
                    model.addAttribute("accounts", accounts);
                    model.addAttribute("totalBalance", totalBalance);
                    model.addAttribute("lastTransaction", java.time.LocalDate.now());
                    
                    return "accounts/index";
                } catch (Exception e) {
                    // Log the error and show a friendly message
                    model.addAttribute("error", "Error loading accounts: " + e.getMessage());
                    return "error";
                }
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/new")
    public String showNewAccountForm(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("accountForm", new AccountForm());
                
                return "accounts/new";
            }
        }
        
        return "redirect:/login";
    }
    
    @PostMapping("/new")
    public String createNewAccount(@ModelAttribute("accountForm") AccountForm accountForm, Model model) {
        // Here you would normally save the account to the database
        // For now, just redirect to the accounts page
        return "redirect:/accounts";
    }
    
    @GetMapping("/{accountNumber}")
    public String viewAccount(@PathVariable String accountNumber, Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // Get the account
                Account account = accountService.getAccountByNumber(accountNumber);
                
                // Use empty list for transactions since we simplified the service
                List<Transaction> transactions = Collections.emptyList();
                
                model.addAttribute("user", user);
                model.addAttribute("account", account);
                model.addAttribute("transactions", transactions);
                
                return "accounts/details";
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/{accountNumber}/statement")
    public String generateStatement(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String format,
            Model model) {
        
        // Get the account
        Account account = accountService.getAccountByNumber(accountNumber);
        
        // Use empty list for transactions since we simplified the service
        List<Transaction> transactions = Collections.emptyList();
        
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
        
        // Default to HTML statement
        return "accounts/statement";
    }
    
    // Helper method to create sample accounts for demonstration
    private List<Account> createSampleAccounts() {
        List<Account> accounts = new ArrayList<>();
        
        // Sample checking account
        Account checkingAccount = new Account();
        checkingAccount.setId(1L);
        checkingAccount.setAccountNumber("XXXX-1234");
        checkingAccount.setAccountType(Account.AccountType.CHECKING);
        checkingAccount.setBalance(new BigDecimal("2540.50"));
        checkingAccount.setActive(true);
        checkingAccount.setCreatedAt(LocalDateTime.now());
        checkingAccount.setUpdatedAt(LocalDateTime.now());
        accounts.add(checkingAccount);
        
        // Sample savings account
        Account savingsAccount = new Account();
        savingsAccount.setId(2L);
        savingsAccount.setAccountNumber("XXXX-5678");
        savingsAccount.setAccountType(Account.AccountType.SAVINGS);
        savingsAccount.setBalance(new BigDecimal("7250.25"));
        savingsAccount.setActive(true);
        savingsAccount.setCreatedAt(java.time.LocalDateTime.now());
        savingsAccount.setUpdatedAt(java.time.LocalDateTime.now());
        savingsAccount.setActive(true);
        accounts.add(savingsAccount);
        
        // Sample emergency fund account
        Account emergencyFund = new Account();
        emergencyFund.setId(3L);
        emergencyFund.setAccountNumber("XXXX-9012");
        emergencyFund.setAccountType(Account.AccountType.SAVINGS);
        emergencyFund.setBalance(new BigDecimal("10000.00"));
        emergencyFund.setActive(true);
        emergencyFund.setCreatedAt(java.time.LocalDateTime.now());
        emergencyFund.setUpdatedAt(java.time.LocalDateTime.now());
        accounts.add(emergencyFund);
        
        // Sample fixed deposit account
        Account fixedDeposit = new Account();
        fixedDeposit.setId(4L);
        fixedDeposit.setAccountNumber("XXXX-3456");
        fixedDeposit.setAccountType(Account.AccountType.FIXED_DEPOSIT);
        fixedDeposit.setBalance(new BigDecimal("15000.00"));
        fixedDeposit.setActive(true);
        fixedDeposit.setCreatedAt(java.time.LocalDateTime.now());
        fixedDeposit.setUpdatedAt(java.time.LocalDateTime.now());
        accounts.add(fixedDeposit);
        
        return accounts;
    }
    
    // Form class for creating new accounts
    public static class AccountForm {
        private String name;
        private Account.AccountType accountType;
        private BigDecimal initialDeposit;
        private Integer term;
        private Boolean jointAccount = false;
        private String secondaryOwner;
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Account.AccountType getAccountType() {
            return accountType;
        }
        
        public void setAccountType(Account.AccountType accountType) {
            this.accountType = accountType;
        }
        
        public BigDecimal getInitialDeposit() {
            return initialDeposit;
        }
        
        public void setInitialDeposit(BigDecimal initialDeposit) {
            this.initialDeposit = initialDeposit;
        }
        
        public Integer getTerm() {
            return term;
        }
        
        public void setTerm(Integer term) {
            this.term = term;
        }
        
        public Boolean getJointAccount() {
            return jointAccount;
        }
        
        public void setJointAccount(Boolean jointAccount) {
            this.jointAccount = jointAccount;
        }
        
        public String getSecondaryOwner() {
            return secondaryOwner;
        }
        
        public void setSecondaryOwner(String secondaryOwner) {
            this.secondaryOwner = secondaryOwner;
        }
    }
}
