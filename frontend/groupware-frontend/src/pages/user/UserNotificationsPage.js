import React, { useState, useEffect } from 'react';
import { useNotification } from '../../context/NotificationContext';
import { notificationApi } from '../../api/apiService';
import './UserNotificationsPage.css';

const UserNotificationsPage = ({ userData }) => {
  const { notificationList, markAsRead, markAllAsRead, fetchUnreadNotifications } = useNotification();
  const [allNotifications, setAllNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    type: '',
    keyword: ''
  });

  const fetchAllNotifications = async (page = 0) => {
    setLoading(true);
    try {
      const result = await notificationApi.getAllNotifications(page, 20, filters);
      if (result.success && result.data) {
        let responseData = result.data;
        
        if (responseData.data) {
          responseData = responseData.data;
        }
        
        if (responseData.content && Array.isArray(responseData.content)) {
          setAllNotifications(responseData.content);
          setTotalPages(responseData.totalPages || 1);
        } else if (Array.isArray(responseData)) {
          setAllNotifications(responseData);
          setTotalPages(1);
        } else {
          console.log('API 응답 구조:', responseData);
          setAllNotifications([]);
          setTotalPages(1);
        }
        setCurrentPage(page);
      } else {
        console.log('API 호출 실패:', result);
        setAllNotifications([]);
        setTotalPages(1);
      }
    } catch (error) {
      console.error('알림 목록 조회 오류:', error);
      setAllNotifications([]);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllNotifications(0);
  }, [filters]);

  useEffect(() => {
    if (Array.isArray(notificationList) && notificationList.length > 0) {
      setAllNotifications(prev => {
        const existingIds = new Set(prev.map(n => n.notificationNo));
        const newNotifications = notificationList.filter(n => !existingIds.has(n.notificationNo));
        return [...newNotifications, ...prev];
      });
    }
  }, [notificationList]);

  const handleMarkAsRead = async (notificationNo) => {
    try {
      await markAsRead(notificationNo);
      setAllNotifications(prev => {
        if (!Array.isArray(prev)) return [];
        return prev.map(notif => 
          notif.notificationNo === notificationNo 
            ? { ...notif, readDate: new Date().toISOString(), isRead: true }
            : notif
        );
      });
      await fetchUnreadNotifications();
    } catch (error) {
      console.error('알림 읽음 처리 오류:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      setAllNotifications(prev => {
        if (!Array.isArray(prev)) return [];
        return prev.map(notif => ({ 
          ...notif, 
          readDate: new Date().toISOString(), 
          isRead: true 
        }));
      });
      await fetchUnreadNotifications();
    } catch (error) {
      console.error('전체 읽음 처리 오류:', error);
    }
  };

  const getNotificationIcon = (type) => {
    const iconMap = {
      'NOTICE': 'fa-bullhorn',
      'SYSTEM': 'fa-cog',
      'APPROVAL': 'fa-check-circle',
      'SCHEDULE': 'fa-calendar',
      'FACILITY_APPROVED': 'fa-check',
      'FACILITY_REJECTED': 'fa-times',
      'TASK': 'fa-tasks',
      'CHAT': 'fa-comments',
      'MEETING': 'fa-users'
    };
    return iconMap[type] || 'fa-bell';
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR') + ' ' + date.toLocaleTimeString('ko-KR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  const safeNotifications = Array.isArray(allNotifications) ? allNotifications : [];

  return (
    <div className="user-notifications-page">
      <div className="page-header">
        <h1>
          <i className="fas fa-bell"></i>
          알림 센터
        </h1>
        <p>받은 알림을 확인하고 관리하세요</p>
      </div>

      <div className="notifications-controls">
        <div className="search-filters">
          <select 
            value={filters.type} 
            onChange={(e) => setFilters({...filters, type: e.target.value})}
            className="filter-select"
          >
            <option value="">모든 유형</option>
            <option value="NOTICE">공지사항</option>
            <option value="APPROVAL">결재</option>
            <option value="SCHEDULE">일정</option>
            <option value="FACILITY_APPROVED">시설 승인</option>
            <option value="FACILITY_REJECTED">시설 거절</option>
          </select>
          
          <input
            type="text"
            placeholder="알림 내용 검색..."
            value={filters.keyword}
            onChange={(e) => setFilters({...filters, keyword: e.target.value})}
            className="search-input"
          />
        </div>

        <div className="action-buttons">
          <button onClick={handleMarkAllAsRead} className="btn btn-secondary">
            <i className="fas fa-check-double"></i>
            모두 읽음 처리
          </button>
          <button onClick={() => fetchAllNotifications(0)} className="btn btn-primary">
            <i className="fas fa-sync-alt"></i>
            새로고침
          </button>
        </div>
      </div>

      <div className="notifications-list">
        {loading ? (
          <div className="loading-state">
            <div className="loading-spinner"></div>
            <p>알림을 불러오는 중...</p>
          </div>
        ) : safeNotifications.length > 0 ? (
          safeNotifications.map((notification) => (
            <div 
              key={notification.notificationNo}
              className={`notification-item ${notification.isRead || notification.readDate ? 'read' : 'unread'}`}
              onClick={() => !(notification.isRead || notification.readDate) && handleMarkAsRead(notification.notificationNo)}
            >
              <div className="notification-icon">
                <i className={`fas ${getNotificationIcon(notification.type)}`}></i>
              </div>
              
              <div className="notification-content">
                <div className="notification-header">
                  <h4 className="notification-title">{notification.title}</h4>
                  <span className="notification-date">
                    {formatDate(notification.createDate)}
                  </span>
                </div>
                
                <p className="notification-message">{notification.content}</p>
                
                <div className="notification-meta">
                  <span className={`notification-type type-${notification.type?.toLowerCase()}`}>
                    {notification.type}
                  </span>
                  <span className={`notification-status ${(notification.isRead || notification.readDate) ? 'read' : 'unread'}`}>
                    {(notification.isRead || notification.readDate) ? '읽음' : '읽지 않음'}
                  </span>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <i className="fas fa-bell-slash empty-icon"></i>
            <h3>알림이 없습니다</h3>
            <p>새로운 알림이 도착하면 여기에 표시됩니다.</p>
          </div>
        )}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button 
            onClick={() => fetchAllNotifications(currentPage - 1)}
            disabled={currentPage === 0}
            className="page-btn"
          >
            <i className="fas fa-chevron-left"></i>
          </button>
          
          {Array.from({ length: totalPages }, (_, i) => (
            <button
              key={i}
              onClick={() => fetchAllNotifications(i)}
              className={`page-btn ${currentPage === i ? 'active' : ''}`}
            >
              {i + 1}
            </button>
          ))}
          
          <button 
            onClick={() => fetchAllNotifications(currentPage + 1)}
            disabled={currentPage === totalPages - 1}
            className="page-btn"
          >
            <i className="fas fa-chevron-right"></i>
          </button>
        </div>
      )}
    </div>
  );
};

export default UserNotificationsPage;