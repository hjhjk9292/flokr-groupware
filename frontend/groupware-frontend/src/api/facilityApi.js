// src/api/facilityApi.js
import { getAuthHeaders, handleAuthError, isAuthenticated, getAuthToken } from '../utils/authUtils';

const API_BASE_URL = 'http://localhost:8080/api';

// 토큰 유효성 사전 검증 함수
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
      // 토큰 디코딩에 실패해도 요청은 시도 (서버에서 최종 판단)
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
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    } catch (parseError) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
  }
  
  const data = await response.json();
  return data;
};

// 공통 API 요청 함수
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
  // 시설 목록 조회
  getFacilities: async () => {
    return await apiRequest('/facilities');
  },
  
  // 시설 상세 조회
  getFacilityDetail: async (facilityNo) => {
    return await apiRequest(`/facilities/${facilityNo}`);
  },
  
  // 시설 추가
  addFacility: async (facilityData) => {
    return await apiRequest('/facilities', {
      method: 'POST',
      body: JSON.stringify(facilityData)
    });
  },
  
  // 시설 수정
  updateFacility: async (facilityNo, facilityData) => {
    return await apiRequest(`/facilities/${facilityNo}`, {
      method: 'PUT',
      body: JSON.stringify(facilityData)
    });
  },
  
  // 시설 삭제
  deleteFacility: async (facilityNo) => {
    return await apiRequest(`/facilities/${facilityNo}`, {
      method: 'DELETE'
    });
  },
  
  // 모든 예약 목록 조회 (관리자용)
  getAllReservations: async () => {
    return await apiRequest('/admin/reservations');
  },
  
  // 승인 대기 예약 목록 조회 (관리자용)
  getPendingReservations: async () => {
    return await apiRequest('/admin/reservations/pending');
  },
  
  // 예약 상태 업데이트 (관리자용)
  updateReservationStatus: async (reservationNo, status) => {
    return await apiRequest(`/admin/reservations/${reservationNo}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: status })
    });
  },

  // 예약 상세 조회
  getReservationDetail: async (reservationNo) => {
    return await apiRequest(`/reservations/${reservationNo}`);
  },
  
  // 예약 생성 (사용자용)
  createReservation: async (reservationData) => {
    return await apiRequest(`/facilities/${reservationData.facilityNo}/reservations`, {
      method: 'POST',
      body: JSON.stringify(reservationData)
    });
  },
  
  // 내 예약 목록 조회 (사용자용)
  getMyReservations: async () => {
    return await apiRequest('/my-reservations');
  },


  // 예약 수정 (사용자용)
  updateMyReservation: async (reservationNo, reservationData) => {
    return await apiRequest(`/reservations/${reservationNo}`, {
      method: 'PUT',
      body: JSON.stringify(reservationData)
    });
  },
  
  // 예약 취소 (사용자용)
  cancelReservation: async (reservationNo) => {
    return await apiRequest(`/reservations/${reservationNo}/cancel`, {
      method: 'PUT'
    });
  }

};