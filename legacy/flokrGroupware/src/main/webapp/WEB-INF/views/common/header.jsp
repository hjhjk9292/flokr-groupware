<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/alertify.min.js"></script>
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/alertify.min.css"/>
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/themes/default.min.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/header.css">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">

<!-- SockJS와 STOMP 라이브러리 추가 -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.5/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script> 

<c:if test="${ not empty alertMsg }">
    <script>
        alertify.alert("${ alertMsg }");
    </script>
    <c:remove var="alertMsg" scope="session"/> </c:if>

<header class="header-top">
    <div class="header-logo">
        <c:choose>
            <c:when test="${not empty loginUser and loginUser.isAdmin eq 'Y'}">
                <a href="${pageContext.request.contextPath}/adminMain" style="display: flex; align-items: center; text-decoration: none; color: inherit;">
            </c:when>
            <c:when test="${not empty loginUser and loginUser.isAdmin eq 'N'}">
                <a href="${pageContext.request.contextPath}/userMain" style="display: flex; align-items: center; text-decoration: none; color: inherit;">
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/" style="display: flex; align-items: center; text-decoration: none; color: inherit;">
            </c:otherwise>
        </c:choose>
            <img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="Flokr" class="header-logo-img">
            Flokr
        </a>
    </div>

    <div class="header-right-section">
        <a href="chatList.ch">
            <div class="header-icon-badge">
                <svg class="header-icon" viewBox="0 0 24 24" fill="none" stroke="#555" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
                <span id="header-chat-badge" class="header-badge" style="display: none;">0</span>
            </div>
        </a>

        <!-- 알림 드롭다운 -->
        <div class="header-icon-badge header-notification-wrapper">
            <button id="header-notification-btn" class="header-notification-btn">
                <svg class="header-icon" viewBox="0 0 24 24" fill="none" stroke="#555" stroke-width="2">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                </svg>
                <span id="header-notification-badge" class="header-notification-badge" style="display: none;">0</span>
            </button>
            <div id="header-notification-dropdown" class="header-notification-dropdown">
                <div class="header-notification-header">
                    <h3>알림</h3>
                    <a href="${pageContext.request.contextPath}/notificationAll" class="header-all-notifications">모두 보기</a>
                </div>
                <ul id="header-notification-list" class="header-notification-list">
                    <li class="header-empty-notification">새로운 알림이 없습니다.</li>
                </ul>
            </div>
        </div>

        <c:if test="${not empty loginUser}">
            <div class="header-profile">
                <c:choose>
                    <c:when test="${not empty loginUser.profileImgPath}"> <%-- loginUser 변수 사용 --%>
                        <img src="${loginUser.profileImgPath}" alt="프로필" class="header-profile-img"> <%-- loginUser 변수 사용 --%>
                    </c:when>
					<c:otherwise>
					    <%-- 기본적인 색상 배열 (스크립틀릿 사용) --%>
					    <% 
					        String[] colors = {"#4285f4", "#34a853", "#ea4335", "#fbbc05", "#9c27b0"};
					        String empName = ((com.kh.flokrGroupware.employee.model.vo.Employee)session.getAttribute("loginUser")).getEmpName();
					        char firstChar = empName.charAt(0);
					        int colorIndex = Math.abs(firstChar) % 5; // char를 int로 자동 변환하여 인덱스 계산
					        pageContext.setAttribute("bgColor", colors[colorIndex]);
					        pageContext.setAttribute("firstChar", String.valueOf(firstChar));
					    %>
					    
					    <svg class="header-profile-img" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
					        <circle cx="20" cy="20" r="20" fill="${bgColor}" />
					        <text x="20" y="22" font-family="Arial, 'Malgun Gothic', sans-serif" font-size="16" fill="white" text-anchor="middle" dominant-baseline="middle">${firstChar}</text>
					    </svg>
					</c:otherwise>
                </c:choose>
                <div class="header-profile-info">
                    <span class="header-profile-name">${loginUser.empName}님</span>
                </div>
                <a href="${pageContext.request.contextPath}/logout.me" class="header-btn-sm header-logout-btn">로그아웃</a>
            </div>
        </c:if>
    </div>
</header>

<nav class="header-nav-bar">
    <div class="header-nav-container">
        <c:choose>
            <c:when test="${loginUser.isAdmin eq 'Y'}">
                <a href="${pageContext.request.contextPath}/adminMain" class="header-nav-item ${currentMenu eq 'home' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                    </svg>
                    Home
                </a>
                <a href="${pageContext.request.contextPath}/adminOrganization" class="header-nav-item ${currentMenu eq 'organization' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    조직 관리
                </a>
                <a href="${pageContext.request.contextPath}/employeeList" class="header-nav-item ${currentMenu eq 'empRegister' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    사원 관리
                </a>
                <a href="${pageContext.request.contextPath}/noticeList" class="header-nav-item ${currentMenu eq 'notice' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                        <line x1="8" y1="21" x2="16" y2="21"></line>
                        <line x1="12" y1="17" x2="12" y2="21"></line>
                    </svg>
                    사내 공지 관리
                </a>
                <a href="${pageContext.request.contextPath}/adminOnlineEmployee" class="header-nav-item ${currentMenu eq 'onlineUsers' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </svg>
                    접속 사용자 관리
                </a>
                <a href="${pageContext.request.contextPath}/adminEmployeeManagement" class="header-nav-item ${currentMenu eq 'users' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    사용자 정보 관리
                </a>
                <a href="${pageContext.request.contextPath}/adminFacility" class="header-nav-item ${currentMenu eq 'facility' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="3" y1="9" x2="21" y2="9"></line>
                        <line x1="9" y1="21" x2="9" y2="9"></line>
                    </svg>
                    시설 관리
                </a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/userMain" class="header-nav-item ${currentMenu eq 'home' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                    </svg>
                    Home
                </a>
                <a href="${pageContext.request.contextPath}/task/list" class="header-nav-item ${currentMenu eq 'task' ? 'active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    업무 관리
                </a>
                <a href="${pageContext.request.contextPath}/calendar.sc" class="header-nav-item ${currentMenu eq 'schedule' ? 'active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    일정
                </a>
                <a href="${pageContext.request.contextPath}/attendance/main" class="header-nav-item ${currentMenu eq 'attendance' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect>
                        <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path>
                        <line x1="17.5" y1="6.5" x2="17.5" y2="6.5"></line>
                    </svg>
                    근태 관리
                </a>
                <a href="${pageContext.request.contextPath}/address" class="header-nav-item ${currentMenu eq 'address' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                    </svg>
                    주소록
                </a>
                <a href="${pageContext.request.contextPath}/main.ap" class="header-nav-item ${currentMenu eq 'approval' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="9 11 12 14 22 4"></polyline>
                        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
                    </svg>
                    전자 결재
                </a>
                <a href="${pageContext.request.contextPath}/facilityReservation" class="header-nav-item ${currentMenu eq 'facility' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="3" y1="9" x2="21" y2="9"></line>
                        <line x1="9" y1="21" x2="9" y2="9"></line>
                    </svg>
                    시설 예약
                </a>
                <a href="${pageContext.request.contextPath}/noticeList" class="header-nav-item ${currentMenu eq 'notice' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                        <line x1="8" y1="21" x2="16" y2="21"></line>
                        <line x1="12" y1="17" x2="12" y2="21"></line>
                    </svg>
                    공지사항
                </a>
                <a href="${pageContext.request.contextPath}/help" class="header-nav-item ${currentMenu eq 'help' ? 'header-active' : ''}">
                    <svg class="header-nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                    Help
                </a>
            </c:otherwise>
        </c:choose>

        
    </div>
</nav>

<!-- 사용자 정보 히든 필드 추가 -->
<input type="hidden" id="contextPath" value="${pageContext.request.contextPath}" />
<c:if test="${not empty loginUser}">
    <input type="hidden" id="loginUserId" value="${loginUser.empId}" />
    <input type="hidden" id="userDeptNo" value="${loginUser.deptNo}" />
    <input type="hidden" id="loginUserEmpNo" value="${loginUser.empNo}" />
    <input type="hidden" id="loginUserIsAdmin" value="${loginUser.isAdmin}">
</c:if>

<!-- 알림 스크립트 -->
<script src="${pageContext.request.contextPath}/resources/js/notification.js"></script>

<!-- 채팅 알림 뱃지 스크립트 -->
<script src="${pageContext.request.contextPath}/resources/js/chatBadge.js"></script>