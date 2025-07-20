package com.kh.flokrGroupware.notification.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.notification.model.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 알림 시스템 보조 REST API
 * WebSocket 시스템의 보조 및 백업용으로 사용되는 REST 엔드포인트 제공
 */
@RestController
public class NotificationRestController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationRestController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 읽지 않은 알림 목록 조회 API
     * 클라이언트는 WebSocket 연결 실패 시 이 API를 통해 알림 목록을 조회할 수 있음
     */
    @GetMapping("/unreadNotifications")
    public List<Map<String, Object>> getUnreadNotifications(HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            logger.warn("로그인하지 않은 사용자의 알림 조회 요청이 거부됨");
            return null;
        }
        
        logger.info("REST API: 읽지 않은 알림 목록 요청 - empNo: {}", loginUser.getEmpNo());
        List<Map<String, Object>> notifications = notificationService.getUnreadNotifications(loginUser.getEmpNo());
        
        // 로그: 조회된 알림 수
        logger.info("REST API: 읽지 않은 알림 {}개 조회됨", notifications.size());
        
        return notifications;
    }
}