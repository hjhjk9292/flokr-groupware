package com.kh.flokrGroupware.notification.model.service;

import java.util.List;
import java.util.Map;

import com.kh.flokrGroupware.employee.model.vo.Employee;

public interface NotificationService {
    
    // 읽지 않은 알림 목록 조회
    List<Map<String, Object>> getUnreadNotifications(int empNo);
    
    // 모든 알림 목록 조회
    List<Map<String, Object>> getAllNotifications(int empNo);
    
    // 페이징 처리된 알림 목록 조회
    List<Map<String, Object>> getAllNotificationsPaging(int empNo, int page, int limit);
    
    // 알림 읽음 처리
    int markAsRead(int notificationNo, int empNo);
    
    // 알림 저장
    int saveNotification(int empNo, String type, String title, String content, String refType, String refNo);
    
    // 전체 알림 저장
    int saveNotificationForAll(String type, String title, String content, String refType, String refNo);
    
    // 부서 알림 저장
    int saveNotificationForDepartment(int deptNo, String type, String title, String content, String refType, String refNo);
    
    // 오래된 알림 삭제
    int deleteOldNotifications(int days);
    
    // 관리자용 알림 목록 조회
    List<Map<String, Object>> getNotificationsForAdmin(int page, int limit, String type, String searchKeyword);
    
    // 알림 총 개수 조회
    int getTotalNotificationsCount(String type, String searchKeyword);
    
    // 특정 사용자의 총 알림 수 조회
    int getUserNotificationsCount(int empNo);
}