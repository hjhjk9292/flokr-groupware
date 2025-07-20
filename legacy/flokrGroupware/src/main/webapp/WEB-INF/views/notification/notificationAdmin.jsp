<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
 <meta charset="UTF-8">
 <meta name="viewport" content="width=device-width, initial-scale=1.0">
 <title>알림 관리</title>
 <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/notificationAdmin.css">
 <!-- jQuery 라이브러리 -->
 <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
 <!-- Font Awesome CDN -->
 <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
 <!-- Alertify -->
 <script src="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/alertify.min.js"></script>
 <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/alertify.min.css"/>
 <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/themes/default.min.css"/>
</head>
<body>
 <jsp:include page="../common/header.jsp"/>
 
 <!-- 알림 관리 메인 콘텐츠 -->
 <main class="admin-notification">
   <div class="admin-notification-title">
     <h1>알림 관리</h1>
     <p>알림을 발송하고 관리할 수 있습니다.</p>
   </div>
   
   <!-- 탭 메뉴 -->
   <div class="notification-tabs">
     <div class="notification-tab active" data-tab="send">알림 발송</div>
     <div class="notification-tab" data-tab="list">알림 목록</div>
   </div>
   
   <!-- 알림 발송 탭 -->
   <div class="notification-content active" id="tab-send">
     <div class="notification-form">
       <div class="form-group">
         <label class="form-label">수신 대상</label>
         <select class="form-select" id="targetType">
           <option value="ALL">전체 직원</option>
           <option value="DEPARTMENT">특정 부서</option>
           <option value="EMPLOYEE">특정 직원</option>
         </select>
       </div>
       
       <div class="form-group target-specific" id="departmentSelect">
         <label class="form-label">부서 선택</label>
         <select class="form-select" id="deptNo">
           <c:forEach items="${departments}" var="dept">
             <option value="${dept.deptNo}">${dept.deptName}</option>
           </c:forEach>
         </select>
       </div>
       
       <!-- 직원 검색 영역 (고유 ID/클래스 사용) -->
       <div class="form-group target-specific" id="employeeSelect">
         <label class="form-label">직원 검색</label>
         <div class="input-group mb-3">
           <input type="text" class="form-control" id="emp_search_input" placeholder="이름 또는 사번으로 검색">
           <button class="btn btn-primary" type="button" id="emp_search_btn">검색</button>
         </div>
         
         <!-- 검색 결과 표시 영역 -->
         <div id="emp_search_results" class="emp_search_results mt-2"></div>
         
         <!-- 선택된 직원 정보 표시 영역 -->
         <div id="emp_selected_info" class="emp_selected_info" style="display:none;"></div>
         
         <!-- 선택된 직원 번호 저장 (hidden) -->
         <input type="hidden" id="selectedEmpNo" value="">
       </div>
       
       <div class="form-group">
         <label class="form-label">알림 유형</label>
         <select class="form-select" id="notificationType">
           <option value="NOTICE">공지사항</option>
           <option value="APPROVAL">결재</option>
           <option value="TASK">업무</option>
           <option value="CHAT">채팅</option>
           <option value="SCHEDULE">일정</option>
           <option value="FACILITY">시설 예약</option>
         </select>
       </div>
       
       <div class="form-group">
         <label class="form-label">알림 제목</label>
         <input type="text" class="form-control" id="notificationTitle" placeholder="알림 제목을 입력하세요">
       </div>
       
       <div class="form-group">
         <label class="form-label">알림 내용</label>
         <textarea class="form-control" id="notificationContent" rows="4" placeholder="알림 내용을 입력하세요"></textarea>
       </div>
       
       <div class="form-group">
         <label class="form-label">연결 페이지 (선택사항)</label>
         <div style="display: flex; gap: 0.5rem;">
           <select class="form-select" id="refType" style="width: 40%;">
             <option value="">선택 안함</option>
             <option value="notice">공지사항</option>
             <option value="approval">결재</option>
             <option value="task">업무</option>
             <option value="schedule">일정</option>
           </select>
           <input type="text" class="form-control" id="refNo" placeholder="관련 ID 입력">
         </div>
       </div>
       
       <div class="form-actions">
         <button type="button" class="btn btn-secondary" id="resetBtn">초기화</button>
         <button type="button" class="btn btn-primary" id="sendBtn">알림 발송</button>
       </div>
     </div>
   </div>
   
   <!-- 알림 목록 탭 -->
   <div class="notification-content" id="tab-list">
     <!-- 검색 및 필터 -->
     <div class="notification-filters">
       <select class="notification-select" id="typeFilter">
         <option value="">전체 유형</option>
         <option value="NOTICE" ${type eq 'NOTICE' ? 'selected' : ''}>공지사항</option>
         <option value="APPROVAL" ${type eq 'APPROVAL' ? 'selected' : ''}>결재</option>
         <option value="TASK" ${type eq 'TASK' ? 'selected' : ''}>업무</option>
         <option value="CHAT" ${type eq 'CHAT' ? 'selected' : ''}>채팅</option>
         <option value="SCHEDULE" ${type eq 'SCHEDULE' ? 'selected' : ''}>일정</option>
       </select>
       
       <div class="notification-search">
         <input type="text" id="searchKeyword" placeholder="검색어 입력" value="${keyword}">
         <button id="searchBtn"><i class="fas fa-search"></i></button>
       </div>
     </div>
     
     <!-- 알림 테이블 -->
     <div class="notification-table-container">
       <table class="notification-table">
         <thead>
           <tr>
             <th width="5%">번호</th>
             <th width="10%">유형</th>
             <th width="30%">제목</th>
             <th width="15%">수신자</th>
             <th width="15%">발송일시</th>
             <th width="15%">읽음 상태</th>
             <th width="10%">관리</th>
           </tr>
         </thead>
         <tbody>
           <c:choose>
             <c:when test="${empty notifications}">
               <tr>
                 <td colspan="7" style="text-align: center;">조회된 알림이 없습니다.</td>
               </tr>
             </c:when>
             <c:otherwise>
               <c:forEach items="${notifications}" var="notification">
                 <tr>
                   <td>${notification.notificationNo}</td>
                   <td>
                     <c:choose>
                       <c:when test="${notification.type eq 'NOTICE'}">공지사항</c:when>
                       <c:when test="${notification.type eq 'APPROVAL'}">결재</c:when>
                       <c:when test="${notification.type eq 'TASK'}">업무</c:when>
                       <c:when test="${notification.type eq 'CHAT'}">채팅</c:when>
                       <c:when test="${notification.type eq 'SCHEDULE'}">일정</c:when>
                       <c:when test="${notification.type eq 'FACILITY'}">시설 예약</c:when>
                       <c:otherwise>${notification.type}</c:otherwise>
                     </c:choose>
                   </td>
                   <td>${notification.title}</td>
                   <td>${notification.empName} (${notification.empId})</td>
                   <td>
                     <fmt:formatDate value="${notification.createDate}" pattern="yyyy-MM-dd HH:mm" />
                   </td>
                   <td>
                     <c:choose>
                       <c:when test="${empty notification.readDate}">
                         <span style="color: #ff6b6b;">읽지 않음</span>
                       </c:when>
                       <c:otherwise>
                         <span style="color: #4dabf7;">
                           <fmt:formatDate value="${notification.readDate}" pattern="yyyy-MM-dd HH:mm" />
                         </span>
                       </c:otherwise>
                     </c:choose>
                   </td>
                   <td>
                     <button class="btn btn-secondary btn-sm notification-view-btn" 
                             data-no="${notification.notificationNo}" 
                             data-title="${notification.title}" 
                             data-content="${notification.notificationContent}"
                             data-type="${notification.type}"
                             data-reftype="${notification.refType}"
                             data-refno="${notification.refNo}">
                       보기
                     </button>
                   </td>
                 </tr>
               </c:forEach>
             </c:otherwise>
           </c:choose>
         </tbody>
       </table>
     </div>
     
     <!-- 페이지네이션 -->
     <div class="notification-pagination">
       <c:if test="${currentPage > 1}">
         <a href="${pageContext.request.contextPath}/notificationAdmin?page=1&type=${type}&keyword=${keyword}">
           <i class="fas fa-angle-double-left"></i>
         </a>
         <a href="${pageContext.request.contextPath}/notificationAdmin?page=${currentPage - 1}&type=${type}&keyword=${keyword}">
           <i class="fas fa-angle-left"></i>
         </a>
       </c:if>
       
       <c:forEach var="p" begin="${startPage}" end="${endPage}">
         <c:choose>
           <c:when test="${p eq currentPage}">
             <a href="#" class="active">${p}</a>
           </c:when>
           <c:otherwise>
             <a href="${pageContext.request.contextPath}/notificationAdmin?page=${p}&type=${type}&keyword=${keyword}">${p}</a>
           </c:otherwise>
         </c:choose>
       </c:forEach>
       
       <c:if test="${currentPage < maxPage}">
         <a href="${pageContext.request.contextPath}/notificationAdmin?page=${currentPage + 1}&type=${type}&keyword=${keyword}">
           <i class="fas fa-angle-right"></i>
         </a>
         <a href="${pageContext.request.contextPath}/notificationAdmin?page=${maxPage}&type=${type}&keyword=${keyword}">
           <i class="fas fa-angle-double-right"></i>
         </a>
       </c:if>
     </div>
   </div>
 </main>
 
<script>
// 알림 관리 페이지 스크립트
$(document).ready(function() {
    // contextPath 정의
    const contextPath = '${pageContext.request.contextPath}';
    
    // URL에서 tab 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const tabParam = urlParams.get('tab');
    
    // 탭 파라미터가 있으면 해당 탭 활성화
    if(tabParam) {
        $('.notification-tab').removeClass('active');
        $('.notification-tab[data-tab="' + tabParam + '"]').addClass('active');
        
        $('.notification-content').removeClass('active');
        $('#tab-' + tabParam).addClass('active');
    }
    
    // 탭 전환 JS - 클릭 시 tab 파라미터 포함하여 URL 업데이트
    $('.notification-tab').click(function() {
        const tab = $(this).data('tab');
        
        // 탭 활성화
        $('.notification-tab').removeClass('active');
        $(this).addClass('active');
        
        // 콘텐츠 표시/숨김 처리
        $('.notification-content').removeClass('active');
        $('#tab-' + tab).addClass('active');
        
        // URL 히스토리 업데이트 (페이지 새로고침 없이)
        const url = new URL(window.location.href);
        url.searchParams.set('tab', tab);
        window.history.replaceState({}, '', url);
        
        // 페이지네이션 링크 업데이트
        updatePaginationLinks();
        
        return false;
    });
    
    // 대상 유형에 따라 추가 입력 필드 표시
    $('#targetType').change(function() {
        const targetType = $(this).val();
        $('.target-specific').hide();
        
        if(targetType === 'DEPARTMENT') {
            $('#departmentSelect').show();
        } else if(targetType === 'EMPLOYEE') {
            $('#employeeSelect').show();
        }
    });
    
    // 직원 검색 버튼 클릭 이벤트 - notification.js의 searchEmployee 함수 사용
    document.getElementById('emp_search_btn').addEventListener('click', function() {
        const keyword = document.getElementById('emp_search_input').value.trim();
        window.searchEmployee(
            keyword, 
            '#emp_search_results', 
            '#selectedEmpNo', 
            '#emp_selected_info'
        );
    });
    
    // 엔터키 이벤트 처리
    document.getElementById('emp_search_input').addEventListener('keypress', function(e) {
        if(e.which === 13 || e.keyCode === 13) {
            e.preventDefault();
            document.getElementById('emp_search_btn').click();
        }
    });
    
    // 문서 클릭 이벤트 (검색 결과 영역 외 클릭 시 결과 숨김)
    document.addEventListener('click', function(e) {
        const searchResults = document.getElementById('emp_search_results');
        const searchInput = document.getElementById('emp_search_input');
        const searchBtn = document.getElementById('emp_search_btn');
        
        if(searchResults && !searchResults.contains(e.target) && e.target !== searchInput && e.target !== searchBtn) {
            searchResults.innerHTML = '';
        }
    });
    
    // 알림 발송 - notification.js의 sendNotification 함수 사용
    $('#sendBtn').click(function() {
        const targetType = $('#targetType').val();
        let targetId = null;
        
        if(targetType === 'DEPARTMENT') {
            targetId = $('#deptNo').val();
            if(!targetId) {
                alertify.alert("부서를 선택해주세요.");
                return;
            }
        } else if(targetType === 'EMPLOYEE') {
            targetId = $('#selectedEmpNo').val();
            if(!targetId) {
                alertify.alert("직원을 검색하여 선택해주세요.");
                return;
            }
        }
        
        const notificationType = $('#notificationType').val();
        const title = $('#notificationTitle').val();
        const content = $('#notificationContent').val() || "";
        const refType = $('#refType').val();
        const refNo = $('#refNo').val();
        
        if(!title) {
            alertify.alert("알림 제목을 입력해주세요.");
            return;
        }
        
        // window.sendNotification 함수 호출 (notification.js에 구현)
        window.sendNotification({
            data: {
                targetType: targetType,
                targetId: targetId,
                notificationType: notificationType,
                title: title,
                content: content || "",
                refType: refType,
                refNo: refNo
            },
            success: function(response) {
                resetForm();
            },
            error: function(xhr) {
                alertify.error("알림 발송 중 오류가 발생했습니다: " + (xhr.responseJSON?.message || "알 수 없는 오류"));
            }
        });
    });
    
    // 폼 초기화
    function resetForm() {
        $('#targetType').val('ALL');
        $('.target-specific').hide();
        $('#deptNo').val($('#deptNo option:first').val());
        $('#selectedEmpNo').val('');
        $('#emp_selected_info').hide();
        $('#notificationType').val('NOTICE');
        $('#notificationTitle').val('');
        $('#notificationContent').val('');
        $('#refType').val('');
        $('#refNo').val('');
    }
    
    $('#resetBtn').click(resetForm);
    
    // 알림 보기 버튼
    $(document).on('click', '.notification-view-btn', function() {
        const no = $(this).data('no');
        const title = $(this).data('title');
        const content = $(this).data('content');
        const type = $(this).data('type');
        const refType = $(this).data('reftype');
        const refNo = $(this).data('refno');
        
        let typeText = '';
        switch(type) {
            case 'NOTICE': typeText = '공지사항'; break;
            case 'APPROVAL': typeText = '결재'; break;
            case 'TASK': typeText = '업무'; break;
            case 'CHAT': typeText = '채팅'; break;
            case 'SCHEDULE': typeText = '일정'; break;
            default: typeText = type;
        }
        
        let refText = '';
        if(refType && refNo) {
            refText = `<p><strong>연결 페이지:</strong> ${refType}/${refNo}</p>`;
        }
        
        alertify.alert(
            "알림 상세 보기", 
            `<h3>${title}</h3>
            <p><strong>유형:</strong> ${typeText}</p>
            <p><strong>내용:</strong> ${content}</p>
            ${refText}`
        );
    });
    
    // 알림 검색 - 현재 탭 유지
    $('#searchBtn').click(function(e) {
        e.preventDefault();
        const type = $('#typeFilter').val();
        const keyword = $('#searchKeyword').val();
        
        // 현재 활성화된 탭 가져오기
        const currentTab = $('.notification-tab.active').data('tab');
        
        // 탭 파라미터 포함하여 URL 이동
        location.href = contextPath + '/notificationAdmin?page=1&type=' + type + '&keyword=' + keyword + '&tab=' + currentTab;
    });
    
    // 페이지네이션 링크에도 현재 탭 파라미터 추가
    function updatePaginationLinks() {
        const currentTab = $('.notification-tab.active').data('tab');
        
        $('.notification-pagination a').each(function() {
            const href = $(this).attr('href');
            if(href && href !== '#') {
                const url = new URL(href, window.location.origin);
                url.searchParams.set('tab', currentTab);
                $(this).attr('href', url.pathname + url.search);
            }
        });
    }
    
    // 엔터키 이벤트 바인딩
    $('#searchKeyword').on('keypress', function(e) {
        if (e.which == 13) {
            $('#searchBtn').click();
        }
    });
    
    // 페이지 로드 시 페이지네이션 링크 업데이트
    updatePaginationLinks();
});
</script>
</body>
</html>