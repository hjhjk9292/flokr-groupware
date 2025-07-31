import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { notificationApi } from '../api/apiService';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const NotificationContext = createContext();

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState(0);
  const [notificationList, setNotificationList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [connected, setConnected] = useState(false);
  
  const stompClientRef = useRef(null);
  const processedNotificationsRef = useRef(new Set());
  const isConnectingRef = useRef(false);
  const currentUserRef = useRef(null);
  const isInitializedRef = useRef(false);

  const fetchUnreadNotifications = useCallback(async () => {
    const token = localStorage.getItem('accessToken') || localStorage.getItem('authToken');
    if (!token) return;

    try {
      setLoading(true);
      
      const [countResult, listResult] = await Promise.all([
        notificationApi.getUnreadCount(),
        notificationApi.getUnreadNotifications()
      ]);

      if (countResult.success) {
        const count = countResult.data?.data || countResult.data || 0;
        setNotifications(count);
      }

      if (listResult.success) {
        const data = listResult.data?.data || listResult.data;
        if (Array.isArray(data)) {
          setNotificationList(data);
        } else {
          setNotificationList([]);
        }
      }

    } catch (error) {
      if (error.response?.status !== 401) {
        console.error('알림 조회 오류:', error);
      }
      setNotificationList([]);
      setNotifications(0);
    } finally {
      setLoading(false);
    }
  }, []);

  const markAsRead = useCallback(async (notificationNo) => {
    try {
      const result = await notificationApi.markAsRead(notificationNo);
      
      if (result.success) {
        setNotifications(prev => {
          const newCount = Math.max(0, prev - 1);
          return newCount;
        });
        setNotificationList(prev => {
          if (!Array.isArray(prev)) return [];
          return prev.filter(notif => notif.notificationNo !== notificationNo);
        });
      }
      
      return result;
    } catch (error) {
      console.error('알림 읽음 처리 오류:', error);
      return { success: false };
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    try {
      const result = await notificationApi.markAllAsRead();
      
      if (result.success) {
        setNotifications(0);
        setNotificationList([]);
        processedNotificationsRef.current.clear();
      }
      
      return result;
    } catch (error) {
      console.error('모든 알림 읽음 처리 오류:', error);
      return { success: false };
    }
  }, []);

  const handleNewNotification = useCallback((notification) => {
    if (!notification) return;
    
    const notificationId = notification.notificationNo || notification.id || `${Date.now()}-${Math.random()}`;
    const currentUser = currentUserRef.current;
    
    if (!currentUser) return;
    
    if (notification.recipientEmpNo && notification.recipientEmpNo !== currentUser.empNo) {
      return;
    }
    
    if (processedNotificationsRef.current.has(notificationId)) {
      return;
    }
    
    processedNotificationsRef.current.add(notificationId);
    
    const processedNotification = {
      ...notification,
      notificationNo: notification.notificationNo || notificationId,
      isRead: false,
      readDate: null
    };
    
    setNotificationList(prev => {
      if (!Array.isArray(prev)) return [processedNotification];
      
      const exists = prev.some(n => 
        (n.notificationNo && n.notificationNo === processedNotification.notificationNo) ||
        (n.title === processedNotification.title && n.createDate === processedNotification.createDate)
      );
      
      if (exists) return prev;
      
      return [processedNotification, ...prev];
    });
    
    setNotifications(prev => prev + 1);
    
    setTimeout(() => {
      if (typeof window !== 'undefined' && window.showToast && typeof window.showToast === 'function') {
        let toastMessage = notification.title;
        let toastType = 'info';
        
        if (notification.type === 'FACILITY_APPROVED') {
          toastType = 'success';
          toastMessage = `✅ ${notification.title}`;
        } else if (notification.type === 'FACILITY_REJECTED') {
          toastType = 'error';  
          toastMessage = `❌ ${notification.title}`;
        } else if (notification.type === 'FACILITY') {
          toastType = 'info';
          toastMessage = `🏢 ${notification.title}`;
        }
        
        window.showToast(toastMessage, toastType);
      }
    }, 300);

    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title || '새 알림', {
        body: notification.content || '',
        icon: '/favicon.ico',
        tag: `notification-${notificationId}`
      });
    }
  }, []);

  const connectWebSocket = useCallback((userData) => {
    const userIdentifier = userData?.empId || userData?.empNo;
    
    if (!userIdentifier || connected || stompClientRef.current || isConnectingRef.current) {
      return;
    }

    try {
      isConnectingRef.current = true;
      currentUserRef.current = userData;
      
      const socket = new SockJS(process.env.NODE_ENV === 'development' ? 'http://localhost:8080/ws-stomp' : `${process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080'}/ws-stomp`);
      const client = Stomp.over(() => socket);
      
      client.debug = () => {};

      client.connect(
        {},
        (frame) => {
          setConnected(true);
          stompClientRef.current = client;
          isConnectingRef.current = false;

          const personalTopic = `/user/${userIdentifier}/queue/notifications`;
          client.subscribe(personalTopic, (message) => {
            try {
              const notification = JSON.parse(message.body);
              handleNewNotification(notification);
            } catch (e) {
              console.error('개인 알림 파싱 오류:', e);
            }
          });

          if (userData.empNo && userData.empNo !== userIdentifier) {
            const personalTopicByEmpNo = `/user/${userData.empNo}/queue/notifications`;
            client.subscribe(personalTopicByEmpNo, (message) => {
              try {
                const notification = JSON.parse(message.body);
                handleNewNotification(notification);
              } catch (e) {
                console.error('empNo 기반 알림 파싱 오류:', e);
              }
            });
          }

          const broadcastTopic = '/topic/notifications';
          client.subscribe(broadcastTopic, (message) => {
            try {
              const notification = JSON.parse(message.body);
              handleNewNotification(notification);
            } catch (e) {
              console.error('전체 알림 파싱 오류:', e);
            }
          });
        },
        (error) => {
          console.error('WebSocket 연결 실패:', error);
          setConnected(false);
          stompClientRef.current = null;
          isConnectingRef.current = false;
          
          setTimeout(() => {
            if (!connected && !stompClientRef.current && !isConnectingRef.current) {
              connectWebSocket(userData);
            }
          }, 5000);
        }
      );
    } catch (error) {
      console.error('WebSocket 연결 중 오류:', error);
      isConnectingRef.current = false;
    }
  }, [connected, handleNewNotification]);

  const requestNotificationPermission = useCallback(async () => {
    if ('Notification' in window && Notification.permission === 'default') {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    }
    return Notification.permission === 'granted';
  }, []);

  useEffect(() => {
    if (isInitializedRef.current) {
      return;
    }
    
    const userDataStr = localStorage.getItem('userData');
    const token = localStorage.getItem('accessToken') || localStorage.getItem('authToken');
    
    if (userDataStr && token) {
      try {
        const userData = JSON.parse(userDataStr);
        
        const normalizedUserData = {
          empNo: userData.empNo || userData.emp_no || userData.employeeNo,
          empId: userData.empId || userData.emp_id || userData.employeeId || userData.username,
          empName: userData.empName || userData.emp_name || userData.name || userData.employeeName
        };
        
        if (!normalizedUserData.empNo) {
          const empIdToEmpNoMap = {
            'admin': 1,
            'lee002': 2,
            'park003': 6,
            'kim004': 9,
            'choi005': 10,
            'jung006': 13,
            'kang007': 32,
            '125001': 2 // 유재석 사원 추가
          };
          
          normalizedUserData.empNo = empIdToEmpNoMap[normalizedUserData.empId];
          
          if (!normalizedUserData.empNo) {
            console.error('empNo를 찾을 수 없습니다. 사용자 데이터:', userData);
            return;
          }
        }
        
        if (currentUserRef.current && currentUserRef.current.empNo === normalizedUserData.empNo) {
          return;
        }
        
        currentUserRef.current = normalizedUserData;
        isInitializedRef.current = true;
        
        fetchUnreadNotifications();
        
        if (!connected && !stompClientRef.current && !isConnectingRef.current) {
          setTimeout(() => {
            connectWebSocket(normalizedUserData);
            requestNotificationPermission();
          }, 1000);
        }
      } catch (error) {
        console.error('사용자 데이터 파싱 오류:', error);
      }
    } else {
      if (stompClientRef.current) {
        stompClientRef.current.disconnect();
        setConnected(false);
        stompClientRef.current = null;
        setNotifications(0);
        setNotificationList([]);
        processedNotificationsRef.current.clear();
        currentUserRef.current = null;
        isConnectingRef.current = false;
        isInitializedRef.current = false;
      }
    }

    return () => {
      if (stompClientRef.current && connected) {
        stompClientRef.current.disconnect();
        setConnected(false);
        stompClientRef.current = null;
        isConnectingRef.current = false;
        isInitializedRef.current = false;
      }
    };
  }, [connected, connectWebSocket, fetchUnreadNotifications, requestNotificationPermission]);

  const contextValue = {
    notifications,
    notificationList: Array.isArray(notificationList) ? notificationList : [],
    loading,
    connected,
    fetchUnreadNotifications,
    markAsRead,
    markAllAsRead,
    connectWebSocket,
    requestNotificationPermission
  };

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
    </NotificationContext.Provider>
  );
};