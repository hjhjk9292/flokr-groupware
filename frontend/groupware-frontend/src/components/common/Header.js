import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotification } from '../../context/NotificationContext';
import './Header.css';

const Header = ({ userData, onLogout, isAdmin = false }) => {
  const navigate = useNavigate();
  const { 
    notifications, 
    notificationList, 
    loading, 
    fetchUnreadNotifications, 
    markAsRead, 
    markAllAsRead 
  } = useNotification();
  
  const [chatMessages, setChatMessages] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [toasts, setToasts] = useState([]);

  const showToast = (message, type = 'info') => {
    const id = Date.now();
    const toast = { id, message, type };
    setToasts(prev => [...prev, toast]);

    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
  };

  const removeToast = (id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  };

  useEffect(() => {
    window.showToast = showToast;
    return () => {
      delete window.showToast;
    };
  }, []);

  useEffect(() => {
    if (userData) {
      fetchUnreadNotifications();
    }
  }, [userData, fetchUnreadNotifications]);

  const handleNotificationButtonClick = async () => {
    if (!showNotifications) {
      await fetchUnreadNotifications();
    }
    setShowNotifications(!showNotifications);
  };

  const handleNotificationClick = async (notification) => {
    try {
      await markAsRead(notification.notificationNo);
    } catch (error) {
      console.error('알림 읽음 처리 오류:', error);
    }
    setShowNotifications(false);

    if (notification.refType === 'FACILITY_RESERVATION') {
      const path = isAdmin ? '/admin/facilities?tab=reservations' : '/user/facilities?tab=reservations';
      navigate(path);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      showToast('모든 알림을 읽음 처리했습니다.', 'success');
    } catch (error) {
      console.error('모든 알림 읽음 처리 오류:', error);
      showToast('알림 처리 중 오류가 발생했습니다.', 'error');
    }
  };

  const handleLogoutClick = () => {
    onLogout();
  };

  const handleLogoClick = () => {
    if (isAdmin) {
      navigate('/admin');
    } else {
      navigate('/user');
    }
  };

  const safeNotificationList = Array.isArray(notificationList) ? notificationList : [];

  return (
    <>
      <header className="header-top">
        <div className="header-left">
          <div className="header-logo" onClick={handleLogoClick}>
            <img src="/images/logo.png" alt="Flokr" className="header-logo-img" />
            <span className="header-logo-text">Flokr</span>
          </div>
        </div>
        <div className="header-right-section">
          <div className="header-search-container">
            <input type="text" className="header-search-bar" placeholder="검색..." />
            <i className="fas fa-search header-search-icon"></i>
          </div>

          <div className="header-icon-badge notification-container">
            <i
              className="fas fa-bell header-icon"
              onClick={handleNotificationButtonClick}
            ></i>
            {notifications > 0 && (
              <span id="notification-badge" className="header-badge">
                {notifications > 99 ? '99+' : notifications}
              </span>
            )}

            {showNotifications && (
              <div className="notification-dropdown">
                <div className="notification-header">
                  <h3>알림</h3>
                  {notifications > 0 && (
                    <button
                      onClick={handleMarkAllRead}
                      className="mark-all-read-btn"
                    >
                      모두 읽음
                    </button>
                  )}
                  <button
                    onClick={() => setShowNotifications(false)}
                    className="close-notification-btn"
                  >
                    ✕
                  </button>
                </div>

                <div className="notification-content">
                  {loading ? (
                    <div className="notification-loading">
                      <div className="loading-spinner"></div>
                      <p>로딩 중...</p>
                    </div>
                  ) : safeNotificationList.length === 0 ? (
                    <div className="no-notifications">
                      <i className="fas fa-bell" style={{fontSize: '48px', color: '#ddd'}}></i>
                      <p>새로운 알림이 없습니다</p>
                    </div>
                  ) : (
                    <div className="notification-list">
                      {safeNotificationList.map((notification) => (
                        <div
                          key={notification.notificationNo}
                          className={`notification-item ${!(notification.isRead || notification.readDate) ? 'unread' : 'read'}`}
                          onClick={() => handleNotificationClick(notification)}
                        >
                          <div className="notification-icon">
                            <i className={`fas ${
                              notification.type === 'FACILITY_APPROVED' || notification.type === 'FACILITY_REJECTED' 
                                ? 'fa-building' : 'fa-info-circle'
                            }`}></i>
                          </div>
                          <div className="notification-body">
                            <h4 className="notification-title">{notification.title}</h4>
                            <p className="notification-message">{notification.content}</p>
                            <span className="notification-time">
                              {new Date(notification.createDate).toLocaleString()}
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="header-icon-badge">
            <i className="fas fa-comments header-icon"></i>
            {chatMessages > 0 && (
              <span className="header-badge">{chatMessages > 99 ? '99+' : chatMessages}</span>
            )}
          </div>

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

      <nav className="header-nav-bar">
        <div className="header-nav-container">
          {isAdmin ? (
            <>
              <a href="/admin" className="header-nav-item header-active">
                <i className="fas fa-home header-nav-icon"></i>
                Home
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
            <>
              <a href="/user" className="header-nav-item header-active">
                <i className="fas fa-home header-nav-icon"></i>
                Home
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
              <a href="/user/schedule" className="header-nav-item">
                <i className="fas fa-calendar header-nav-icon"></i>
                일정 관리
              </a>
              <a href="/user/facilities" className="header-nav-item">
                <i className="fas fa-building header-nav-icon"></i>
                시설 예약
              </a>
              <a href="/user/notifications" className="header-nav-item">
                <i className="fas fa-bell header-nav-icon"></i>
                모든 알림
              </a>
            </>
          )}
        </div>
      </nav>

      {showNotifications && (
        <div
          className="notification-overlay"
          onClick={() => setShowNotifications(false)}
        />
      )}

      <div className="toast-container">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`toast toast-${toast.type}`}
            onClick={() => removeToast(toast.id)}
          >
            <div className="toast-icon">
              <i className={`fas ${
                toast.type === 'success' ? 'fa-check-circle' :
                toast.type === 'error' ? 'fa-exclamation-circle' :
                toast.type === 'warning' ? 'fa-exclamation-triangle' :
                'fa-info-circle'
              }`}></i>
            </div>
            <div className="toast-content">
              <span className="toast-message">{toast.message}</span>
            </div>
            <div className="toast-close">
              <i className="fas fa-times"></i>
            </div>
          </div>
        ))}
      </div>
    </>
  );
};

export default Header;