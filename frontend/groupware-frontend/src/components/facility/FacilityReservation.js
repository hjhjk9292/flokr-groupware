import React, { useState, useEffect } from 'react';
import { Building2, Users, MapPin, Calendar, Clock, X } from 'lucide-react';
import { facilityApi } from '../../api/facilityApi';
import { isAuthenticated, handleAuthError } from '../../utils/authUtils';

const FacilityReservation = ({ userData, initialTab = 'facilities' }) => {
  const [facilities, setFacilities] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [activeTab, setActiveTab] = useState(initialTab);
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [selectedFacility, setSelectedFacility] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [reservationForm, setReservationForm] = useState({
    facilityNo: '',
    startTime: '',
    endTime: '',
    purpose: ''
  });

  // 시설 목록 조회
  const fetchFacilities = async () => {
    console.log('사용자 시설 데이터 요청 시작...');
    
    try {
      setLoading(true);
      setError(null);
      
      console.log('API 호출 전 - 로딩:', true, '에러:', null);
      
      const response = await facilityApi.getFacilities();
      console.log('사용자 시설 API 응답:', response);
      
      // 응답 데이터 타입별 처리
      if (Array.isArray(response)) {
        console.log('배열 데이터 설정:', response.length, '개 시설');
        setFacilities(response);
      } else if (response && response.data && Array.isArray(response.data)) {
        console.log('객체.data 배열 설정:', response.data.length, '개 시설');
        setFacilities(response.data);
      } else {
        console.warn('예상치 못한 응답 구조:', typeof response, response);
        setFacilities([]);
      }
    } catch (error) {
      console.error('사용자 시설 조회 중 오류:', error);
      setError(`시설 목록을 가져올 수 없습니다: ${error.message}`);
      setFacilities([]);
      
      if (handleAuthError(error)) {
        return;
      }
    } finally {
      console.log('사용자 시설 API 호출 완료 - 로딩 false');
      setLoading(false);
    }
  };

  // 사용자 예약 목록 조회
  const fetchMyReservations = async () => {
    console.log('내 예약 목록 조회 시작...');
    
    try {
      setLoading(true);
      setError(null);
      const response = await facilityApi.getMyReservations();
      
      console.log('내 예약 조회 응답:', response);
      
      if (Array.isArray(response)) {
        setReservations(response);
      } else if (response && response.data && Array.isArray(response.data)) {
        setReservations(response.data);
      } else {
        console.warn('예상치 못한 예약 응답 형식:', response);
        setReservations([]);
      }
    } catch (error) {
      console.error('내 예약 조회 오류:', error);
      
      if (handleAuthError(error)) {
        return;
      }
      
      setError('예약 목록을 가져오는데 실패했습니다.');
      setReservations([]);
    } finally {
      setLoading(false);
    }
  };

  // 예약 신청
  const handleReservation = async () => {
    if (!reservationForm.startTime || !reservationForm.endTime || !reservationForm.purpose) {
      alert('모든 필드를 입력해주세요.');
      return;
    }

    if (new Date(reservationForm.startTime) >= new Date(reservationForm.endTime)) {
      alert('종료 시간은 시작 시간보다 늦어야 합니다.');
      return;
    }

    console.log('예약 신청 데이터:', reservationForm);

    try {
      setLoading(true);
      const reservationData = {
        ...reservationForm,
        facilityNo: selectedFacility.facilityNo
      };
      
      await facilityApi.createReservation(reservationData);
      alert('예약이 신청되었습니다. 승인을 기다려주세요.');
      
      setShowReserveModal(false);
      setReservationForm({
        facilityNo: '',
        startTime: '',
        endTime: '',
        purpose: ''
      });
      setSelectedFacility(null);
      
      // 예약 목록 새로고침
      if (activeTab === 'reservations') {
        await fetchMyReservations();
      }
    } catch (error) {
      console.error('예약 신청 오류:', error);
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`예약 신청에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  };

  // 예약 취소
  const handleCancelReservation = async (reservationNo) => {
    if (!window.confirm('정말로 이 예약을 취소하시겠습니까?')) {
      return;
    }

    try {
      setLoading(true);
      await facilityApi.cancelReservation(reservationNo);
      alert('예약이 취소되었습니다.');
      await fetchMyReservations();
    } catch (error) {
      console.error('예약 취소 오류:', error);
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`예약 취소에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  };

  // 초기 데이터 로드
  useEffect(() => {
    console.log('사용자 컴포넌트 마운트, 활성 탭:', activeTab);
    
    if (!isAuthenticated()) {
      setError('로그인이 필요합니다.');
      return;
    }

    if (activeTab === 'facilities') {
      fetchFacilities();
    } else if (activeTab === 'reservations') {
      fetchMyReservations();
    }
  }, [activeTab]);

  // 날짜/시간 포맷팅
  const formatDateTime = (dateTime) => {
    if (!dateTime) return '';
    try {
      return new Date(dateTime).toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return dateTime;
    }
  };

  // 예약 상태 표시
  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return <span className="status-badge status-pending">승인 대기</span>;
      case 'APPROVED':
        return <span className="status-badge status-approved">승인 완료</span>;
      case 'CANCELED':
        return <span className="status-badge status-canceled">취소됨</span>;
      default:
        return <span className="status-badge status-pending">승인 대기</span>;
    }
  };

  // 시설 타입에 따른 아이콘
  const getFacilityIcon = (facilityName) => {
    const name = facilityName?.toLowerCase() || '';
    if (name.includes('회의') || name.includes('미팅')) {
      return <Building2 className="facility-type-icon" />;
    } else if (name.includes('강의') || name.includes('교육')) {
      return <Users className="facility-type-icon" />;
    } else {
      return <Building2 className="facility-type-icon" />;
    }
  };

  // 필터링된 시설 목록
  const filteredFacilities = facilities.filter(facility => 
    !searchTerm || facility.facilityName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 예약 모달 열기
  const openReserveModal = (facility) => {
    console.log('예약 모달 열기:', facility);
    setSelectedFacility(facility);
    setReservationForm({
      facilityNo: facility.facilityNo,
      startTime: '',
      endTime: '',
      purpose: ''
    });
    setShowReserveModal(true);
  };

  // 로그인 확인
  if (!isAuthenticated()) {
    return (
      <div className="user-facility-page">
        <div className="user-facility-container">
          <div className="auth-required">
            <Building2 className="auth-icon" />
            <h2>로그인이 필요합니다</h2>
            <p>시설 예약 서비스를 이용하려면 로그인해주세요.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="user-facility-page">
      <div className="user-facility-container">
        <div className="user-facility-header">
          <h1>
            <i className="fa-solid fa-calendar-check"></i>
            시설 예약
          </h1>
          <p>회사 시설을 예약하고 관리하세요</p>
        </div>

        {/* 탭 네비게이션 */}
        <div className="facility-tabs">
          <button
            className={`tab-button ${activeTab === 'facilities' ? 'active' : ''}`}
            onClick={() => setActiveTab('facilities')}
          >
            <Building2 className="tab-icon" />
            시설 예약
          </button>
          <button
            className={`tab-button ${activeTab === 'reservations' ? 'active' : ''}`}
            onClick={() => setActiveTab('reservations')}
          >
            <Calendar className="tab-icon" />
            예약 내역
          </button>
        </div>

        {error && (
          <div className="alert alert-error">
            <span>{error}</span>
          </div>
        )}

        {/* 시설 예약 탭 */}
        {activeTab === 'facilities' && (
          <div className="tab-content">
            {/* 검색 영역 */}
            <div className="filter-container">
              <div className="filter-row">
                <div className="filter-item">
                  <label>시설명 검색</label>
                  <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="시설명으로 검색..."
                    className="form-control"
                  />
                </div>
              </div>
            </div>

            {loading ? (
              <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>로딩 중...</p>
              </div>
            ) : (
              <div className="facility-grid">
                {filteredFacilities.length === 0 ? (
                  <div className="empty-state">
                    <Building2 className="empty-icon" />
                    <p>등록된 시설이 없습니다.</p>
                  </div>
                ) : (
                  filteredFacilities.map((facility) => (
                    <div key={facility.facilityNo} className="facility-card">
                      <div className="facility-icon-container">
                        {getFacilityIcon(facility.facilityName)}
                        <div className="facility-status">
                          <span className="status-available">예약 가능</span>
                        </div>
                      </div>
                      
                      <div className="facility-content">
                        <h3 className="facility-name">{facility.facilityName}</h3>
                        
                        <div className="facility-info">
                          {facility.capacity && (
                            <p><Users className="info-icon" /> 최대 {facility.capacity}명</p>
                          )}
                          
                          {facility.facilityLocation && (
                            <p><MapPin className="info-icon" /> {facility.facilityLocation}</p>
                          )}
                          
                          {facility.description && (
                            <p className="facility-description">{facility.description}</p>
                          )}
                        </div>
                        
                        <div className="facility-actions">
                          <button
                            onClick={() => openReserveModal(facility)}
                            className="btn btn-primary"
                            disabled={loading}
                            type="button"
                          >
                            <Calendar className="btn-icon" />
                            예약하기
                          </button>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        )}

        {/* 예약 내역 탭 */}
        {activeTab === 'reservations' && (
          <div className="tab-content">
            {loading ? (
              <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>로딩 중...</p>
              </div>
            ) : (
              <div className="reservation-grid">
                {reservations.length === 0 ? (
                  <div className="empty-state">
                    <Calendar className="empty-icon" />
                    <p>예약 내역이 없습니다.</p>
                  </div>
                ) : (
                  reservations.map((reservation) => (
                    <div key={reservation.reservationNo} className="reservation-card">
                      <div className="reservation-header">
                        <h4 className="facility-name">{reservation.facilityName}</h4>
                        {getStatusBadge(reservation.resStatus)}
                      </div>
                      
                      <div className="reservation-content">
                        <div className="reservation-info">
                          <div className="info-item">
                            <Clock className="info-icon" />
                            <div className="time-info">
                              <div>{formatDateTime(reservation.startTime)}</div>
                              <div className="time-end">~ {formatDateTime(reservation.endTime)}</div>
                            </div>
                          </div>
                          
                          <div className="info-item">
                            <span className="info-label">사용 목적:</span>
                            <span className="info-value">{reservation.purpose}</span>
                          </div>
                          
                          <div className="info-item">
                            <span className="info-label">예약 일시:</span>
                            <span className="info-value">{formatDateTime(reservation.createDate)}</span>
                          </div>
                        </div>
                        
                        {reservation.resStatus === 'PENDING' && (
                          <div className="reservation-actions">
                            <button
                              onClick={() => handleCancelReservation(reservation.reservationNo)}
                              className="btn btn-sm btn-danger"
                              disabled={loading}
                              type="button"
                            >
                              <X className="btn-icon" />
                              취소
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {/* 예약 모달 */}
      {showReserveModal && selectedFacility && (
        <div className="modal-overlay" onClick={(e) => {
          if (e.target === e.currentTarget) {
            setShowReserveModal(false);
          }
        }}>
          <div className="modal-content">
            <div className="modal-header">
              <h2>{selectedFacility.facilityName} 예약</h2>
              <button 
                onClick={() => setShowReserveModal(false)} 
                className="modal-close"
                type="button"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="facility-info-section">
                <div className="facility-details">
                  {selectedFacility.capacity && (
                    <div className="detail-item">
                      <Users className="detail-icon" />
                      <span>최대 {selectedFacility.capacity}명</span>
                    </div>
                  )}
                  {selectedFacility.facilityLocation && (
                    <div className="detail-item">
                      <MapPin className="detail-icon" />
                      <span>{selectedFacility.facilityLocation}</span>
                    </div>
                  )}
                </div>
                {selectedFacility.description && (
                  <p className="facility-description">{selectedFacility.description}</p>
                )}
              </div>
              
              <div className="form-section">
                <div className="form-group">
                  <label>시작 시간 *</label>
                  <input
                    type="datetime-local"
                    value={reservationForm.startTime}
                    onChange={(e) => setReservationForm({...reservationForm, startTime: e.target.value})}
                    className="form-control"
                    min={new Date().toISOString().slice(0, 16)}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>종료 시간 *</label>
                  <input
                    type="datetime-local"
                    value={reservationForm.endTime}
                    onChange={(e) => setReservationForm({...reservationForm, endTime: e.target.value})}
                    className="form-control"
                    min={reservationForm.startTime || new Date().toISOString().slice(0, 16)}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>사용 목적 *</label>
                  <textarea
                    value={reservationForm.purpose}
                    onChange={(e) => setReservationForm({...reservationForm, purpose: e.target.value})}
                    className="form-control"
                    placeholder="사용 목적을 입력하세요"
                    rows="3"
                    required
                  />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button
                onClick={() => setShowReserveModal(false)}
                className="btn btn-secondary"
                disabled={loading}
                type="button"
              >
                취소
              </button>
              <button
                onClick={handleReservation}
                className="btn btn-primary"
                disabled={loading}
                type="button"
              >
                {loading ? '처리 중...' : '예약 신청'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacilityReservation;