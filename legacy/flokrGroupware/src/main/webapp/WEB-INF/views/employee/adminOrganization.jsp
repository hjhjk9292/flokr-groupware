<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>조직도 관리</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/adminMain.css">
  <!-- jQuery 라이브러리 -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
  <!-- 아이콘 사용을 위한 Font Awesome -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    main {
      padding: 20px;
    }
    
    .organization-container {
      display: flex;
      gap: 20px;
      padding: 20px;
      min-height: 600px;
    }
    
    .org-tree-container {
      width: 350px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      padding: 20px;
      overflow: auto;
      max-height: 700px;
    }
    
    .employee-info-container {
      flex-grow: 1;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      padding: 20px;
      height: fit-content;
    }
    
    .org-controls {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;
    }
    
    .org-controls button {
      padding: 5px 10px;
      background: #f0f0f0;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
    }
    
    .org-controls button:hover {
      background: #e0e0e0;
    }
    
    .search-box {
      width: 100%;
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
      margin-bottom: 15px;
    }
    
    .org-tree {
      list-style-type: none;
      padding-left: 0;
    }
    
    .org-tree ul {
      list-style-type: none;
      padding-left: 20px;
    }
    
    .org-tree li {
      margin: 5px 0;
      position: relative;
    }
    
    .org-tree .toggle {
      cursor: pointer;
      width: 15px;
      display: inline-block;
      text-align: center;
    }
    
    .org-tree .dept-item {
      font-weight: bold;
      cursor: pointer;
      padding: 5px;
      border-radius: 4px;
      display: flex;
      align-items: center;
    }
    
    .org-tree .dept-item:hover {
      background-color: #f0f0f0;
    }
    
    .org-tree .dept-item i {
      margin-right: 5px;
      color: #4a6cf7;
    }
    
    .org-tree .emp-item {
      cursor: pointer;
      padding: 5px;
      padding-left: 20px;
      border-radius: 4px;
      display: flex;
      align-items: center;
    }
    
    .org-tree .emp-item:hover {
      background-color: #f0f0f0;
    }
    
    .org-tree .emp-item.selected {
      background-color: #e8f0fe;
    }
    
    .org-tree .emp-item img, .org-tree .emp-item .avatar {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      margin-right: 8px;
      background-color: #ddd;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      color: #666;
    }
    
    .employee-info {
      display: flex;
      flex-direction: column;
      width: 100%;
    }
    
    .employee-profile {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #eee;
    }
    
    .employee-profile img, .employee-profile .avatar {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      margin-right: 20px;
      background-color: #ddd;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
      color: #666;
    }
    
    .employee-name {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 5px;
    }
    
    .employee-position {
      font-size: 16px;
      color: #666;
    }
    
    .employee-details {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 15px;
    }
    
    .detail-item {
      margin-bottom: 15px;
    }
    
    .detail-label {
      font-weight: bold;
      margin-bottom: 5px;
      color: #555;
    }
    
    .detail-value {
      color: #333;
    }
    
    .dept-actions {
      display: none;
      margin-left: 10px;
    }
    
    .dept-item:hover .dept-actions {
      display: inline-flex;
    }
    
    .dept-actions button {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 14px;
      color: #666;
      margin-left: 5px;
    }
    
    .dept-actions button:hover {
      color: #4a6cf7;
    }
    
    .modal {
      display: none;
      position: fixed;
      z-index: 1000;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0,0,0,0.5);
    }
    
    .modal-content {
      background-color: #fff;
      margin: 15% auto;
      padding: 20px;
      border-radius: 8px;
      width: 50%;
      max-width: 500px;
    }
    
    .close {
      float: right;
      cursor: pointer;
      font-size: 1.5rem;
    }
    
    .form-group {
      margin-bottom: 15px;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 5px;
      font-weight: 500;
    }
    
    .form-group input {
      width: 100%;
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    
    .btn-primary {
      background: #4a6cf7;
      color: white;
      border: none;
      border-radius: 4px;
      padding: 8px 12px;
      cursor: pointer;
    }
    
    .btn-cancel {
      background: #f0f0f0;
      border: none;
      border-radius: 4px;
      padding: 8px 12px;
      color: #333;
      margin-left: 5px;
      cursor: pointer;
    }
    
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #666;
      text-align: center;
      padding: 40px 0;
    }
    
    .empty-state i {
      font-size: 48px;
      color: #ddd;
      margin-bottom: 20px;
    }
    
    /* 최상위 항목 */
    .root-header {
      font-weight: bold;
      font-size: 1.2em;
      color: #555;
      margin-bottom: 10px;
      padding: 5px;
      border-radius: 4px;
      display: flex;
      align-items: center;
    }
    
    .root-header .icon {
      margin-right: 5px;
      color: #4a6cf7;
    }
  </style>
</head>
<body>
  <jsp:include page="../common/header.jsp"/>
  
  <main>
    <div class="dashboard-title">
      <h1>조직도 관리</h1>
      <p>회사 조직도와 부서 정보를 설정하고 관리합니다.</p>
    </div>
    
    <div class="organization-container">
      <!-- 왼쪽 조직도 트리 -->
      <div class="org-tree-container">
        <div class="org-controls">
          <button id="btn-add-dept">부서 추가</button>
          <button id="btn-refresh">새로고침</button>
        </div>
        
        <input type="text" class="search-box" id="org-search" placeholder="이름 또는 부서 검색...">
        
        <!-- 최상위 항목 추가 -->
        <div class="root-header">
          <i class="fas fa-building icon"></i>
          <span>Flokr</span>
        </div>
        
        <ul class="org-tree" id="org-tree">
          <!-- 트리 구조는 동적 생성 -->
        </ul>
      </div>
      
      <!-- 오른쪽 직원 정보 -->
      <div class="employee-info-container">
        <div id="employee-info" class="employee-info">
          <!-- 직원 선택 전 빈 상태 표시 -->
          <div class="empty-state">
            <i class="fas fa-user"></i>
            <p>왼쪽에서 직원을 선택하면 상세 정보가 표시됩니다.</p>
          </div>
        </div>
      </div>
    </div>
  </main>
  
  <!-- 부서 추가 모달 -->
  <div id="add-dept-modal" class="modal">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>부서 추가</h2>
      <form id="add-dept-form" action="insertDepartment" method="post">
        <div class="form-group">
          <label for="dept-name">부서명</label>
          <input type="text" id="dept-name" name="deptName" required>
        </div>
        <button type="submit" class="btn-primary">저장</button>
        <button type="button" class="btn-cancel" onclick="closeModal('add-dept-modal')">취소</button>
      </form>
    </div>
  </div>
  
  <!-- 부서 수정 모달 -->
  <div id="edit-dept-modal" class="modal">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>부서 수정</h2>
      <form id="edit-dept-form" action="updateDepartment" method="post">
        <input type="hidden" id="edit-dept-no" name="deptNo">
        <div class="form-group">
          <label for="edit-dept-name">부서명</label>
          <input type="text" id="edit-dept-name" name="deptName" required>
        </div>
        <button type="submit" class="btn-primary">저장</button>
        <button type="button" class="btn-cancel" onclick="closeModal('edit-dept-modal')">취소</button>
      </form>
    </div>
  </div>
  
  <script>
    $(function() {
      // 페이지 로드 시 부서 목록 로드
      loadDepartments();
      
      // 부서 추가 버튼 클릭
      $('#btn-add-dept').click(function() {
        $('#add-dept-modal').css('display', 'block');
      });
      
      // 새로고침 버튼 클릭
      $('#btn-refresh').click(function() {
        location.reload();
      });
      
      // 모달 닫기 버튼
      $('.close').click(function() {
        $(this).closest('.modal').css('display', 'none');
      });
      
      // 검색 기능
      $('#org-search').on('keyup', function() {
        const keyword = $(this).val().toLowerCase();
        searchOrganization(keyword);
      });
    });
    
    // 부서 목록 로드
    function loadDepartments() {
      $.ajax({
        url: '${pageContext.request.contextPath}/getDepartments',
        type: 'GET',
        success: function(departments) {
          renderDepartmentList(departments);
        },
        error: function() {
          console.error('부서 목록을 가져오는데 실패했습니다.');
          // 임시 데이터로 초기화
          const tempDepartments = [
            { deptNo: 1, deptName: '인사팀' },
            { deptNo: 2, deptName: '재무팀' },
            { deptNo: 3, deptName: '마케팅팀' },
            { deptNo: 4, deptName: '개발팀' },
            { deptNo: 5, deptName: '영업팀' }
          ];
          renderDepartmentList(tempDepartments);
        }
      });
    }
    
    // 부서 목록 렌더링
    function renderDepartmentList(departments) {
      const $tree = $('#org-tree');
      $tree.empty();
      
      if (!departments || departments.length === 0) {
        $tree.html('<li>등록된 부서가 없습니다.</li>');
        return;
      }
      
      departments.forEach(function(dept) {
        const $li = $('<li></li>');
        const $deptItem = $('<div class="dept-item"></div>')
          .attr('data-dept-no', dept.deptNo);
        
        const $toggle = $('<span class="toggle"><i class="fas fa-caret-right"></i></span>');
        const $icon = $('<i class="fas fa-sitemap"></i>');
        const $name = $('<span></span>').text(' ' + dept.deptName);
        const $actions = $('<div class="dept-actions"></div>');
        
        const $editBtn = $('<button type="button"></button>')
          .html('<i class="fas fa-edit"></i>')
          .click(function(e) {
            e.stopPropagation();
            showEditDeptForm(dept.deptNo, dept.deptName);
          });
        
        const $deleteBtn = $('<button type="button"></button>')
          .html('<i class="fas fa-trash"></i>')
          .click(function(e) {
            e.stopPropagation();
            deleteDept(dept.deptNo);
          });
        
        $actions.append($editBtn, $deleteBtn);
        $deptItem.append($toggle, $icon, $name, $actions);
        
        const $empList = $('<ul class="emp-list"></ul>').hide();
        
        // 부서 클릭 이벤트
        $deptItem.click(function(e) {
          if ($(e.target).closest('.dept-actions').length > 0) {
            return;
          }
          
          const $toggle = $(this).find('.toggle i');
          $toggle.toggleClass('fa-caret-right fa-caret-down');
          
          if ($empList.is(':visible')) {
            $empList.slideUp();
          } else {
            $empList.slideDown();
            loadEmployees(dept.deptNo, $empList);
          }
        });
        
        $li.append($deptItem, $empList);
        $tree.append($li);
      });
    }
    
    // 직원 목록 로드
    function loadEmployees(deptNo, $container) {
      $container.html('<li><div class="emp-item">로딩 중...</div></li>');
      
      $.ajax({
        url: '${pageContext.request.contextPath}/getEmployeesByDept',
        type: 'GET',
        data: { deptNo: deptNo },
        success: function(employees) {
          $container.empty();
          
          if (!employees || employees.length === 0) {
            $container.html('<li><div class="emp-item">직원이 없습니다.</div></li>');
            return;
          }
          
          employees.forEach(function(emp) {
            // 직원 목록 DOM 직접 생성
            const $li = $('<li></li>');
            const $item = $('<div class="emp-item"></div>');
            
            // data 속성으로 직원 번호 저장
            $item.attr('data-emp-no', emp.empNo);
            
            let avatarHtml = '';
            if (emp.profileImgPath) {
              avatarHtml = '<img src="' + emp.profileImgPath + '" alt="' + emp.empName + '">';
            } else {
              const initial = emp.empName ? emp.empName.charAt(0) : '?';
              avatarHtml = '<div class="avatar">' + initial + '</div>';
            }
            
            const nameHtml = emp.empName || '';
            const positionHtml = emp.positionName ? ' (' + emp.positionName + ')' : '';
            
            $item.html(avatarHtml + nameHtml + positionHtml);
            
            // 클릭 이벤트 바인딩
            $item.on('click', function() {
              // 모든 emp-item에서 선택 클래스 제거
              $('.emp-item').removeClass('selected');
              // 현재 항목에 선택 클래스 추가
              $(this).addClass('selected');
              
              // 직원 정보 로드
              const empNo = $(this).attr('data-emp-no');
              loadEmployeeInfo(empNo);
            });
            
            $li.append($item);
            $container.append($li);
          });
        },
        error: function() {
          $container.html('<li><div class="emp-item">직원 정보를 불러올 수 없습니다.</div></li>');
        }
      });
    }
    
    // 직원 정보 로드
	function loadEmployeeInfo(empNo) {
	  if (!empNo) {
	    console.error('직원 번호가 없습니다.');
	    return;
	  }
	  
	  console.log('직원 정보 로드 시작:', empNo);
	  
	  // 로딩 상태 표시
	  $('#employee-info').html(`
	    <div class="empty-state">
	      <i class="fas fa-spinner fa-spin"></i>
	      <p>직원 정보를 불러오는 중입니다...</p>
	    </div>
	  `);
	  
	  $.ajax({
	    url: '${pageContext.request.contextPath}/getEmployeeInfo',
	    type: 'GET',
	    data: { empNo: empNo },
	    dataType: 'json',
	    success: function(employee) {
	      console.log('수신된 직원 정보:', employee);
	      
	      if (!employee) {
	        $('#employee-info').html(`
	          <div class="empty-state">
	            <i class="fas fa-exclamation-circle"></i>
	            <p>직원 정보를 찾을 수 없습니다.</p>
	          </div>
	        `);
	        return;
	      }
	      
	      // jQuery로 DOM 요소 직접 생성
	      const $employeeInfo = $('<div></div>');
	      
	      // 프로필 영역 생성
	      const $profile = $('<div class="employee-profile"></div>');
	      
	      // 아바타 생성
	      let $avatar;
	      if (employee.profileImgPath) {
	        $avatar = $('<img>').attr('src', employee.profileImgPath).attr('alt', '프로필');
	      } else {
	        const initial = employee.empName ? employee.empName.charAt(0) : '?';
	        $avatar = $('<div class="avatar"></div>').text(initial);
	      }
	      
	      // 이름 및 직위 정보 생성
	      const $info = $('<div></div>');
	      const $name = $('<div class="employee-name"></div>').text(employee.empName || '-');
	      const $position = $('<div class="employee-position"></div>')
	        .text((employee.deptName || '-') + ' / ' + (employee.positionName || '-'));
	      
	      $info.append($name, $position);
	      $profile.append($avatar, $info);
	      
	      // 상세 정보 영역 생성
	      const $details = $('<div class="employee-details"></div>');
	      
	      // 사번 정보
	      const $idItem = $('<div class="detail-item"></div>');
	      $idItem.append(
	        $('<div class="detail-label"></div>').text('사번'),
	        $('<div class="detail-value"></div>').text(employee.empId || '-')
	      );
	      
	      // 이메일 정보
	      const $emailItem = $('<div class="detail-item"></div>');
	      $emailItem.append(
	        $('<div class="detail-label"></div>').text('이메일'),
	        $('<div class="detail-value"></div>').text(employee.email || '-')
	      );
	      
	      // 전화번호 정보
	      const $phoneItem = $('<div class="detail-item"></div>');
	      $phoneItem.append(
	        $('<div class="detail-label"></div>').text('전화번호'),
	        $('<div class="detail-value"></div>').text(employee.phone || '-')
	      );
	      
	      // 입사일 정보
	      const $hireDateItem = $('<div class="detail-item"></div>');
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
	          console.error('날짜 포맷 오류:', e);
	        }
	      }
	      $hireDateItem.append(
	        $('<div class="detail-label"></div>').text('입사일'),
	        $('<div class="detail-value"></div>').text(hireDate)
	      );
	      
	      // 관리자 여부 정보
	      const $adminItem = $('<div class="detail-item"></div>');
	      const isAdmin = employee.isAdmin === 'Y' ? '관리자' : '일반사용자';
	      $adminItem.append(
	        $('<div class="detail-label"></div>').text('관리자 여부'),
	        $('<div class="detail-value"></div>').text(isAdmin)
	      );
	      
	      // 모든 상세 정보 항목을 details에 추가
	      $details.append($idItem, $emailItem, $phoneItem, $hireDateItem, $adminItem);
	      
	      // 프로필과 상세 정보를 employeeInfo에 추가
	      $employeeInfo.append($profile, $details);
	      
	      // DOM에 삽입
	      $('#employee-info').empty().append($employeeInfo);
	      
	      console.log('직원 정보 렌더링 완료');
	    },
	    error: function(xhr, status, error) {
	      console.error('AJAX 오류:', error);
	      console.log('상태:', status);
	      console.log('응답:', xhr.responseText);
	      
	      $('#employee-info').html(`
	        <div class="empty-state">
	          <i class="fas fa-exclamation-circle"></i>
	          <p>직원 정보를 불러올 수 없습니다. 오류: ${error}</p>
	        </div>
	      `);
	    }
	  });
	}
    
    // 부서 수정 폼 표시
    function showEditDeptForm(deptNo, deptName) {
      $('#edit-dept-no').val(deptNo);
      $('#edit-dept-name').val(deptName);
      $('#edit-dept-modal').css('display', 'block');
    }
    
    // 부서 삭제
    function deleteDept(deptNo) {
      if(confirm('이 부서를 삭제하시겠습니까?')) {
        // 폼 생성 및 제출
        const form = document.createElement('form');
        form.method = 'post';
        form.action = 'deleteDepartment';
        
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'deptNo';
        input.value = deptNo;
        
        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
      }
    }
    
    // 모달 닫기
    function closeModal(modalId) {
      $('#' + modalId).css('display', 'none');
    }
    
    // 조직도 검색
    function searchOrganization(keyword) {
      if(!keyword) {
        $('.dept-item, .emp-item').closest('li').show();
        $('.emp-list').hide();
        $('.toggle i').removeClass('fa-caret-down').addClass('fa-caret-right');
        return;
      }
      
      $('.dept-item, .emp-item').closest('li').hide();
      
      // 부서명에서 검색
      $(`.dept-item:contains('${keyword}')`).each(function() {
        $(this).closest('li').show();
        $(this).find('.toggle i').removeClass('fa-caret-right').addClass('fa-caret-down');
        $(this).next('.emp-list').show();
      });
      
      // 직원명에서 검색
      $(`.emp-item:contains('${keyword}')`).each(function() {
        $(this).closest('li').show();
        $(this).closest('.emp-list').show();
        $(this).closest('.emp-list').prev('.dept-item').find('.toggle i').removeClass('fa-caret-right').addClass('fa-caret-down');
        $(this).closest('.emp-list').prev('.dept-item').closest('li').show();
      });
    }
    
    // 대소문자 구분 없는 :contains 확장
    $.expr[':'].contains = function(a, i, m) {
      return $(a).text().toLowerCase().indexOf(m[3].toLowerCase()) >= 0;
    };
  </script>
</body>
</html>