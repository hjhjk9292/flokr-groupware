// src/services/websocketService.js

import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 5000;
    this.subscribers = new Map();
    
    // 이벤트 핸들러
    this.onConnectHandler = null;
    this.onDisconnectHandler = null;
    this.onNewNotificationHandler = null;
    this.onErrorHandler = null;
  }

  // 연결
  async connect() {
    if (this.connected) {
      console.log('이미 WebSocket에 연결되어 있습니다.');
      return;
    }

    try {
      console.log('WebSocket 연결 시도...');
      
      // SockJS 소켓 생성
      const socket = new SockJS(process.env.NODE_ENV === 'development' ? 'http://localhost:8080/ws-stomp' : `${process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080'}/ws-stomp`);
      this.client = Stomp.over(socket);
      
      // 디버그 로그 비활성화 (프로덕션에서)
      this.client.debug = (str) => {
        console.log('STOMP:', str);
      };

      // 연결 설정
      const connectHeaders = {
        'heart-beat': '10000,10000'
      };

      return new Promise((resolve, reject) => {
        this.client.connect(
          connectHeaders,
          (frame) => {
            console.log('WebSocket 연결 성공:', frame);
            this.connected = true;
            this.reconnectAttempts = 0;
            
            // 구독 설정
            this.setupSubscriptions();
            
            if (this.onConnectHandler) {
              this.onConnectHandler();
            }
            
            resolve();
          },
          (error) => {
            console.error('WebSocket 연결 실패:', error);
            this.connected = false;
            
            if (this.onErrorHandler) {
              this.onErrorHandler(error);
            }
            
            // 재연결 시도
            this.scheduleReconnect();
            
            reject(error);
          }
        );
      });

    } catch (error) {
      console.error('WebSocket 연결 중 오류:', error);
      this.scheduleReconnect();
      throw error;
    }
  }

  // 구독 설정
  setupSubscriptions() {
    if (!this.client || !this.connected) return;

    const userData = this.getUserData();
    if (!userData) return;

    try {
      // 전체 알림 구독
      this.subscribe('/topic/notifications', (message) => {
        const notification = JSON.parse(message.body);
        this.handleNewNotification(notification);
      });

      // 개인 알림 구독
      this.subscribe(`/user/${userData.empId}/queue/notifications`, (message) => {
        const notification = JSON.parse(message.body);
        this.handleNewNotification(notification);
      });

      // 부서별 알림 구독
      if (userData.deptNo) {
        this.subscribe(`/topic/department/${userData.deptNo}/notifications`, (message) => {
          const notification = JSON.parse(message.body);
          this.handleNewNotification(notification);
        });
      }

      // 읽음 처리 응답 구독
      this.subscribe(`/user/${userData.empId}/queue/notification-read`, (message) => {
        const response = JSON.parse(message.body);
        console.log('알림 읽음 처리 응답:', response);
      });

      console.log('모든 구독 설정 완료');

    } catch (error) {
      console.error('구독 설정 중 오류:', error);
    }
  }

  // 구독
  subscribe(destination, callback) {
    if (!this.client || !this.connected) {
      console.warn('WebSocket이 연결되지 않아 구독할 수 없습니다:', destination);
      return null;
    }

    try {
      const subscription = this.client.subscribe(destination, callback);
      this.subscribers.set(destination, subscription);
      console.log('구독 완료:', destination);
      return subscription;
    } catch (error) {
      console.error('구독 실패:', destination, error);
      return null;
    }
  }

  // 메시지 발송
  send(destination, headers = {}, body = '') {
    if (!this.client || !this.connected) {
      console.warn('WebSocket이 연결되지 않아 메시지를 보낼 수 없습니다');
      return false;
    }

    try {
      this.client.send(destination, headers, body);
      return true;
    } catch (error) {
      console.error('메시지 발송 실패:', error);
      return false;
    }
  }

  // 알림 읽음 처리
  markAsRead(notificationNo) {
    const message = JSON.stringify({ notificationNo });
    return this.send('/app/notification/read', {}, message);
  }

  // 모든 알림 읽음 처리
  markAllAsRead() {
    return this.send('/app/notification/read-all', {}, '{}');
  }

  // 새 알림 처리
  handleNewNotification(notification) {
    console.log('새 알림 처리:', notification);
    
    if (this.onNewNotificationHandler) {
      this.onNewNotificationHandler(notification);
    }
  }

  // 연결 해제
  disconnect() {
    if (this.client && this.connected) {
      try {
        // 모든 구독 해제
        this.subscribers.forEach((subscription, destination) => {
          try {
            subscription.unsubscribe();
            console.log('구독 해제:', destination);
          } catch (error) {
            console.error('구독 해제 실패:', destination, error);
          }
        });
        this.subscribers.clear();

        // 연결 해제
        this.client.disconnect(() => {
          console.log('WebSocket 연결 해제 완료');
        });
        
        this.connected = false;
        
        if (this.onDisconnectHandler) {
          this.onDisconnectHandler();
        }
        
      } catch (error) {
        console.error('연결 해제 중 오류:', error);
      }
    }
  }

  // 재연결 스케줄링
  scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('최대 재연결 시도 횟수 초과');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectInterval * Math.pow(1.5, this.reconnectAttempts - 1);
    
    console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts} (${delay}ms 후)`);
    
    setTimeout(() => {
      if (!this.connected) {
        this.connect().catch(error => {
          console.error('재연결 실패:', error);
        });
      }
    }, delay);
  }

  // 사용자 데이터 가져오기
  getUserData() {
    try {
      // localStorage 또는 다른 저장소에서 사용자 정보 가져오기
      const userStr = localStorage.getItem('userData');
      if (userStr) {
        return JSON.parse(userStr);
      }

      // DOM에서 사용자 정보 가져오기 (fallback)
      const empId = document.querySelector('#loginUserId')?.value;
      const deptNo = document.querySelector('#userDeptNo')?.value;
      const empNo = document.querySelector('#loginUserEmpNo')?.value;

      if (empId) {
        return { empId, deptNo, empNo };
      }

      return null;
    } catch (error) {
      console.error('사용자 데이터 조회 오류:', error);
      return null;
    }
  }

  // 이벤트 핸들러 설정
  onConnect(handler) {
    this.onConnectHandler = handler;
  }

  onDisconnect(handler) {
    this.onDisconnectHandler = handler;
  }

  onNewNotification(handler) {
    this.onNewNotificationHandler = handler;
  }

  onError(handler) {
    this.onErrorHandler = handler;
  }

  // 연결 상태 확인
  isConnected() {
    return this.connected && this.client && this.client.connected;
  }

  // 강제 재연결
  forceReconnect() {
    console.log('강제 재연결 시도...');
    this.disconnect();
    setTimeout(() => {
      this.connect();
    }, 1000);
  }
}

// 싱글톤 인스턴스 생성
export const websocketService = new WebSocketService();

export default websocketService;