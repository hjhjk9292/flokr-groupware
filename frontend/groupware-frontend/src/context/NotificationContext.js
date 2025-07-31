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
        console.log('알림 개수 업데이트:', count);
        setNotifications(count);
      }

      if (listResult.success) {
        const data = listResult.data?.data || listResult.data;
        if (Array.isArray(data)) {
          console.log('알림 목록 업데이트:', data.length);
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
        console.log('알림 읽음 처리:', notificationNo);
        setNotifications(prev => {
          const newCount = Math.max(0, prev - 1);
          console.log('뱃지 카운트 감소:', prev, '->', newCount);
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
        console.log('모든 알림 읽음 처리 완료');
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
    console.log('=== 새 알림 처리 시작 ===');
    console.log('수신 알림 원본:', notification);
    
    if (!notification) {
      console.log('유효하지 않은 알림');
      return;
    }
    
    const notificationId = notification.notificationNo || notification.id || `${Date.now()}-${Math.random()}`;
    const currentUser = currentUserRef.current;
    
    console.log('알림 정보:', {
      title: notification.title,
      type: notification.type,
      recipientEmpNo: notification.recipientEmpNo,
      currentUserEmpNo: currentUser?.empNo,
      notificationId
    });
    
    if (!currentUser) {
      console.log('현재 사용자 정보 없음');
      return;
    }
    
    // 개인 알림인 경우 수신자 확인
    if (notification.recipientEmpNo && notification.recipientEmpNo !== currentUser.empNo) {
      console.log('다른 사용자 알림 무시:', notification.recipientEmpNo, '!==', currentUser.empNo);
      return;
    }
    
    // 중복 알림 방지
    if (processedNotificationsRef.current.has(notificationId)) {
      console.log('중복 알림 무시:', notificationId);
      return;
    }
    
    console.log('새 알림 처리 진행:', notification.title);
    processedNotificationsRef.current.add(notificationId);
    
    const processedNotification = {
      ...notification,
      notificationNo: notification.notificationNo || notificationId,
      isRead: false,
      readDate: null
    };
    
    // 알림 목록 업데이트
    setNotificationList(prev => {
      if (!Array.isArray(prev)) return [processedNotification];
      
      const exists = prev.some(n => 
        (n.notificationNo && n.notificationNo === processedNotification.notificationNo) ||
        (n.title === processedNotification.title && n.createDate === processedNotification.createDate)
      );
      
      if (exists) {
        console.log('알림 목록에 이미 존재:', notificationId);
        return prev;
      }
      
      const newList = [processedNotification, ...prev];
      console.log('알림 목록 업데이트 완료:', newList.length);
      return newList;
    });
    
    // 뱃지 카운트 업데이트
    setNotifications(prev => {
      const newCount = prev + 1;
      console.log('뱃지 카운트 증가:', prev, '->', newCount);
      return newCount;
    });
    
    // 토스트 알림 표시
    setTimeout(() => {
      console.log('토스트 알림 표시 시도:', notification.title);
      if (typeof window !== 'undefined' && window.showToast && typeof window.showToast === 'function') {
        console.log('토스트 함수 확인됨');
        
        let toastMessage = notification.title;
        let toastType = 'info';
        
        // 시설 관련 알림 타입 처리
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
        
        console.log('토스트 메시지:', toastMessage, '타입:', toastType);
        window.showToast(toastMessage, toastType);
        console.log('토스트 함수 호출 완료');
      } else {
        console.log('showToast 함수가 없거나 함수가 아닙니다:', typeof window.showToast);
      }
    }, 300);

    // 브라우저 알림
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title || '새 알림', {
        body: notification.content || '',
        icon: '/favicon.ico',
        tag: `notification-${notificationId}`
      });
    }
    
    console.log('=== 새 알림 처리 완료 ===');
  }, []);

  const connectWebSocket = useCallback((userData) => {
    const userIdentifier = userData?.empId || userData?.empNo;
    
    if (!userIdentifier || connected || stompClientRef.current || isConnectingRef.current) {
      console.log('WebSocket 연결 건너뜀:', { 
        userIdentifier: !!userIdentifier, 
        connected, 
        hasClient: !!stompClientRef.current,
        isConnecting: isConnectingRef.current 
      });
      return;
    }

    try {
      console.log('=== WebSocket 연결 시작 ===');
      console.log('사용자 식별자:', userIdentifier, '사용자 데이터:', userData);
      
      isConnectingRef.current = true;
      currentUserRef.current = userData;
      
      const socket = new SockJS(`${process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080'}/ws-stomp`);
      const client = Stomp.over(socket);
      
      client.debug = () => {};

      client.connect(
        {},
        (frame) => {
          console.log('WebSocket 연결 성공');
          setConnected(true);
          stompClientRef.current = client;
          isConnectingRef.current = false;

          // 1. empId 기반 개인 알림 구독
          const personalTopic = `/user/${userIdentifier}/queue/notifications`;
          console.log('개인 알림 구독 (empId):', personalTopic);
          
          client.subscribe(personalTopic, (message) => {
            try {
              const notification = JSON.parse(message.body);
              console.log('empId 기반 개인 알림 수신:', {
                title: notification.title,
                type: notification.type,
                recipientEmpNo: notification.recipientEmpNo
              });
              handleNewNotification(notification);
            } catch (e) {
              console.error('empId 기반 개인 알림 파싱 오류:', e);
            }
          });

          // 2. empNo 기반 개인 알림 구독 (백업)
          if (userData.empNo && userData.empNo !== userIdentifier) {
            const personalTopicByEmpNo = `/user/${userData.empNo}/queue/notifications`;
            console.log('개인 알림 구독 (empNo):', personalTopicByEmpNo);
            
            client.subscribe(personalTopicByEmpNo, (message) => {
              try {
                const notification = JSON.parse(message.body);
                console.log('empNo 기반 개인 알림 수신:', {
                  title: notification.title,
                  type: notification.type,
                  recipientEmpNo: notification.recipientEmpNo
                });
                handleNewNotification(notification);
              } catch (e) {
                console.error('empNo 기반 개인 알림 파싱 오류:', e);
              }
            });
          }

          // 3. 전체 알림 구독
          const broadcastTopic = '/topic/notifications';
          console.log('전체 알림 구독:', broadcastTopic);
          
          client.subscribe(broadcastTopic, (message) => {
            try {
              const notification = JSON.parse(message.body);
              console.log('전체 알림 수신:', {
                title: notification.title,
                type: notification.type,
                recipientEmpNo: notification.recipientEmpNo
              });
              
              handleNewNotification(notification);
            } catch (e) {
              console.error('전체 알림 파싱 오류:', e);
            }
          });

          console.log('WebSocket 구독 설정 완료');
        },
        (error) => {
          console.error('WebSocket 연결 실패:', error);
          setConnected(false);
          stompClientRef.current = null;
          isConnectingRef.current = false;
          
          setTimeout(() => {
            if (!connected && !stompClientRef.current && !isConnectingRef.current) {
              console.log('WebSocket 재연결 시도');
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
      console.log('브라우저 알림 권한:', permission);
      return permission === 'granted';
    }
    return Notification.permission === 'granted';
  }, []);

  useEffect(() => {
    if (isInitializedRef.current) {
      console.log('이미 초기화됨, 중복 실행 방지');
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
        
        console.log('원본 사용자 데이터:', userData);
        console.log('정규화된 사용자 데이터:', normalizedUserData);
        
        if (!normalizedUserData.empNo) {
          const empIdToEmpNoMap = {
            'admin': 1,
            'lee002': 2,
            'park003': 6,
            'kim004': 9,
            'choi005': 10,
            'jung006': 13,
            'kang007': 32
          };
          
          normalizedUserData.empNo = empIdToEmpNoMap[normalizedUserData.empId];
          console.log(`임시 empNo 매핑: ${normalizedUserData.empId} -> ${normalizedUserData.empNo}`);
          
          if (!normalizedUserData.empNo) {
            console.error('empNo를 찾을 수 없습니다. 사용자 데이터:', userData);
            console.error('지원되는 empId:', Object.keys(empIdToEmpNoMap));
            return;
          }
        }
        
        if (currentUserRef.current && currentUserRef.current.empNo === normalizedUserData.empNo) {
          console.log('동일한 사용자로 이미 설정됨, 중복 실행 방지');
          return;
        }
        
        currentUserRef.current = normalizedUserData;
        isInitializedRef.current = true;
        console.log('사용자 데이터 설정 완료:', normalizedUserData);
        
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
      console.log('로그인되지 않은 상태 - 연결 해제');
      
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
        console.log('컴포넌트 언마운트 - WebSocket 연결 해제');
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