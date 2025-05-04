package com.bankingsystem.repository;

import com.bankingsystem.model.Notification;
import com.bankingsystem.model.Notification.DeliveryChannel;
import com.bankingsystem.model.Notification.NotificationType;
import com.bankingsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUser(User user);
    
    Page<Notification> findByUser(User user, Pageable pageable);
    
    List<Notification> findByUserAndRead(User user, boolean read);
    
    List<Notification> findByUserAndType(User user, NotificationType type);
    
    List<Notification> findByUserAndDeliveryChannel(User user, DeliveryChannel deliveryChannel);
    
    List<Notification> findByUserAndCreatedAtAfter(User user, LocalDateTime dateTime);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = ?1 AND n.read = false")
    Long countUnreadNotifications(User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = ?1")
    void markAsRead(Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = ?1")
    void markAllAsRead(User user);
}
