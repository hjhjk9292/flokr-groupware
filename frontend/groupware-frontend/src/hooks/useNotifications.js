// src/hooks/useNotification.js

import { useState, useEffect, useCallback } from 'react';
import { notificationApi } from '../api/apiService';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export const useNotifications = (userData) => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadNotifications, setUnreadNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);

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

  // WebSocket 연결
  const connectWebSocket = useCallback(() => {
    if (!userData?.empId || connected) return;

    try {
      console.log('WebSocket 연결 시도...');
      
      // SockJS 소켓 생성 (인증 없이)
      const socket = new SockJS('http://localhost:8080/ws-stomp');
      const client = Stomp.over(socket);
      
      // 디버그 로그 최소화
      client.debug = () => {};

      client.connect(
        {}, // 빈 헤더 (인증 없이 연결)
        (frame) => {
          console.log('WebSocket 연결 성공:', frame);
          setConnected(true);
          setStompClient(client);

          // 개인 알림 구독
          if (userData.empId) {
            client.subscribe(`/user/${userData.empId}/queue/notifications`, (message) => {
              const notification = JSON.parse(message.body);
              console.log('새 알림 수신:', notification);
              handleNewNotification(notification);
            });

            // 전체 알림 구독
            client.subscribe('/topic/notifications', (message) => {
              const notification = JSON.parse(message.body);
              console.log('전체 알림 수신:', notification);
              handleNewNotification(notification);
            });
          }
        },
        (error) => {
          console.error('WebSocket 연결 실패:', error);
          setConnected(false);
          // 30초 후 재연결 시도
          setTimeout(connectWebSocket, 30000);
        }
      );
    } catch (error) {
      console.error('WebSocket 연결 중 오류:', error);
      setError('실시간 알림 연결에 실패했습니다.');
    }
  }, [userData, connected]);

  // 새 알림 처리
  const handleNewNotification = useCallback((notification) => {
    console.log('새 알림 처리:', notification);
    
    // 읽지 않은 알림 목록에 추가
    setUnreadNotifications(prev => [notification, ...prev]);
    setUnreadCount(prev => prev + 1);
    
    // 토스트 알림 표시
    if (window.showToast) {
      window.showToast(`새 알림: ${notification.title}`, 'info');
    }

    // 브라우저 알림 (권한이 있는 경우)
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title || '새 알림', {
        body: notification.content || '',
        icon: '/favicon.ico',
        tag: `notification-${notification.notificationNo}`
      });
    }
  }, []);

  // 초기화 및 WebSocket 연결
  useEffect(() => {
    if (!userData?.empNo) return;

    // 초기 데이터 로드
    fetchUnreadCount();
    fetchUnreadNotifications();

    // WebSocket 연결 (약간의 지연 후)
    const timer = setTimeout(connectWebSocket, 1000);

    return () => {
      clearTimeout(timer);
      if (stompClient && connected) {
        stompClient.disconnect();
        setConnected(false);
      }
    };
  }, [userData?.empNo]);

  // 정기적 폴링 (WebSocket 백업)
  useEffect(() => {
    if (!userData?.empNo) return;

    const interval = setInterval(() => {
      if (!connected) {
        fetchUnreadCount();
        fetchUnreadNotifications();
      }
    }, 60000); // 1분마다

    return () => clearInterval(interval);
  }, [userData?.empNo, connected, fetchUnreadCount, fetchUnreadNotifications]);

  return {
    unreadCount,
    unreadNotifications,
    loading,
    error,
    connected,
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