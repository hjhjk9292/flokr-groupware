package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 수신자별 알림 조회 (페이징)
    Page<Notification> findByReceiver_EmpIdOrderByCreatedAtDesc(String receiverId, Pageable pageable);
    
    // 읽지 않은 알림 조회
    List<Notification> findByReceiver_EmpIdAndIsReadFalseOrderByCreatedAtDesc(String receiverId);
    
    // 읽지 않은 알림 개수
    long countByReceiver_EmpIdAndIsReadFalse(String receiverId);
    
    // 알림 타입별 조회
    List<Notification> findByReceiver_EmpIdAndTypeOrderByCreatedAtDesc(String receiverId, Notification.NotificationType type);
    
    // 우선순위별 알림 조회
    List<Notification> findByReceiver_EmpIdAndPriorityOrderByCreatedAtDesc(String receiverId, Notification.NotificationPriority priority);
    
    // 특정 기간의 알림 조회
    List<Notification> findByReceiver_EmpIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String receiverId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId, @Param("readAt") LocalDateTime readAt);
    
    // 수신자의 모든 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.receiver.empId = :receiverId AND n.isRead = false")
    void markAllAsRead(@Param("receiverId") String receiverId, @Param("readAt") LocalDateTime readAt);
}
