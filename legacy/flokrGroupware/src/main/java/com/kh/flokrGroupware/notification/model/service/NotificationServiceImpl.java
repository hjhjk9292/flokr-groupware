package com.kh.flokrGroupware.notification.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.notification.model.dao.NotificationDao;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private NotificationDao notificationDao;

    @Override
    public List<Map<String, Object>> getUnreadNotifications(int empNo) {
        return notificationDao.getUnreadNotifications(empNo);
    }

    @Override
    public List<Map<String, Object>> getAllNotifications(int empNo) {
        return notificationDao.getAllNotifications(empNo);
    }

    @Override
    public List<Map<String, Object>> getAllNotificationsPaging(int empNo, int page, int limit) {
        int offset = (page - 1) * limit;
        Map<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        params.put("offset", offset);
        params.put("limit", limit);
        return notificationDao.getAllNotificationsPaging(params);
    }

    @Override
    public int markAsRead(int notificationNo, int empNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("notificationNo", notificationNo);
        params.put("empNo", empNo);
        return notificationDao.markAsRead(params);
    }

    @Override
    public int saveNotification(int empNo, String type, String title, String content, String refType, String refNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        params.put("type", type);
        params.put("title", title);
        params.put("content", content);
        params.put("refType", refType);
        params.put("refNo", refNo);
        return notificationDao.saveNotification(params);
    }

    @Override
    public int saveNotificationForAll(String type, String title, String content, String refType, String refNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("title", title);
        params.put("content", content);
        params.put("refType", refType);
        params.put("refNo", refNo);
        return notificationDao.saveNotificationForAll(params);
    }

    @Override
    public int saveNotificationForDepartment(int deptNo, String type, String title, String content, String refType, String refNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("deptNo", deptNo);
        params.put("type", type);
        params.put("title", title);
        params.put("content", content);
        params.put("refType", refType);
        params.put("refNo", refNo);
        return notificationDao.saveNotificationForDepartment(params);
    }

    @Override
    public int deleteOldNotifications(int days) {
        return notificationDao.deleteOldNotifications(days);
    }

    @Override
    public List<Map<String, Object>> getNotificationsForAdmin(int page, int limit, String type, String searchKeyword) {
        int offset = (page - 1) * limit;
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("type", type);
        params.put("searchKeyword", searchKeyword);
        return notificationDao.getNotificationsForAdmin(params);
    }

    @Override
    public int getTotalNotificationsCount(String type, String searchKeyword) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("searchKeyword", searchKeyword);
        return notificationDao.getTotalNotificationsCount(params);
    }
    
    @Override
    public int getUserNotificationsCount(int empNo) {
        return notificationDao.getUserNotificationsCount(empNo);
    }
}