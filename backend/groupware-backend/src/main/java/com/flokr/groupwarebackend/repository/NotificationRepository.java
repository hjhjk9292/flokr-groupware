package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 수신자별 읽지 않은 알림 개수
    long countByRecipient_EmpNoAndReadDateIsNull(Long empNo);

    // 수신자별 읽지 않은 알림 조회
    List<Notification> findByRecipient_EmpNoAndReadDateIsNullOrderByCreateDateDesc(Long empNo);

    // 수신자별 모든 알림 조회 (최신순)
    List<Notification> findByRecipient_EmpNoOrderByCreateDateDesc(Long empNo);

    // 알림 타입별 조회
    List<Notification> findByRecipient_EmpNoAndTypeOrderByCreateDateDesc(Long empNo, String type);

    // 특정 기간의 알림 조회
    List<Notification> findByRecipient_EmpNoAndCreateDateBetweenOrderByCreateDateDesc(
            Long empNo, LocalDateTime startDate, LocalDateTime endDate);

    // 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.readDate = :readDate WHERE n.notificationNo = :notificationNo")
    void markAsRead(@Param("notificationNo") Long notificationNo, @Param("readDate") LocalDateTime readDate);

    // 수신자의 모든 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.readDate = :readDate WHERE n.recipient.empNo = :empNo AND n.readDate IS NULL")
    void markAllAsRead(@Param("empNo") Long empNo, @Param("readDate") LocalDateTime readDate);
}