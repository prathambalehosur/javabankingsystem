package com.bankingsystem.service;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.Transaction.TransactionStatus;
import com.bankingsystem.model.Transaction.TransactionType;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Get a transaction by its transaction number
     */
    public Transaction getTransactionByNumber(String transactionNumber) {
        return transactionRepository.findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    /**
     * Get recent transactions for a user
     */
    public List<Transaction> getRecentTransactions(User user, int limit) {
        // Get all user accounts
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        if (userAccounts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get transactions for all accounts
        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : userAccounts) {
            allTransactions.addAll(transactionRepository.findBySourceAccountOrTargetAccount(account, account));
        }
        
        // Sort by date descending and limit
        return allTransactions.stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated transactions for a user
     */
    public Page<Transaction> getUserTransactions(User user, Pageable pageable) {
        // Get all user accounts
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        if (userAccounts.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Get first account for pagination (in a real app, you'd handle multiple accounts better)
        Account account = userAccounts.get(0);
        
        return transactionRepository.findBySourceAccountOrTargetAccount(account, account, pageable);
    }

    /**
     * Get all transactions for an account
     */
    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findBySourceAccountOrTargetAccount(account, account);
    }

    /**
     * Search transactions based on criteria
     */
    public List<Transaction> searchTransactions(
            User user,
            String type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String accountNumber) {
        
        // Get all user accounts
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        if (userAccounts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Filter by account number if provided
        Account specificAccount = null;
        if (accountNumber != null && !accountNumber.isEmpty()) {
            specificAccount = accountRepository.findByAccountNumber(accountNumber)
                    .orElse(null);
            
            // Check if the account belongs to the user
            if (specificAccount != null && !specificAccount.getUser().equals(user)) {
                specificAccount = null;
            }
        }
        
        // Get transactions
        List<Transaction> transactions;
        if (specificAccount != null) {
            transactions = transactionRepository.findBySourceAccountOrTargetAccount(specificAccount, specificAccount);
        } else {
            transactions = new ArrayList<>();
            for (Account account : userAccounts) {
                transactions.addAll(transactionRepository.findBySourceAccountOrTargetAccount(account, account));
            }
        }
        
        // Apply filters
        return transactions.stream()
                .filter(t -> type == null || type.isEmpty() || t.getTransactionType().name().equals(type))
                .filter(t -> startDate == null || !t.getTransactionDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTransactionDate().isAfter(endDate))
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        // Get all user accounts
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        if (userAccounts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get all transactions
        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : userAccounts) {
            allTransactions.addAll(transactionRepository.findBySourceAccountOrTargetAccount(account, account));
        }
        
        // Filter by date range
        return allTransactions.stream()
                .filter(t -> startDate == null || !t.getTransactionDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTransactionDate().isAfter(endDate))
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .collect(Collectors.toList());
    }

    /**
     * Get recurring transactions for a user
     */
    public List<Transaction> getRecurringTransactions(User user) {
        // In a real application, you would have a separate model for recurring transactions
        // For now, we'll just return an empty list
        return new ArrayList<>();
    }

    /**
     * Get scheduled transactions for a user
     */
    public List<Transaction> getScheduledTransactions(User user) {
        // Get all user accounts
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        if (userAccounts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get all transactions with PENDING status
        List<Transaction> scheduledTransactions = new ArrayList<>();
        for (Account account : userAccounts) {
            List<Transaction> accountTransactions = transactionRepository.findBySourceAccountOrTargetAccount(account, account);
            scheduledTransactions.addAll(accountTransactions.stream()
                    .filter(t -> t.getStatus() == TransactionStatus.PENDING)
                    .collect(Collectors.toList()));
        }
        
        return scheduledTransactions;
    }

    /**
     * Cancel a scheduled transaction
     */
    @Transactional
    public void cancelScheduledTransaction(String transactionNumber) {
        Transaction transaction = getTransactionByNumber(transactionNumber);
        
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Only pending transactions can be cancelled");
        }
        
        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
    }
}
