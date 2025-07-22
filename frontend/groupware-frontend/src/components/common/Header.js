// frontend/groupware-frontend/src/components/common/Header.js

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Header.css';

const Header = ({ userData, onLogout, isAdmin = false }) => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState(0);
  const [chatMessages, setChatMessages] = useState(0);

  useEffect(() => {
    fetchNotificationCount();
    fetchChatCount();
  }, []);

  const fetchNotificationCount = async () => {
    try {
      // TODO: 실제 알림 API 호출 구현
      setNotifications(0); // 임시값
    } catch (error) {
      console.error('알림 수 조회 오류:', error);
      setNotifications(0);
    }
  };

  const fetchChatCount = async () => {
    try {
      // TODO: 실제 채팅 API 호출 구현
      setChatMessages(0); // 임시값
    } catch (error) {
      console.error('채팅 수 조회 오류:', error);
      setChatMessages(0);
    }
  };

  const handleLogoutClick = () => {
    onLogout();
  };

  // 로고 클릭 시 역할에 따라 대시보드 페이지로 이동
  const handleLogoClick = () => {
    if (isAdmin) {
      navigate('/admin');
    } else {
      navigate('/user');
    }
  };

  return (
    <>
      <header className="header-top">
        <div className="header-left">
          <div className="header-logo" onClick={handleLogoClick}>
            <img src="/images/logo.png" alt="Flokr" className="header-logo-img" />
            <span className="header-logo-text">Flokr</span>
          </div>
          <nav className="main-nav">
            {/* 네비게이션 항목들 (향후 React Router Link 컴포넌트로 변경 권장) */}
          </nav>
        </div>
        <div className="header-right-section">
          {/* 검색바 */}
          <div className="header-search-container">
            <input type="text" className="header-search-bar" placeholder="검색..." />
            <i className="fas fa-search header-search-icon"></i>
          </div>
          
          {/* 알림 */}
          <div className="header-icon-badge">
            <i className="fas fa-bell header-icon"></i>
            {notifications > 0 && (
              <span className="header-badge">{notifications > 99 ? '99+' : notifications}</span>
            )}
          </div>
          
          {/* 채팅 */}
          <div className="header-icon-badge">
            <i className="fas fa-comments header-icon"></i>
            {chatMessages > 0 && (
              <span className="header-badge">{chatMessages > 99 ? '99+' : chatMessages}</span>
            )}
          </div>
          
          {/* 사용자 프로필 - userData가 존재할 때만 안전하게 접근 */}
          <div className="header-profile">
            <div className="header-profile-avatar">
              {userData?.empName?.charAt(0) || ''}
            </div>
            <div className="header-profile-info">
              <span className="header-profile-name">환영합니다, {userData?.empName || 'Guest'}님!</span>
              <span className="header-profile-dept">
                {userData?.departmentName || userData?.deptName || ''} | {userData?.positionName || userData?.positionCode || ''}
              </span>
            </div>
          </div>
          
          <button onClick={handleLogoutClick} className="header-btn-sm">
            <i className="fas fa-sign-out-alt"></i>
            로그아웃
          </button>
        </div>
      </header>

      {/* 네비게이션 바 - 관리자/사용자 분기 */}
      <nav className="header-nav-bar">
        <div className="header-nav-container">
          {isAdmin ? (
            // 관리자 네비게이션 (href를 React Router 경로로 직접 지정)
            <>
              <a href="/admin" className="header-nav-item header-active">
                <i className="fas fa-tachometer-alt header-nav-icon"></i>
                대시보드
              </a>
              <a href="/admin/organization" className="header-nav-item">
                <i className="fas fa-sitemap header-nav-icon"></i>
                조직 관리
              </a>
              <a href="/admin/employees/list" className="header-nav-item">
                <i className="fas fa-users header-nav-icon"></i>
                사원 관리
              </a>
              <a href="/admin/notices" className="header-nav-item">
                <i className="fas fa-bullhorn header-nav-icon"></i>
                사내 공지 관리
              </a>
              <a href="/admin/users" className="header-nav-item">
                <i className="fas fa-user-shield header-nav-icon"></i>
                사용자 관리
              </a>
              <a href="/admin/facilities" className="header-nav-item">
                <i className="fas fa-building header-nav-icon"></i>
                시설 관리
              </a>
              <a href="/admin/notifications" className="header-nav-item">
                <i className="fas fa-bell header-nav-icon"></i>
                알림 관리
              </a>
            </>
          ) : (
            // 사용자 네비게이션
            <>
              <a href="/user" className="header-nav-item header-active">
                <i className="fas fa-home header-nav-icon"></i>
                홈
              </a>
              <a href="/user/attendance" className="header-nav-item">
                <i className="fas fa-clock header-nav-icon"></i>
                근태 관리
              </a>
              <a href="/user/approval" className="header-nav-item">
                <i className="fas fa-file-signature header-nav-icon"></i>
                전자 결재
              </a>
              <a href="/user/notices" className="header-nav-item">
                <i className="fas fa-bullhorn header-nav-icon"></i>
                공지사항
              </a>
              <a href="/user/chat" className="header-nav-item">
                <i className="fas fa-comments header-nav-icon"></i>
                메신저
              </a>
              <a href="/user/schedule" className="header-nav-item">
                <i className="fas fa-calendar header-nav-icon"></i>
                일정 관리
              </a>
            </>
          )}
        </div>
      </nav>
    </>
  );
};

export default Header;