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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

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
        // Simplified implementation - just return empty list for now
        return Collections.emptyList();
    }

    /**
     * Get all transactions for a user
     */
    public Page<Transaction> getUserTransactions(User user, Pageable pageable) {
        // Simplified implementation - just return empty page for now
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    /**
     * Search transactions based on criteria
     */
    public List<Transaction> searchTransactions(User user, String type, LocalDateTime startDate, LocalDateTime endDate, String accountNumber) {
        // Simplified implementation
        return Collections.emptyList();
    }

    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return Collections.emptyList();
    }

    /**
     * Get recurring transactions
     */
    public List<Transaction> getRecurringTransactions(User user) {
        // Simplified implementation
        return Collections.emptyList();
    }

    /**
     * Get scheduled transactions
     */
    public List<Transaction> getScheduledTransactions(User user) {
        // Simplified implementation
        return Collections.emptyList();
    }

    /**
     * Cancel a scheduled transaction
     */
    public void cancelScheduledTransaction(String transactionNumber) {
        // Simplified implementation - do nothing for now
    }
    
    /**
     * Transfer money between accounts
     * @param fromAccountNumber Source account number
     * @param toAccountNumber Destination account number
     * @param amount Amount to transfer
     * @param description Transfer description
     * @return The created transaction
     */
    @Transactional
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        // Get the source and destination accounts
        Account sourceAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
                
        Account destinationAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));
        
        // Check if source account has sufficient balance
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        // Update account balances
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        
        // Save the transaction and update accounts
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        return transactionRepository.save(transaction);
    }
    
    /**
     * Generate a unique transaction number
     */
    private String generateTransactionNumber() {
        return "TXN" + System.currentTimeMillis();
    }
}
