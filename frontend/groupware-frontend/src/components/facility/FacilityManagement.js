import React, { useState, useEffect } from 'react';
import { Building2, Users, MapPin, Plus, Edit2, Trash2, Calendar, CheckCircle, XCircle, Eye } from 'lucide-react';
import { facilityApi } from '../../api/facilityApi';
import { isAuthenticated, handleAuthError } from '../../utils/authUtils';
import { useNavigate } from 'react-router-dom';
import { notificationService } from '../../services/NotificationService';
import './FacilityManagement.css';

const FacilityManagement = () => {
  const navigate = useNavigate();
  
  const [facilities, setFacilities] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [activeTab, setActiveTab] = useState('facilities');
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showReservationModal, setShowReservationModal] = useState(false);
  const [selectedFacility, setSelectedFacility] = useState(null);
  const [selectedReservation, setSelectedReservation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState({
    facilityType: '',
    facilityStatus: '',
    facilitySearch: '',
    reservationFacility: '',
    reservationStatus: '',
    reservationDate: '',
    reserverSearch: ''
  });
  const [newFacility, setNewFacility] = useState({
    facilityName: '',
    facilityLocation: '',
    capacity: '',
    description: '',
    facilityType: 'MEETING_ROOM',
    facilityStatus: 'ACTIVE'
  });

  // 시설 목록 조회
  const fetchFacilities = async () => {
    console.log('🔍 시설 목록 조회 시작...');
    try {
      setLoading(true);
      setError(null);
      const response = await facilityApi.getFacilities();
      
      if (Array.isArray(response)) {
        setFacilities(response);
      } else if (response && response.data) {
        setFacilities(response.data);
      } else {
        setFacilities([]);
      }
    } catch (error) {
      
      if (handleAuthError(error)) {
        return;
      }
      
      setError('시설 목록을 가져오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 예약 목록 조회 (관리자용)
  const fetchReservations = async () => {
    console.log('🔍 예약 목록 조회 시작...');
    try {
      setLoading(true);
      setError(null);
      const response = await facilityApi.getAllReservations();
      
      if (Array.isArray(response)) {
        setReservations(response);
      } else if (response && response.data) {
        setReservations(response.data);
      } else {
        console.warn('⚠️ 예상치 못한 예약 응답 구조:', response);
        setReservations([]);
      }
    } catch (error) {
      
      if (handleAuthError(error)) {
        return;
      }
      
      setError('예약 목록을 가져오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 시설 추가
  const handleAddFacility = async () => {
    
    if (!newFacility.facilityName || !newFacility.facilityLocation) {
      alert('시설명과 위치는 필수 입력 항목입니다.');
      return;
    }

    try {
      setLoading(true);
      const result = await facilityApi.addFacility(newFacility);
      
      alert('시설이 추가되었습니다.');
      setShowAddModal(false);
      setNewFacility({
        facilityName: '',
        facilityLocation: '',
        capacity: '',
        description: '',
        facilityType: 'MEETING_ROOM',
        facilityStatus: 'ACTIVE'
      });
      await fetchFacilities();
    } catch (error) {
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`시설 추가에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  };

  // 시설 수정
  const handleEditFacility = async () => {
    
    if (!selectedFacility.facilityName || !selectedFacility.facilityLocation) {
      alert('시설명과 위치는 필수 입력 항목입니다.');
      return;
    }

    try {
      setLoading(true);
      const result = await facilityApi.updateFacility(selectedFacility.facilityNo, selectedFacility);
      
      alert('시설이 수정되었습니다.');
      setShowEditModal(false);
      setSelectedFacility(null);
      await fetchFacilities();
    } catch (error) {
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`시설 수정에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  };

  // 시설 삭제
  const handleDeleteFacility = async (facilityNo) => {
    if (!window.confirm('정말로 이 시설을 삭제하시겠습니까?')) {
      return;
    }

    try {
      setLoading(true);
      await facilityApi.deleteFacility(facilityNo);
      
      alert('시설이 삭제되었습니다.');
      await fetchFacilities();
    } catch (error) {
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`시설 삭제에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  };

// 예약 승인/거절 - 알림 시스템 포함
const handleReservationAction = async (reservationNo, action) => {
    
    try {
      setLoading(true);
      
      // 예약 상태 업데이트
      await facilityApi.updateReservationStatus(reservationNo, action);
      
      // 알림 전송
      const reservation = reservations.find(r => r.reservationNo === reservationNo);
      if (reservation) {
        try {
          if (action === 'APPROVED') {
            await notificationService.sendReservationApproved(reservation);
          } else if (action === 'CANCELED') {
            await notificationService.sendReservationRejected(reservation);
          }
        } catch (notificationError) {
          // 알림 실패해도 예약 처리는 계속 진행
        }
      }
      
      alert(`예약이 ${action === 'APPROVED' ? '승인' : '거절'}되었습니다.`);
      await fetchReservations();
      setShowReservationModal(false);
    } catch (error) {
      
      if (handleAuthError(error)) {
        return;
      }
      
      alert(`예약 ${action === 'APPROVED' ? '승인' : '거절'}에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  };
 
  // 초기 데이터 로드
  useEffect(() => {
    
    if (!isAuthenticated()) {
      setError('관리자 권한이 필요합니다. 로그인 후 이용해주세요.');
      return;
    }
 
    if (activeTab === 'facilities') {
      fetchFacilities();
    } else if (activeTab === 'reservations') {
      fetchReservations();
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
  const filteredFacilities = facilities.filter(facility => {
    return (
      (!filters.facilityType || facility.facilityType === filters.facilityType) &&
      (!filters.facilityStatus || facility.facilityStatus === filters.facilityStatus) &&
      (!filters.facilitySearch || facility.facilityName.toLowerCase().includes(filters.facilitySearch.toLowerCase()))
    );
  });
 
  // 필터링된 예약 목록 
  const filteredReservations = reservations.filter(reservation => {
    return (
      (!filters.reservationFacility || reservation.facilityNo == filters.reservationFacility) &&
      (!filters.reservationStatus || reservation.resStatus === filters.reservationStatus) &&
      (!filters.reserverSearch || 
        reservation.reserverName?.toLowerCase().includes(filters.reserverSearch.toLowerCase()) ||
        reservation.deptName?.toLowerCase().includes(filters.reserverSearch.toLowerCase()))
    );
  });
 
  // 관리자 권한 확인
  if (!isAuthenticated()) {
    return (
      <div className="admin-auth-required">
        <Building2 className="auth-icon" />
        <h2>관리자 권한 필요</h2>
        <p>이 페이지에 접근하려면 관리자 계정으로 로그인해주세요.</p>
        <button 
          onClick={() => navigate('/login')}
          className="login-btn"
        >
          로그인 페이지로 이동
        </button>
      </div>
    );
  }
 
  return (
    <div className="facility-container">
      <div className="admin-header">
        <h1>
          <Building2 className="header-icon" />
          시설 관리
        </h1>
      </div>
      
      <div className="tab-container">
        {/* 탭 네비게이션 */}
        <div className="nav-tabs">
          <button
            className={`nav-link ${activeTab === 'facilities' ? 'active' : ''}`}
            onClick={() => setActiveTab('facilities')}
          >
            <Building2 className="tab-icon" />
            시설 현황
          </button>
          <button
            className={`nav-link ${activeTab === 'reservations' ? 'active' : ''}`}
            onClick={() => setActiveTab('reservations')}
          >
            <Calendar className="tab-icon" />
            예약 관리
          </button>
        </div>
 
        {error && (
          <div className="error-container">
            <span>{error}</span>
          </div>
        )}
 
        {/* 시설 관리 탭 */}
        {activeTab === 'facilities' && (
          <div className="tab-content">
            {/* 필터링 영역 */}
            <div className="filter-container">
              <div className="filter-row">
                <div className="filter-item">
                  <label>시설 유형</label>
                  <select 
                    value={filters.facilityType} 
                    onChange={(e) => setFilters({...filters, facilityType: e.target.value})}
                    className="form-control"
                  >
                    <option value="">전체</option>
                    <option value="MEETING_ROOM">회의실</option>
                    <option value="EQUIPMENT">장비</option>
                    <option value="VEHICLE">차량</option>
                  </select>
                </div>
                <div className="filter-item">
                  <label>시설 상태</label>
                  <select 
                    value={filters.facilityStatus} 
                    onChange={(e) => setFilters({...filters, facilityStatus: e.target.value})}
                    className="form-control"
                  >
                    <option value="">전체</option>
                    <option value="ACTIVE">사용 가능</option>
                    <option value="INACTIVE">사용 불가</option>
                    <option value="MAINTENANCE">점검 중</option>
                  </select>
                </div>
                <div className="filter-item">
                  <label>시설명 검색</label>
                  <input
                    type="text"
                    value={filters.facilitySearch}
                    onChange={(e) => setFilters({...filters, facilitySearch: e.target.value})}
                    placeholder="시설명 검색..."
                    className="form-control"
                  />
                </div>
              </div>
              <div className="filter-actions">
                <button
                  onClick={() => {
                    console.log('➕ 시설 추가 모달 열기');
                    setShowAddModal(true);
                  }}
                  className="btn btn-primary"
                  type="button"
                >
                  <Plus className="btn-icon" />
                  시설 추가
                </button>
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
                          <span className="status-available">사용 가능</span>
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
                            onClick={() => {
                              console.log('✏️ 시설 수정 모달 열기:', facility);
                              setSelectedFacility(facility);
                              setShowEditModal(true);
                            }}
                            className="btn btn-sm btn-secondary"
                            type="button"
                          >
                            <Edit2 className="btn-icon" /> 수정
                          </button>
                          <button
                            onClick={() => handleDeleteFacility(facility.facilityNo)}
                            className="btn btn-sm btn-danger"
                            disabled={loading}
                            type="button"
                          >
                            <Trash2 className="btn-icon" /> 삭제
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
 
        {/* 예약 관리 탭 */}
        {activeTab === 'reservations' && (
          <div className="tab-content">
            {/* 필터링 영역 */}
            <div className="filter-container">
              <div className="filter-row">
                <div className="filter-item">
                  <label>시설 선택</label>
                  <select 
                    value={filters.reservationFacility} 
                    onChange={(e) => setFilters({...filters, reservationFacility: e.target.value})}
                    className="form-control"
                  >
                    <option value="">전체 시설</option>
                    {facilities.map(facility => (
                      <option key={facility.facilityNo} value={facility.facilityNo}>
                        {facility.facilityName}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="filter-item">
                  <label>예약 상태</label>
                  <select 
                    value={filters.reservationStatus} 
                    onChange={(e) => setFilters({...filters, reservationStatus: e.target.value})}
                    className="form-control"
                  >
                    <option value="">전체</option>
                    <option value="PENDING">승인 대기</option>
                    <option value="APPROVED">승인 완료</option>
                    <option value="CANCELED">취소됨</option>
                  </select>
                </div>
                <div className="filter-item">
                  <label>예약자 검색</label>
                  <input
                    type="text"
                    value={filters.reserverSearch}
                    onChange={(e) => setFilters({...filters, reserverSearch: e.target.value})}
                    placeholder="예약자명 또는 부서명 검색..."
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
              <div className="table-responsive">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>예약번호</th>
                      <th>시설명</th>
                      <th>예약자</th>
                      <th>예약 시간</th>
                      <th>사용 목적</th>
                      <th>상태</th>
                      <th>관리</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredReservations.length === 0 ? (
                      <tr>
                        <td colSpan="7" className="text-center">
                          예약이 없습니다.
                        </td>
                      </tr>
                    ) : (
                      filteredReservations.map((reservation) => (
                        <tr key={reservation.reservationNo}>
                          <td>{reservation.reservationNo}</td>
                          <td>{reservation.facilityName}</td>
                          <td>{reservation.reserverName}</td>
                          <td>
                            <div>
                              {formatDateTime(reservation.startTime)}
                            </div>
                            <div className="time-end">
                              ~ {formatDateTime(reservation.endTime)}
                            </div>
                          </td>
                          <td>{reservation.purpose}</td>
                          <td>{getStatusBadge(reservation.resStatus)}</td>
                          <td>
                            <div className="action-buttons">
                              <button
                                onClick={() => {
                                  setSelectedReservation(reservation);
                                  setShowReservationModal(true);
                                }}
                                className="btn btn-sm btn-secondary"
                                type="button"
                                title="상세보기"
                              >
                                <Eye className="btn-icon" />
                              </button>
                              {reservation.resStatus === 'PENDING' && (
                                <>
                                  <button
                                    onClick={() => handleReservationAction(reservation.reservationNo, 'APPROVED')}
                                    className="btn btn-sm btn-success"
                                    disabled={loading}
                                    type="button"
                                    title="승인"
                                  >
                                    <CheckCircle className="btn-icon" />
                                  </button>
                                  <button
                                    onClick={() => handleReservationAction(reservation.reservationNo, 'CANCELED')}
                                    className="btn btn-sm btn-danger"
                                    disabled={loading}
                                    type="button"
                                    title="거절"
                                  >
                                    <XCircle className="btn-icon" />
                                  </button>
                                </>
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
 
      {/* 시설 추가 모달 */}
      {showAddModal && (
        <div className="modal-overlay" onClick={(e) => {
          if (e.target === e.currentTarget) {
            setShowAddModal(false);
          }
        }}>
          <div className="modal-content">
            <div className="modal-header">
              <h2>시설 추가</h2>
              <button 
                onClick={() => {
                  setShowAddModal(false);
                }} 
                className="modal-close"
                type="button"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>시설명 *</label>
                <input
                  type="text"
                  value={newFacility.facilityName}
                  onChange={(e) => setNewFacility({...newFacility, facilityName: e.target.value})}
                  className="form-control"
                  placeholder="시설명을 입력하세요"
                  required
                />
              </div>
              <div className="form-group">
                <label>위치 *</label>
                <input
                  type="text"
                  value={newFacility.facilityLocation}
                  onChange={(e) => setNewFacility({...newFacility, facilityLocation: e.target.value})}
                  className="form-control"
                  placeholder="위치를 입력하세요"
                  required
                />
              </div>
              <div className="form-group">
                <label>수용 인원</label>
                <input
                  type="number"
                  value={newFacility.capacity}
                  onChange={(e) => setNewFacility({...newFacility, capacity: e.target.value})}
                  className="form-control"
                  placeholder="수용 인원"
                  min="1"
                />
              </div>
              <div className="form-group">
                <label>시설 유형</label>
                <select
                  value={newFacility.facilityType}
                  onChange={(e) => setNewFacility({...newFacility, facilityType: e.target.value})}
                  className="form-control"
                >
                  <option value="MEETING_ROOM">회의실</option>
                  <option value="EQUIPMENT">장비</option>
                  <option value="VEHICLE">차량</option>
                </select>
              </div>
              <div className="form-group">
                <label>설명</label>
                <textarea
                  value={newFacility.description}
                  onChange={(e) => setNewFacility({...newFacility, description: e.target.value})}
                  className="form-control"
                  placeholder="시설에 대한 설명"
                  rows="3"
                />
              </div>
            </div>
            <div className="modal-footer">
              <button
                onClick={() => {
                  setShowAddModal(false);
                }}
                className="btn btn-secondary"
                disabled={loading}
                type="button"
              >
                취소
              </button>
              <button
                onClick={handleAddFacility}
                className="btn btn-primary"
                disabled={loading}
                type="button"
              >
                {loading ? '처리 중...' : '추가'}
              </button>
            </div>
          </div>
        </div>
      )}
 
      {/* 시설 수정 모달 */}
      {showEditModal && selectedFacility && (
        <div className="modal-overlay" onClick={(e) => {
          if (e.target === e.currentTarget) {
            setShowEditModal(false);
          }
        }}>
          <div className="modal-content">
            <div className="modal-header">
              <h2>시설 수정</h2>
              <button 
                onClick={() => setShowEditModal(false)} 
                className="modal-close"
                type="button"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>시설명 *</label>
                <input
                  type="text"
                  value={selectedFacility.facilityName || ''}
                  onChange={(e) => setSelectedFacility({...selectedFacility, facilityName: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group">
                <label>위치 *</label>
                <input
                  type="text"
                  value={selectedFacility.facilityLocation || ''}
                  onChange={(e) => setSelectedFacility({...selectedFacility, facilityLocation: e.target.value})}
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group">
                <label>수용 인원</label>
                <input
                  type="number"
                  value={selectedFacility.capacity || ''}
                  onChange={(e) => setSelectedFacility({...selectedFacility, capacity: e.target.value})}
                  className="form-control"
                  min="1"
                />
              </div>
              <div className="form-group">
                <label>시설 유형</label>
                <select
                  value={selectedFacility.facilityType || 'MEETING_ROOM'}
                  onChange={(e) => setSelectedFacility({...selectedFacility, facilityType: e.target.value})}
                  className="form-control"
                >
                  <option value="MEETING_ROOM">회의실</option>
                  <option value="EQUIPMENT">장비</option>
                  <option value="VEHICLE">차량</option>
                </select>
              </div>
              <div className="form-group">
                <label>시설 상태</label>
                <select
                  value={selectedFacility.facilityStatus || 'ACTIVE'}
                  onChange={(e) => setSelectedFacility({...selectedFacility, facilityStatus: e.target.value})}
                  className="form-control"
                >
                  <option value="ACTIVE">사용 가능</option>
                  <option value="INACTIVE">사용 불가</option>
                  <option value="MAINTENANCE">점검 중</option>
                </select>
              </div>
              <div className="form-group">
                <label>설명</label>
                <textarea
                  value={selectedFacility.description || ''}
                  onChange={(e) => setSelectedFacility({...selectedFacility, description: e.target.value})}
                  className="form-control"
                  rows="3"
                />
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
                onClick={handleEditFacility}
                className="btn btn-primary"
                disabled={loading}
                type="button"
              >
                {loading ? '처리 중...' : '수정'}
              </button>
            </div>
          </div>
        </div>
      )}
 
      {/* 예약 상세 모달 */}
      {showReservationModal && selectedReservation && (
        <div className="modal-overlay" onClick={(e) => {
          if (e.target === e.currentTarget) {
            setShowReservationModal(false);
          }
        }}>
          <div className="modal-content">
            <div className="modal-header">
              <h2>예약 상세 정보</h2>
              <button 
                onClick={() => setShowReservationModal(false)} 
                className="modal-close"
                type="button"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="info-row">
                <span className="label">예약 번호:</span>
                <span className="value">{selectedReservation.reservationNo}</span>
              </div>
              <div className="info-row">
                <span className="label">시설명:</span>
                <span className="value">{selectedReservation.facilityName}</span>
              </div>
              <div className="info-row">
                <span className="label">예약자:</span>
                <span className="value">{selectedReservation.reserverName}</span>
              </div>
              <div className="info-row">
                <span className="label">부서:</span>
                <span className="value">{selectedReservation.deptName}</span>
              </div>
              <div className="info-row">
                <span className="label">예약 시간:</span>
                <span className="value">
                  {formatDateTime(selectedReservation.startTime)} ~ {formatDateTime(selectedReservation.endTime)}
                </span>
              </div>
              <div className="info-row">
                <span className="label">사용 목적:</span>
                <span className="value">{selectedReservation.purpose}</span>
              </div>
              <div className="info-row">
                <span className="label">예약 상태:</span>
                <span className="value">{getStatusBadge(selectedReservation.resStatus)}</span>
              </div>
              <div className="info-row">
                <span className="label">예약 일시:</span>
                <span className="value">{formatDateTime(selectedReservation.createDate)}</span>
              </div>
            </div>
            <div className="modal-footer">
              {selectedReservation.resStatus === 'PENDING' && (
                <>
                  <button
                    onClick={() => handleReservationAction(selectedReservation.reservationNo, 'APPROVED')}
                    className="btn btn-success"
                    disabled={loading}
                    type="button"
                  >
                    <CheckCircle className="btn-icon" />
                    승인
                  </button>
                  <button
                    onClick={() => handleReservationAction(selectedReservation.reservationNo, 'CANCELED')}
                    className="btn btn-danger"
                    disabled={loading}
                    type="button"
                  >
                    <XCircle className="btn-icon" />
                    거절
                  </button>
                </>
              )}
              <button
                onClick={() => setShowReservationModal(false)}
                className="btn btn-secondary"
                type="button"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
 };
 
 export default FacilityManagement;