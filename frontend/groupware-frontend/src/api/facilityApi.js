import { getAuthHeaders, handleAuthError, isAuthenticated, getAuthToken } from '../utils/authUtils';

const API_BASE_URL = 'http://localhost:8080/api';

const verifyTokenBeforeRequest = async () => {
  const token = getAuthToken();
  if (!token) {
    throw new Error('토큰이 없습니다.');
  }

  try {
    const cleanToken = token.replace('Bearer ', '');
    const parts = cleanToken.split('.');
    
    if (parts.length !== 3) {
      throw new Error('잘못된 JWT 토큰 형식입니다.');
    }

    try {
      const payload = JSON.parse(window.atob(parts[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      
      if (payload.exp && payload.exp < currentTime) {
        throw new Error('토큰이 만료되었습니다. 다시 로그인해주세요.');
      }
      return cleanToken;
    } catch (decodeError) {
      return cleanToken;
    }
  } catch (error) {
    throw new Error('토큰 형식이 올바르지 않습니다.');
  }
};

const handleResponse = async (response) => {
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('인증 토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.');
    }
    
    try {
      const errorData = await response.json();
      
      if (errorData.message && errorData.message.includes('이미 예약이 있습니다')) {
        throw new Error('해당 시간에 이미 예약이 있습니다. 다른 시간을 선택해주세요.');
      }
      
      if (errorData.message && errorData.message.includes('충돌')) {
        throw new Error('예약 시간이 중복됩니다. 다른 시간을 선택해주세요.');
      }
      
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    } catch (parseError) {
      if (parseError.message && parseError.message !== `HTTP ${response.status}: ${response.statusText}`) {
        throw parseError;
      }
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
  }
  
  const data = await response.json();
  return data;
};

const apiRequest = async (url, options = {}) => {
  try {
    await verifyTokenBeforeRequest();
  } catch (tokenError) {
    throw tokenError;
  }
  
  if (!isAuthenticated()) {
    throw new Error('로그인이 필요합니다.');
  }
  
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
    return result;
  } catch (error) {
    throw error;
  }
};

export const facilityApi = {
  getFacilities: async () => {
    return await apiRequest('/facilities');
  },
  
  getFacilityDetail: async (facilityNo) => {
    return await apiRequest(`/facilities/${facilityNo}`);
  },
  
  addFacility: async (facilityData) => {
    return await apiRequest('/facilities', {
      method: 'POST',
      body: JSON.stringify(facilityData)
    });
  },
  
  updateFacility: async (facilityNo, facilityData) => {
    return await apiRequest(`/facilities/${facilityNo}`, {
      method: 'PUT',
      body: JSON.stringify(facilityData)
    });
  },
  
  deleteFacility: async (facilityNo) => {
    return await apiRequest(`/facilities/${facilityNo}`, {
      method: 'DELETE'
    });
  },
  
  getAllReservations: async () => {
    return await apiRequest('/admin/reservations');
  },
  
  getPendingReservations: async () => {
    return await apiRequest('/admin/reservations/pending');
  },
  
  updateReservationStatus: async (reservationNo, status) => {
    console.log('=== facilityApi.updateReservationStatus 호출 ===');
    console.log('예약번호:', reservationNo, '상태:', status);
    
    const requestBody = JSON.stringify({ status: status });
    console.log('요청 본문:', requestBody);
    
    const url = `/admin/reservations/${reservationNo}/status`;
    console.log('요청 URL:', url);
    
    try {
      const result = await apiRequest(url, {
        method: 'PUT',
        body: requestBody
      });
      
      console.log('API 응답 성공:', result);
      return result;
      
    } catch (error) {
      console.error('API 요청 실패:', error);
      throw error;
    }
  },

  getReservationDetail: async (reservationNo) => {
    return await apiRequest(`/reservations/${reservationNo}`);
  },
  
  createReservation: async (reservationData) => {
    return await apiRequest(`/facilities/${reservationData.facilityNo}/reservations`, {
      method: 'POST',
      body: JSON.stringify(reservationData)
    });
  },
  
  getMyReservations: async () => {
    return await apiRequest('/my-reservations');
  },

  updateMyReservation: async (reservationNo, reservationData) => {
    return await apiRequest(`/reservations/${reservationNo}`, {
      method: 'PUT',
      body: JSON.stringify(reservationData)
    });
  },
  
  cancelReservation: async (reservationNo) => {
    return await apiRequest(`/reservations/${reservationNo}/cancel`, {
      method: 'PUT'
    });
  }
};