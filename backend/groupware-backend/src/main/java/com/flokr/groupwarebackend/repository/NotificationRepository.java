package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 사용자의 알림 목록 (최신순)
    List<Notification> findByRecipientEmpNoOrderByCreateDateDesc(Long recipientEmpNo);

    // 특정 사용자의 읽지 않은 알림 목록
    List<Notification> findByRecipientEmpNoAndReadDateIsNullOrderByCreateDateDesc(Long recipientEmpNo);

    // 특정 사용자의 읽지 않은 알림 개수
    long countByRecipientEmpNoAndReadDateIsNull(Long recipientEmpNo);

    // 특정 기간 내 알림 목록
    @Query("SELECT n FROM Notification n WHERE n.recipientEmpNo = :recipientEmpNo " +
            "AND n.createDate >= :startDate AND n.createDate <= :endDate " +
            "ORDER BY n.createDate DESC")
    List<Notification> findByRecipientEmpNoAndDateRange(
            @Param("recipientEmpNo") Long recipientEmpNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 특정 타입의 알림 목록
    List<Notification> findByRecipientEmpNoAndTypeOrderByCreateDateDesc(Long recipientEmpNo, String type);
}