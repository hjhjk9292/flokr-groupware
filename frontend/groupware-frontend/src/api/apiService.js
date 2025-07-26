import axios from 'axios';

const authApi = axios.create({
  baseURL: '/api',
});

authApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

authApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('empName');
      alert('인증이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// 기존 API 함수들
export const login = async (empId, password) => {
  const response = await axios.post('/api/auth/login', { empId, password });
  return response.data;
};

export const getDepartments = async () => {
  const response = await authApi.get('/departments');
  return response.data;
};

export const getPositions = async () => {
  const response = await authApi.get('/positions');
  return response.data;
};

export const getEmployees = async () => {
  const response = await authApi.get('/employees');
  return response.data;
};

// 시설 관련 API 함수들 추가
export const facilityApi = {
  // 관리자용 API
  admin: {
    // 모든 시설 목록 조회
    getFacilities: async (keyword = '') => {
      const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
      const response = await authApi.get(`/facilities/admin${params}`);
      return response.data;
    },

    // 시설 추가
    createFacility: async (facilityData) => {
      const response = await authApi.post('/facilities/admin', facilityData);
      return response.data;
    },

    // 시설 수정
    updateFacility: async (facilityNo, facilityData) => {
      const response = await authApi.put(`/facilities/admin/${facilityNo}`, facilityData);
      return response.data;
    },

    // 시설 삭제
    deleteFacility: async (facilityNo) => {
      const response = await authApi.delete(`/facilities/admin/${facilityNo}`);
      return response.data;
    },

    // 예약 목록 조회
    getReservations: async (params = {}) => {
      const queryParams = new URLSearchParams();
      Object.entries(params).forEach(([key, value]) => {
        if (value) queryParams.append(key, value);
      });
      const queryString = queryParams.toString();
      const response = await authApi.get(`/facilities/admin/reservations${queryString ? `?${queryString}` : ''}`);
      return response.data;
    }
  },

  // 사용자용 API
  user: {
    // 사용 가능한 시설 목록 조회
    getAvailableFacilities: async (keyword = '') => {
      const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
      const response = await authApi.get(`/facilities/available${params}`);
      return response.data;
    },

    // 내 예약 목록 조회
    getMyReservations: async (empNo, params = {}) => {
      const queryParams = new URLSearchParams({ empNo: empNo.toString() });
      Object.entries(params).forEach(([key, value]) => {
        if (value) queryParams.append(key, value);
      });
      const response = await authApi.get(`/facilities/my-reservations?${queryParams}`);
      return response.data;
    },

    // 예약 생성
    createReservation: async (reservationData) => {
      const response = await authApi.post('/facilities/reservations', reservationData);
      return response.data;
    },

    // 예약 수정
    updateReservation: async (reservationNo, reservationData) => {
      const response = await authApi.put(`/facilities/reservations/${reservationNo}`, reservationData);
      return response.data;
    }
  },

  // 공통 API
  common: {
    // 시설 상세 정보 조회
    getFacilityDetail: async (facilityNo) => {
      const response = await authApi.get(`/facilities/${facilityNo}`);
      return response.data;
    },

    // 예약 상세 정보 조회
    getReservationDetail: async (reservationNo) => {
      const response = await authApi.get(`/facilities/reservations/${reservationNo}`);
      return response.data;
    },

    // 예약 상태 변경
    updateReservationStatus: async (reservationNo, status, currentUserEmpNo) => {
      const response = await authApi.put(`/facilities/reservations/${reservationNo}/status`, {
        status,
        currentUserEmpNo
      });
      return response.data;
    },

    // 예약 시간 중복 체크
    checkAvailability: async (reservationData) => {
      const response = await authApi.post('/facilities/check-availability', reservationData);
      return response.data;
    }
  }
};

// 알림 관련 API 함수들 추가
export const notificationApi = {
  // 읽지 않은 알림 개수 조회
  getUnreadCount: async () => {
    const response = await authApi.get('/notifications/unread/count');
    return response.data;
  },

  // 읽지 않은 알림 목록 조회
  getUnreadNotifications: async () => {
    const response = await authApi.get('/notifications/unread');
    return response.data;
  },

  // 알림 읽음 처리
  markAsRead: async (notificationNo) => {
    const response = await authApi.put(`/notifications/${notificationNo}/read`);
    return response.data;
  },

  // 모든 알림 읽음 처리
  markAllAsRead: async () => {
    const response = await authApi.put('/notifications/read-all');
    return response.data;
  }
};

export default authApi;