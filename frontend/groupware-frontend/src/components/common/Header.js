// src/components/common/Header.js
import React, { useState, useEffect } from 'react';
import './Header.css';

const Header = ({ userData, onLogout, isAdmin = false }) => {
  const [notifications, setNotifications] = useState(0);
  const [chatMessages, setChatMessages] = useState(0);

  useEffect(() => {
    // 실제로는 API에서 가져올 데이터
    // 지금은 임시로 0으로 설정
    fetchNotificationCount();
    fetchChatCount();
  }, []);

  const fetchNotificationCount = async () => {
    try {
      // TODO: 실제 알림 API 호출
      // const response = await fetch('/api/notifications/count');
      // const data = await response.json();
      // setNotifications(data.count);
      setNotifications(0); // 임시로 0
    } catch (error) {
      console.error('알림 수 조회 오류:', error);
      setNotifications(0);
    }
  };

  const fetchChatCount = async () => {
    try {
      // TODO: 실제 채팅 API 호출
      // const response = await fetch('/api/chat/unread-count');
      // const data = await response.json();
      // setChatMessages(data.count);
      setChatMessages(0); // 임시로 0
    } catch (error) {
      console.error('채팅 수 조회 오류:', error);
      setChatMessages(0);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userData');
    onLogout();
  };

  return (
    <>
      {/* 상단 헤더 - 관리자/사용자 공통 */}
      <header className="header-top">
        <div className="header-logo">
          <img src="/images/logo.png" alt="Flokr" className="header-logo-img" />
          <span className="header-logo-text">Flokr</span>
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
          
          {/* 사용자 프로필 */}
          <div className="header-profile">
            <div className="header-profile-avatar">
              {userData.empName.charAt(0)}
            </div>
            <div className="header-profile-info">
              <span className="header-profile-name">환영합니다, {userData.empName}님!</span>
              <span className="header-profile-dept">{userData.departmentName} | {userData.positionName}</span>
            </div>
          </div>
          
          <button onClick={handleLogout} className="header-btn-sm">
            <i className="fas fa-sign-out-alt"></i>
            로그아웃
          </button>
        </div>
      </header>

      {/* 네비게이션 바 - 관리자/사용자 분기 */}
      <nav className="header-nav-bar">
        <div className="header-nav-container">
          {isAdmin ? (
            // 관리자 네비게이션
            <>
              <a href="#dashboard" className="header-nav-item header-active">
                <i className="fas fa-tachometer-alt header-nav-icon"></i>
                대시보드
              </a>
              <a href="#organization" className="header-nav-item">
                <i className="fas fa-sitemap header-nav-icon"></i>
                조직 관리
              </a>
              <a href="#employees" className="header-nav-item">
                <i className="fas fa-users header-nav-icon"></i>
                사원 관리
              </a>
              <a href="#notices" className="header-nav-item">
                <i className="fas fa-bullhorn header-nav-icon"></i>
                사내 공지 관리
              </a>
              <a href="#users" className="header-nav-item">
                <i className="fas fa-user-shield header-nav-icon"></i>
                사용자 관리
              </a>
              <a href="#facilities" className="header-nav-item">
                <i className="fas fa-building header-nav-icon"></i>
                시설 관리
              </a>
              <a href="#notifications" className="header-nav-item">
                <i className="fas fa-bell header-nav-icon"></i>
                알림 관리
              </a>
            </>
          ) : (
            // 사용자 네비게이션
            <>
              <a href="#home" className="header-nav-item header-active">
                <i className="fas fa-home header-nav-icon"></i>
                홈
              </a>
              <a href="#attendance" className="header-nav-item">
                <i className="fas fa-clock header-nav-icon"></i>
                근태 관리
              </a>
              <a href="#approval" className="header-nav-item">
                <i className="fas fa-file-signature header-nav-icon"></i>
                전자 결재
              </a>
              <a href="#notice" className="header-nav-item">
                <i className="fas fa-bullhorn header-nav-icon"></i>
                공지사항
              </a>
              <a href="#chat" className="header-nav-item">
                <i className="fas fa-comments header-nav-icon"></i>
                메신저
              </a>
              <a href="#schedule" className="header-nav-item">
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