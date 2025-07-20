package com.kh.flokrGroupware.notification.model.dao;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationDao {
    
    @Autowired
    private SqlSessionTemplate sqlSession;
    
    public List<Map<String, Object>> getUnreadNotifications(int empNo) {
        return sqlSession.selectList("notificationMapper.getUnreadNotifications", empNo);
    }
    
    public List<Map<String, Object>> getAllNotifications(int empNo) {
        return sqlSession.selectList("notificationMapper.getAllNotifications", empNo);
    }
    
    public List<Map<String, Object>> getAllNotificationsPaging(Map<String, Object> params) {
        return sqlSession.selectList("notificationMapper.getAllNotificationsPaging", params);
    }
    
    public Map<String, Object> getNotification(int notificationNo) {
        return sqlSession.selectOne("notificationMapper.getNotification", notificationNo);
    }
    
    public int markAsRead(Map<String, Object> params) {
        return sqlSession.update("notificationMapper.markAsRead", params);
    }
    
    public int saveNotification(Map<String, Object> params) {
        return sqlSession.insert("notificationMapper.saveNotification", params);
    }
    
    public int saveNotificationForAll(Map<String, Object> params) {
        return sqlSession.insert("notificationMapper.saveNotificationForAll", params);
    }
    
    public int saveNotificationForDepartment(Map<String, Object> params) {
        return sqlSession.insert("notificationMapper.saveNotificationForDepartment", params);
    }
    
    public int deleteOldNotifications(int days) {
        return sqlSession.delete("notificationMapper.deleteOldNotifications", days);
    }
    
    public List<Map<String, Object>> getNotificationsForAdmin(Map<String, Object> params) {
        return sqlSession.selectList("notificationMapper.getNotificationsForAdmin", params);
    }
    
    public int getTotalNotificationsCount(Map<String, Object> params) {
        return sqlSession.selectOne("notificationMapper.getTotalNotificationsCount", params);
    }
    
    public int getUserNotificationsCount(int empNo) {
        return sqlSession.selectOne("notificationMapper.getUserNotificationsCount", empNo);
    }

}