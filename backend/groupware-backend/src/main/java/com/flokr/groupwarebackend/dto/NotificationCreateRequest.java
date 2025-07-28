package com.flokr.groupwarebackend.dto;

import lombok.Data;

@Data
public class NotificationCreateRequest {
    private String targetType;
    private String targetId;
    private String type;
    private String title;
    private String content;
    private String refType;
    private String refNo;
    private String priority;
}