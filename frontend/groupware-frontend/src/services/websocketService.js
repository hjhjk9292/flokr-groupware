// src/services/websocketService.js

class WebSocketService {
    constructor() {
      this.socket = null;
      this.reconnectInterval = null;
      this.isConnected = false;
      this.reconnectAttempts = 0;
      this.maxReconnectAttempts = 5;
      this.reconnectDelay = 1000;
      this.listeners = new Map();
    }
  
    connect(userData) {
      if (!userData || !userData.empNo) {
        console.error('WebSocket 연결을 위한 사용자 정보가 없습니다.');
        return Promise.reject(new Error('사용자 정보 없음'));
      }
  
      return new Promise((resolve, reject) => {
        try {
          const wsUrl = `${process.env.REACT_APP_WS_URL || 'ws://localhost:8080'}/ws-stomp`;
          this.socket = new WebSocket(wsUrl);
  
          this.socket.onopen = () => {
            console.log('WebSocket 연결됨');
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this.emit('connected');
            resolve();
          };
  
          this.socket.onmessage = (event) => {
            try {
              const data = JSON.parse(event.data);
              this.handleMessage(data);
            } catch (error) {
              console.error('WebSocket 메시지 파싱 오류:', error);
            }
          };
  
          this.socket.onclose = () => {
            console.log('WebSocket 연결 종료됨');
            this.isConnected = false;
            this.emit('disconnected');
            this.attemptReconnect(userData);
            reject(new Error('WebSocket 연결 종료'));
          };
  
          this.socket.onerror = (error) => {
            console.error('WebSocket 오류:', error);
            this.emit('error', error);
            reject(error);
          };
  
        } catch (error) {
          console.error('WebSocket 연결 실패:', error);
          reject(error);
        }
      });
    }
  
    handleMessage(data) {
      console.log('WebSocket 메시지 수신:', data);
      switch (data.type) {
        case 'NOTIFICATION':
          this.emit('notification', data);
          break;
        case 'FACILITY_RESERVATION':
          this.emit('facilityReservation', data);
          break;
        case 'SYSTEM':
          this.handleSystemMessage(data);
          break;
        default:
          console.log('알 수 없는 메시지 타입:', data.type);
      }
    }
  
    handleSystemMessage(data) {
      console.log('시스템 메시지:', data);
      this.emit('system', data);
    }
    
    async showBrowserNotification(title, body) {
      if ('Notification' in window) {
        let permission = Notification.permission;
        if (permission === 'default') {
          permission = await Notification.requestPermission();
        }
        if (permission === 'granted') {
          const notification = new Notification(title, {
            body: body,
            icon: '/favicon.ico',
            badge: '/favicon.ico',
            tag: 'flokr-notification',
            requireInteraction: true
          });
          notification.onclick = () => {
            window.focus();
            notification.close();
          };
          setTimeout(() => {
            notification.close();
          }, 5000);
        }
      }
    }
  
    attemptReconnect(userData) {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        console.log('최대 재연결 시도 횟수 초과');
        return;
      }
      this.reconnectAttempts++;
      const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
      console.log(`${delay}ms 후 재연결 시도 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      this.reconnectInterval = setTimeout(() => {
        this.connect(userData);
      }, delay);
    }
    
    addMessageHandler(event, callback) {
      if (!this.listeners.has(event)) {
        this.listeners.set(event, []);
      }
      this.listeners.get(event).push(callback);
    }
  
    removeMessageHandler(event, callback) {
      if (this.listeners.has(event)) {
        const callbacks = this.listeners.get(event);
        const index = callbacks.indexOf(callback);
        if (index > -1) {
          callbacks.splice(index, 1);
        }
      }
    }
  
    emit(event, data) {
      if (this.listeners.has(event)) {
        this.listeners.get(event).forEach(callback => {
          try {
            callback(data);
          } catch (error) {
            console.error(`이벤트 콜백 오류 (${event}):`, error);
          }
        });
      }
    }
  
    send(data) {
      if (this.isConnected && this.socket) {
        this.socket.send(JSON.stringify(data));
      } else {
        console.warn('WebSocket이 연결되지 않았습니다.');
      }
    }
  
    disconnect() {
      if (this.reconnectInterval) {
        clearTimeout(this.reconnectInterval);
        this.reconnectInterval = null;
      }
      
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      }
      
      this.isConnected = false;
      this.listeners.clear();
    }
  
    isConnectedToServer() {
      return this.isConnected;
    }
  }
  
  export const websocketService = new WebSocketService();