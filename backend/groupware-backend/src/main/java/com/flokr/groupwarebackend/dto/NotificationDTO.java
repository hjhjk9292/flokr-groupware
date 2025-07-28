package com.flokr.groupwarebackend.dto;

import com.flokr.groupwarebackend.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long notificationNo;
    private Long recipientEmpNo;
    private String type;
    private String title;
    private String content;
    private String refType;
    private String refNo;
    private String priority;
    private String createDate;
    private String readDate;
    private boolean isRead;

    public static NotificationDTO from(Notification notification) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationNo(notification.getNotificationNo());
        dto.setRecipientEmpNo(notification.getRecipientEmpNo());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setRefType(notification.getRefType());
        dto.setRefNo(notification.getRefNo());
        dto.setPriority(notification.getPriority());
        dto.setCreateDate(notification.getCreateDate() != null ?
                notification.getCreateDate().format(formatter) : null);
        dto.setReadDate(notification.getReadDate() != null ?
                notification.getReadDate().format(formatter) : null);
        dto.setRead(notification.isRead());

        return dto;
    }
}