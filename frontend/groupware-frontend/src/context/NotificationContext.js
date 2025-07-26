// src/context/NotificationContext.js

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { websocketService } from '../services/websocketService';
import { notificationApi } from '../api/notificationApi';
import { getUserData } from '../utils/authUtils';

const NotificationContext = createContext();

export const useNotification = () => useContext(NotificationContext);

export const NotificationProvider = ({ children, userData }) => {
  const [notifications, setNotifications] = useState(0); // 읽지 않은 알림 개수
  const [notificationList, setNotificationList] = useState([]); // 알림 목록
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUnreadCount = useCallback(async () => {
    if (!userData?.empNo) return;
    try {
      const result = await notificationApi.getUnreadCount();
      if (result.success) {
        setNotifications(result.data || 0);
      }
    } catch (error) {
      console.error('읽지 않은 알림 개수 조회 오류:', error);
      setError(error.message);
    }
  }, [userData?.empNo]);

  const fetchUnreadNotifications = useCallback(async () => {
    if (!userData?.empNo) return;
    setLoading(true);
    try {
      const result = await notificationApi.getUnreadNotifications();
      if (result.success) {
        setNotificationList(result.data || []);
      }
    } catch (error) {
      console.error('읽지 않은 알림 목록 조회 오류:', error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [userData?.empNo]);

  const markAsRead = useCallback(async (notificationNo) => {
    try {
      const result = await notificationApi.markAsRead(notificationNo);
      if (result.success) {
        setNotificationList(prev => 
          prev.filter(notif => notif.notificationNo !== notificationNo)
        );
        setNotifications(prev => Math.max(0, prev - 1));
      }
      return result;
    } catch (error) {
      console.error('알림 읽음 처리 오류:', error);
      setError(error.message);
      return { success: false, message: error.message };
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    try {
      const result = await notificationApi.markAllAsRead();
      if (result.success) {
        setNotificationList([]);
        setNotifications(0);
      }
      return result;
    } catch (error) {
      console.error('모든 알림 읽음 처리 오류:', error);
      setError(error.message);
      return { success: false, message: error.message };
    }
  }, []);
  
  const handleNewNotification = useCallback((notification) => {
    console.log('새 알림 수신:', notification);
    setNotifications(prev => prev + 1);
    setNotificationList(prev => [notification, ...prev]);
    websocketService.showBrowserNotification(notification.title, notification.content);
  }, []);

  useEffect(() => {
    if (!userData?.empNo) {
      websocketService.disconnect();
      return;
    }

    fetchUnreadCount();

    const connectService = async () => {
      try {
        await websocketService.connect(userData);
        console.log('WebSocket 연결 완료 - 알림 서비스 준비됨');
        websocketService.constructor.requestNotificationPermission();

        websocketService.addMessageHandler('personalNotification', handleNewNotification);
        websocketService.addMessageHandler('systemNotification', handleNewNotification);
      } catch (error) {
        console.error('WebSocket 연결 실패:', error);
        setError('실시간 알림 연결에 실패했습니다.');
      }
    };
    
    connectService();

    return () => {
      websocketService.removeMessageHandler('personalNotification', handleNewNotification);
      websocketService.removeMessageHandler('systemNotification', handleNewNotification);
      websocketService.disconnect();
    };
  }, [userData?.empNo, fetchUnreadCount, handleNewNotification]);

  useEffect(() => {
    if (!userData?.empNo) return;

    const interval = setInterval(() => {
      if (!websocketService.isConnected()) {
        fetchUnreadCount();
        fetchUnreadNotifications();
      }
    }, 30000);

    return () => clearInterval(interval);
  }, [userData?.empNo, fetchUnreadCount, fetchUnreadNotifications]);

  const value = {
    notifications,
    notificationList,
    loading,
    error,
    fetchUnreadNotifications,
    markAsRead,
    markAllAsRead,
    refreshNotifications: () => {
      fetchUnreadCount();
      fetchUnreadNotifications();
    },
    clearError: () => setError(null)
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};