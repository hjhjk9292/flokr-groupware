import { getAuthHeaders } from '../utils/authUtils';

const API_BASE_URL = 'http://localhost:8080/api';

export const notificationService = {
  sendReservationApproved: async (reservationData) => {
    try {
      console.log('예약 승인 알림 전송:', reservationData);
      
      const response = await fetch(`${API_BASE_URL}/notifications`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({
          type: 'RESERVATION_APPROVED',
          userId: reservationData.userId,
          title: '시설 예약 승인',
          message: `${reservationData.facilityName} 예약이 승인되었습니다.`,
          data: reservationData
        })
      });

      if (response.ok) {
        console.log('승인 알림 전송 성공');
        return await response.json();
      }
    } catch (error) {
      console.error('승인 알림 전송 오류:', error);
    }
  },

  sendReservationRejected: async (reservationData) => {
    try {
      console.log('예약 거절 알림 전송:', reservationData);
      
      const response = await fetch(`${API_BASE_URL}/notifications`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({
          type: 'RESERVATION_REJECTED',
          userId: reservationData.userId,
          title: '시설 예약 거절',
          message: `${reservationData.facilityName} 예약이 거절되었습니다.`,
          data: reservationData
        })
      });

      if (response.ok) {
        console.log('거절 알림 전송 성공');
        return await response.json();
      }
    } catch (error) {
      console.error('거절 알림 전송 오류:', error);
    }
  }
};