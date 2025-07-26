// src/api/notificationApi.js

import { getAuthHeaders } from '../utils/authUtils';

const API_BASE_URL = 'http://localhost:8080/api';

// 공통 응답 처리 함수
const handleResponse = async (response) => {
  const data = await response.json();
  
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('인증 토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.');
    }
    throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
  }
  
  return data;
};

// 공통 API 요청 함수
const apiRequest = async (url, options = {}) => {
  console.log('알림 API 호출:', url, options.method || 'GET');
  
  const headers = {
    ...getAuthHeaders(),
    ...options.headers
  };

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers
    });

    const result = await handleResponse(response);
    console.log('알림 API 응답:', result);
    return result;
  } catch (error) {
    console.error('알림 API 오류:', error);
    // 알림은 실패해도 앱이 중단되지 않도록 기본값 반환
    return { success: false, data: null, message: error.message };
  }
};

export const notificationApi = {
  // 읽지 않은 알림 개수 조회
  getUnreadCount: async () => {
    try {
      return await apiRequest('/notifications/unread-count');
    } catch (error) {
      // 실패 시 기본값 반환
      return { success: true, data: 0 };
    }
  },

  // 읽지 않은 알림 목록 조회
  getUnreadNotifications: async () => {
    try {
      return await apiRequest('/notifications/unread');
    } catch (error) {
      // 실패 시 빈 배열 반환
      return { success: true, data: [] };
    }
  },

  // 모든 알림 목록 조회
  getAllNotifications: async (page = 0, size = 20) => {
    try {
      return await apiRequest(`/notifications?page=${page}&size=${size}`);
    } catch (error) {
      return { success: true, data: [] };
    }
  },

  // 알림 읽음 처리
  markAsRead: async (notificationNo) => {
    try {
      return await apiRequest(`/notifications/${notificationNo}/read`, {
        method: 'PUT'
      });
    } catch (error) {
      return { success: false, message: error.message };
    }
  },

  // 모든 알림 읽음 처리
  markAllAsRead: async () => {
    try {
      return await apiRequest('/notifications/read-all', {
        method: 'PUT'
      });
    } catch (error) {
      return { success: false, message: error.message };
    }
  },

  // 알림 삭제
  deleteNotification: async (notificationNo) => {
    try {
      return await apiRequest(`/notifications/${notificationNo}`, {
        method: 'DELETE'
      });
    } catch (error) {
      return { success: false, message: error.message };
    }
  },

  // 알림 생성 (관리자용)
  createNotification: async (notificationData) => {
    try {
      return await apiRequest('/notifications', {
        method: 'POST',
        body: JSON.stringify(notificationData)
      });
    } catch (error) {
      return { success: false, message: error.message };
    }
  }
};