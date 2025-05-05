package com.bankingsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-db")
public class TestDatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection
            String dbStatus = jdbcTemplate.queryForObject("SELECT 'Connected' as status", String.class);
            response.put("status", "success");
            response.put("message", "Database connection: " + dbStatus);
            
            // Check if tables exist
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()");
            response.put("tables", tables);
            
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            return response;
        }
    }
}
