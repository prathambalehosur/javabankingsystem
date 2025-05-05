package com.bankingsystem.repository;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.Transaction.TransactionStatus;
import com.bankingsystem.model.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    
    List<Transaction> findBySourceAccount(Account account);
    
    List<Transaction> findByDestinationAccount(Account account);
    
    List<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount);
    
    Page<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount, Pageable pageable);
    
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findBySourceAccountAndTransactionDateBetween(
            Account account, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findByDestinationAccountAndTransactionDateBetween(
            Account account, LocalDateTime startDate, LocalDateTime endDate);
}
