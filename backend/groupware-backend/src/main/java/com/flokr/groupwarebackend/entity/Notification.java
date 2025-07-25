package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_NO")
    private Long notificationNo;

    @Column(name = "RECIPIENT_EMP_NO", nullable = false)
    private Long recipientEmpNo;

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

    @Column(name = "PRIORITY", length = 50)
    private String priority = "NORMAL";

    @CreationTimestamp
    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "read_DATE")
    private LocalDateTime readDate;

    // 생성자 (Builder 대신)
    public Notification(Long recipientEmpNo, String type, String title, String content,
                        String refType, String refNo, String priority) {
        this.recipientEmpNo = recipientEmpNo;
        this.type = type;
        this.title = title;
        this.content = content;
        this.refType = refType;
        this.refNo = refNo;
        this.priority = priority != null ? priority : "NORMAL";
    }

    // Helper methods
    public boolean isRead() {
        return readDate != null;
    }

    public void markAsRead() {
        this.readDate = LocalDateTime.now();
    }
}