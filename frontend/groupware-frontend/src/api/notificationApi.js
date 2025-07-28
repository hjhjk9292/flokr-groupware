// src/api/notificationApi.js

const API_BASE_URL = 'http://localhost:8080/api';

// API 요청 헬퍼 함수
const apiRequest = async (url, options = {}) => {
  const token = localStorage.getItem('token');
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers
    }
  };

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...defaultOptions,
      ...options
    });

    if (response.status === 401) {
      // 토큰 만료 처리
      localStorage.removeItem('token');
      window.location.href = '/login';
      return { success: false, message: '인증이 만료되었습니다.' };
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('API 요청 오류:', error);
    return { 
      success: false, 
      message: '네트워크 오류가 발생했습니다.' 
    };
  }
};

export const notificationApi = {
  // 읽지 않은 알림 목록 조회
  getUnreadNotifications: async () => {
    return await apiRequest('/notifications/unread');
  },

  // 읽지 않은 알림 개수 조회
  getUnreadCount: async () => {
    return await apiRequest('/notifications/unread-count');
  },

  // 모든 알림 목록 조회 (페이징)
  getAllNotifications: async (page = 0, size = 20, filters = {}) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(filters.type && { type: filters.type }),
      ...(filters.keyword && { keyword: filters.keyword })
    });

    return await apiRequest(`/notifications?${params}`);
  },

  // 알림 읽음 처리
  markAsRead: async (notificationNo) => {
    return await apiRequest(`/notifications/${notificationNo}/read`, {
      method: 'PUT'
    });
  },

  // 모든 알림 읽음 처리
  markAllAsRead: async () => {
    return await apiRequest('/notifications/read-all', {
      method: 'PUT'
    });
  },

  // 관리자 알림 생성 및 발송
  createNotification: async (notificationData) => {
    return await apiRequest('/admin/notifications', {
      method: 'POST',
      body: JSON.stringify(notificationData)
    });
  },

  // 관리자 알림 발송 내역 조회
  getAdminNotificationHistory: async (page = 0, size = 15, filters = {}) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(filters.type && { type: filters.type }),
      ...(filters.keyword && { keyword: filters.keyword })
    });

    return await apiRequest(`/admin/notifications/history?${params}`);
  },

  // 부서별 직원 목록 조회
  getDepartmentEmployees: async (deptNo) => {
    return await apiRequest(`/admin/departments/${deptNo}/employees`);
  },

  // 직원 검색
  searchEmployees: async (keyword) => {
    const params = new URLSearchParams({ keyword });
    return await apiRequest(`/admin/employees/search?${params}`);
  },

  // 시설 예약 알림 발송
  sendFacilityNotification: async (empNo, type, facilityName) => {
    const notificationData = {
      targetType: 'EMPLOYEE',
      targetId: empNo,
      type: type, // 'FACILITY_APPROVED' 또는 'FACILITY_REJECTED'
      title: type === 'FACILITY_APPROVED' ? '시설 예약 승인' : '시설 예약 거절',
      content: `${facilityName} 시설 예약이 ${type === 'FACILITY_APPROVED' ? '승인' : '거절'}되었습니다.`,
      refType: 'FACILITY_RESERVATION',
      priority: 'HIGH'
    };

    return await apiRequest('/admin/notifications', {
      method: 'POST',
      body: JSON.stringify(notificationData)
    });
  },

  // 공지사항 알림 발송
  sendNoticeNotification: async (noticeTitle, noticeContent) => {
    const notificationData = {
      targetType: 'ALL',
      type: 'NOTICE',
      title: `새 공지사항: ${noticeTitle}`,
      content: noticeContent.length > 50 ? noticeContent.substring(0, 50) + '...' : noticeContent,
      refType: 'NOTICE',
      priority: 'NORMAL'
    };

    return await apiRequest('/admin/notifications', {
      method: 'POST',
      body: JSON.stringify(notificationData)
    });
  },

  // 알림 타입 텍스트 반환
  getNotificationTypeText: (type) => {
    const typeMap = {
      'NOTICE': '공지사항',
      'SYSTEM': '시스템',
      'APPROVAL': '결재',
      'SCHEDULE': '일정',
      'FACILITY_APPROVED': '시설 승인',
      'FACILITY_REJECTED': '시설 거절',
      'TASK': '업무',
      'CHAT': '채팅',
      'MEETING': '회의'
    };
    return typeMap[type] || '알림';
  },

  // 알림 아이콘 반환
  getNotificationIcon: (type) => {
    const iconMap = {
      'NOTICE': 'fa-bullhorn',
      'SYSTEM': 'fa-cog',
      'APPROVAL': 'fa-check-circle',
      'SCHEDULE': 'fa-calendar',
      'FACILITY_APPROVED': 'fa-check',
      'FACILITY_REJECTED': 'fa-times',
      'TASK': 'fa-tasks',
      'CHAT': 'fa-comments',
      'MEETING': 'fa-users'
    };
    return iconMap[type] || 'fa-bell';
  },

  // 알림 우선순위 텍스트 반환
  getPriorityText: (priority) => {
    const priorityMap = {
      'LOW': '낮음',
      'NORMAL': '일반',
      'HIGH': '높음',
      'URGENT': '긴급'
    };
    return priorityMap[priority] || '일반';
  },

  // 알림 우선순위 색상 반환
  getPriorityColor: (priority) => {
    const colorMap = {
      'LOW': '#95a5a6',
      'NORMAL': '#3498db',
      'HIGH': '#f39c12',
      'URGENT': '#e74c3c'
    };
    return colorMap[priority] || '#3498db';
  }
};