package com.kh.flokrGroupware.notification.model.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.notification.model.vo.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationSenderService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSenderService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 특정 사용자에게 알림 전송
     */
    public void sendNotificationToUser(int empNo, String empId, String type, String title, 
                                     String content, String refType, String refNo) {
        try {
            // 1. DB에 알림 저장
            int result = notificationService.saveNotification(empNo, type, title, content, refType, refNo);
            
            if (result > 0) {
                logger.info("사용자 알림 저장 성공: empNo=" + empNo + ", type=" + type + ", title=" + title);
                
                // 2. WebSocket으로 실시간 알림 전송 (사용자가 온라인인 경우)
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", type);
                notification.put("title", title);
                notification.put("content", content);
                notification.put("refType", refType);
                notification.put("refNo", refNo);
                notification.put("timestamp", new java.util.Date());
                
                // 사용자 개인 큐로 알림 전송
                if (empId != null && !empId.isEmpty()) {
                    messagingTemplate.convertAndSendToUser(
                            empId,
                            "/queue/notification.new",
                            notification);
                    
                    logger.info("사용자에게 실시간 알림 전송: empId=" + empId);
                }
            } else {
                logger.warn("사용자 알림 저장 실패: empNo=" + empNo);
            }
        } catch (Exception e) {
            logger.error("사용자 알림 전송 중 오류 발생", e);
        }
    }
    
    /**
     * 모든 사용자에게 알림 전송 (공지사항 등)
     */
    public void sendNotificationToAll(String type, String title, String content, String refType, String refNo) {
        try {
            // 1. DB에 모든 사용자를 위한 알림 저장
            int result = notificationService.saveNotificationForAll(type, title, content, refType, refNo);
            
            if (result > 0) {
                logger.info("전체 알림 저장 성공: type=" + type + ", title=" + title);
                
                // 2. WebSocket으로 실시간 알림 전송 (현재 접속 중인 모든 사용자)
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", type);
                notification.put("title", title);
                notification.put("content", content);
                notification.put("refType", refType);
                notification.put("refNo", refNo);
                notification.put("timestamp", new java.util.Date());
                
                // 전체 토픽으로 알림 전송
                messagingTemplate.convertAndSend("/topic/notification.new", notification);
                
                logger.info("전체 사용자에게 실시간 알림 전송 완료");
            } else {
                logger.warn("전체 알림 저장 실패");
            }
        } catch (Exception e) {
            logger.error("전체 알림 전송 중 오류 발생", e);
        }
    }
    
    /**
     * 특정 부서에 속한 사용자들에게 알림 전송
     */
    public void sendNotificationToDepartment(int deptNo, String type, String title, 
                                           String content, String refType, String refNo) {
        try {
            // 1. DB에 부서 사용자를 위한 알림 저장
            int result = notificationService.saveNotificationForDepartment(deptNo, type, title, content, refType, refNo);
            
            if (result > 0) {
                logger.info("부서 알림 저장 성공: deptNo=" + deptNo + ", type=" + type + ", title=" + title);
                
                // 2. WebSocket으로 실시간 알림 전송 (현재 접속 중인 해당 부서 사용자)
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", type);
                notification.put("title", title);
                notification.put("content", content);
                notification.put("refType", refType);
                notification.put("refNo", refNo);
                notification.put("timestamp", new java.util.Date());
                
                // 부서별 토픽으로 알림 전송
                messagingTemplate.convertAndSend("/topic/department/" + deptNo + "/notification.new", notification);
                
                logger.info("부서 사용자에게 실시간 알림 전송 완료: deptNo=" + deptNo);
            } else {
                logger.warn("부서 알림 저장 실패: deptNo=" + deptNo);
            }
        } catch (Exception e) {
            logger.error("부서 알림 전송 중 오류 발생", e);
        }
    }
}