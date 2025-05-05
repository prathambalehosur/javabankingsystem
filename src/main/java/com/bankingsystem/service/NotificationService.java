package com.bankingsystem.service;

import com.bankingsystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    /**
     * Send a welcome notification to a new user
     */
    public void sendWelcomeNotification(User user) {
        // In a real application, this would create a notification
        System.out.println("Welcome notification sent to: " + user.getEmail());
    }
    
    /**
     * Send OTP notification
     */
    public void sendOtpNotification(User user, String otp) {
        // In a real application, this would send an SMS or email with the OTP
        System.out.println("OTP notification sent to: " + user.getEmail() + ", OTP: " + otp);
    }
}
