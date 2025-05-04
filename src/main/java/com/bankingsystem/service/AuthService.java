package com.bankingsystem.service;

import com.bankingsystem.model.Role;
import com.bankingsystem.model.SecurityConfig;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.RoleRepository;
import com.bankingsystem.repository.SecurityConfigRepository;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityConfigRepository securityConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;
    
    /**
     * Register a new user with default role
     */
    @Transactional
    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role (ROLE_USER)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        roles.add(userRole);
        user.setRoles(roles);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Create security config
        SecurityConfig securityConfig = new SecurityConfig();
        securityConfig.setUser(savedUser);
        securityConfigRepository.save(securityConfig);
        
        // Send welcome notification
        notificationService.sendWelcomeNotification(savedUser);
        
        return savedUser;
    }
    
    /**
     * Authenticate user
     */
    public Authentication authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Reset login attempts on successful login
        Optional<User> user = userRepository.findByUsername(username);
        user.ifPresent(u -> {
            securityConfigRepository.resetLoginAttempts(u);
        });
        
        return authentication;
    }
    
    /**
     * Generate and send OTP for two-factor authentication
     */
    public String generateAndSendOtp(User user) {
        String otp = generateOtp();
        
        // In a real application, you would send this OTP via SMS or email
        // For simulation, we'll just return it
        
        // Send OTP notification
        notificationService.sendOtpNotification(user, otp);
        
        return otp;
    }
    
    /**
     * Verify biometric data (simulation)
     */
    public boolean verifyBiometric(User user, String biometricData) {
        Optional<SecurityConfig> securityConfig = securityConfigRepository.findByUser(user);
        
        if (securityConfig.isPresent() && securityConfig.get().isBiometricEnabled()) {
            // In a real application, you would compare the biometric data properly
            // For simulation, we'll just do a simple check
            return biometricData.equals(securityConfig.get().getBiometricData());
        }
        
        return false;
    }
    
    /**
     * Enable two-factor authentication
     */
    @Transactional
    public void enableTwoFactorAuth(User user, String method) {
        SecurityConfig securityConfig = securityConfigRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Security config not found"));
        
        securityConfig.setTwoFactorEnabled(true);
        securityConfig.setTwoFactorMethod(method);
        securityConfigRepository.save(securityConfig);
        
        // Send notification
        notificationService.sendSecurityUpdateNotification(user, "Two-factor authentication enabled");
    }
    
    /**
     * Enable biometric authentication
     */
    @Transactional
    public void enableBiometricAuth(User user, String biometricType, String biometricData) {
        SecurityConfig securityConfig = securityConfigRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Security config not found"));
        
        securityConfig.setBiometricEnabled(true);
        securityConfig.setBiometricType(biometricType);
        securityConfig.setBiometricData(biometricData);
        securityConfigRepository.save(securityConfig);
        
        // Send notification
        notificationService.sendSecurityUpdateNotification(user, "Biometric authentication enabled");
    }
    
    /**
     * Handle failed login attempt
     */
    @Transactional
    public void handleFailedLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            SecurityConfig securityConfig = securityConfigRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Security config not found"));
            
            // Increment login attempts
            securityConfigRepository.incrementLoginAttempts(user);
            
            // Check if account should be locked (after 5 failed attempts)
            if (securityConfig.getLoginAttempts() >= 5) {
                securityConfigRepository.updateAccountLockStatus(user, true);
                
                // Send notification about account lock
                notificationService.sendSecurityAlertNotification(user, 
                        "Account locked due to multiple failed login attempts");
            }
        }
    }
    
    /**
     * Change password
     */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Update last password change date
        SecurityConfig securityConfig = securityConfigRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Security config not found"));
        securityConfig.setLastPasswordChange(LocalDateTime.now());
        securityConfigRepository.save(securityConfig);
        
        // Send notification
        notificationService.sendSecurityUpdateNotification(user, "Password changed successfully");
    }
    
    /**
     * Generate a random 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
