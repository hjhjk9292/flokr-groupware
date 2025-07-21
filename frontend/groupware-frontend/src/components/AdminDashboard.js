// src/components/AdminDashboard.js
import React, { useState, useEffect } from 'react';
import Header from './common/Header';
import './AdminDashboard.css';

const AdminDashboard = ({ userData, onLogout }) => {
  const [stats, setStats] = useState({
    totalEmployees: 0,
    activeEmployees: 0,
    totalDepartments: 0,
    totalPositions: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      
      // 통계 API 호출들
      const [employeesRes, departmentsRes, positionsRes] = await Promise.all([
        fetch('/api/employees/stats/total', {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch('/api/departments', {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch('/api/positions', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      const [employeesData, departmentsData, positionsData] = await Promise.all([
        employeesRes.json(),
        departmentsRes.json(),
        positionsRes.json()
      ]);

      // 활성 직원 수 조회
      const activeEmployeesRes = await fetch('/api/employees/stats/active', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const activeEmployeesData = await activeEmployeesRes.json();

      setStats({
        totalEmployees: employeesData.success ? employeesData.data : 0,
        activeEmployees: activeEmployeesData.success ? activeEmployeesData.data : 0,
        totalDepartments: departmentsData.success ? departmentsData.data.length : 0,
        totalPositions: positionsData.success ? positionsData.data.length : 0
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
      {/* 공통 헤더 사용 */}
      <Header 
        userData={userData}
        onLogout={onLogout}
        isAdmin={true}
      />

      {/* 메인 콘텐츠 */}
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-title">
          <h1>관리자 대시보드</h1>
          <p>조직 관리와 시스템 설정을 할 수 있습니다.</p>
        </div>

        {/* 통계 카드들 */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon employees">
              <i className="fas fa-users"></i>
            </div>
            <div className="stat-content">
              <div className="stat-title">총 직원 수</div>
              <div className="stat-value">{stats.totalEmployees}명</div>
            </div>
          </div>

          <div className="stat-card">
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
            <div className="stat-icon positions">
              <i className="fas fa-award"></i>
            </div>
            <div className="stat-content">
              <div className="stat-title">직급 수</div>
              <div className="stat-value">{stats.totalPositions}개</div>
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
              <button className="feature-btn primary">조직도 관리</button>
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
              <button className="feature-btn primary">사원 등록</button>
              <button className="feature-btn secondary">사원 목록</button>
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
              <button className="feature-btn primary">공지 등록</button>
              <button className="feature-btn secondary">공지 목록</button>
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
              <button className="feature-btn primary">사용자 정보 관리</button>
              <button className="feature-btn secondary">접속 사용자 관리</button>
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
              <button className="feature-btn primary">시설 현황</button>
              <button className="feature-btn secondary">예약 관리</button>
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
              <button className="feature-btn primary">알림 설정</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;