<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>사용자 정보 관리</title>
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
    
    .flex-container {
      display: flex;
      gap: 2rem;
      min-height: 600px;
    }
    
    .user-list-sidebar {
      width: 300px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
      padding: 1.5rem;
      height: 700px;
      overflow: auto;
      flex-shrink: 0;
    }
    
    .user-detail-container {
      flex-grow: 1;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
      padding: 1.5rem;
      height: fit-content;
    }
    
    .sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }
    
    .sidebar-title {
      font-size: 1.2rem;
      font-weight: 600;
    }
    
    .filter-bar {
      margin-bottom: 1rem;
    }
    
    .filter-select, .search-input {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #e2e8f0;
      border-radius: 4px;
      margin-bottom: 0.5rem;
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
    
    .btn-danger {
      background-color: #dc2626;
    }
    
    .btn-danger:hover {
      background-color: #b91c1c;
    }
    
    .btn-success {
      background-color: #10b981;
    }
    
    .btn-success:hover {
      background-color: #059669;
    }
    
    .user-list {
      list-style-type: none;
      padding: 0;
      margin: 0;
    }
    
    .user-item {
      padding: 0.75rem;
      border-radius: 4px;
      cursor: pointer;
      border-bottom: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
    }
    
    .user-item:hover {
      background-color: #f1f5f9;
    }
    
    .user-item.selected {
      background-color: #e2e8f0;
    }
    
    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: #cbd5e1;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 0.75rem;
      color: #475569;
      font-weight: 600;
    }
    
    .user-info {
      flex-grow: 1;
    }
    
    .user-name {
      font-weight: 600;
      color: #334155;
    }
    
    .user-position {
      font-size: 0.8rem;
      color: #64748b;
    }
    
    .admin-badge {
      display: inline-block;
      background-color: #fef3c7;
      color: #b45309;
      padding: 0.1rem 0.4rem;
      border-radius: 9999px;
      font-size: 0.7rem;
      margin-left: 0.5rem;
    }
    
    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      margin-top: 0.25rem;
    }
    
    .status-active {
      background-color: #dcfce7;
      color: #166534;
    }
    
    .status-inactive {
      background-color: #fee2e2;
      color: #b91c1c;
    }
    
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 3rem 1rem;
      color: #64748b;
    }
    
    .empty-state i {
      font-size: 3rem;
      margin-bottom: 1rem;
      color: #cbd5e1;
    }
    
    .detail-header {
      display: flex;
      align-items: center;
      margin-bottom: 2rem;
      padding-bottom: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
    }
    
    .detail-avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background-color: #cbd5e1;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 1.5rem;
      color: #475569;
      font-size: 2rem;
      font-weight: 600;
    }
    
    .detail-title {
      flex-grow: 1;
    }
    
    .detail-name {
      font-size: 1.5rem;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 0.25rem;
    }
    
    .detail-position {
      font-size: 1.1rem;
      color: #64748b;
    }
    
    .detail-content {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 2rem;
    }
    
    .detail-section {
      margin-bottom: 1.5rem;
    }
    
    .detail-section h3 {
      font-size: 1.1rem;
      font-weight: 600;
      color: #475569;
      margin-bottom: 1rem;
      padding-bottom: 0.5rem;
      border-bottom: 1px solid #e2e8f0;
    }
    
    .detail-item {
      margin-bottom: 1rem;
    }
    
    .detail-label {
      font-size: 0.9rem;
      color: #64748b;
      margin-bottom: 0.25rem;
    }
    
    .detail-value {
      font-size: 1rem;
      color: #334155;
    }
    
    .action-buttons {
      display: flex;
      gap: 0.75rem;
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid #e2e8f0;
    }
    
    .modal {
      display: none;
      position: fixed;
      z-index: 1000;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
    }
    
    .modal-content {
      background-color: #fff;
      margin: 10% auto;
      padding: 2rem;
      border-radius: 8px;
      width: 500px;
      max-width: 90%;
    }
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }
    
    .modal-title {
      font-size: 1.2rem;
      font-weight: 600;
      color: #1e293b;
    }
    
    .close {
      font-size: 1.5rem;
      font-weight: 600;
      color: #64748b;
      cursor: pointer;
    }
    
    .close:hover {
      color: #1e293b;
    }
    
    .form-group {
      margin-bottom: 1rem;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #475569;
    }
    
    .form-control {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #cbd5e1;
      border-radius: 0.25rem;
    }
    
    .form-row {
      display: flex;
      gap: 1rem;
    }
    
    .form-row .form-group {
      flex: 1;
    }
    
    .radio-group {
      display: flex;
      gap: 1.5rem;
    }
    
    .radio-label {
      display: flex;
      align-items: center;
    }
    
    .radio-label input {
      margin-right: 0.5rem;
    }
    
    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1.5rem;
    }
  </style>
</head>
<body>
  <jsp:include page="../common/header.jsp"/>
  
  <div class="content-container">
    <div class="page-title">
      <h1>사용자 정보 관리</h1>
      <p>계정 정보와 권한을 관리합니다.</p>
    </div>
    
    <div class="flex-container">
      <!-- 사용자 목록 사이드바 -->
      <div class="user-list-sidebar">
        <div class="sidebar-header">
          <h2 class="sidebar-title">사용자 목록</h2>
          <button id="refreshBtn" class="btn btn-secondary">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
        
        <div class="filter-bar">
          <select id="departmentFilter" class="filter-select">
            <option value="">전체 부서</option>
            <c:forEach items="${departments}" var="dept">
              <option value="${dept.deptName}">${dept.deptName}</option>
            </c:forEach>
          </select>
          
          <select id="statusFilter" class="filter-select">
            <option value="">전체 상태</option>
            <option value="Y">활성</option>
            <option value="N">비활성</option>
          </select>
          
          <input type="text" id="searchInput" placeholder="사원명 또는 ID 검색" class="search-input">
        </div>
        
        <ul id="userList" class="user-list">
          <!-- 사용자 목록은 JavaScript로 동적 생성 -->
        </ul>
      </div>
      
      <!-- 사용자 상세 정보 영역 -->
      <div class="user-detail-container">
        <div id="userDetail">
          <!-- 초기 상태: 사용자 선택 안내 -->
          <div class="empty-state">
            <i class="fas fa-user"></i>
            <h3>사용자 정보를 확인하려면</h3>
            <p>왼쪽의 사용자 목록에서 사용자를 선택하세요.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 사용자 정보 수정 모달 -->
  <div id="editModal" class="modal">
    <div class="modal-content">
      <div class="modal-header">
        <h3 class="modal-title">사용자 정보 수정</h3>
        <span class="close">&times;</span>
      </div>
      
      <form id="editForm">
        <input type="hidden" id="empNo" name="empNo">
        
        <div class="form-row">
          <div class="form-group">
            <label for="empId">사번</label>
            <input type="text" id="empId" name="empId" class="form-control" readonly>
          </div>
          
          <div class="form-group">
            <label for="empName">사원명</label>
            <input type="text" id="empName" name="empName" class="form-control" required>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label for="deptNo">부서</label>
            <select id="deptNo" name="deptNo" class="form-control" required>
              <c:forEach items="${departments}" var="dept">
                <option value="${dept.deptNo}">${dept.deptName}</option>
              </c:forEach>
            </select>
          </div>
          
          <div class="form-group">
            <label for="positionNo">직급</label>
            <select id="positionNo" name="positionNo" class="form-control" required>
              <c:forEach items="${positions}" var="position">
                <option value="${position.positionNo}">${position.positionName}</option>
              </c:forEach>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label for="email">이메일</label>
          <input type="email" id="email" name="email" class="form-control" required>
        </div>
        
        <div class="form-group">
          <label for="phone">연락처</label>
          <input type="text" id="phone" name="phone" class="form-control">
        </div>
        
        <div class="form-group">
          <label>상태</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" name="status" value="Y" required> 활성
            </label>
            <label class="radio-label">
              <input type="radio" name="status" value="N"> 비활성
            </label>
          </div>
        </div>
        
        <div class="form-group">
          <label>관리자 권한</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" name="isAdmin" value="Y" required> 부여
            </label>
            <label class="radio-label">
              <input type="radio" name="isAdmin" value="N"> 미부여
            </label>
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary close-btn">취소</button>
          <button type="submit" class="btn">저장</button>
        </div>
      </form>
    </div>
  </div>
  
  <!-- 비밀번호 초기화 확인 모달 -->
  <div id="resetPasswordModal" class="modal">
    <div class="modal-content">
      <div class="modal-header">
        <h3 class="modal-title">비밀번호 초기화</h3>
        <span class="close">&times;</span>
      </div>
      
      <p><span id="resetPasswordUserName"></span> 사용자의 비밀번호를 초기화하시겠습니까?</p>
      <p>초기 비밀번호는 <strong id="initialPassword"></strong> 입니다.</p>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary close-btn">취소</button>
        <button type="button" id="confirmResetBtn" class="btn btn-warning">초기화</button>
      </div>
    </div>
  </div>
  
  <!-- 비밀번호 초기화 중 로딩 모달 -->
  <div id="loadingModal" class="modal">
    <div class="modal-content" style="width: 300px; text-align: center;">
      <div style="padding: 20px;">
        <i class="fas fa-spinner fa-spin" style="font-size: 2rem; margin-bottom: 15px;"></i>
        <p>처리 중입니다. 잠시만 기다려주세요...</p>
      </div>
    </div>
  </div>
  
  <script>
    $(document).ready(function() {
      // 페이지 로드 시 사용자 목록 로드
      loadEmployees();
      
      // 새로고침 버튼 클릭
      $('#refreshBtn').click(function() {
        loadEmployees();
      });
      
      // 검색 필터링
      $('#searchInput').on('keyup', function() {
        filterUsers();
      });
      
      // 부서, 상태 필터 변경 시 필터링
      $('#departmentFilter, #statusFilter').on('change', function() {
        filterUsers();
      });
      
      // 모달 닫기 버튼 클릭
      $('.close, .close-btn').click(function() {
        closeAllModals();
      });
      
      // 사용자 정보 수정 폼 제출
      $('#editForm').on('submit', function(e) {
        e.preventDefault();
        updateUserInfo();
      });
      
      // 비밀번호 초기화 확인 버튼 클릭
      $('#confirmResetBtn').click(function() {
        resetUserPassword();
      });
      
      // ESC 키로 모달 닫기
      $(document).keydown(function(e) {
        if (e.keyCode === 27) {
          closeAllModals();
        }
      });
      
      // 모달 외부 클릭 시 닫기
      $(window).click(function(e) {
        if ($(e.target).hasClass('modal')) {
          closeAllModals();
        }
      });
    });
    
    // 모든 모달 닫기
    function closeAllModals() {
      $('.modal').hide();
    }
    
    // 로딩 모달 표시/숨김
    function showLoading(show) {
      if(show) {
        $('#loadingModal').show();
      } else {
        $('#loadingModal').hide();
      }
    }
    
    // 사용자 목록 로드
    function loadEmployees() {
      $.ajax({
        url: '${pageContext.request.contextPath}/getAllEmployees',
        type: 'GET',
        dataType: 'json',
        success: function(employees) {
          renderEmployeeList(employees);
        },
        error: function(xhr, status, error) {
          console.error('사용자 목록을 불러오는 중 오류가 발생했습니다: ' + error);
          $('#userList').html('<div class="empty-state"><i class="fas fa-exclamation-circle"></i><p>사용자 목록을 불러올 수 없습니다.</p></div>');
        }
      });
    }
    
    // 사용자 목록 렌더링
    function renderEmployeeList(employees) {
      const $list = $('#userList');
      $list.empty();
      
      if (!employees || employees.length === 0) {
        $list.html('<div class="empty-state"><i class="fas fa-users-slash"></i><p>등록된 사용자가 없습니다.</p></div>');
        return;
      }
      
      $.each(employees, function(index, employee) {
        const $item = $('<li>').addClass('user-item').attr('data-emp-no', employee.empNo);
        
        // 아바타 (이니셜)
        const initial = employee.empName ? employee.empName.charAt(0) : '?';
        const $avatar = $('<div>').addClass('user-avatar').text(initial);
        
        // 사용자 정보
        const $info = $('<div>').addClass('user-info');
        
        // 이름 (관리자인 경우 배지 표시)
        const $name = $('<div>').addClass('user-name').text(employee.empName);
        if (employee.isAdmin === 'Y') {
          $name.append($('<span>').addClass('admin-badge').text('관리자'));
        }
        
        // 부서 / 직급
        const $position = $('<div>').addClass('user-position').text(
          (employee.deptName || '부서 미지정') + ' / ' + (employee.positionName || '직급 미지정')
        );
        
        // 상태 배지
        const statusClass = employee.status === 'Y' ? 'status-active' : 'status-inactive';
        const statusText = employee.status === 'Y' ? '활성' : '비활성';
        const $status = $('<div>').addClass('status-badge ' + statusClass).text(statusText);
        
        $info.append($name, $position, $status);
        $item.append($avatar, $info);
        
        // 클릭 이벤트
        $item.click(function() {
          $('.user-item').removeClass('selected');
          $(this).addClass('selected');
          loadUserDetail(employee.empNo);
        });
        
        $list.append($item);
      });
      
      // 필터링 적용
      filterUsers();
    }
    
    // 사용자 필터링
    function filterUsers() {
      const keyword = $('#searchInput').val().toLowerCase();
      const department = $('#departmentFilter').val();
      const status = $('#statusFilter').val();
      
      $('.user-item').each(function() {
        const $item = $(this);
        
        // 이름, 부서, 상태 정보
        const name = $item.find('.user-name').text().toLowerCase();
        const dept = $item.find('.user-position').text().toLowerCase();
        const userStatus = $item.find('.status-badge').hasClass('status-active') ? 'Y' : 'N';
        
        // 필터링 조건
        const nameMatch = !keyword || name.includes(keyword);
        const deptMatch = !department || dept.includes(department.toLowerCase());
        const statusMatch = !status || userStatus === status;
        
        // 모든 조건 충족 시 표시, 아니면 숨김
        if (nameMatch && deptMatch && statusMatch) {
          $item.show();
        } else {
          $item.hide();
        }
      });
    }
    
    // 사용자 상세 정보 로드
    function loadUserDetail(empNo) {
      $.ajax({
        url: '${pageContext.request.contextPath}/getUserDetail',
        type: 'GET',
        data: { empNo: empNo },
        dataType: 'json',
        success: function(employee) {
          renderUserDetail(employee);
        },
        error: function(xhr, status, error) {
          console.error('사용자 정보를 불러오는 중 오류가 발생했습니다: ' + error);
          $('#userDetail').html('<div class="empty-state"><i class="fas fa-exclamation-circle"></i><p>사용자 정보를 불러올 수 없습니다.</p></div>');
        }
      });
    }
    
    // 사용자 상세 정보 렌더링
    function renderUserDetail(employee) {
      if (!employee || !employee.empNo) {
        $('#userDetail').html('<div class="empty-state"><i class="fas fa-user-slash"></i><p>사용자 정보가 없습니다.</p></div>');
        return;
      }
      
      // 이니셜
      const initial = employee.empName ? employee.empName.charAt(0) : '?';
      
      // 관리자 권한 텍스트
      const isAdmin = employee.isAdmin === 'Y' ? '있음' : '없음';
      
      // 상태 텍스트
      const status = employee.status === 'Y' ? '활성' : '비활성';
      
      // 날짜 형식 처리 (직접 JavaScript에서 처리)
      let hireDate = '-';
      if (employee.hireDate) {
        try {
          const date = new Date(employee.hireDate);
          if (!isNaN(date.getTime())) {
            hireDate = date.getFullYear() + '-' + 
              String(date.getMonth() + 1).padStart(2, '0') + '-' + 
              String(date.getDate()).padStart(2, '0');
          }
        } catch (e) {
          console.error('날짜 변환 오류:', e);
        }
      }
      
      // 상세 정보 HTML 구성
      let html = '<div class="detail-header">' +
                  '<div class="detail-avatar">' + initial + '</div>' +
                  '<div class="detail-title">' +
                    '<div class="detail-name">' + (employee.empName || '-') + '</div>' +
                    '<div class="detail-position">' + (employee.deptName || '-') + ' / ' + (employee.positionName || '-') + '</div>' +
                  '</div>' +
                '</div>' +
                
                '<div class="detail-content">' +
                  '<div class="detail-section">' +
                    '<h3>기본 정보</h3>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">사번</div>' +
                      '<div class="detail-value">' + (employee.empId || '-') + '</div>' +
                    '</div>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">이메일</div>' +
                      '<div class="detail-value">' + (employee.email || '-') + '</div>' +
                    '</div>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">연락처</div>' +
                      '<div class="detail-value">' + (employee.phone || '-') + '</div>' +
                    '</div>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">입사일</div>' +
                      '<div class="detail-value">' + hireDate + '</div>' +
                    '</div>' +
                  '</div>' +
                  
                  '<div class="detail-section">' +
                    '<h3>계정 정보</h3>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">계정 상태</div>' +
                      '<div class="detail-value">' + status + '</div>' +
                    '</div>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">관리자 권한</div>' +
                      '<div class="detail-value">' + isAdmin + '</div>' +
                    '</div>' +
                    '<div class="detail-item">' +
                      '<div class="detail-label">사원번호</div>' +
                      '<div class="detail-value">' + (employee.empNo || '-') + '</div>' +
                    '</div>' +
                  '</div>' +
                '</div>' +
                
                '<div class="action-buttons">' +
                  '<button type="button" class="btn" onclick="showEditForm(' + employee.empNo + ')">' +
                    '<i class="fas fa-edit"></i> 정보 수정' +
                  '</button>' +
                  '<button type="button" class="btn btn-warning" onclick="showResetPasswordModal(' + employee.empNo + ', \'' + employee.empId + '\', \'' + employee.empName + '\')">' +
                    '<i class="fas fa-key"></i> 비밀번호 초기화' +
                  '</button>' +
                '</div>';
  
      $('#userDetail').html(html);
    }
    
    // 정보 수정 폼 표시
function showEditForm(empNo) {
  $.ajax({
    url: '${pageContext.request.contextPath}/getUserDetail',
    type: 'GET',
    data: { empNo: empNo },
    dataType: 'json',
    success: function(employee) {
      if (!employee || !employee.empNo) {
        alert('사용자 정보를 찾을 수 없습니다.');
        return;
      }
      
      // 폼 필드에 데이터 채우기
      $('#empNo').val(employee.empNo);
      $('#empId').val(employee.empId);
      $('#empName').val(employee.empName);
      $('#deptNo').val(employee.deptNo);
      $('#positionNo').val(employee.positionNo);
      $('#email').val(employee.email);
      $('#phone').val(employee.phone);
      
      // 라디오 버튼 상태 설정
      $('input[name="status"][value="' + employee.status + '"]').prop('checked', true);
      $('input[name="isAdmin"][value="' + employee.isAdmin + '"]').prop('checked', true);
      
      // 모달 표시
      $('#editModal').show();
    },
    error: function(xhr, status, error) {
      alert('사용자 정보를 불러오는 중 오류가 발생했습니다: ' + error);
    }
  });
}

// 비밀번호 초기화 모달 표시
function showResetPasswordModal(empNo, empId, empName) {
  $('#resetPasswordModal').data('empNo', empNo);
  $('#resetPasswordUserName').text(empName);
  $('#initialPassword').text(empId + 'init');
  $('#resetPasswordModal').show();
}

// 사용자 정보 업데이트
function updateUserInfo() {
  const formData = $('#editForm').serialize();
  
  // 로딩 표시
  showLoading(true);
  
  $.ajax({
    url: '${pageContext.request.contextPath}/updateUserInfo',
    type: 'POST',
    data: formData,
    dataType: 'json',
    success: function(response) {
      // 로딩 숨김
      showLoading(false);
      
      if (response.success) {
        alert(response.message);
        closeAllModals();
        loadEmployees();
        loadUserDetail($('#empNo').val());
      } else {
        alert(response.message || '사용자 정보 수정에 실패했습니다.');
      }
    },
    error: function(xhr, status, error) {
      // 로딩 숨김
      showLoading(false);
      alert('사용자 정보 수정 중 오류가 발생했습니다: ' + error);
    }
  });
}

// 비밀번호 초기화
function resetUserPassword() {
  const empNo = $('#resetPasswordModal').data('empNo');
  
  // 로딩 표시
  showLoading(true);
  
  $.ajax({
    url: '${pageContext.request.contextPath}/resetPassword',
    type: 'POST',
    data: { empNo: empNo },
    dataType: 'json',
    success: function(response) {
      // 로딩 숨김
      showLoading(false);
      
      if (response.success) {
        alert(response.message);
        closeAllModals();
      } else {
        alert(response.message || '비밀번호 초기화에 실패했습니다.');
      }
    },
    error: function(xhr, status, error) {
      // 로딩 숨김
      showLoading(false);
      alert('비밀번호 초기화 중 오류가 발생했습니다: ' + error);
    }
  });
}
</script>
</body>
</html>