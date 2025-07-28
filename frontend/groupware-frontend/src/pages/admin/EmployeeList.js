// src/pages/admin/EmployeeList.js

import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuthHeaders } from '../../utils/authUtils'; // getAuthHeaders 임포트
import './EmployeeList.css';

const EmployeeList = ({ userData, onLogout }) => {
  const navigate = useNavigate();
  
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [positions, setPositions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [editForm, setEditForm] = useState({});
  
  const [filters, setFilters] = useState({
    deptNo: '',
    searchType: 'name',
    keyword: '',
    statusFilter: 'active'
  });

  useEffect(() => {
    // userData가 있을 때만 데이터 로드를 시작하도록 수정
    if (userData) {
      loadInitialData();
    }
  }, [userData]);

  const loadInitialData = async () => {
    setLoading(true);
    await Promise.all([
      fetchDepartments(),
      fetchPositions(),
      fetchEmployees()
    ]);
    setLoading(false);
  };

  const fetchDepartments = async () => {
    try {
      // getAuthHeaders() 사용
      const headers = getAuthHeaders();
      const response = await fetch('http://localhost:8080/api/departments', { headers });
      if (response.ok) {
        const data = await response.json();
        if (data.success) setDepartments(data.data || []);
      }
    } catch (err) {
      console.error('부서 목록 로딩 오류:', err);
    }
  };

  const fetchPositions = async () => {
    try {
      // getAuthHeaders() 사용
      const headers = getAuthHeaders();
      const response = await fetch('http://localhost:8080/api/positions', { headers });
      if (response.ok) {
        const data = await response.json();
        if (data.success) setPositions(data.data || []);
      }
    } catch (err) {
      console.error('직급 목록 로딩 오류:', err);
    }
  };

  const fetchEmployees = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // getAuthHeaders() 사용
      const headers = getAuthHeaders();
      const queryParams = new URLSearchParams();
      if (filters.keyword) {
        if (filters.searchType === 'name') queryParams.append('name', filters.keyword);
        else if (filters.searchType === 'id') queryParams.append('empId', filters.keyword);
        else if (filters.searchType === 'email') queryParams.append('email', filters.keyword);
      }
      if (filters.deptNo) queryParams.append('deptNo', filters.deptNo);
      if (filters.statusFilter !== 'all') {
        queryParams.append('status', filters.statusFilter === 'active' ? 'Y' : 'N');
      }
      
      const url = `http://localhost:8080/api/employees${queryParams.toString() ? '?' + queryParams.toString() : ''}`;
      console.log('Fetching employees from:', url);
      
      const response = await fetch(url, { headers });
      
      if (response.ok) {
        const data = await response.json();
        console.log('Employee data received:', data);
        if (data.success) {
          console.log('첫 번째 사원 데이터 구조:', data.data[0]);
          setEmployees(data.data || []);
        } else {
          setError(data.message || '사원 목록을 불러오는데 실패했습니다.');
        }
      } else if (response.status === 401) {
        setError('인증이 만료되었습니다. 다시 로그인해주세요.');
      } else {
        const errorData = await response.json().catch(() => ({}));
        console.error('서버 응답 오류:', errorData);
        setError(errorData.message || '서버에서 데이터를 가져오는데 실패했습니다.');
      }
    } catch (err) {
      console.error('사원 목록 로딩 오류:', err);
      setError('서버 연결에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchEmployees();
  };

  const handleReset = () => {
    setFilters({
      deptNo: '',
      searchType: 'name',
      keyword: '',
      statusFilter: 'active'
    });
    setTimeout(fetchEmployees, 100);
  };

  const handleView = (empNo) => {
    const employee = employees.find(emp => emp.empNo === empNo);
    if (employee) {
      setSelectedEmployee(employee);
      setModalType('view');
      setShowModal(true);
    }
  };

  const handleEdit = (empNo) => {
    const employee = employees.find(emp => emp.empNo === empNo);
    if (employee) {
      setSelectedEmployee(employee);
      setEditForm({
        empName: employee.empName || '',
        email: employee.email || '',
        phone: employee.phone || '',
        deptNo: employee.deptNo || '',
        positionNo: employee.positionNo || ''
      });
      setModalType('edit');
      setShowModal(true);
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    setSelectedEmployee(null);
    setEditForm({});
  };

  const handleEditFormChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({ ...prev, [name]: value }));
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    
    try {
      // getAuthHeaders() 사용
      const headers = getAuthHeaders();
      const updateData = {
        empName: editForm.empName,
        password: selectedEmployee.empId + 'init',
        email: editForm.email,
        phone: editForm.phone,
        hireDate: selectedEmployee.hireDate,
        deptNo: parseInt(editForm.deptNo),
        positionNo: parseInt(editForm.positionNo),
        isAdmin: selectedEmployee.isAdmin || 'N',
        status: selectedEmployee.status || 'Y'
      };

      console.log('수정 데이터:', updateData);

      const response = await fetch(`http://localhost:8080/api/employees/${selectedEmployee.empNo}`, {
        method: 'PUT',
        headers,
        body: JSON.stringify(updateData)
      });

      console.log('응답 상태:', response.status);

      if (response.ok) {
        const data = await response.json();
        if (data.success) {
          alert('사원 정보가 수정되었습니다.');
          handleModalClose();
          fetchEmployees();
        } else {
          alert(data.message || '수정에 실패했습니다.');
        }
      } else if (response.status === 401) {
        alert('인증이 만료되었습니다. 다시 로그인해주세요.');
      } else {
        const errorData = await response.json().catch(() => ({}));
        console.error('수정 실패:', errorData);
        alert(errorData.message || `수정에 실패했습니다. (${response.status})`);
      }
    } catch (err) {
      console.error('사원 수정 오류:', err);
      alert('수정 중 오류가 발생했습니다.');
    }
  };

  const handleDelete = async (empNo, empName) => {
    if (window.confirm(`${empName} 님을 퇴사 처리하시겠습니까?`)) {
      try {
        // getAuthHeaders() 사용
        const headers = getAuthHeaders();
        const response = await fetch(`http://localhost:8080/api/employees/${empNo}`, {
          method: 'DELETE',
          headers
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data.success) {
            alert('퇴사 처리가 완료되었습니다.');
            fetchEmployees();
          } else {
            alert(data.message || '퇴사 처리에 실패했습니다.');
          }
        }
      } catch (err) {
        console.error('퇴사 처리 오류:', err);
        alert('퇴사 처리 중 오류가 발생했습니다.');
      }
    }
  };

  // ... (이하 UI 코드 생략)
  const getProfileDisplay = (emp) => {
    if (emp.profileImageUrl || emp.profileImgPath) {
      return (
        <img 
          src={emp.profileImageUrl || emp.profileImgPath} 
          alt="프로필" 
          className="profile-img"
        />
      );
    }
    const colors = ['#4285f4', '#34a853', '#ea4335', '#fbbc05', '#9c27b0'];
    const firstChar = (emp.empName || '?').charAt(0);
    const colorIndex = Math.abs(firstChar.charCodeAt(0)) % 5;
    
    return (
      <div className="profile-default" style={{ backgroundColor: colors[colorIndex] }}>
        <span>{firstChar}</span>
      </div>
    );
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    try {
      return new Date(dateStr).toLocaleDateString('ko-KR');
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="employee-list-container">
      <div className="employee-container">
        <div className="page-title">
          <h2>사원 목록</h2>
          <p>등록된 사원 정보를 조회하고 관리할 수 있습니다.</p>
        </div>
        <div className="search-filter">
          <form onSubmit={handleSearch} className="form-inline">
            <div className="form-group mr-3">
              <label htmlFor="deptNo" className="mr-2">부서</label>
              <select 
                name="deptNo" 
                id="deptNo" 
                className="form-control"
                value={filters.deptNo}
                onChange={(e) => handleFilterChange('deptNo', e.target.value)}
              >
                <option value="">전체</option>
                {departments.map(dept => (
                  <option key={dept.deptNo} value={dept.deptNo}>
                    {dept.deptName}
                  </option>
                ))}
              </select>
            </div>
            
            <div className="form-group mr-3">
              <label htmlFor="searchType" className="mr-2">검색 조건</label>
              <select 
                name="searchType" 
                id="searchType" 
                className="form-control"
                value={filters.searchType}
                onChange={(e) => handleFilterChange('searchType', e.target.value)}
              >
                <option value="name">이름</option>
                <option value="id">사번</option>
                <option value="email">이메일</option>
              </select>
            </div>
            
            <div className="form-group mr-3">
              <input 
                type="text" 
                name="keyword" 
                id="keyword" 
                className="form-control" 
                placeholder="검색어 입력" 
                value={filters.keyword}
                onChange={(e) => handleFilterChange('keyword', e.target.value)}
              />
            </div>
            
            <div className="status-filter w-100 mt-2 mb-2">
              <div className="custom-control custom-radio custom-control-inline">
                <input 
                  type="radio" 
                  id="statusAll" 
                  name="statusFilter" 
                  value="all" 
                  className="custom-control-input"
                  checked={filters.statusFilter === 'all'}
                  onChange={(e) => handleFilterChange('statusFilter', e.target.value)}
                />
                <label className="custom-control-label" htmlFor="statusAll">전체 보기</label>
              </div>
              <div className="custom-control custom-radio custom-control-inline">
                <input 
                  type="radio" 
                  id="statusActive" 
                  name="statusFilter" 
                  value="active" 
                  className="custom-control-input"
                  checked={filters.statusFilter === 'active'}
                  onChange={(e) => handleFilterChange('statusFilter', e.target.value)}
                />
                <label className="custom-control-label" htmlFor="statusActive">재직자만</label>
              </div>
              <div className="custom-control custom-radio custom-control-inline">
                <input 
                  type="radio" 
                  id="statusInactive" 
                  name="statusFilter" 
                  value="inactive" 
                  className="custom-control-input"
                  checked={filters.statusFilter === 'inactive'}
                  onChange={(e) => handleFilterChange('statusFilter', e.target.value)}
                />
                <label className="custom-control-label" htmlFor="statusInactive">퇴사자만</label>
              </div>
            </div>
            
            <div className="w-100 mt-2">
              <button type="submit" className="btn btn-primary mr-2">검색</button>
              <button type="button" onClick={handleReset} className="btn btn-outline-secondary">초기화</button>
            </div>
          </form>
        </div>

        <div className="table-container">
          <table className="employee-table">
            <thead>
              <tr>
                <th>프로필</th>
                <th>사번</th>
                <th>이름</th>
                <th>부서</th>
                <th>직급</th>
                <th>이메일</th>
                <th>전화번호</th>
                <th>입사일</th>
                <th>상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {employees.length > 0 ? (
                employees.map((emp, index) => (
                  <tr key={emp.empNo || index}>
                    <td className="profile-cell">
                      {getProfileDisplay(emp)}
                    </td>
                    <td>{emp.empId}</td>
                    <td>{emp.empName}</td>
                    <td>{emp.department?.deptName || emp.deptName || '-'}</td>
                    <td>{emp.position?.positionName || emp.positionName || '-'}</td>
                    <td>{emp.email}</td>
                    <td>{emp.phone || '-'}</td>
                    <td>{formatDate(emp.hireDate)}</td>
                    <td>
                      {emp.status === 'Y' ? (
                        <span className="status-badge status-active">재직</span>
                      ) : (
                        <span className="status-badge status-inactive">퇴사</span>
                      )}
                    </td>
                    <td>
                      <button 
                        className="action-btn detail" 
                        onClick={() => handleView(emp.empNo)}
                        title="상세보기"
                      >
                        <i className="fas fa-search"></i>
                      </button>
                      <button 
                        className="action-btn edit" 
                        onClick={() => handleEdit(emp.empNo)}
                        title="수정"
                      >
                        <i className="fas fa-edit"></i>
                      </button>
                      {emp.status === 'Y' && (
                        <button 
                          className="action-btn delete" 
                          onClick={() => handleDelete(emp.empNo, emp.empName)}
                          title="퇴사처리"
                        >
                          <i className="fas fa-user-slash"></i>
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="10" className="text-center">등록된 사원이 없습니다.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <div className="text-right mt-3">
          <button 
            onClick={() => navigate('/admin/employees/register')}
            className="btn btn-primary"
          >
            <i className="fas fa-plus mr-1"></i> 신규 등록
          </button>
        </div>
      </div>
      {showModal && selectedEmployee && (
        <div className="modal-overlay" onClick={handleModalClose}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{modalType === 'view' ? '사원 상세 정보' : '사원 정보 수정'}</h3>
              <button className="modal-close-btn" onClick={handleModalClose}>
                <i className="fas fa-times"></i>
              </button>
            </div>
            <div className="modal-body">
              {modalType === 'view' ? (
                <div className="employee-detail">
                  <div className="detail-row">
                    <div className="detail-col">
                      <div className="profile-section">
                        {getProfileDisplay(selectedEmployee)}
                        <div className="profile-info">
                          <h4>{selectedEmployee.empName}</h4>
                          <p>{selectedEmployee.empId}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="detail-row">
                    <div className="detail-col">
                      <label>부서</label>
                      <p>{selectedEmployee.department?.deptName || selectedEmployee.deptName || '-'}</p>
                    </div>
                    <div className="detail-col">
                      <label>직급</label>
                      <p>{selectedEmployee.position?.positionName || selectedEmployee.positionName || '-'}</p>
                    </div>
                  </div>
                  <div className="detail-row">
                    <div className="detail-col">
                      <label>이메일</label>
                      <p>{selectedEmployee.email}</p>
                    </div>
                    <div className="detail-col">
                      <label>전화번호</label>
                      <p>{selectedEmployee.phone || '-'}</p>
                    </div>
                  </div>
                  <div className="detail-row">
                    <div className="detail-col">
                      <label>입사일</label>
                      <p>{formatDate(selectedEmployee.hireDate)}</p>
                    </div>
                    <div className="detail-col">
                      <label>상태</label>
                      <p>
                        {selectedEmployee.status === 'Y' ? (
                          <span className="status-badge status-active">재직</span>
                        ) : (
                          <span className="status-badge status-inactive">퇴사</span>
                        )}
                      </p>
                    </div>
                  </div>
                </div>
              ) : (
                <form onSubmit={handleEditSubmit}>
                  <div className="edit-form">
                    <div className="form-row">
                      <div className="form-col">
                        <label>이름</label>
                        <input
                          type="text"
                          name="empName"
                          value={editForm.empName}
                          onChange={handleEditFormChange}
                          required
                        />
                      </div>
                      <div className="form-col">
                        <label>이메일</label>
                        <input
                          type="email"
                          name="email"
                          value={editForm.email}
                          onChange={handleEditFormChange}
                          required
                        />
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-col">
                        <label>전화번호</label>
                        <input
                          type="text"
                          name="phone"
                          value={editForm.phone}
                          onChange={handleEditFormChange}
                        />
                      </div>
                      <div className="form-col">
                        <label>부서</label>
                        <select
                          name="deptNo"
                          value={editForm.deptNo}
                          onChange={handleEditFormChange}
                          required
                        >
                          <option value="">선택</option>
                          {departments.map(dept => (
                            <option key={dept.deptNo} value={dept.deptNo}>
                              {dept.deptName}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-col">
                        <label>직급</label>
                        <select
                          name="positionNo"
                          value={editForm.positionNo}
                          onChange={handleEditFormChange}
                          required
                        >
                          <option value="">선택</option>
                          {positions.map(pos => (
                            <option key={pos.positionNo} value={pos.positionNo}>
                              {pos.positionName}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="form-col"></div>
                    </div>
                  </div>
                  <div className="modal-actions">
                    <button type="submit" className="btn btn-primary">수정 완료</button>
                    <button type="button" className="btn btn-outline-secondary" onClick={handleModalClose}>취소</button>
                  </div>
                </form>
              )}
            </div>
            {modalType === 'view' && (
              <div className="modal-actions">
                <button 
                  className="btn btn-primary" 
                  onClick={() => {
                    setModalType('edit');
                    setEditForm({
                      empName: selectedEmployee.empName || '',
                      email: selectedEmployee.email || '',
                      phone: selectedEmployee.phone || '',
                      deptNo: selectedEmployee.department?.deptNo || selectedEmployee.deptNo || '',
                      positionNo: selectedEmployee.position?.positionNo || selectedEmployee.positionNo || ''
                    });
                  }}
                >
                  수정하기
                </button>
                <button className="btn btn-outline-secondary" onClick={handleModalClose}>닫기</button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
export default EmployeeList;