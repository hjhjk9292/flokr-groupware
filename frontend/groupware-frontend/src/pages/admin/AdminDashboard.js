// frontend/groupware-frontend/src/pages/admin/AdminDashboardPage.js

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위해 useNavigate 훅 추가

import Header from '../../components/common/Header';
import EmployeeListPage from './EmployeeList';
import EmployeeRegisterPage from './EmployeeRegister';
import './AdminDashboard.css';

const AdminDashboardPage = ({ userData, onLogout }) => {
  const navigate = useNavigate(); // useNavigate 훅 사용

  // 이 컴포넌트 내부에서 라우팅을 직접 관리하는 대신, React Router를 활용
  const [stats, setStats] = useState({
    totalEmployees: 0,
    activeEmployees: 0,
    totalDepartments: 0,
    totalNotices: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData(); // 대시보드 데이터는 컴포넌트 마운트 시 한 번만 가져오도록
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('accessToken');
      
      // 통계 API 호출
      const [employeesRes, departmentsRes, noticesRes] = await Promise.all([
        fetch('http://localhost:8080/api/employees/stats/total', {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch('http://localhost:8080/api/departments', {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch('http://localhost:8080/api/notices', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      const [employeesData, departmentsData, noticesData] = await Promise.all([
        employeesRes.json(),
        departmentsRes.json(),
        noticesRes.json()
      ]);

      const activeEmployeesRes = await fetch('http://localhost:8080/api/employees/stats/active', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
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

  // 페이지 네비게이션 navigate 훅 직접 사용
  // 해당 페이지로 이동하는 함수들 직접 연결
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
      {/* 공통 헤더는 항상 표시하거나, AdminLayout 컴포넌트를 만들어서 관리할 수 있습니다. */}
      {/* 여기서는 Header 컴포넌트가 App.js의 <AdminDashboardPage>에 속하므로 그대로 둡니다. */}
      <Header 
        userData={userData}
        onLogout={onLogout}
        isAdmin={true}
        // onNavigate prop은 더 이상 필요 없으며, Header 내부에서 navigate 훅을 사용해야 합니다.
      />

      {/* 라우팅은 App.js에서 이미 처리하고 있으므로, AdminDashboardPage는 대시보드 내용만 렌더링합니다. */}
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-title">
          <h1>관리자 대시보드</h1>
          <p>조직 관리와 시스템 설정을 할 수 있습니다.</p>
        </div>

        {/* 통계 카드들 - 클릭 가능 */}
        <div className="stats-grid">
          <div 
            className="stat-card clickable" 
            onClick={() => navigate('/admin/employees/list')} // useNavigate 훅 사용
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
            onClick={() => navigate('/admin/employees/list')} // useNavigate 훅 사용
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

        {/* 기능 카드들 */}
        <div className="features-grid">
          {/* 조직 관리 */}
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
                onClick={() => navigate('/admin/organization')} // 예시 경로
              >
                조직도 관리
              </button>
            </div>
          </div>

          {/* 사원 관리 */}
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
                onClick={() => navigate('/admin/employees/register')} // useNavigate 훅 사용
              >
                사원 등록
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/employees/list')} // useNavigate 훅 사용
              >
                사원 목록
              </button>
            </div>
          </div>

          {/* 사내 공지 관리 */}
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
                onClick={() => navigate('/admin/notices/create')} // 예시 경로
              >
                공지 등록
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/notices/list')} // 예시 경로
              >
                공지 목록
              </button>
            </div>
          </div>

          {/* 사용자 관리 */}
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
                onClick={() => navigate('/admin/users/manage')} // 예시 경로
              >
                사용자 정보 관리
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/users/sessions')} // 예시 경로
              >
                접속 사용자 관리
              </button>
            </div>
          </div>

          {/* 시설 관리 */}
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
                onClick={() => navigate('/admin/facilities/status')} // 예시 경로
              >
                시설 현황
              </button>
              <button 
                className="feature-btn secondary"
                onClick={() => navigate('/admin/facilities/reservations')} // 예시 경로
              >
                예약 관리
              </button>
            </div>
          </div>

          {/* 알림 관리 */}
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
                onClick={() => navigate('/admin/notifications/settings')} // 예시 경로
              >
                알림 설정
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboardPage;