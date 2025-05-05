package com.bankingsystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTester implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Test database connection
            String result = jdbcTemplate.queryForObject("SELECT 'Connected successfully' as result", String.class);
            System.out.println("Database connection test: " + result);
            
            // Check if users table exists
            try {
                Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
                System.out.println("Found " + userCount + " users in the database");
            } catch (Exception e) {
                System.out.println("Users table doesn't exist yet or cannot be queried: " + e.getMessage());
            }
            
            System.out.println("Database connection is working properly!");
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
