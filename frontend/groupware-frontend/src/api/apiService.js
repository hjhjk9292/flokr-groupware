import axios from 'axios';

// 환경에 따른 API URL 설정
const API_BASE_URL = process.env.NODE_ENV === 'development' 
  ? 'http://localhost:8080/api' 
  : `${process.env.REACT_APP_API_BASE_URL}/api`;

console.log('apiService - Environment:', process.env.NODE_ENV);
console.log('apiService - API_BASE_URL:', API_BASE_URL);

const authApi = axios.create({
  baseURL: API_BASE_URL,  // 절대 URL 사용
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
      console.log('401 Unauthorized - 토큰 확인 필요');
    }
    return Promise.reject(error);
  }
);

// 로그인은 절대 URL 사용
export const login = async (empId, password) => {
  const response = await axios.post(`${API_BASE_URL.replace('/api', '')}/api/auth/login`, { empId, password });
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

export const facilityApi = {
  admin: {
    getFacilities: async (keyword = '') => {
      const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
      const response = await authApi.get(`/facilities/admin${params}`);
      return response.data;
    },

    createFacility: async (facilityData) => {
      const response = await authApi.post('/facilities/admin', facilityData);
      return response.data;
    },

    updateFacility: async (facilityNo, facilityData) => {
      const response = await authApi.put(`/facilities/admin/${facilityNo}`, facilityData);
      return response.data;
    },

    deleteFacility: async (facilityNo) => {
      const response = await authApi.delete(`/facilities/admin/${facilityNo}`);
      return response.data;
    },

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

  user: {
    getAvailableFacilities: async (keyword = '') => {
      const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
      const response = await authApi.get(`/facilities/available${params}`);
      return response.data;
    },

    getMyReservations: async (empNo, params = {}) => {
      const queryParams = new URLSearchParams({ empNo: empNo.toString() });
      Object.entries(params).forEach(([key, value]) => {
        if (value) queryParams.append(key, value);
      });
      const response = await authApi.get(`/facilities/my-reservations?${queryParams}`);
      return response.data;
    },

    createReservation: async (reservationData) => {
      const response = await authApi.post('/facilities/reservations', reservationData);
      return response.data;
    },

    updateReservation: async (reservationNo, reservationData) => {
      const response = await authApi.put(`/facilities/reservations/${reservationNo}`, reservationData);
      return response.data;
    }
  },

  common: {
    getFacilityDetail: async (facilityNo) => {
      const response = await authApi.get(`/facilities/${facilityNo}`);
      return response.data;
    },

    getReservationDetail: async (reservationNo) => {
      const response = await authApi.get(`/facilities/reservations/${reservationNo}`);
      return response.data;
    },

    updateReservationStatus: async (reservationNo, status, currentUserEmpNo) => {
      const response = await authApi.put(`/facilities/reservations/${reservationNo}/status`, {
        status,
        currentUserEmpNo
      });
      return response.data;
    },

    checkAvailability: async (reservationData) => {
      const response = await authApi.post('/facilities/check-availability', reservationData);
      return response.data;
    }
  }
};

export const notificationApi = {
  getUnreadCount: async () => {
    try {
      const response = await authApi.get('/notifications/unread-count');
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      throw error;
    }
  },

  getUnreadNotifications: async () => {
    try {
      const response = await authApi.get('/notifications/unread');
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      throw error;
    }
  },

  getAllNotifications: async (page = 0, size = 20, filters = {}) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(filters.type && { type: filters.type }),
      ...(filters.keyword && { keyword: filters.keyword })
    });

    try {
      const response = await authApi.get(`/notifications?${params}`);
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      throw error;
    }
  },

  markAsRead: async (notificationNo) => {
    try {
      const response = await authApi.put(`/notifications/${notificationNo}/read`);
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      throw error;
    }
  },

  markAllAsRead: async () => {
    try {
      const response = await authApi.put('/notifications/read-all');
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      throw error;
    }
  },

  createNotification: async (notificationData) => {
    try {
      const response = await authApi.post('/admin/notifications', notificationData);
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      return { success: false, error: error.message };
    }
  },

  getAdminNotificationHistory: async (page = 0, size = 15, filters = {}) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(filters.type && { type: filters.type }),
      ...(filters.keyword && { keyword: filters.keyword })
    });

    try {
      const response = await authApi.get(`/admin/notifications/history?${params}`);
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      return { success: false, error: error.message };
    }
  },

  searchEmployees: async (keyword) => {
    try {
      const response = await authApi.get(`/admin/notifications/employees/search?keyword=${encodeURIComponent(keyword)}`);
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      return { success: false, error: error.message };
    }
  },

  getDepartmentEmployees: async (deptNo) => {
    try {
      const response = await authApi.get(`/admin/notifications/departments/${deptNo}/employees`);
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response?.status === 401) {
        return { success: false, error: 'UNAUTHORIZED' };
      }
      return { success: false, error: error.message };
    }
  }
};

export default authApi;