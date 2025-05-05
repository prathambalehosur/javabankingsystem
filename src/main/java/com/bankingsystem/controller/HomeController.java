package com.bankingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bankingsystem.model.User;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "index";
    }
}
