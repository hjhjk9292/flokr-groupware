package com.kh.flokrGroupware.notification.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.notification.model.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class NotificationWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 클라이언트로부터 특정 알림 읽음 처리 메시지를 수신
     */
    @MessageMapping("/notification.read")
    public void markNotificationAsRead(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Integer notificationNo = (Integer) payload.get("notificationNo");
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            
            if (sessionAttributes != null) {
                Employee loginUser = (Employee) sessionAttributes.get("loginUser");
                
                if (loginUser != null && notificationNo != null) {
                    logger.info("STOMP 알림 읽음 처리: notificationNo=" + notificationNo + ", empNo=" + loginUser.getEmpNo());
                    
                    // 알림 읽음 처리 실행
                    notificationService.markAsRead(notificationNo, loginUser.getEmpNo());
                    
                    // 읽음 처리 성공 응답 전송 (선택 사항)
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("notificationNo", notificationNo);
                    
                    // 사용자 개인 큐로 응답 전송
                    messagingTemplate.convertAndSendToUser(
                            loginUser.getEmpId(), 
                            "/queue/notification.read.response", 
                            response);
                }
            }
        } catch (Exception e) {
            logger.error("알림 읽음 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 클라이언트의 알림 구독 요청 처리
     */
    @MessageMapping("/notification.subscribe")
    public void handleSubscription(SimpMessageHeaderAccessor headerAccessor) {
        try {
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            
            if (sessionAttributes != null) {
                Employee loginUser = (Employee) sessionAttributes.get("loginUser");
                
                if (loginUser != null) {
                    logger.info("사용자 알림 구독: empNo=" + loginUser.getEmpNo() + ", empId=" + loginUser.getEmpId());
                    
                    // 읽지 않은 알림 목록 조회
                    List<Map<String, Object>> unreadNotifications = notificationService.getUnreadNotifications(loginUser.getEmpNo());
                    
                    // 클라이언트에게 읽지 않은 알림 목록 전송
                    messagingTemplate.convertAndSendToUser(
                            loginUser.getEmpId(),
                            "/queue/notifications.unread",
                            unreadNotifications);
                }
            }
        } catch (Exception e) {
            logger.error("알림 구독 처리 중 오류 발생", e);
        }
    }
}