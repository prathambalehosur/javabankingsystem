package com.bankingsystem.controller;

import com.bankingsystem.model.Notification;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserRepository userRepository;

    @GetMapping
    public String listNotifications(Model model) {
        try {
            // Get the currently authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
                String username = ((UserDetails) auth.getPrincipal()).getUsername();
                Optional<User> userOptional = userRepository.findByUsername(username);
                
                if (userOptional.isPresent()) {
                    // For demo purposes, create sample notifications
                    List<Notification> notifications = createSampleNotifications();
                    
                    model.addAttribute("notifications", notifications);
                    model.addAttribute("user", userOptional.get());
                    model.addAttribute("unreadCount", countUnreadNotifications(notifications));
                    
                    return "notifications/index";
                }
            }
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    @GetMapping("/{notificationId}")
    public String viewNotification(@PathVariable Long notificationId, Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // For demo purposes, find the notification by ID
                Notification notification = findNotificationById(notificationId);
                
                if (notification != null) {
                    // Mark as read
                    notification.setRead(true);
                    
                    model.addAttribute("notification", notification);
                    model.addAttribute("user", user);
                    
                    return "notifications/details";
                }
            }
        }
        
        return "redirect:/notifications";
    }
    
    @PostMapping("/{notificationId}/mark-read")
    @ResponseBody
    public String markAsRead(@PathVariable Long notificationId) {
        // Find the notification by ID
        Notification notification = findNotificationById(notificationId);
        
        if (notification != null) {
            // Mark as read
            notification.setRead(true);
            
            return "success";
        }
        
        return "error";
    }
    
    @PostMapping("/mark-all-read")
    public String markAllAsRead() {
        // Get all notifications and mark them as read
        List<Notification> notifications = createSampleNotifications();
        
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        
        return "redirect:/notifications";
    }
    
    // Helper method to create sample notifications for demonstration
    private List<Notification> createSampleNotifications() {
        List<Notification> notifications = new ArrayList<>();
        
        // Sample notification 1
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setTitle("Welcome to Online Banking");
        notification1.setMessage("Thank you for registering with our online banking service.");
        notification1.setType(Notification.NotificationType.SYSTEM);
        notification1.setDeliveryChannel(Notification.DeliveryChannel.IN_APP);
        notification1.setCreatedAt(LocalDateTime.now().minusDays(2));
        notification1.setRead(false);
        notification1.setPriority(Notification.NotificationPriority.NORMAL);
        notifications.add(notification1);
        
        // Sample notification 2
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setTitle("New Transaction");
        notification2.setMessage("A new transaction of $100.00 has been credited to your account.");
        notification2.setType(Notification.NotificationType.TRANSACTION);
        notification2.setDeliveryChannel(Notification.DeliveryChannel.IN_APP);
        notification2.setCreatedAt(LocalDateTime.now().minusHours(5));
        notification2.setRead(false);
        notification2.setPriority(Notification.NotificationPriority.NORMAL);
        notifications.add(notification2);
        
        // Sample notification 3
        Notification notification3 = new Notification();
        notification3.setId(3L);
        notification3.setTitle("Security Alert");
        notification3.setMessage("A new device has been detected logging into your account.");
        notification3.setType(Notification.NotificationType.SECURITY);
        notification3.setDeliveryChannel(Notification.DeliveryChannel.IN_APP);
        notification3.setCreatedAt(LocalDateTime.now().minusHours(1));
        notification3.setRead(true);
        notification3.setPriority(Notification.NotificationPriority.HIGH);
        notifications.add(notification3);
        
        // Sample notification 4
        Notification notification4 = new Notification();
        notification4.setId(4L);
        notification4.setTitle("Low Balance Alert");
        notification4.setMessage("Your Primary Checking account balance is below $500. Consider transferring funds to avoid overdraft fees.");
        notification4.setType(Notification.NotificationType.ALERT);
        notification4.setDeliveryChannel(Notification.DeliveryChannel.IN_APP);
        notification4.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        notification4.setRead(false);
        notification4.setPriority(Notification.NotificationPriority.NORMAL);
        notifications.add(notification4);
        
        return notifications;
    }
    
    // Helper method to find a notification by ID
    private Notification findNotificationById(Long id) {
        return createSampleNotifications().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    // Helper method to count unread notifications
    private int countUnreadNotifications(List<Notification> notifications) {
        return (int) notifications.stream()
                .filter(n -> !n.isRead())
                .count();
    }
}
