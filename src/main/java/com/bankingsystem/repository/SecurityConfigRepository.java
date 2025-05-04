package com.bankingsystem.repository;

import com.bankingsystem.model.SecurityConfig;
import com.bankingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityConfigRepository extends JpaRepository<SecurityConfig, Long> {
    
    Optional<SecurityConfig> findByUser(User user);
    
    List<SecurityConfig> findByTwoFactorEnabled(boolean enabled);
    
    List<SecurityConfig> findByBiometricEnabled(boolean enabled);
    
    List<SecurityConfig> findByAccountLocked(boolean locked);
    
    @Query("SELECT s FROM SecurityConfig s WHERE s.lastPasswordChange < ?1")
    List<SecurityConfig> findByPasswordExpired(LocalDateTime expiryDate);
    
    @Modifying
    @Transactional
    @Query("UPDATE SecurityConfig s SET s.loginAttempts = s.loginAttempts + 1 WHERE s.user = ?1")
    void incrementLoginAttempts(User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE SecurityConfig s SET s.loginAttempts = 0, s.lastLogin = CURRENT_TIMESTAMP WHERE s.user = ?1")
    void resetLoginAttempts(User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE SecurityConfig s SET s.accountLocked = ?2, s.accountLockTime = CASE WHEN ?2 = true THEN CURRENT_TIMESTAMP ELSE NULL END WHERE s.user = ?1")
    void updateAccountLockStatus(User user, boolean locked);
}
