import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { notificationApi } from '../../api/apiService';
import { getDepartments, getEmployees } from '../../api/apiService';
import './AdminNotificationManagement.css';

const AdminNotificationManagement = ({ userData }) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState(() => {
    const tabFromUrl = searchParams.get('tab');
    return (tabFromUrl === 'history') ? 'history' : 'send';
  });
  const [notifications, setNotifications] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  
  const [formData, setFormData] = useState({
    targetType: 'ALL',
    deptNo: '',
    selectedEmpNo: '',
    notificationType: 'NOTICE',
    title: '',
    content: '',
    refType: '',
    refNo: '',
    priority: 'NORMAL'
  });
  
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState({
    type: '',
    keyword: ''
  });

  useEffect(() => {
    if (isInitialized) return;
    
    fetchDepartments();
    if (activeTab === 'history') {
      fetchNotifications();
    }
    
    setIsInitialized(true);
  }, [activeTab, currentPage, filters]);

  useEffect(() => {
    const tabFromUrl = searchParams.get('tab');
    if (tabFromUrl && (tabFromUrl === 'send' || tabFromUrl === 'history')) {
      setActiveTab(tabFromUrl);
    }
  }, [searchParams]);

  const fetchDepartments = async () => {
    try {
      const result = await getDepartments();
      if (result.success || result.data) {
        setDepartments(Array.isArray(result.data) ? result.data : (result.data || []));
      }
    } catch (error) {
      console.error('부서 목록 조회 오류:', error);
      setDepartments([]);
    }
  };

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      console.log('발송 내역 조회 시작:', { currentPage, filters });
      
      const result = await notificationApi.getAdminNotificationHistory(
        currentPage - 1, 
        10, 
        filters
      );
      
      console.log('발송 내역 API 응답:', result);
      
      if (result.success) {
        let responseData = result.data;
        
        // 중첩된 data 구조 처리 - 콘솔에서 확인된 구조에 따라 수정
        if (responseData && responseData.data && typeof responseData.data === 'object') {
          responseData = responseData.data;
          console.log('중첩 data 구조 처리됨:', responseData);
        }
        
        console.log('처리된 응답 데이터:', responseData);
        
        if (responseData && typeof responseData === 'object') {
          // Spring Page 객체 처리
          if (responseData.content && Array.isArray(responseData.content)) {
            console.log('Page 객체 감지:', responseData.content.length, '개');
            setNotifications(responseData.content);
            setTotalPages(responseData.totalPages || 0);
            setTotalElements(responseData.totalElements || 0);
          } 
          // 직접 배열인 경우
          else if (Array.isArray(responseData)) {
            console.log('직접 배열 감지:', responseData.length, '개');
            setNotifications(responseData);
            setTotalPages(Math.ceil(responseData.length / 10));
            setTotalElements(responseData.length);
          } 
          // success, message, data 구조에서 data가 Page 객체인 경우
          else if (responseData.success && responseData.data && responseData.data.content) {
            console.log('중첩된 success 구조의 Page 객체:', responseData.data.content.length, '개');
            setNotifications(responseData.data.content);
            setTotalPages(responseData.data.totalPages || 0);
            setTotalElements(responseData.data.totalElements || 0);
          }
          // 빈 객체이거나 다른 형태
          else {
            console.log('빈 데이터 또는 알 수 없는 형태:', responseData);
            setNotifications([]);
            setTotalPages(0);
            setTotalElements(0);
          }
        } else {
          console.log('응답 데이터가 객체가 아님:', typeof responseData);
          setNotifications([]);
          setTotalPages(0);
          setTotalElements(0);
        }
      } else {
        console.log('API 호출 실패:', result);
        setNotifications([]);
        setTotalPages(0);
        setTotalElements(0);
      }
    } catch (error) {
      console.error('알림 목록 조회 오류:', error);
      setNotifications([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  const searchEmployees = async () => {
    if (!searchKeyword.trim()) return;
    
    try {
      const result = await getEmployees();
      if (result.success || result.data) {
        const employees = Array.isArray(result.data) ? result.data : (result.data || []);
        const filtered = employees.filter(emp => 
          emp.empName?.includes(searchKeyword) || 
          emp.empId?.includes(searchKeyword) ||
          emp.empNo?.toString().includes(searchKeyword)
        );
        setSearchResults(filtered);
      }
    } catch (error) {
      console.error('직원 검색 오류:', error);
      setSearchResults([]);
    }
  };

  const selectEmployee = (employee) => {
    setSelectedEmployee(employee);
    setFormData(prev => ({ ...prev, selectedEmpNo: employee.empNo }));
    setSearchResults([]);
    setSearchKeyword('');
  };

  const sendNotification = async () => {
    if (!formData.title.trim()) {
      alert('알림 제목을 입력해주세요.');
      return;
    }

    if (formData.targetType === 'DEPARTMENT' && !formData.deptNo) {
      alert('부서를 선택해주세요.');
      return;
    }

    if (formData.targetType === 'EMPLOYEE' && !formData.selectedEmpNo) {
      alert('직원을 선택해주세요.');
      return;
    }

    try {
      setLoading(true);
      
      const notificationData = {
        targetType: formData.targetType,
        targetId: formData.targetType === 'DEPARTMENT' ? formData.deptNo : 
                  formData.targetType === 'EMPLOYEE' ? formData.selectedEmpNo : null,
        type: formData.notificationType,
        title: formData.title,
        content: formData.content,
        refType: formData.refType || null,
        refNo: formData.refNo || null,
        priority: formData.priority
      };

      const result = await notificationApi.createNotification(notificationData);

      if (result.success) {
        alert('알림이 성공적으로 발송되었습니다.');
        resetForm();
      } else {
        alert('알림 발송에 실패했습니다: ' + result.message);
      }
    } catch (error) {
      console.error('알림 발송 오류:', error);
      alert('알림 발송 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      targetType: 'ALL',
      deptNo: '',
      selectedEmpNo: '',
      notificationType: 'NOTICE',
      title: '',
      content: '',
      refType: '',
      refNo: '',
      priority: 'NORMAL'
    });
    setSelectedEmployee(null);
    setSearchKeyword('');
    setSearchResults([]);
  };

  const viewNotification = (notification) => {
    const modal = `
제목: ${notification.title}
유형: ${notification.type}
내용: ${notification.content || '내용 없음'}
발송일: ${formatDate(notification.createDate)}
    `.trim();
    alert(modal);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('ko-KR');
  };

  const refreshData = () => {
    if (activeTab === 'history') {
      setCurrentPage(1);
      fetchNotifications();
    }
  };

  const handleTabChange = (tabName, event) => {
    event.preventDefault();
    event.stopPropagation();
    setActiveTab(tabName);
    
    if (tabName === 'send') {
      setSearchParams({});
    } else {
      setSearchParams({ tab: tabName });
    }
    
    if (tabName === 'history') {
      setCurrentPage(1);
      setTimeout(() => fetchNotifications(), 100);
    }
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    setCurrentPage(1);
  };

  const getTypeDisplayName = (type) => {
    const typeMap = {
      'NOTICE': '공지사항',
      'SYSTEM': '시스템',
      'APPROVAL': '결재',
      'SCHEDULE': '일정',
      'FACILITY_APPROVED': '시설 승인',
      'FACILITY_REJECTED': '시설 거절'
    };
    return typeMap[type] || type;
  };

  return (
    <div className="admin-notification-management">
      <div className="page-header">
        <h1 className="page-title">알림 관리</h1>
        <p className="page-desc">시스템 알림을 발송하고 관리할 수 있습니다.</p>
      </div>

      <div className="tab-navigation">
        <button 
          className={`tab-button ${activeTab === 'send' ? 'active' : ''}`}
          onClick={(e) => handleTabChange('send', e)}
          type="button"
        >
          <i className="fas fa-paper-plane"></i>
          알림 발송
        </button>
        <button 
          className={`tab-button ${activeTab === 'history' ? 'active' : ''}`}
          onClick={(e) => handleTabChange('history', e)}
          type="button"
        >
          <i className="fas fa-history"></i>
          발송 내역
        </button>
        <button className="refresh-button" onClick={refreshData} type="button">
          <i className="fas fa-sync-alt"></i>
        </button>
      </div>

      <div className="tab-content">
        {activeTab === 'send' && (
          <div className="send-notification-section">
            <div className="notification-form">
              <div className="form-section">
                <h3 className="section-title">발송 설정</h3>
                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">수신 대상</label>
                    <select 
                      className="form-select" 
                      value={formData.targetType}
                      onChange={(e) => setFormData(prev => ({ ...prev, targetType: e.target.value }))}
                    >
                      <option value="ALL">전체 직원</option>
                      <option value="DEPARTMENT">특정 부서</option>
                      <option value="EMPLOYEE">특정 직원</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">알림 유형</label>
                    <select 
                      className="form-select"
                      value={formData.notificationType}
                      onChange={(e) => setFormData(prev => ({ ...prev, notificationType: e.target.value }))}
                    >
                      <option value="NOTICE">공지사항</option>
                      <option value="SYSTEM">시스템 알림</option>
                      <option value="APPROVAL">결재</option>
                      <option value="SCHEDULE">일정</option>
                      <option value="FACILITY_APPROVED">시설 예약 승인</option>
                      <option value="FACILITY_REJECTED">시설 예약 거절</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">우선순위</label>
                    <select 
                      className="form-select"
                      value={formData.priority}
                      onChange={(e) => setFormData(prev => ({ ...prev, priority: e.target.value }))}
                    >
                      <option value="NORMAL">일반</option>
                      <option value="HIGH">높음</option>
                      <option value="URGENT">긴급</option>
                    </select>
                  </div>
                </div>

                {formData.targetType === 'DEPARTMENT' && (
                  <div className="form-group">
                    <label className="form-label">부서 선택</label>
                    <select 
                      className="form-select"
                      value={formData.deptNo}
                      onChange={(e) => setFormData(prev => ({ ...prev, deptNo: e.target.value }))}
                    >
                      <option value="">부서를 선택하세요</option>
                      {Array.isArray(departments) && departments.map(dept => (
                        <option key={dept.deptNo} value={dept.deptNo}>
                          {dept.deptName}
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                {formData.targetType === 'EMPLOYEE' && (
                  <div className="form-group">
                    <label className="form-label">직원 검색</label>
                    <div className="employee-search">
                      <div className="search-input-group">
                        <input
                          type="text"
                          className="form-control"
                          placeholder="이름 또는 사번으로 검색"
                          value={searchKeyword}
                          onChange={(e) => setSearchKeyword(e.target.value)}
                          onKeyPress={(e) => e.key === 'Enter' && searchEmployees()}
                        />
                        <button 
                          type="button"
                          className="search-button" 
                          onClick={searchEmployees}
                        >
                          <i className="fas fa-search"></i>
                        </button>
                      </div>

                      {searchResults.length > 0 && (
                        <div className="search-results">
                          {searchResults.map(emp => (
                            <div
                              key={emp.empNo}
                              className="search-result-item"
                              onClick={() => selectEmployee(emp)}
                            >
                              <div className="employee-info">
                                <span className="employee-name">{emp.empName}</span>
                                <span className="employee-details">
                                  {emp.department?.deptName || emp.deptName || '부서 정보 없음'} - {emp.position?.positionName || emp.positionName || '직급 정보 없음'}
                                </span>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}

                      {selectedEmployee && (
                        <div className="selected-employee">
                          <i className="fas fa-user"></i>
                          선택된 직원: <strong>{selectedEmployee.empName}</strong> ({selectedEmployee.department?.deptName || selectedEmployee.deptName})
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>

              <div className="form-section">
                <h3 className="section-title">알림 내용</h3>
                <div className="form-group">
                  <label className="form-label">제목</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="알림 제목을 입력하세요"
                    value={formData.title}
                    onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">내용</label>
                  <textarea
                    className="form-control"
                    rows="4"
                    placeholder="알림 내용을 입력하세요"
                    value={formData.content}
                    onChange={(e) => setFormData(prev => ({ ...prev, content: e.target.value }))}
                  />
                </div>

                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">연결 페이지 (선택)</label>
                    <select
                      className="form-select"
                      value={formData.refType}
                      onChange={(e) => setFormData(prev => ({ ...prev, refType: e.target.value }))}
                    >
                      <option value="">선택 안함</option>
                      <option value="notice">공지사항</option>
                      <option value="approval">결재</option>
                      <option value="schedule">일정</option>
                      <option value="facility">시설</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">관련 ID</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="관련 ID 입력"
                      value={formData.refNo}
                      onChange={(e) => setFormData(prev => ({ ...prev, refNo: e.target.value }))}
                    />
                  </div>
                </div>
              </div>

              <div className="form-actions">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={resetForm}
                >
                  초기화
                </button>
                <button 
                  type="button" 
                  className="btn btn-primary" 
                  onClick={sendNotification}
                  disabled={loading}
                >
                  {loading ? '발송 중...' : '알림 발송'}
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'history' && (
          <div className="notification-history-section">
            <div className="history-header">
              <div className="history-info">
                <h3>발송 내역</h3>
                <span className="total-count">
                  총 {totalElements}개의 알림이 발송되었습니다.
                </span>
              </div>
            </div>

            <div className="history-filters">
              <div className="filter-group">
                <select
                  className="form-select"
                  value={filters.type}
                  onChange={(e) => handleFilterChange({ ...filters, type: e.target.value })}
                >
                  <option value="">전체 유형</option>
                  <option value="NOTICE">공지사항</option>
                  <option value="SYSTEM">시스템</option>
                  <option value="APPROVAL">결재</option>
                  <option value="SCHEDULE">일정</option>
                  <option value="FACILITY_APPROVED">시설 승인</option>
                  <option value="FACILITY_REJECTED">시설 거절</option>
                </select>

                <div className="search-group">
                  <input
                    type="text"
                    className="form-control"
                    placeholder="제목으로 검색"
                    value={filters.keyword}
                    onChange={(e) => setFilters(prev => ({ ...prev, keyword: e.target.value }))}
                    onKeyPress={(e) => e.key === 'Enter' && handleFilterChange(filters)}
                  />
                  <button 
                    className="btn btn-primary"
                    onClick={() => handleFilterChange(filters)}
                    type="button"
                  >
                    <i className="fas fa-search"></i>
                  </button>
                </div>
              </div>
            </div>

            <div className="notifications-table-container">
              <table className="notifications-table">
                <thead>
                  <tr>
                    <th>유형</th>
                    <th>제목</th>
                    <th>수신자</th>
                    <th>발송일시</th>
                    <th>읽음 상태</th>
                    <th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan="6" className="text-center">
                        <div className="loading-state">
                          <div className="loading-spinner"></div>
                          로딩 중...
                        </div>
                      </td>
                    </tr>
                  ) : !Array.isArray(notifications) || notifications.length === 0 ? (
                    <tr>
                      <td colSpan="6" className="text-center">
                        {filters.type || filters.keyword ? '검색 결과가 없습니다.' : '발송된 알림이 없습니다.'}
                      </td>
                    </tr>
                  ) : (
                    notifications.map((notification, index) => (
                      <tr key={notification.notificationNo || `notification-${index}`}>
                        <td>
                          <span className={`notification-type-badge ${notification.type?.toLowerCase()}`}>
                            <i className="fas fa-bell"></i>
                            {getTypeDisplayName(notification.type)}
                          </span>
                        </td>
                        <td className="notification-title">{notification.title}</td>
                        <td>{notification.empName || '전체'}</td>
                        <td>{formatDate(notification.createDate)}</td>
                        <td>
                          {notification.readDate ? (
                            <span className="status-read">
                              <i className="fas fa-check-circle"></i>
                              읽음
                            </span>
                          ) : (
                            <span className="status-unread">
                              <i className="fas fa-circle"></i>
                              읽지 않음
                            </span>
                          )}
                        </td>
                        <td>
                          <button
                            className="btn-view"
                            onClick={() => viewNotification(notification)}
                            type="button"
                          >
                            <i className="fas fa-eye"></i>
                            보기
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <div className="pagination">
                <button 
                  className="page-btn"
                  onClick={() => handlePageChange(1)}
                  disabled={currentPage === 1}
                  type="button"
                >
                  <i className="fas fa-angle-double-left"></i>
                </button>
                <button 
                  className="page-btn"
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 1}
                  type="button"
                >
                  <i className="fas fa-angle-left"></i>
                </button>
                
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 5) {
                    pageNum = i + 1;
                  } else if (currentPage <= 3) {
                    pageNum = i + 1;
                  } else if (currentPage >= totalPages - 2) {
                    pageNum = totalPages - 4 + i;
                  } else {
                    pageNum = currentPage - 2 + i;
                  }
                  
                  return (
                    <button
                      key={pageNum}
                      className={`page-btn ${currentPage === pageNum ? 'active' : ''}`}
                      onClick={() => handlePageChange(pageNum)}
                      type="button"
                    >
                      {pageNum}
                    </button>
                  );
                })}
                
                <button 
                  className="page-btn"
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages}
                  type="button"
                >
                  <i className="fas fa-angle-right"></i>
                </button>
                <button 
                  className="page-btn"
                  onClick={() => handlePageChange(totalPages)}
                  disabled={currentPage === totalPages}
                  type="button"
                >
                  <i className="fas fa-angle-double-right"></i>
                </button>
              </div>
            )}

            <div className="pagination-info">
              페이지 {currentPage} / {totalPages} (총 {totalElements}개)
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminNotificationManagement;