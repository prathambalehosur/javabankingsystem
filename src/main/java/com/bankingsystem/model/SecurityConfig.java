package com.bankingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;
    
    @Column(name = "two_factor_method")
    private String twoFactorMethod;
    
    @Column(name = "biometric_enabled")
    private boolean biometricEnabled = false;
    
    @Column(name = "biometric_type")
    private String biometricType;
    
    @Column(name = "biometric_data", length = 1000)
    private String biometricData;
    
    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;
    
    @Column(name = "password_expiry_days")
    private Integer passwordExpiryDays = 90;
    
    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;
    
    @Column(name = "account_locked")
    private boolean accountLocked = false;
    
    @Column(name = "account_lock_time")
    private LocalDateTime accountLockTime;
    
    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes = 30;
    
    @Column(name = "remember_me_enabled")
    private boolean rememberMeEnabled = false;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "security_questions_enabled")
    private boolean securityQuestionsEnabled = false;
    
    @Column(name = "ip_restriction_enabled")
    private boolean ipRestrictionEnabled = false;
    
    @Column(name = "allowed_ips", length = 1000)
    private String allowedIps;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastPasswordChange = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
