// src/pages/user/UserDashboardPage.js
import React, { useState, useEffect } from 'react';
import './UserDashboard.css';

const UserDashboard = ({ userData, onLogout }) => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [attendance, setAttendance] = useState({
    clockInTime: null,
    clockOutTime: null,
    status: 'OUT'
  });

  // 동적 데이터 상태
  const [dashboardData, setDashboardData] = useState({
    user: {
      name: userData?.empName || '사용자',
      department: userData?.departmentName || '개발팀',
      position: userData?.positionName || '팀원',
      avatar: userData?.avatar || null,
      status: 'online'
    },
    tasks: [
      {
        id: 1,
        title: '사용자 데이터 시각화',
        createDate: '2025/07/20',
        deadline: '2025/07/25',
        status: '진행중',
        priority: 'high'
      },
      {
        id: 2,
        title: '리액트 컴포넌트 개발',
        createDate: '2025/07/19',
        deadline: '2025/07/26',
        status: '진행중',
        priority: 'medium'
      },
      {
        id: 3,
        title: 'UI 디자인 원칙 및 컴포넌트',
        createDate: '2025/07/18',
        deadline: '2025/07/24',
        status: '완료',
        priority: 'low'
      }
    ],
    notices: [
      {
        id: 1,
        title: '7월 정기 회의 안내',
        writer: '관리자',
        date: '2025-07-22',
        isNew: true
      },
      {
        id: 2,
        title: '여름휴가 신청 마감 안내',
        writer: 'HR팀',
        date: '2025-07-21',
        isNew: true
      },
      {
        id: 3,
        title: '보안 교육 이수 안내',
        writer: '보안팀',
        date: '2025-07-20',
        isNew: false
      }
    ],
    teamMembers: [
      {
        id: 1,
        name: '홍길동',
        position: 'Senior Developer',
        grade: '선임',
        status: 'online',
        profileImg: null,
        department: '프론트엔드팀'
      },
      {
        id: 2,
        name: '최민준',
        position: 'UI/UX Designer',
        grade: '주임',
        status: 'away',
        profileImg: null,
        department: '디자인팀'
      },
      {
        id: 3,
        name: '유재석',
        position: 'Project Manager',
        grade: '팀장',
        status: 'online',
        profileImg: null,
        department: '기획팀'
      }
    ],
    calendar: {
      currentMonth: new Date().getMonth() + 1,
      currentYear: new Date().getFullYear(),
      events: [
        {
          date: 22,
          title: '팀 회의',
          type: 'meeting',
          color: '#007bff'
        },
        {
          date: 25,
          title: '프로젝트 마감',
          type: 'deadline',
          color: '#dc3545'
        },
        {
          date: 28,
          title: '생일파티',
          type: 'event',
          color: '#28a745'
        }
      ]
    }
  });

  // 시간 업데이트
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // 데이터 로딩 시뮬레이션 (실제로는 API 호출)
  useEffect(() => {
    const loadDashboardData = async () => {
      // TODO: 실제 API 호출로 대체
      try {
        // const response = await fetch('/api/dashboard');
        // const data = await response.json();
        // setDashboardData(data);
        
        // 현재는 시뮬레이션
        console.log('대시보드 데이터 로딩 완료');
      } catch (error) {
        console.error('대시보드 데이터 로딩 실패:', error);
      }
    };

    loadDashboardData();
  }, []);

  // 유틸리티 함수들
  const formatTime = (date) => {
    return date.toLocaleTimeString('ko-KR', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    });
  };

  const formatDate = (date) => {
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const getInitials = (name) => {
    return name.split(' ').map(n => n.charAt(0)).join('').toUpperCase();
  };

  const getStatusColor = (status) => {
    const statusColors = {
      '진행중': 'status-inprogress',
      '완료': 'status-completed',
      '대기': 'status-pending',
      '보류': 'status-hold'
    };
    return statusColors[status] || 'status-pending';
  };

  const getPriorityColor = (priority) => {
    const priorityColors = {
      'high': '#dc3545',
      'medium': '#ffc107',
      'low': '#28a745'
    };
    return priorityColors[priority] || '#6c757d';
  };

  const getStatusIndicator = (status) => {
    const statusClasses = {
      'online': 'online',
      'away': 'away',
      'busy': 'busy',
      'offline': 'offline'
    };
    return statusClasses[status] || 'offline';
  };

  // 출퇴근 핸들러
  const handleClockIn = async () => {
    const now = new Date();
    try {
      // TODO: API 호출
      // await fetch('/api/attendance/clock-in', { method: 'POST' });
      setAttendance(prev => ({
        ...prev,
        clockInTime: now,
        status: 'IN'
      }));
      console.log('출근 처리됨:', formatTime(now));
    } catch (error) {
      console.error('출근 처리 실패:', error);
    }
  };

  const handleClockOut = async () => {
    const now = new Date();
    try {
      // TODO: API 호출
      // await fetch('/api/attendance/clock-out', { method: 'POST' });
      setAttendance(prev => ({
        ...prev,
        clockOutTime: now,
        status: 'OUT'
      }));
      console.log('퇴근 처리됨:', formatTime(now));
    } catch (error) {
      console.error('퇴근 처리 실패:', error);
    }
  };

  // 태스크 상태 업데이트
  const updateTaskStatus = async (taskId, newStatus) => {
    try {
      // TODO: API 호출
      setDashboardData(prev => ({
        ...prev,
        tasks: prev.tasks.map(task =>
          task.id === taskId ? { ...task, status: newStatus } : task
        )
      }));
      console.log(`태스크 ${taskId} 상태가 ${newStatus}로 변경됨`);
    } catch (error) {
      console.error('태스크 상태 업데이트 실패:', error);
    }
  };

  // 캘린더 렌더링
  const renderCalendar = () => {
    const { currentMonth, currentYear, events } = dashboardData.calendar;
    const today = new Date().getDate();
    const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();
    
    return (
      <div className="user-calendar-placeholder">
        <h3>{currentMonth}월 {currentYear}</h3>
        <div className="user-calendar-grid">
          <div className="user-calendar-header">
            {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(day => (
              <span key={day}>{day}</span>
            ))}
          </div>
          <div className="user-calendar-body">
            {Array.from({length: daysInMonth}, (_, i) => {
              const day = i + 1;
              const dayEvents = events.filter(event => event.date === day);
              const isToday = day === today;
              
              return (
                <div 
                  key={day} 
                  className={`user-calendar-day ${isToday ? 'today' : ''}`}
                >
                  {day}
                  {dayEvents.map((event, idx) => (
                    <div 
                      key={idx}
                      className="user-calendar-event"
                      style={{ backgroundColor: event.color }}
                    >
                      {event.title}
                    </div>
                  ))}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="user-dashboard-container">
      {/* 메인 콘텐츠 */}
      <div className="user-dashboard-main">
        {/* 왼쪽 컬럼 */}
        <div className="user-left-column">
          {/* 프로필 + 출퇴근 통합 섹션 */}
          <div className="user-profile-attendance-section">
            {/* 프로필 영역 */}
            <div className="user-profile-area">
              <div className="user-profile-avatar">
                {getInitials(dashboardData.user.name)}
                <span className={`user-status-indicator ${getStatusIndicator(dashboardData.user.status)}`}></span>
              </div>
              <p className="user-profile-name">{dashboardData.user.name}</p>
              <p className="user-profile-title">
                {dashboardData.user.department} {dashboardData.user.position}
              </p>
              <div className="user-profile-buttons">
                <button className="user-btn-small active">OFFICE</button>
              </div>
            </div>

            {/* 구분선 */}
            <div className="user-section-divider"></div>

            {/* 출퇴근 영역 */}
            <div className="user-attendance-area">
              <p className="user-current-date">{formatDate(currentTime)}</p>
              <div className="user-time-info">
                <div className="user-time-item">
                  <span className="user-time-label">출근 시간</span>
                  <span className="user-time-value">
                    {attendance.clockInTime ? formatTime(attendance.clockInTime) : '-- : --'}
                  </span>
                </div>
                <div className="user-time-item">
                  <span className="user-time-label">퇴근 시간</span>
                  <span className="user-time-value">
                    {attendance.clockOutTime ? formatTime(attendance.clockOutTime) : '-- : --'}
                  </span>
                </div>
              </div>
              <div className="user-action-buttons">
                <button 
                  className="user-btn-large user-btn-checkin" 
                  onClick={handleClockIn}
                  disabled={attendance.status === 'IN'}
                >
                  출근
                </button>
                <button 
                  className="user-btn-large user-btn-checkout" 
                  onClick={handleClockOut}
                  disabled={attendance.status === 'OUT'}
                >
                  퇴근
                </button>
              </div>
            </div>
          </div>

          {/* 업무 목록 섹션 */}
          <div className="user-tasks-section">
            <div className="user-section-title">
              업무 목록
            </div>
            <div className="user-tasks-table">
              <table>
                <thead>
                  <tr>
                    <th>업무 제목</th>
                    <th>생성일</th>
                    <th>마감일</th>
                    <th>상태</th>
                    <th>우선순위</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboardData.tasks.map(task => (
                    <tr key={task.id}>
                      <td>{task.title}</td>
                      <td>{task.createDate}</td>
                      <td>{task.deadline}</td>
                      <td>
                        <span 
                          className={`user-status ${getStatusColor(task.status)}`}
                          onClick={() => updateTaskStatus(task.id, task.status === '진행중' ? '완료' : '진행중')}
                          style={{ cursor: 'pointer' }}
                        >
                          {task.status}
                        </span>
                      </td>
                      <td>
                        <span 
                          className="user-priority-indicator"
                          style={{ 
                            color: getPriorityColor(task.priority),
                            fontWeight: 'bold'
                          }}
                        >
                          {task.priority.toUpperCase()}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* 가운데 컬럼 */}
        <div className="user-center-column">
          {/* 캘린더 섹션 */}
          <div className="user-calendar-section">
            {renderCalendar()}
          </div>

          {/* 공지사항 섹션 */}
          <div className="user-notices-section">
            <div className="user-section-title">
              회사 공지
            </div>
            <div className="user-notices-table">
              <table>
                <thead>
                  <tr>
                    <th width="60%">TITLE</th>
                    <th width="20%">WRITER</th>
                    <th width="20%">DATE</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboardData.notices.map(notice => (
                    <tr key={notice.id}>
                      <td>
                        <div className="user-notice-title">
                          {notice.isNew && <span className="user-new-badge">NEW</span>}
                          {notice.title}
                        </div>
                      </td>
                      <td>{notice.writer}</td>
                      <td>{notice.date}</td>
                      <td>
                        <span 
                          className="user-priority-dot"
                          style={{ backgroundColor: getPriorityColor(notice.priority) }}
                        ></span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* 오른쪽 컬럼 */}
        <div className="user-right-column">
          <div className="user-section-title">
            팀원 주소록
          </div>
          <div className="user-team-table">
            <table>
              <thead>
                <tr>
                  <th>NAME</th>
                  <th>GRADE</th>
                  <th>CHAT</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {dashboardData.teamMembers.map(member => (
                  <tr key={member.id}>
                    <td>
                      <div className="user-member-info">
                        <div className="user-member-avatar">
                          {getInitials(member.name)}
                          <span className={`user-member-status ${getStatusIndicator(member.status)}`}></span>
                        </div>
                        <div className="user-member-text">
                          <span className="user-member-name">{member.name}</span>
                          <span className="user-member-position">{member.position}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="user-grade-badge">{member.grade}</span>
                    </td>
                    <td>
                      <i className="fas fa-comments user-chat-icon"></i>
                    </td>
                    <td>
                      <span className="user-options-icon">⋮</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;