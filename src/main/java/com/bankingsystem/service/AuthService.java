package com.bankingsystem.service;

import com.bankingsystem.model.Role;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.RoleRepository;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
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
        
        // Set default role (ROLE_USER) if available
        try {
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                    .orElse(null);
            
            if (userRole != null) {
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not assign default role: " + e.getMessage());
            // Continue without role assignment
        }
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Log welcome message instead of sending notification
        System.out.println("User registered successfully: " + savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * Authenticate user
     */
    public Authentication authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        return authentication;
    }
}
