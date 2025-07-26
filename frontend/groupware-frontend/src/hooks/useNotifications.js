// src/hooks/useNotification.js

import { useState, useEffect, useCallback } from 'react';
import { notificationApi } from '../api/apiService';
import websocketService from '../services/websocketService';

export const useNotifications = (userData) => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadNotifications, setUnreadNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // 읽지 않은 알림 개수 조회
  const fetchUnreadCount = useCallback(async () => {
    if (!userData?.empNo) return;

    try {
      const result = await notificationApi.getUnreadCount();
      if (result.success) {
        setUnreadCount(result.data || 0);
      }
    } catch (error) {
      console.error('읽지 않은 알림 개수 조회 오류:', error);
      setError(error.message);
    }
  }, [userData?.empNo]);

  // 읽지 않은 알림 목록 조회
  const fetchUnreadNotifications = useCallback(async () => {
    if (!userData?.empNo) return;

    try {
      setLoading(true);
      const result = await notificationApi.getUnreadNotifications();
      if (result.success) {
        setUnreadNotifications(result.data || []);
      }
    } catch (error) {
      console.error('읽지 않은 알림 목록 조회 오류:', error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [userData?.empNo]);

  // 알림 읽음 처리
  const markAsRead = useCallback(async (notificationNo) => {
    try {
      const result = await notificationApi.markAsRead(notificationNo);
      if (result.success) {
        // 로컬 상태 업데이트
        setUnreadNotifications(prev => 
          prev.filter(notif => notif.notificationNo !== notificationNo)
        );
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error('알림 읽음 처리 오류:', error);
      setError(error.message);
    }
  }, []);

  // 모든 알림 읽음 처리
  const markAllAsRead = useCallback(async () => {
    try {
      const result = await notificationApi.markAllAsRead();
      if (result.success) {
        setUnreadNotifications([]);
        setUnreadCount(0);
      }
    } catch (error) {
      console.error('모든 알림 읽음 처리 오류:', error);
      setError(error.message);
    }
  }, []);

  // WebSocket 알림 핸들러
  const handleNewNotification = useCallback((notification) => {
    console.log('새 알림 수신:', notification);
    
    // 읽지 않은 알림 목록에 추가
    setUnreadNotifications(prev => [notification, ...prev]);
    setUnreadCount(prev => prev + 1);
    
    // 토스트 알림 표시
    if (window.showToast) {
      window.showToast(`새 알림: ${notification.title}`, 'info');
    }
  }, []);

  // WebSocket 연결 및 핸들러 등록
  useEffect(() => {
    if (!userData?.empNo) return;

    // 초기 데이터 로드
    fetchUnreadCount();
    fetchUnreadNotifications();

    // WebSocket 연결
    websocketService.connect(userData).then(() => {
      console.log('WebSocket 연결 완료 - 알림 서비스 준비됨');
      
      // 브라우저 알림 권한 요청
      websocketService.constructor.requestNotificationPermission();
    }).catch(error => {
      console.error('WebSocket 연결 실패:', error);
      setError('실시간 알림 연결에 실패했습니다.');
    });

    // 새 알림 핸들러 등록
    websocketService.addMessageHandler('personalNotification', handleNewNotification);
    websocketService.addMessageHandler('systemNotification', handleNewNotification);

    // 정리 함수
    return () => {
      websocketService.removeMessageHandler('personalNotification', handleNewNotification);
      websocketService.removeMessageHandler('systemNotification', handleNewNotification);
    };
  }, [userData?.empNo, fetchUnreadCount, fetchUnreadNotifications, handleNewNotification]);

  // 컴포넌트 언마운트 시 WebSocket 연결 해제
  useEffect(() => {
    return () => {
      if (userData?.empNo) {
        websocketService.disconnect();
      }
    };
  }, [userData?.empNo]);

  // 주기적으로 알림 데이터 새로고침 (WebSocket 백업)
  useEffect(() => {
    if (!userData?.empNo) return;

    const interval = setInterval(() => {
      if (!websocketService.isConnected()) {
        fetchUnreadCount();
        fetchUnreadNotifications();
      }
    }, 30000); // 30초마다 체크

    return () => clearInterval(interval);
  }, [userData?.empNo, fetchUnreadCount, fetchUnreadNotifications]);

  return {
    unreadCount,
    unreadNotifications,
    loading,
    error,
    markAsRead,
    markAllAsRead,
    refreshNotifications: () => {
      fetchUnreadCount();
      fetchUnreadNotifications();
    },
    clearError: () => setError(null)
  };
};

export default useNotifications;