package com.bankingsystem.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        
        model.addAttribute("status", statusCode);
        model.addAttribute("error", request.getAttribute("javax.servlet.error.message"));
        model.addAttribute("message", exception != null ? exception.getMessage() : "Something went wrong");
        model.addAttribute("timestamp", new java.util.Date());
        
        return "error";
    }
}
