package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.service.TransactionService;
import com.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @GetMapping
    public String listTransactions(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // Get all accounts for the user
                List<Account> userAccounts = accountService.getUserAccounts(user);
                
                // Get transactions for all user's accounts with pagination
                List<Transaction> transactions = new ArrayList<>();
                Pageable pageable = PageRequest.of(page, size);
                for (Account userAccount : userAccounts) {
                    Page<Transaction> accountTransactions = transactionRepository.findBySourceAccountOrDestinationAccount(userAccount, userAccount, pageable);
                    transactions.addAll(accountTransactions.getContent());
                }
                
                model.addAttribute("transactions", transactions);
                model.addAttribute("user", user);
                
                // Add sample accounts for the filter dropdown
                List<Account> accounts = createSampleAccounts();
                model.addAttribute("accounts", accounts);
                
                return "transactions/index";
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/{transactionId}")
    public String viewTransaction(@PathVariable Long transactionId, Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // For demo purposes, create a sample transaction
                Transaction transaction = createSampleTransaction(transactionId);
                
                model.addAttribute("transaction", transaction);
                model.addAttribute("user", user);
                
                return "transactions/details";
            }
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/new")
    public String showNewTransactionForm(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // Get user's accounts for the transfer form
                List<Account> userAccounts = accountService.getUserAccounts(user);
                
                model.addAttribute("user", user);
                model.addAttribute("userAccounts", userAccounts);
                model.addAttribute("transactionForm", new TransactionForm());
                
                return "transactions/new";
            }
        }
        
        return "redirect:/login";
    }
    
    @PostMapping("/transfer")
    public String processTransfer(
            @ModelAttribute("transactionForm") TransactionForm transactionForm,
            RedirectAttributes redirectAttributes) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                try {
                    // Process the transfer
                    Transaction transaction = transactionService.transfer(
                            transactionForm.getFromAccountNumber(),
                            transactionForm.getToAccountNumber(),
                            transactionForm.getAmount(),
                            transactionForm.getDescription()
                    );
                    
                    redirectAttributes.addFlashAttribute("success", "Transfer completed successfully!");
                    return "redirect:/transactions/" + transaction.getId();
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Transfer failed: " + e.getMessage());
                    return "redirect:/transactions/new";
                }
            }
        }
        
        return "redirect:/login";
    }
    
    @PostMapping("/new")
    public String createTransaction(
            @ModelAttribute("transactionForm") TransactionForm transactionForm,
            RedirectAttributes redirectAttributes) {
        
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                try {
                    // Process the transaction based on type
                    Transaction transaction;
                    if (transactionForm.getType() == Transaction.TransactionType.TRANSFER) {
                        transaction = transactionService.transfer(
                                transactionForm.getFromAccountNumber(),
                                transactionForm.getToAccountNumber(),
                                transactionForm.getAmount(),
                                transactionForm.getDescription()
                        );
                    } else if (transactionForm.getType() == Transaction.TransactionType.DEPOSIT) {
                        // Handle deposit
                        // Implementation needed
                        throw new UnsupportedOperationException("Deposit functionality not implemented yet");
                    } else {
                        // Handle withdrawal
                        // Implementation needed
                        throw new UnsupportedOperationException("Withdrawal functionality not implemented yet");
                    }
                    
                    redirectAttributes.addFlashAttribute("success", "Transaction completed successfully!");
                    return "redirect:/transactions/" + transaction.getId();
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Transaction failed: " + e.getMessage());
                    return "redirect:/transactions/new";
                }
            }
        }
        
        return "redirect:/login";
    }
    
    // Helper method to create sample transactions for demonstration
    private List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        // Sample transaction 1: Salary Deposit
        Transaction transaction1 = new Transaction();
        transaction1.setId(123L);
        transaction1.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction1.setAmount(new BigDecimal("3250.00"));
        transaction1.setDescription("Salary Deposit");
        transaction1.setReferenceNumber("From: ABC Company");
        transaction1.setTransactionNumber("TRX-" + System.currentTimeMillis());
        transaction1.setTransactionDate(LocalDateTime.of(2025, 5, 1, 9, 30));
        transaction1.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactions.add(transaction1);
        
        // Sample transaction 2: Grocery Shopping
        Transaction transaction2 = new Transaction();
        transaction2.setId(124L);
        transaction2.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction2.setAmount(new BigDecimal("125.40"));
        transaction2.setDescription("Grocery Shopping");
        transaction2.setReferenceNumber("Supermarket XYZ");
        transaction2.setTransactionNumber("TRX-" + (System.currentTimeMillis() + 1));
        transaction2.setTransactionDate(LocalDateTime.of(2025, 5, 3, 14, 15));
        transaction2.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactions.add(transaction2);
        
        // Sample transaction 3: Utility Bill
        Transaction transaction3 = new Transaction();
        transaction3.setId(125L);
        transaction3.setTransactionType(Transaction.TransactionType.PAYMENT);
        transaction3.setAmount(new BigDecimal("85.20"));
        transaction3.setDescription("Utility Bill");
        transaction3.setReferenceNumber("Electric Company");
        transaction3.setTransactionNumber("TRX-" + (System.currentTimeMillis() + 2));
        transaction3.setTransactionDate(LocalDateTime.of(2025, 5, 5, 10, 0));
        transaction3.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactions.add(transaction3);
        
        // Sample transaction 4: Transfer to Savings
        Transaction transaction4 = new Transaction();
        transaction4.setId(126L);
        transaction4.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction4.setAmount(new BigDecimal("500.00"));
        transaction4.setDescription("Transfer to Savings");
        transaction4.setReferenceNumber("Internal Transfer");
        transaction4.setTransactionNumber("TRX-" + (System.currentTimeMillis() + 3));
        transaction4.setTransactionDate(LocalDateTime.of(2025, 5, 7, 16, 45));
        transaction4.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactions.add(transaction4);
        
        return transactions;
    }
    
    // Helper method to create a sample transaction for demonstration
    private Transaction createSampleTransaction(Long id) {
        // Find the transaction in the sample list
        return createSampleTransactions().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    // Helper method to create sample accounts for demonstration
    private List<Account> createSampleAccounts() {
        List<Account> accounts = new ArrayList<>();
        
        Account checkingAccount = new Account();
        checkingAccount.setId(1L);
        checkingAccount.setAccountNumber("XXXX-1234");
        checkingAccount.setAccountType(Account.AccountType.CHECKING);
        checkingAccount.setBalance(new BigDecimal("2540.50"));
        checkingAccount.setActive(true);
        accounts.add(checkingAccount);
        
        Account savingsAccount = new Account();
        savingsAccount.setId(2L);
        savingsAccount.setAccountNumber("XXXX-5678");
        savingsAccount.setAccountType(Account.AccountType.SAVINGS);
        savingsAccount.setBalance(new BigDecimal("7250.25"));
        savingsAccount.setActive(true);
        accounts.add(savingsAccount);
        
        Account emergencyFund = new Account();
        emergencyFund.setId(3L);
        emergencyFund.setAccountNumber("XXXX-9012");
        emergencyFund.setAccountType(Account.AccountType.SAVINGS);
        emergencyFund.setBalance(new BigDecimal("3709.25"));
        emergencyFund.setActive(true);
        accounts.add(emergencyFund);
        
        return accounts;
    }
    
    // Form class for transfers
    public static class TransferForm {
        private String fromAccount;
        private String toAccount;
        private BigDecimal amount;
        private String description;
        private boolean recurring = false;
        private LocalDate recurringStartDate;
        private String recurringFrequency;
        
        // Getters and setters
        public String getFromAccount() {
            return fromAccount;
        }
        
        public void setFromAccount(String fromAccount) {
            this.fromAccount = fromAccount;
        }
        
        public String getToAccount() {
            return toAccount;
        }
        
        public void setToAccount(String toAccount) {
            this.toAccount = toAccount;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public boolean isRecurring() {
            return recurring;
        }
        
        public void setRecurring(boolean recurring) {
            this.recurring = recurring;
        }
        
        public LocalDate getRecurringStartDate() {
            return recurringStartDate;
        }
        
        public void setRecurringStartDate(LocalDate recurringStartDate) {
            this.recurringStartDate = recurringStartDate;
        }
        
        public String getRecurringFrequency() {
            return recurringFrequency;
        }
        
        public void setRecurringFrequency(String recurringFrequency) {
            this.recurringFrequency = recurringFrequency;
        }
    }
}
