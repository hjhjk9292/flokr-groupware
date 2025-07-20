package com.kh.flokrGroupware.notification.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Notification {
    private int notificationNo;
    private int recipientEmpNo;
    private String type;
    private String title;
    private String notificationContent;
    private String refType;
    private String refNo;
    private Date createDate;
    private Date readDate;
}