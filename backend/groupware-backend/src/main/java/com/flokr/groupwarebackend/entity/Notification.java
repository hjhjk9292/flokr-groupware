package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_NO")
    private Long notificationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECIPIENT_EMP_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_notification_recipient"))
    private Employee recipient;

    @Column(name = "TYPE", nullable = false, length = 50)
    private String type;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "NOTIFICATION_CONTENT", columnDefinition = "TEXT")
    private String content;

    @Column(name = "REF_TYPE", length = 50)
    private String refType;

    @Column(name = "REF_NO", length = 255)
    private String refNo;

    @CreationTimestamp
    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "read_DATE")
    private LocalDateTime readDate;

    // Helper methods
    public boolean isRead() {
        return readDate != null;
    }

    public void markAsRead() {
        this.readDate = LocalDateTime.now();
    }

    public enum NotificationType {
        APPROVAL, SCHEDULE, TASK, ANNOUNCEMENT, SYSTEM, CHAT, ATTENDANCE
    }

    public enum NotificationPriority {
        LOW, NORMAL, HIGH, URGENT
    }
}