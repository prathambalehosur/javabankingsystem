package com.bankingsystem.repository;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByUser(User user);
    
    List<Account> findByUserAndActive(User user, boolean active);
    
    boolean existsByAccountNumber(String accountNumber);
}
