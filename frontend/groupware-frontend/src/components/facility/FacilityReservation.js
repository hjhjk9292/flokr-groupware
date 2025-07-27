// FacilityReservation.js
import React, { useState, useEffect } from 'react';
import { Building2, Users, MapPin, Calendar, Clock, X, Edit2, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { facilityApi } from '../../api/facilityApi';
import { isAuthenticated, handleAuthError } from '../../utils/authUtils';
import './FacilityReservation.css';

const FacilityReservation = ({ userData, initialTab = 'facilities' }) => {
  const [facilities, setFacilities] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [activeTab, setActiveTab] = useState(initialTab);
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedFacility, setSelectedFacility] = useState(null);
  const [selectedReservation, setSelectedReservation] = useState(null);
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
    try {
      setLoading(true);
      setError(null);
      
      const response = await facilityApi.getFacilities();
      
      if (Array.isArray(response)) {
        setFacilities(response);
      } else if (response && response.data && Array.isArray(response.data)) {
        setFacilities(response.data);
      } else {
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
      setLoading(false);
    }
  };

  // 사용자 예약 목록 조회
  const fetchMyReservations = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await facilityApi.getMyReservations();
      
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

    try {
      setLoading(true);
      const reservationData = {
        ...reservationForm,
        facilityNo: selectedFacility.facilityNo
      };
      
      await facilityApi.createReservation(reservationData);
      alert('예약이 신청되었습니다. 승인을 기다려주세요.');
      
      setShowReserveModal(false);
      resetReservationForm();
      
      // 예약 신청 후 예약 내역 새로고침
      await fetchMyReservations();
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

  // 예약 수정
  const handleEditReservation = async () => {
    if (!reservationForm.startTime || !reservationForm.endTime || !reservationForm.purpose) {
      alert('모든 필드를 입력해주세요.');
      return;
    }

    if (new Date(reservationForm.startTime) >= new Date(reservationForm.endTime)) {
      alert('종료 시간은 시작 시간보다 늦어야 합니다.');
      return;
    }

    try {
      setLoading(true);
      
      await facilityApi.updateMyReservation(selectedReservation.reservationNo, {
        facilityNo: selectedReservation.facilityNo,
        startTime: reservationForm.startTime,
        endTime: reservationForm.endTime,
        purpose: reservationForm.purpose
      });
      
      alert('예약이 수정되었습니다.');
      
      setShowEditModal(false);
      resetReservationForm();
      await fetchMyReservations();
      
    } catch (error) {
      console.error('예약 수정 오류:', error);
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`예약 수정에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
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

  // 예약 폼 초기화
  const resetReservationForm = () => {
    setReservationForm({
      facilityNo: '',
      startTime: '',
      endTime: '',
      purpose: ''
    });
    setSelectedFacility(null);
    setSelectedReservation(null);
  };

  // 초기 데이터 로드
  useEffect(() => {
    if (!isAuthenticated()) {
      setError('로그인이 필요합니다.');
      return;
    }

    const loadInitialData = async () => {
      await Promise.all([
        fetchFacilities(),
        fetchMyReservations()
      ]);
    };

    loadInitialData();
  }, []);

  // URL 파라미터로 초기 탭 설정
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const tabParam = urlParams.get('tab');
    
    if (tabParam === 'reservations') {
      setActiveTab('reservations');
    } else {
      setActiveTab('facilities');
    }
  }, []);

  // 탭 변경 시 URL 업데이트 및 데이터 새로고침
  const handleTabChange = (tabName) => {
    setActiveTab(tabName);
    
    const url = new URL(window.location);
    url.searchParams.set('tab', tabName);
    window.history.pushState({}, '', url);
    
    // 탭 변경 시 해당 탭의 데이터 새로고침
    if (tabName === 'reservations') {
      fetchMyReservations();
    } else if (tabName === 'facilities') {
      fetchFacilities();
    }
  };

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
        return (
          <span className="status-badge status-pending">
            <AlertCircle className="status-icon" />
            승인 대기
          </span>
        );
      case 'APPROVED':
        return (
          <span className="status-badge status-approved">
            <CheckCircle className="status-icon" />
            승인 완료
          </span>
        );
      case 'CANCELED':
        return (
          <span className="status-badge status-canceled">
            <XCircle className="status-icon" />
            승인 거절
          </span>
        );
      default:
        return (
          <span className="status-badge status-pending">
            <AlertCircle className="status-icon" />
            승인 대기
          </span>
        );
    }
  };

  // 시설 타입에 따른 아이콘
  const getFacilityIcon = (facilityName) => {
    return <Building2 className="facility-type-icon" />;
  };

  // 필터링된 시설 목록
  const filteredFacilities = facilities.filter(facility => 
    !searchTerm || facility.facilityName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 예약 모달 열기
  const openReserveModal = (facility) => {
    setSelectedFacility(facility);
    setReservationForm({
      facilityNo: facility.facilityNo,
      startTime: '',
      endTime: '',
      purpose: ''
    });
    setShowReserveModal(true);
  };

  // 예약 수정 모달 열기
  const openEditModal = (reservation) => {
    setSelectedReservation(reservation);
    
    const startTime = new Date(reservation.startTime);
    const endTime = new Date(reservation.endTime);
    
    setReservationForm({
      facilityNo: reservation.facilityNo,
      startTime: formatDateTimeLocal(startTime),
      endTime: formatDateTimeLocal(endTime),
      purpose: reservation.purpose
    });
    setShowEditModal(true);
  };

  // datetime-local input에 맞는 형식으로 변환
  const formatDateTimeLocal = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  // 예약 수정/취소 가능 여부 확인
  const canModifyReservation = (reservation) => {
    return reservation.status === 'PENDING';
  };

  const canCancelReservation = (reservation) => {
    return reservation.status === 'PENDING' || reservation.status === 'APPROVED';
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
            onClick={() => handleTabChange('facilities')}
          >
            <Building2 className="tab-icon" />
            시설 예약
          </button>
          <button
            className={`tab-button ${activeTab === 'reservations' ? 'active' : ''}`}
            onClick={() => handleTabChange('reservations')}
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
              <div className="table-responsive">
                <table className="reservation-table">
                  <thead>
                    <tr>
                      <th>시설명</th>
                      <th>예약 시간</th>
                      <th>사용 목적</th>
                      <th>예약 상태</th>
                      <th>관리</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reservations.length === 0 ? (
                      <tr>
                        <td colSpan="6" className="text-center">
                          예약 내역이 없습니다.
                        </td>
                      </tr>
                    ) : (
                      reservations.map((reservation) => (
                        <tr key={reservation.reservationNo}>
                          <td>
                            <div className="facility-cell">
                              <div className="facility-name">{reservation.facilityName}</div>
                            </div>
                          </td>
                          <td>
                            <div className="reservation-time">
                              <div className="start-time">
                                {formatDateTime(reservation.startTime)}
                              </div>
                              <div className="time-divider">~</div>
                              <div className="end-time">
                                {formatDateTime(reservation.endTime)}
                              </div>
                            </div>
                          </td>
                          <td>
                            <div className="purpose-cell">{reservation.purpose}</div>
                          </td>
                          <td>{getStatusBadge(reservation.status)}</td>
                          <td>
                            <div className="action-buttons">
                              {canModifyReservation(reservation) && (
                                <button
                                  onClick={() => openEditModal(reservation)}
                                  className="action-btn edit-btn"
                                  disabled={loading}
                                  type="button"
                                  title="승인 대기 중인 예약만 수정 가능합니다"
                                >
                                  <Edit2 className="btn-icon" />
                                  수정
                                </button>
                              )}
                              {canCancelReservation(reservation) && (
                                <button
                                  onClick={() => handleCancelReservation(reservation.reservationNo)}
                                  className="action-btn cancel-btn"
                                  disabled={loading}
                                  type="button"
                                >
                                  <X className="btn-icon" />
                                  취소
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* 예약 신청 모달 */}
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

      {/* 예약 수정 모달 */}
      {showEditModal && selectedReservation && (
        <div className="modal-overlay" onClick={(e) => {
          if (e.target === e.currentTarget) {
            setShowEditModal(false);
          }
        }}>
          <div className="modal-content">
            <div className="modal-header">
              <h2>{selectedReservation.facilityName} 예약 수정</h2>
              <button 
                onClick={() => setShowEditModal(false)} 
                className="modal-close"
                type="button"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="edit-notice">
                <AlertCircle className="notice-icon" />
                <p>승인 대기 중인 예약만 수정할 수 있습니다.</p>
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
                onClick={() => setShowEditModal(false)}
                className="btn btn-secondary"
                disabled={loading}
                type="button"
              >
                취소
              </button>
              <button
                onClick={handleEditReservation}
                className="btn btn-primary"
                disabled={loading}
                type="button"
              >
                {loading ? '처리 중...' : '수정 완료'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacilityReservation;