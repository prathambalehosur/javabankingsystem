package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.model.Account.AccountType;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.controller.form.AccountForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                    // Get the most recent transaction date if any
                    LocalDateTime lastTransaction = accounts.stream()
                        .flatMap(account -> account.getTransactions() != null ? account.getTransactions().stream() : null)
                        .filter(Objects::nonNull)
                        .map(Transaction::getTransactionDate)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                    model.addAttribute("user", user);
                    model.addAttribute("accounts", accounts);
                    model.addAttribute("totalBalance", totalBalance);
                    model.addAttribute("lastTransaction", lastTransaction);
                    
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
                if (!model.containsAttribute("accountForm")) {
                    model.addAttribute("accountForm", new AccountForm());
                }
                return "accounts/new";
            }
        }
        
        return "redirect:/login";
    }
    
    @PostMapping("/new")
    public String createAccount(
            @Valid @ModelAttribute("accountForm") AccountForm accountForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
            return "redirect:/login";
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        // Validate minimum deposit based on account type
        if (accountForm.getType() != null && accountForm.getInitialDeposit() != null) {
            if (accountForm.getType().equals("SAVINGS") && accountForm.getInitialDeposit().compareTo(new BigDecimal("100")) < 0) {
                bindingResult.rejectValue("initialDeposit", "Min.initialDeposit", "Minimum deposit for Savings account is $100");
            } else if (accountForm.getType().equals("CHECKING") && accountForm.getInitialDeposit().compareTo(new BigDecimal("50")) < 0) {
                bindingResult.rejectValue("initialDeposit", "Min.initialDeposit", "Minimum deposit for Checking account is $50");
            } else if (accountForm.getType().equals("FIXED_DEPOSIT") && accountForm.getInitialDeposit().compareTo(new BigDecimal("1000")) < 0) {
                bindingResult.rejectValue("initialDeposit", "Min.initialDeposit", "Minimum deposit for Fixed Deposit is $1000");
            }
        }
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.accountForm", bindingResult);
            redirectAttributes.addFlashAttribute("accountForm", accountForm);
            return "redirect:/accounts/new";
        }
        
        try {
            // Create the account with all provided information
            AccountType accountType = AccountType.valueOf(accountForm.getType());
            User secondaryUser = null;
            
            // Find secondary user if this is a joint account
            if (accountForm.isJointAccount() && accountForm.getSecondaryOwner() != null && !accountForm.getSecondaryOwner().trim().isEmpty()) {
                secondaryUser = userRepository.findByUsername(accountForm.getSecondaryOwner().trim())
                        .orElseThrow(() -> new RuntimeException("Secondary user not found"));
            }
            
            // Create the account with all information
            Account account = accountService.createAccount(
                user, 
                accountType, 
                accountForm.getInitialDeposit(),
                accountForm.getName(),
                accountForm.isJointAccount(),
                secondaryUser
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully!");
            return "redirect:/accounts";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating account: " + e.getMessage());
            redirectAttributes.addFlashAttribute("accountForm", accountForm);
            return "redirect:/accounts/new";
        }
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
    
    // AccountForm is now in the com.bankingsystem.controller.form package
}
