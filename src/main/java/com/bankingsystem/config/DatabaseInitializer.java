package com.bankingsystem.config;

import com.bankingsystem.model.Role;
import com.bankingsystem.model.Role.ERole;
import com.bankingsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.count() == 0) {
            System.out.println("Initializing roles...");
            
            // Create roles
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            
            Role staffRole = new Role();
            staffRole.setName(ERole.ROLE_STAFF);
            
            Role managerRole = new Role();
            managerRole.setName(ERole.ROLE_MANAGER);
            
            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            
            // Save roles
            roleRepository.saveAll(Arrays.asList(userRole, staffRole, managerRole, adminRole));
            
            System.out.println("Roles initialized successfully!");
        }
    }
}
