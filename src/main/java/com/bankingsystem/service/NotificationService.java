package com.bankingsystem.service;

import com.bankingsystem.model.Notification;
import com.bankingsystem.model.Notification.DeliveryChannel;
import com.bankingsystem.model.Notification.NotificationPriority;
import com.bankingsystem.model.Notification.NotificationType;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    
    /**
     * Get all notifications for a user
     */
    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUser(user);
    }
    
    /**
     * Get paginated notifications for a user
     */
    public Page<Notification> getNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUser(user, pageable);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndRead(user, false);
    }
    
    /**
     * Count unread notifications for a user
     */
    public Long countUnreadNotifications(User user) {
        return notificationRepository.countUnreadNotifications(user);
    }
    
    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
    
    /**
     * Send a welcome notification to a new user
     */
    @Transactional
    public void sendWelcomeNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Welcome to Banking System");
        notification.setMessage("Thank you for registering with our banking system. We're excited to have you on board!");
        notification.setType(NotificationType.SYSTEM);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setPriority(NotificationPriority.NORMAL);
        notification.setActionUrl("/dashboard");
        
        notificationRepository.save(notification);
        
        // In a real application, you might also send an email or SMS
    }
    
    /**
     * Send a notification about a transaction
     */
    @Transactional
    public void sendTransactionNotification(User user, Transaction transaction, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Transaction Alert");
        notification.setMessage(message);
        notification.setType(NotificationType.TRANSACTION);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setActionUrl("/transactions/" + transaction.getTransactionNumber());
        
        notificationRepository.save(notification);
        
        // For high-value transactions, also send SMS
        if (transaction.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            sendSmsNotification(user, "High-value transaction alert: " + message);
        }
    }
    
    /**
     * Send a security alert notification
     */
    @Transactional
    public void sendSecurityAlertNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Security Alert");
        notification.setMessage(message);
        notification.setType(NotificationType.SECURITY);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setPriority(NotificationPriority.CRITICAL);
        notification.setActionUrl("/security-settings");
        
        notificationRepository.save(notification);
        
        // Security alerts should also be sent via SMS and email
        sendSmsNotification(user, "Security Alert: " + message);
        sendEmailNotification(user, "Security Alert", message);
    }
    
    /**
     * Send a notification about security settings update
     */
    @Transactional
    public void sendSecurityUpdateNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Security Settings Updated");
        notification.setMessage(message);
        notification.setType(NotificationType.SECURITY);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setActionUrl("/security-settings");
        
        notificationRepository.save(notification);
        
        // Also send email
        sendEmailNotification(user, "Security Settings Updated", message);
    }
    
    /**
     * Send OTP notification
     */
    @Transactional
    public void sendOtpNotification(User user, String otp) {
        // In a real application, this would send an SMS or email with the OTP
        // For simulation, we'll just create an in-app notification
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Your OTP Code");
        notification.setMessage("Your one-time password is: " + otp + ". It will expire in 5 minutes.");
        notification.setType(NotificationType.SECURITY);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setPriority(NotificationPriority.CRITICAL);
        
        notificationRepository.save(notification);
        
        // Simulate sending SMS
        sendSmsNotification(user, "Your OTP code is: " + otp);
    }
    
    /**
     * Send a low balance notification
     */
    @Transactional
    public void sendLowBalanceNotification(User user, String accountNumber, BigDecimal balance) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Low Balance Alert");
        notification.setMessage("Your account " + accountNumber + " has a low balance of $" + balance);
        notification.setType(NotificationType.ACCOUNT);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setActionUrl("/accounts/" + accountNumber);
        
        notificationRepository.save(notification);
        
        // Also send email
        sendEmailNotification(user, "Low Balance Alert", 
                "Your account " + accountNumber + " has a low balance of $" + balance);
    }
    
    /**
     * Send an email notification (simulation)
     */
    private void sendEmailNotification(User user, String subject, String message) {
        // In a real application, this would use JavaMail or a similar API to send an actual email
        System.out.println("Sending email to " + user.getEmail());
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
    }
    
    /**
     * Send an SMS notification (simulation)
     */
    private void sendSmsNotification(User user, String message) {
        // In a real application, this would use Twilio or a similar API to send an actual SMS
        System.out.println("Sending SMS to " + user.getPhoneNumber());
        System.out.println("Message: " + message);
    }
}
