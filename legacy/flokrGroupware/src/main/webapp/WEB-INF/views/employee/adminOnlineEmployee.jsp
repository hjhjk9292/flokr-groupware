<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>접속 사용자 관리</title>
  <!-- jQuery 라이브러리 -->
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <!-- 아이콘 사용을 위한 Font Awesome -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    .content-container {
      max-width: 1280px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
    }
    
    .page-title {
      margin-bottom: 1.5rem;
    }
    
    .page-title h1 {
      font-size: 1.75rem;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }
    
    .page-title p {
      color: #64748b;
      font-size: 0.95rem;
    }
    
    .card {
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
      padding: 1.5rem;
      margin-bottom: 1.5rem;
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }
    
    .card-title {
      font-size: 1.2rem;
      font-weight: 600;
    }
    
    .filter-bar {
      display: flex;
      gap: 1rem;
      margin-bottom: 1rem;
      flex-wrap: wrap;
    }
    
    .filter-select, .search-input {
      padding: 0.5rem;
      border: 1px solid #e2e8f0;
      border-radius: 4px;
    }
    
    .filter-select {
      min-width: 150px;
    }
    
    .search-input {
      flex-grow: 1;
      max-width: 300px;
    }
    
    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      background-color: #003561;
      color: white;
      cursor: pointer;
      transition: all 0.2s;
    }
    
    .btn:hover {
      background-color: #002a4c;
    }
    
    .btn-secondary {
      background-color: #64748b;
    }
    
    .btn-secondary:hover {
      background-color: #4b5563;
    }
    
    .btn-warning {
      background-color: #ff9500;
    }
    
    .btn-warning:hover {
      background-color: #e68200;
    }
    
    .btn-sm {
      padding: 0.25rem 0.5rem;
      font-size: 0.875rem;
    }
    
    .table {
      width: 100%;
      border-collapse: collapse;
    }
    
    .table th, .table td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid #e2e8f0;
    }
    
    .table th {
      font-weight: 600;
      color: #64748b;
      background-color: #f8fafc;
    }
    
    .table tbody tr:hover {
      background-color: #f1f5f9;
    }
    
    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 9999px;
      font-size: 0.75rem;
    }
    
    .status-online {
      background-color: #dcfce7;
      color: #166534;
    }
    
    .status-offline {
      background-color: #fee2e2;
      color: #b91c1c;
    }
    
    .refresh-info {
      font-size: 0.8rem;
      color: #64748b;
      text-align: right;
      margin-bottom: 0.5rem;
    }
    
    .empty-state {
      text-align: center;
      padding: 2rem;
      color: #64748b;
    }
    
    .empty-state i {
      font-size: 2rem;
      color: #e2e8f0;
      margin-bottom: 0.5rem;
    }
  </style>
</head>
<body>
  <jsp:include page="../common/header.jsp"/>
  
  <div class="content-container">
    <div class="page-title">
      <h1>접속 사용자 관리</h1>
      <p>현재 시스템에 접속 중인 사용자를 관리합니다.</p>
    </div>
    
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">접속 사용자 목록</h2>
        <button id="refreshBtn" class="btn">
          <i class="fas fa-sync-alt"></i> 새로고침
        </button>
      </div>
      
      <div class="refresh-info">마지막 갱신: <span id="lastRefreshTime"><fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd HH:mm:ss" /></span></div>
      
      <div class="filter-bar">
        <select id="departmentFilter" class="filter-select">
          <option value="">전체 부서</option>
          <c:forEach items="${departments}" var="dept">
            <option value="${dept.deptName}">${dept.deptName}</option>
          </c:forEach>
        </select>
        
        <select id="statusFilter" class="filter-select">
          <option value="">전체 상태</option>
          <option value="online">접속 중</option>
          <option value="offline">미접속</option>
        </select>
        
        <input type="text" id="searchInput" placeholder="사원명 또는 ID 검색" class="search-input">
        <button id="searchBtn" class="btn">검색</button>
      </div>
      
      <div class="table-responsive">
        <table class="table" id="onlineUsersTable">
          <thead>
            <tr>
              <th>사번</th>
              <th>사원명</th>
              <th>부서</th>
              <th>직급</th>
              <th>상태</th>
              <th>로그인 시간</th>
              <th>IP 주소</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <!-- 사용자 목록은 JavaScript로 동적 생성 -->
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 접속 로그 카드 -->
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">최근 접속 로그</h2>
      </div>
      
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>사번</th>
              <th>사원명</th>
              <th>활동 유형</th>
              <th>상세 내용</th>
              <th>IP 주소</th>
              <th>시간</th>
            </tr>
          </thead>
          <tbody>
            <c:choose>
              <c:when test="${not empty recentLogs}">
                <c:forEach items="${recentLogs}" var="log">
                  <tr>
                    <td>${log.empId}</td>
                    <td>${log.empName}</td>
                    <td>
                      <c:choose>
                        <c:when test="${log.actionType eq 'login'}">
                          <span class="status-badge status-online">로그인</span>
                        </c:when>
                        <c:when test="${log.actionType eq 'logout'}">
                          <span class="status-badge status-offline">로그아웃</span>
                        </c:when>
                        <c:otherwise>
                          <span class="status-badge">${log.actionType}</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td>${log.description}</td>
                    <td>${log.ipAddress}</td>
                    <td><fmt:formatDate value="${log.actionTime}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                  </tr>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <tr>
                  <td colspan="6" class="empty-state">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>접속 로그가 없습니다.</p>
                  </td>
                </tr>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  
  <script>
    $(document).ready(function() {
      // 페이지 로드 시 접속자 목록 로드
      loadOnlineUsers();
      
      // 새로고침 버튼 클릭
      $('#refreshBtn').click(function() {
        loadOnlineUsers();
      });
      
      // 검색 버튼 클릭
      $('#searchBtn').click(function() {
        filterUsers();
      });
      
      // 엔터키 검색
      $('#searchInput').keypress(function(e) {
        if (e.which === 13) {
          filterUsers();
        }
      });
      
      // 필터 변경 시 자동 필터링
      $('#departmentFilter, #statusFilter').change(function() {
        filterUsers();
      });
      
      // 접속 사용자 목록 로드
      function loadOnlineUsers() {
        $.ajax({
          url: '${pageContext.request.contextPath}/getOnlineEmployee',
          type: 'GET',
          dataType: 'json',
          success: function(users) {
            renderUsersList(users);
            updateLastRefreshTime();
          },
          error: function(xhr, status, error) {
            console.error('접속자 목록을 가져오는데 실패했습니다. 오류: ' + error);
            $('#onlineUsersTable tbody').html('<tr><td colspan="8" class="empty-state"><i class="fas fa-exclamation-circle"></i><p>접속자 목록을 불러올 수 없습니다. 오류: ' + error + '</p></td></tr>');
          }
        });
      }
      
      // 사용자 목록 렌더링
      function renderUsersList(users) {
        const $tbody = $('#onlineUsersTable tbody');
        $tbody.empty();
        
        if (!users || users.length === 0) {
          $tbody.html('<tr><td colspan="8" class="empty-state"><i class="fas fa-users-slash"></i><p>접속 중인 사용자가 없습니다.</p></td></tr>');
          return;
        }
        
        $.each(users, function(index, user) {
          const statusClass = user.status === 'online' ? 'status-online' : 'status-offline';
          const statusText = user.status === 'online' ? '접속 중' : '미접속';
          
          const $tr = $('<tr>');
          $tr.append($('<td>').text(user.empId));
          $tr.append($('<td>').text(user.empName));
          $tr.append($('<td>').text(user.deptName));
          $tr.append($('<td>').text(user.positionName));
          
          // 상태 배지
          const $statusBadge = $('<span>')
            .addClass('status-badge ' + statusClass)
            .text(statusText);
          $tr.append($('<td>').append($statusBadge));
          
          // 로그인 시간 (날짜 포맷팅)
          let loginTime = '-';
          if (user.lastLoginTime) {
            const date = new Date(user.lastLoginTime);
            loginTime = date.toLocaleString();
          }
          $tr.append($('<td>').text(loginTime));
          
          $tr.append($('<td>').text(user.ipAddress || '-'));
          
          // 관리 버튼
          const $actions = $('<td>');
          if (user.status === 'online') {
            const $logoutBtn = $('<button>')
              .addClass('btn btn-warning btn-sm')
              .html('<i class="fas fa-sign-out-alt"></i> 로그아웃')
              .click(function() {
                if (confirm(user.empName + ' 사용자를 강제 로그아웃 하시겠습니까?')) {
                  logoutUser(user.empId);
                }
              });
            $actions.append($logoutBtn);
          }
          $tr.append($actions);
          
          $tbody.append($tr);
        });
        
        // 필터링 적용
        filterUsers();
      }
      
      // 사용자 필터링
      function filterUsers() {
        const keyword = $('#searchInput').val().toLowerCase();
        const deptFilter = $('#departmentFilter').val();
        const statusFilter = $('#statusFilter').val();
        
        $('#onlineUsersTable tbody tr').each(function() {
          const $row = $(this);
          
          // 빈 상태 메시지는 건너뜀
          if ($row.find('td.empty-state').length > 0) {
            return;
          }
          
          const empId = $row.find('td:eq(0)').text().toLowerCase();
          const empName = $row.find('td:eq(1)').text().toLowerCase();
          const deptName = $row.find('td:eq(2)').text();
          const status = $row.find('td:eq(4) span').text() === '접속 중' ? 'online' : 'offline';
          
          const keywordMatch = !keyword || empId.includes(keyword) || empName.includes(keyword);
          const deptMatch = !deptFilter || deptName === deptFilter;
          const statusMatch = !statusFilter || status === statusFilter;
          
          if (keywordMatch && deptMatch && statusMatch) {
            $row.show();
          } else {
            $row.hide();
          }
        });
      }
      
      // 사용자 강제 로그아웃
      function logoutUser(empId) {
        $.ajax({
          url: '${pageContext.request.contextPath}/logoutEmployee',
          type: 'POST',
          data: { empId: empId },
          dataType: 'json',
          success: function(response) {
            if (response.success) {
              alert(response.message);
              loadOnlineUsers();
            } else {
              alert(response.message || '로그아웃 처리 중 오류가 발생했습니다.');
            }
          },
          error: function(xhr, status, error) {
            alert('로그아웃 처리 중 오류가 발생했습니다. 오류: ' + error);
          }
        });
      }
      
      // 마지막 새로고침 시간 업데이트
      function updateLastRefreshTime() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        
        const formattedTime = year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
        $('#lastRefreshTime').text(formattedTime);
      }
      
      // 자동 새로고침 (30초마다)
      setInterval(loadOnlineUsers, 30000);
    });
  </script>
</body>
</html>