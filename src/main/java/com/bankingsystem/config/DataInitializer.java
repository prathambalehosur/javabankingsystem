package com.bankingsystem.config;

import com.bankingsystem.model.Role;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.RoleRepository;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Initialize database with required roles and admin user
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize roles if they don't exist
        initRoles();
        
        // Create admin user if it doesn't exist
        createAdminUserIfNotExists();
        
        log.info("Data initialization completed");
    }
    
    private void initRoles() {
        // Check if roles already exist
        if (roleRepository.count() == 0) {
            log.info("Initializing roles...");
            
            // Create ROLE_USER
            Role userRole = new Role();
            userRole.setName(Role.ERole.ROLE_USER);
            roleRepository.save(userRole);
            
            // Create ROLE_ADMIN
            Role adminRole = new Role();
            adminRole.setName(Role.ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
            
            // Create ROLE_MANAGER
            Role managerRole = new Role();
            managerRole.setName(Role.ERole.ROLE_MANAGER);
            roleRepository.save(managerRole);
            
            log.info("Roles initialized successfully");
        } else {
            log.info("Roles already exist, skipping initialization");
        }
    }
    
    private void createAdminUserIfNotExists() {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating admin user...");
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@bankingsystem.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setPhoneNumber("1234567890");
            adminUser.setAddress("Banking System HQ");
            adminUser.setEnabled(true);
            
            // Set admin role
            Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
            
            log.info("Admin user created successfully");
        } else {
            log.info("Admin user already exists, skipping creation");
        }
    }
}
