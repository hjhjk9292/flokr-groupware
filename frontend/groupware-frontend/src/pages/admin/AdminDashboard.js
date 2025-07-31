// frontend/groupware-frontend/src/pages/admin/AdminDashboard.js

import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuthHeaders } from '../../utils/authUtils'; // getAuthHeaders 임포트

import './AdminDashboard.css';

const AdminDashboardPage = ({ userData, onLogout }) => {
  const navigate = useNavigate();

  const [stats, setStats] = useState({
    totalEmployees: 0,
    activeEmployees: 0,
    totalDepartments: 0,
    totalNotices: 0
  });
  const [loading, setLoading] = useState(true);

  // userData가 유효할 때만 API 호출 시도
  useEffect(() => {
    if (userData) {
      fetchDashboardData();
    }
  }, [userData]);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // getAuthHeaders() 함수를 사용하여 인증 헤더를 가져온다.
      const headers = getAuthHeaders();
      const API_BASE_URL = process.env.NODE_ENV === 'development' 
      ? 'http://localhost:8080'
      : (process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080');
    
      const [employeesRes, departmentsRes, noticesRes] = await Promise.all([
        fetch(`${API_BASE_URL}/api/employees/stats/total`, { headers }),
        fetch(`${API_BASE_URL}/api/departments`, { headers }),
        fetch(`${API_BASE_URL}/api/notices`, { headers })
      ]);

      const [employeesData, departmentsData, noticesData] = await Promise.all([
        employeesRes.json(),
        departmentsRes.json(),
        noticesRes.json()
      ]);

      const activeEmployeesRes = await fetch(`${API_BASE_URL}/api/employees/stats/active`, { headers });
      const activeEmployeesData = await activeEmployeesRes.json();

      setStats({
        totalEmployees: employeesData.success ? employeesData.data : 0,
        activeEmployees: activeEmployeesData.success ? activeEmployeesData.data : 0,
        totalDepartments: departmentsData.success ? departmentsData.data.length : 0,
        totalNotices: noticesData.success ? noticesData.data.length : 0
      });
    } catch (error) {
      console.error('대시보드 데이터 로딩 오류:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="admin-dashboard-loading">
        <div className="loading-spinner"></div>
        <p>대시보드 로딩 중...</p>
      </div>
    );
  }

  return (
    <div className="admin-dashboard-container">
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-title">
          <h1>관리자 대시보드</h1>
          <p>조직 관리와 시스템 설정을 할 수 있습니다.</p>
        </div>
        <div className="stats-grid">
          <div 
            className="stat-card clickable" 
            onClick={() => navigate('/admin/employees/list')}
            title="사원 목록으로 이동"
          >
            <div className="stat-icon employees">
              <i className="fas fa-users"></i>
            </div>
            <div className="stat-content">
              <div className="stat-title">총 직원 수</div>
              <div className="stat-value">{stats.totalEmployees}명</div>
            </div>
          </div>
          <div 
            className="stat-card clickable" 
            onClick={() => navigate('/admin/employees/list')}
            title="활성 직원 목록으로 이동"
          >
            <div className="stat-icon active">
              <i className="fas fa-user-check"></i>
            </div>
            <div className="stat-content">
              <div className="stat-title">활성 직원</div>
              <div className="stat-value">{stats.activeEmployees}명</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon departments">
              <i className="fas fa-sitemap"></i>
            </div>
            <div className="stat-content">
              <div className="stat-title">부서 수</div>
              <div className="stat-value">{stats.totalDepartments}개</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon notices">
              <i className="fas fa-bullhorn"></i>
            </div>
            <div className="stat-content">
              <div className="stat-title">공지사항 수</div>
              <div className="stat-value">{stats.totalNotices}개</div>
            </div>
          </div>
        </div>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon organization">
                <i className="fas fa-sitemap"></i>
              </div>
              <h2 className="feature-title">조직 관리</h2>
            </div>
            <p className="feature-description">
              부서와 직급 정보를 관리하고 조직도를 설정합니다.
            </p>
            <div className="feature-actions">
              <button 
                className="feature-btn primary"
                onClick={() => navigate('/admin/organization')}
              >
                조직도 관리
              </button>
            </div>
          </div>
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon employee">
                <i className="fas fa-user-plus"></i>
              </div>
              <h2 className="feature-title">사원 관리</h2>
            </div>
            <p className="feature-description">
              신규 직원 정보를 등록하고 관리합니다.
            </p>
            <div className="feature-actions">
              <button 
                className="feature-btn primary"
                onClick={() => navigate('/admin/employees/register')}
              >
                사원 등록
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/employees/list')}
              >
                사원 목록
              </button>
            </div>
          </div>
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon notice">
                <i className="fas fa-bullhorn"></i>
              </div>
              <h2 className="feature-title">사내 공지 관리</h2>
            </div>
            <p className="feature-description">
              공지사항을 등록하고 관리합니다.
            </p>
            <div className="feature-actions">
              <button 
                className="feature-btn primary"
                onClick={() => navigate('/admin/notices/create')}
              >
                공지 등록
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/notices/list')}
              >
                공지 목록
              </button>
            </div>
          </div>
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon user">
                <i className="fas fa-user-shield"></i>
              </div>
              <h2 className="feature-title">사용자 관리</h2>
            </div>
            <p className="feature-description">
              사용자 계정 정보와 접속 상태를 관리합니다.
            </p>
            <div className="feature-actions">
              <button 
                className="feature-btn primary"
                onClick={() => navigate('/admin/users/manage')}
              >
                사용자 정보 관리
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/users/sessions')}
              >
                접속 사용자 관리
              </button>
            </div>
          </div>
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon facility">
                <i className="fas fa-building"></i>
              </div>
              <h2 className="feature-title">시설 관리</h2>
            </div>
            <p className="feature-description">
              회의실, 공용 장비 등 사내 시설을 관리합니다.
            </p>
            <div className="feature-actions">
              <button 
                className="feature-btn primary"
                onClick={() => navigate('/admin/facilities')}
              >
                시설 관리
              </button>
            </div>
          </div>
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon notification">
                <i className="fas fa-bell"></i>
              </div>
              <h2 className="feature-title">알림 관리</h2>
            </div>
            <p className="feature-description">
              시스템 알림을 설정하고 관리합니다.
            </p>
            <div className="feature-actions">
              <button 
                className="feature-btn primary"
                onClick={() => navigate('/admin/notifications')}
              >
                알림 관리
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
export default AdminDashboardPage;