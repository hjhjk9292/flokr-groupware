// src/api/facilityApi.js
import { getAuthHeaders, handleAuthError, isAuthenticated, getAuthToken } from '../utils/authUtils';

const API_BASE_URL = 'http://localhost:8080/api';

// 토큰 유효성 사전 검증 함수 추가
const verifyTokenBeforeRequest = async () => {
  const token = getAuthToken();
  if (!token) {
    throw new Error('토큰이 없습니다.');
  }

  // 토큰 형식 검증
  try {
    // JWT 토큰인지 확인 (3개 부분으로 나뉘는지)
    const cleanToken = token.replace('Bearer ', '');
    const parts = cleanToken.split('.');
    
    if (parts.length !== 3) {
      throw new Error('잘못된 JWT 토큰 형식입니다.');
    }

    // 토큰 페이로드 디코딩해서 만료 시간 확인
    try {
      const payload = JSON.parse(window.atob(parts[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      
      console.log('토큰 검증:', {
        issued: payload.iat ? new Date(payload.iat * 1000).toLocaleString() : 'N/A',
        expires: payload.exp ? new Date(payload.exp * 1000).toLocaleString() : 'N/A',
        current: new Date(currentTime * 1000).toLocaleString(),
        isExpired: payload.exp ? payload.exp < currentTime : false,
        subject: payload.sub || 'N/A'
      });

      if (payload.exp && payload.exp < currentTime) {
        console.error('토큰이 만료되었습니다');
        throw new Error('토큰이 만료되었습니다. 다시 로그인해주세요.');
      }

      console.log('토큰 유효성 검증 통과');
      return cleanToken;
    } catch (decodeError) {
      // 토큰 디코딩에 실패해도 요청은 시도 (서버에서 최종 판단)
      console.warn('토큰 디코딩 실패, 서버 검증으로 진행:', decodeError.message);
      return cleanToken;
    }
  } catch (error) {
    console.error('토큰 형식 검증 실패:', error);
    throw new Error('토큰 형식이 올바르지 않습니다.');
  }
};
const handleResponse = async (response) => {
  console.log('응답 상태:', response.status, response.statusText);
  
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('인증 토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.');
    }
    
    // 응답 본문이 있는 경우 파싱 시도
    try {
      const errorData = await response.json();
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    } catch (parseError) {
      // JSON 파싱 실패시 기본 에러 메시지
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
  }
  
  const data = await response.json();
  return data;
};

// 공통 API 요청 함수
const apiRequest = async (url, options = {}) => {
  console.log('API 호출:', options.method || 'GET', url);
  
  // 토큰 사전 검증
  try {
    await verifyTokenBeforeRequest();
  } catch (tokenError) {
    console.error('토큰 사전 검증 실패:', tokenError);
    throw tokenError;
  }
  
  // 인증 확인
  if (!isAuthenticated()) {
    throw new Error('로그인이 필요합니다.');
  }
  
  const headers = {
    ...getAuthHeaders(),
    ...options.headers
  };

  console.log('요청 헤더:', {
    'Content-Type': headers['Content-Type'],
    'Authorization': headers['Authorization'] ? headers['Authorization'].substring(0, 30) + '...' : '없음',
    'x-access-token': headers['x-access-token'] ? headers['x-access-token'].substring(0, 30) + '...' : '없음'
  });

  // 요청 본문도 로깅
  if (options.body) {
    console.log('요청 본문:', JSON.parse(options.body));
  }

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers
    });

    console.log('응답 상태:', response.status, response.statusText);

    const result = await handleResponse(response);
    console.log('API 성공:', result);
    return result;
  } catch (error) {
    console.error('API 오류 상세:', {
      message: error.message,
      url: `${API_BASE_URL}${url}`,
      method: options.method || 'GET',
      status: error.status || 'unknown'
    });
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
  
  // 예약 생성 - 수정된 버전
  createReservation: async (reservationData) => {
    console.log('예약 데이터 전송:', reservationData);
    
    return await apiRequest('/reservations', {
      method: 'POST',
      body: JSON.stringify(reservationData)
    });
  },
  
  // 내 예약 목록 조회
  getMyReservations: async () => {
    return await apiRequest('/my-reservations');
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
      body: JSON.stringify({ status })
    });
  },
  
  // 예약 취소 (사용자용)
  cancelReservation: async (reservationNo) => {
    return await apiRequest(`/reservations/${reservationNo}/cancel`, {
      method: 'PUT'
    });
  },
  
  // 예약 상세 조회
  getReservationDetail: async (reservationNo) => {
    return await apiRequest(`/reservations/${reservationNo}`);
  }
};