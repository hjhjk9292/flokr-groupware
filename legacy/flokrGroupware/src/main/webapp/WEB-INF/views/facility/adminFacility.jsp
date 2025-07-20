<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 대시보드 - 시설 관리</title>
  
  <!-- CSS 파일 -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/css/alertify.min.css">
  
  <style>
    .facility-container {
      padding: 20px;
      background-color: #f5f7fb;
    }
    
    .admin-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .admin-header h1 {
      color: #003561;
      font-size: 24px;
      margin: 0;
    }
    
    .admin-header h1 i {
      margin-right: 10px;
    }
    
    .tab-container {
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      margin-bottom: 20px;
    }
    
    .nav-tabs {
      display: flex;
      border-bottom: 1px solid #ddd;
      padding: 0;
      list-style: none;
      margin: 0;
    }
    
    .nav-tabs .nav-item {
      margin-bottom: -1px;
    }
    
    .nav-tabs .nav-link {
      display: block;
      padding: 15px 20px;
      text-decoration: none;
      color: #666;
      border: 1px solid transparent;
      border-top-left-radius: 8px;
      border-top-right-radius: 8px;
      transition: all 0.3s ease;
    }
    
    .nav-tabs .nav-link:hover {
      color: #003561;
      border-color: transparent;
    }
    
    .nav-tabs .nav-link.active {
      color: #003561;
      background-color: #fff;
      border-color: #ddd #ddd #fff;
      font-weight: 500;
    }
    
    .tab-content {
      padding: 20px;
    }
    
    .tab-pane {
      display: none;
    }
    
    .tab-pane.active {
      display: block;
    }
    
    .filter-container {
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      padding: 20px;
      margin-bottom: 20px;
    }
    
    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      margin-bottom: 15px;
    }
    
    .filter-item {
      flex: 1;
      min-width: 200px;
    }
    
    .filter-item label {
      display: block;
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #666;
    }
    
    .form-control {
      width: 50%;
      padding: 10px 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
      transition: border-color 0.3s ease;
    }
    
    .form-control:focus {
      border-color: #003561;
      outline: none;
    }
    
    .btn {
      display: inline-block;
      padding: 8px 16px;
      border-radius: 4px;
      font-size: 14px;
      font-weight: 500;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s ease;
      border: none;
    }
    
    .btn-primary {
      background-color: #003561;
      color: #fff;
    }
    
    .btn-primary:hover {
      background-color: #002a4d;
    }
    
    .btn-secondary {
      background-color: #f1f1f1;
      color: #666;
    }
    
    .btn-secondary:hover {
      background-color: #e5e5e5;
    }
    
    .btn-danger {
      background-color: #ea4335;
      color: #fff;
    }
    
    .btn-danger:hover {
      background-color: #d32f2f;
    }
    
    .btn-success {
      background-color: #4285f4;
      color: #fff;
    }
    
    .btn-success:hover {
      background-color: #3367d6;
    }
    
    .btn-warning {
      background-color: #f5a623;
      color: #fff;
    }
    
    .btn-warning:hover {
      background-color: #e59600;
    }
    
    .btn-sm {
      padding: 5px 10px;
      font-size: 12px;
    }
    
    .text-right {
      text-align: right;
    }
    
    .facility-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }
    
    .facility-card {
      background-color: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease;
    }
    
    .facility-card:hover {
      transform: translateY(-5px);
    }
    
    .facility-icon-container {
      height: 160px;
      width: 100%;
      background-color: #003561;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      color: #fff;
    }
    
    .facility-icon-container i {
      font-size: 64px;
    }
    
	.status-badge {
	    display: inline-block;
	    padding: 4px 10px;
	    border-radius: 20px;
	    font-size: 12px;
	    font-weight: 500;
	    color: white;
	    margin: 0;
	}
    
	.status-active {
	    background-color: #4285f4;
	}
	
	.status-inactive {
	    background-color: #ea4335;
	}
	
	.status-pending {
	    background-color: #f5a623;
	}
    
    .facility-content {
      padding: 15px;
    }
    
    .facility-name {
      margin-top: 0;
      margin-bottom: 10px;
      font-size: 18px;
      font-weight: 600;
      color: #003561;
    }
    
    .facility-info {
      margin-bottom: 15px;
    }
    
    .facility-info p {
      margin-bottom: 5px;
      color: #666;
      font-size: 14px;
      display: flex;
      align-items: center;
    }
    
    .facility-info i {
      width: 20px;
      margin-right: 5px;
      color: #888;
    }
    
    .facility-status {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .action-buttons {
      display: flex;
      gap: 5px;
    }
    
    .admin-table {
      width: 100%;
      border-collapse: collapse;
      background-color: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    
    .admin-table th, .admin-table td {
      padding: 12px 15px;
      text-align: left;
      border-bottom: 1px solid #eee;
    }
    
    .admin-table th {
      background-color: #f5f7fb;
      font-weight: 600;
      color: #555;
    }
    
    .admin-table tr:last-child td {
      border-bottom: none;
    }
    
    .admin-table tr:hover td {
      background-color: #f5f7fb;
    }
    
    .table-responsive {
      overflow-x: auto;
      margin-bottom: 20px;
    }
    
    .empty-state {
      padding: 40px;
      text-align: center;
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      margin-bottom: 20px;
    }
    
    .empty-state i {
      font-size: 48px;
      color: #ccc;
      margin-bottom: 15px;
    }
    
    .empty-state p {
      color: #888;
      font-size: 16px;
      margin: 0;
    }
    
    .modal {
      display: none;
      position: fixed;
      z-index: 1000;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      overflow: auto;
      background-color: rgba(0, 0, 0, 0.5);
    }
    
    .modal-content {
      background-color: #fff;
      margin: 10% auto;
      padding: 25px;
      border-radius: 8px;
      max-width: 600px;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
      position: relative;
    }
    
    .modal-close {
      position: absolute;
      right: 20px;
      top: 20px;
      font-size: 24px;
      cursor: pointer;
      color: #888;
      transition: color 0.3s ease;
    }
    
    .modal-close:hover {
      color: #003561;
    }
    
    .modal-title {
      margin-top: 0;
      margin-bottom: 20px;
      font-size: 20px;
      font-weight: 600;
      color: #003561;
    }
    
    .form-group {
      margin-bottom: 20px;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 8px;
      font-weight: 500;
      color: #555;
    }
    
    .btn-container {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 25px;
    }
    
    .info-row {
      margin-bottom: 15px;
      display: flex;
      flex-wrap: wrap;
    }
    
    .info-row .label {
      font-weight: 500;
      width: 120px;
      color: #666;
      flex-shrink: 0;
    }
    
    .info-row .value {
      flex: 1;
    }
    
    @media (max-width: 768px) {
      .filter-item {
        min-width: 100%;
      }
      
      .facility-grid {
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      }
      
      .modal-content {
        width: 90%;
        margin: 15% auto;
      }
    }
    
    .test-notification-btn:hover {
      background-color: #5a6268;
    }
  </style>
</head>
<body>

<!-- 헤더 포함 -->
<jsp:include page="../common/header.jsp" />

<div class="facility-container">
  <div class="admin-header">
    <h1><i class="fas fa-building"></i> 시설 관리</h1>
  </div>
  
  <div class="tab-container">
    <!-- 탭 네비게이션 -->
    <ul class="nav-tabs" id="facilityTabs">
      <li class="nav-item">
        <a class="nav-link" id="facilityListTab" data-toggle="tab" href="#facilityList">시설 현황</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="reservationListTab" data-toggle="tab" href="#reservationList">예약 관리</a>
      </li>
    </ul>
    
    <!-- 탭 컨텐츠 -->
    <div class="tab-content">
      <!-- 시설 현황 탭 -->
      <div class="tab-pane" id="facilityList">
        <!-- 필터링 영역 -->
        <div class="filter-container">
          <div class="filter-row">
            <div class="filter-item">
              <label for="facilityTypeFilter">시설 유형</label>
              <select id="facilityTypeFilter" class="form-control">
                <option value="">전체</option>
                <option value="MEETING_ROOM">회의실</option>
                <option value="EQUIPMENT">장비</option>
                <option value="VEHICLE">차량</option>
              </select>
            </div>
            <div class="filter-item">
              <label for="facilityStatusFilter">시설 상태</label>
              <select id="facilityStatusFilter" class="form-control">
                <option value="">전체</option>
                <option value="ACTIVE">사용 가능</option>
                <option value="INACTIVE">사용 불가</option>
                <option value="MAINTENANCE">점검 중</option>
              </select>
            </div>
            <div class="filter-item">
              <label for="facilitySearch">시설명 검색</label>
              <div class="input-group">
                <input type="text" id="facilitySearch" class="form-control" placeholder="시설명 검색...">
                <button class="btn btn-primary" id="searchFacilityBtn">
                  <i class="fas fa-search"></i> 검색
                </button>
              </div>
            </div>
          </div>
          <div class="text-right">
            <button class="btn btn-primary add-facility">
              <i class="fas fa-plus"></i> 시설 추가
            </button>
          </div>
        </div>
        
        <!-- 시설 카드 목록 -->
        <div class="facility-grid" id="facilityCardContainer">
          <c:choose>
            <c:when test="${empty facilities}">
              <div class="empty-state">
                <i class="fas fa-building"></i>
                <p>등록된 시설이 없습니다.</p>
              </div>
            </c:when>
            <c:otherwise>
              <c:forEach items="${facilities}" var="facility">
                <div class="facility-card" data-type="${facility.facilityType}" data-status="${facility.facilityStatus}">
                  <div class="facility-icon-container">
                    <c:choose>
                      <c:when test="${facility.facilityType eq 'MEETING_ROOM'}">
                        <i class="fas fa-chalkboard-teacher"></i>
                      </c:when>
                      <c:when test="${facility.facilityType eq 'EQUIPMENT'}">
                        <i class="fas fa-tools"></i>
                      </c:when>
                      <c:when test="${facility.facilityType eq 'VEHICLE'}">
                        <i class="fas fa-car"></i>
                      </c:when>
                      <c:otherwise>
                        <i class="fas fa-building"></i>
                      </c:otherwise>
                    </c:choose>
                    <c:choose>
                      <c:when test="${facility.facilityStatus eq 'ACTIVE'}">
                        <span class="status-badge status-active">사용 가능</span>
                      </c:when>
                      <c:when test="${facility.facilityStatus eq 'MAINTENANCE'}">
                        <span class="status-badge status-pending">점검 중</span>
                      </c:when>
                      <c:otherwise>
                        <span class="status-badge status-inactive">사용 불가</span>
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div class="facility-content">
                    <h3 class="facility-name">${facility.facilityName}</h3>
                    <div class="facility-info">
                      <c:if test="${not empty facility.capacity && facility.capacity > 0}">
                        <p><i class="fas fa-users"></i> 최대 ${facility.capacity}명 수용 가능</p>
                      </c:if>
                      <c:if test="${not empty facility.facilityLocation}">
                        <p><i class="fas fa-map-marker-alt"></i> ${facility.facilityLocation}</p>
                      </c:if>
                      <c:if test="${not empty facility.description}">
                        <p><i class="fas fa-info-circle"></i> ${facility.description}</p>
                      </c:if>
                    </div>
                    <div class="facility-status">
                      <div class="action-buttons">
                        <button class="btn btn-sm btn-primary edit-facility" data-id="${facility.facilityNo}">
                          <i class="fas fa-edit"></i> 수정
                        </button>
                        <button class="btn btn-sm btn-danger delete-facility" data-id="${facility.facilityNo}">
                          <i class="fas fa-trash"></i> 삭제
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
      
      <!-- 예약 관리 탭 -->
      <div class="tab-pane" id="reservationList">
        <!-- 필터링 영역 -->
        <div class="filter-container">
          <div class="filter-row">
            <div class="filter-item">
              <label for="facilityFilter">시설 선택</label>
              <select id="facilityFilter" class="form-control">
                <option value="">전체 시설</option>
                <c:forEach items="${facilities}" var="facility">
                  <option value="${facility.facilityNo}">${facility.facilityName}</option>
                </c:forEach>
              </select>
            </div>
            <div class="filter-item">
              <label for="statusFilter">예약 상태</label>
              <select id="statusFilter" class="form-control">
                <option value="">전체</option>
                <option value="PENDING">승인 대기</option>
                <option value="APPROVED">승인 완료</option>
                <option value="CANCELED">취소됨</option>
              </select>
            </div>
            <div class="filter-item">
              <label for="dateFilter">날짜 선택</label>
              <input type="text" id="dateFilter" class="form-control date-picker" placeholder="날짜 선택...">
            </div>
          </div>
          <div class="filter-row">
            <div class="filter-item">
              <label for="reserverSearch">예약자 검색</label>
              <div class="input-group">
                <input type="text" id="reserverSearch" class="form-control" placeholder="예약자명 또는 부서명 검색...">
                <button class="btn btn-primary" id="searchReservationBtn">
                  <i class="fas fa-search"></i> 검색
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 예약 목록 테이블 -->
        <div class="table-responsive">
          <table class="admin-table" id="reservationsTable">
            <thead>
              <tr>
                <th>예약번호</th>
                <th>시설명</th>
                <th>예약자</th>
                <th>예약 시간</th>
                <th>사용 목적</th>
                <th>예약 상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <!-- 예약 목록은 JavaScript에서 동적으로 로드 -->
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- 시설 추가/수정 모달 -->
<div class="modal" id="facilityModal">
  <div class="modal-content">
    <span class="modal-close">&times;</span>
    <h2 class="modal-title" id="facilityModalTitle">시설 추가</h2>
    <form id="facilityForm">
      <input type="hidden" id="facilityId" name="facilityNo" value="0">
      
      <div class="form-group">
        <label for="facilityName">시설명 *</label>
        <input type="text" id="facilityName" name="facilityName" class="form-control" required>
      </div>
      
      <div class="form-group">
        <label for="facilityLocation">위치</label>
        <input type="text" id="facilityLocation" name="facilityLocation" class="form-control">
      </div>
      
      <div class="form-group">
        <label for="facilityCapacity">수용 인원</label>
        <input type="number" id="facilityCapacity" name="capacity" class="form-control" min="0">
      </div>
      
      <div class="form-group">
        <label for="facilityType">시설 유형</label>
        <select id="facilityType" name="facilityType" class="form-control">
          <option value="MEETING_ROOM">회의실</option>
          <option value="EQUIPMENT">장비</option>
          <option value="VEHICLE">차량</option>
        </select>
      </div>
      
      <div class="form-group">
        <label for="facilityStatus">시설 상태</label>
        <select id="facilityStatus" name="facilityStatus" class="form-control">
          <option value="ACTIVE">사용 가능</option>
          <option value="INACTIVE">사용 불가</option>
          <option value="MAINTENANCE">점검 중</option>
        </select>
      </div>
      
      <div class="form-group">
        <label for="facilityDescription">상세 설명</label>
        <textarea id="facilityDescription" name="description" class="form-control" rows="3"></textarea>
      </div>
      
      <div class="btn-container">
        <button type="button" class="btn btn-secondary modal-close-btn">취소</button>
        <button type="submit" class="btn btn-primary">저장</button>
      </div>
    </form>
  </div>
</div>

<!-- 예약 상세 정보 모달 -->
<div class="modal" id="reservationModal">
  <div class="modal-content">
    <span class="modal-close">&times;</span>
    <h2 class="modal-title">예약 상세 정보</h2>
    <div id="reservationDetails">
      <div class="info-row">
        <span class="label">예약 번호:</span>
        <span class="value" id="resNo"></span>
      </div>
      <div class="info-row">
        <span class="label">시설명:</span>
        <span class="value" id="resFacility"></span>
      </div>
      <div class="info-row">
        <span class="label">예약자:</span>
        <span class="value" id="resReserver"></span>
      </div>
      <div class="info-row">
        <span class="label">부서:</span>
        <span class="value" id="resDept"></span>
      </div>
      <div class="info-row">
        <span class="label">직급:</span>
        <span class="value" id="resPosition"></span>
      </div>
      <div class="info-row">
        <span class="label">예약 시간:</span>
        <span class="value" id="resTime"></span>
      </div>
      <div class="info-row">
        <span class="label">사용 목적:</span>
        <span class="value" id="resPurpose"></span>
      </div>
      <div class="info-row">
        <span class="label">예약 상태:</span>
        <span class="value" id="resStatus"></span>
      </div>
      <div class="info-row">
        <span class="label">예약 일시:</span>
        <span class="value" id="resCreateDate"></span>
      </div>
      <div class="info-row">
        <span class="label">최종 수정:</span>
        <span class="value" id="resUpdateDate"></span>
      </div>
    </div>
    <div id="reservationActions" class="btn-container">
      <!-- 버튼들은 JavaScript에서 동적으로 추가 -->
    </div>
  </div>
</div>

<!-- JavaScript 파일 -->
<script src="${pageContext.request.contextPath}/resources/js/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
<script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/ko.js"></script>
<script src="https://cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/alertify.min.js"></script>

<script>
  $(document).ready(function() {
    // URL에서 tab 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const tabParam = urlParams.get('tab');
    
    // 탭 초기화 (URL 파라미터 기준)
    if (tabParam === 'reservationList') {
      $("#facilityListTab").removeClass("active");
      $("#reservationListTab").addClass("active");
      $("#facilityList").removeClass("active");
      $("#reservationList").addClass("active");
      
      // 예약 목록 로드
      searchReservations();
    } else {
      // 기본값은 시설 현황 탭
      $("#facilityListTab").addClass("active");
      $("#facilityList").addClass("active");
    }
    
    // 탭 전환
    $("#facilityTabs .nav-link").click(function(e) {
      e.preventDefault();
      
      // 모든 탭 비활성화
      $("#facilityTabs .nav-link").removeClass("active");
      $(".tab-pane").removeClass("active");
      
      // 클릭한 탭 활성화
      $(this).addClass("active");
      
      // 해당 탭 내용 표시
      var targetId = $(this).attr("href");
      $(targetId).addClass("active");
      
      // URL 업데이트 (페이지 새로고침 없이)
      const tabId = targetId.substring(1); // '#' 제거
      const url = new URL(window.location);
      url.searchParams.set('tab', tabId);
      window.history.pushState({}, '', url);
      
      // 예약 관리 탭이면 예약 목록 로드
      if (tabId === 'reservationList') {
        searchReservations();
      }
    });
    
    // 플랫피커 초기화 (날짜 선택기)
    flatpickr(".date-picker", {
      locale: "ko",
      dateFormat: "Y-m-d",
    });
    
    // 모달 닫기 (모든 모달에 적용)
    $(document).on('click', '.modal-close, .modal-close-btn', function() {
      $(this).closest(".modal").hide();
    });
    
    // 시설 추가 버튼 클릭
    $(".add-facility").click(function() {
      // 폼 초기화
      $("#facilityForm")[0].reset();
      $("#facilityId").val("0"); // 0으로 설정하여 새 시설임을 표시
      
      // 모달 타이틀 변경
      $("#facilityModalTitle").text("시설 추가");
      
      // 모달 표시
      $("#facilityModal").show();
    });
    
    // 시설 수정 버튼 클릭
    $(document).on('click', '.edit-facility', function(e) {
      e.stopPropagation();
      const facilityNo = $(this).data('id');
      
      // AJAX로 시설 정보 로드
      $.ajax({
        url: '${pageContext.request.contextPath}/getFacilityDetail',
        type: 'GET',
        data: { facilityNo: facilityNo },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
              const facility = response.facility;
              
              // 모달 폼에 데이터 채우기
              $("#facilityId").val(facility.facilityNo);
              $("#facilityName").val(facility.facilityName);
              $("#facilityLocation").val(facility.facilityLocation);
              $("#facilityCapacity").val(facility.capacity);
              $("#facilityDescription").val(facility.description);
              $("#facilityStatus").val(facility.facilityStatus || 'ACTIVE');
              $("#facilityType").val(facility.facilityType || 'MEETING_ROOM');
              
              // 모달 타이틀 변경
              $("#facilityModalTitle").text("시설 수정");
              
              // 모달 표시
              $("#facilityModal").show();
            } else {
              alertify.error(response.message || "시설 정보를 불러올 수 없습니다.");
            }
          },
          error: function(xhr, status, error) {
            alertify.error("시설 정보를 불러오는 중 오류가 발생했습니다.");
          }
        });
      });
      
      // 시설 삭제 버튼 클릭
      $(document).on('click', '.delete-facility', function(e) {
        e.stopPropagation();
        const facilityNo = $(this).data('id');
        
        if (confirm("정말로 이 시설을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
          // AJAX로 시설 삭제 요청
          $.ajax({
            url: '${pageContext.request.contextPath}/deleteFacility',
            type: 'POST',
            data: { facilityNo: facilityNo },
            dataType: 'json',
            success: function(response) {
              if (response.success) {
                alertify.success(response.message || "시설이 삭제되었습니다.");
                // 페이지 새로고침
                location.reload();
              } else {
                alertify.error(response.message || "시설 삭제에 실패했습니다.");
              }
            },
            error: function(xhr, status, error) {
              alertify.error("시설 삭제 중 오류가 발생했습니다.");
            }
          });
        }
      });
      
      // 시설 검색 버튼 클릭
      $("#searchFacilityBtn").click(function() {
        const keyword = $("#facilitySearch").val().trim();
        
        if (keyword) {
          // AJAX로 시설 검색 요청
          $.ajax({
            url: '${pageContext.request.contextPath}/getFacilities',
            type: 'GET',
            data: { keyword: keyword },
            dataType: 'json',
            success: function(response) {
              if (response.success) {
                updateFacilityCards(response.facilities);
              } else {
                alertify.error(response.message || "시설 검색에 실패했습니다.");
              }
            },
            error: function(xhr, status, error) {
              alertify.error("시설 검색 중 오류가 발생했습니다.");
            }
          });
        } else {
          // 검색어가 없으면 전체 목록 표시
          $.ajax({
            url: '${pageContext.request.contextPath}/getFacilities',
            type: 'GET',
            dataType: 'json',
            success: function(response) {
              if (response.success) {
                updateFacilityCards(response.facilities);
              }
            }
          });
        }
      });
      
      // 예약 검색 버튼 클릭
      $("#searchReservationBtn").click(function() {
        searchReservations();
      });
      
      // 예약 필터 변경 시 검색 실행
      $("#facilityFilter, #statusFilter, #dateFilter").change(function() {
        searchReservations();
      });
      
      // 시설 카드 필터링
      $("#facilityTypeFilter, #facilityStatusFilter").change(function() {
        filterFacilityCards();
      });
      
      // 시설 폼 제출
      $("#facilityForm").submit(function(e) {
        e.preventDefault();
        
        // FormData 생성
        const formData = new FormData(this);
        
        // 폼 유효성 검사
        if (!formData.get('facilityName')) {
          alertify.error("시설명은 필수 입력사항입니다.");
          return;
        }
        
        // facilityNo 확인 및 처리
        const facilityId = formData.get('facilityNo');
        const isNew = facilityId === '0' || facilityId === '' || facilityId === null;
        const url = isNew ? 
          '${pageContext.request.contextPath}/addFacility' : 
          '${pageContext.request.contextPath}/updateFacility';
        
        // AJAX로 폼 제출
        $.ajax({
          url: url,
          type: 'POST',
          data: formData,
          processData: false,
          contentType: false,
          success: function(response) {
            if (response.success) {
              alertify.success(response.message || "시설 정보가 저장되었습니다.");
              $("#facilityModal").hide();
              // 페이지 새로고침
              location.reload();
            } else {
              alertify.error(response.message || "시설 정보 저장에 실패했습니다.");
            }
          },
          error: function(xhr, status, error) {
            alertify.error("시설 정보 저장 중 오류가 발생했습니다: " + xhr.status);
          }
        });
      });
      
      // 예약 상세 보기 버튼 클릭
      $(document).on('click', '.view-reservation', function() {
        const reservationNo = $(this).data('id');
        
        // AJAX로 예약 상세 정보 로드
        $.ajax({
          url: '${pageContext.request.contextPath}/getReservationDetail',
          type: 'GET',
          data: { reservationNo: reservationNo },
          dataType: 'json',
          success: function(response) {
            if (response.success) {
              const reservation = response.reservation;
              
              // 모달에 데이터 채우기
              $("#resNo").text(reservation.RESERVATION_NO);
              $("#resFacility").text(reservation.FACILITY_NAME);
              $("#resReserver").text(reservation.RESERVER_NAME + " (" + reservation.RESERVER_ID + ")");
              $("#resDept").text(reservation.DEPT_NAME);
              $("#resPosition").text(reservation.POSITION_NAME);
              
              // 날짜 포맷팅
              const startTime = new Date(reservation.START_TIME);
              const endTime = new Date(reservation.END_TIME);
              const formattedStartTime = startTime.toLocaleDateString('ko-KR') + ' ' + startTime.toLocaleTimeString('ko-KR', {hour: '2-digit', minute:'2-digit'});
              const formattedEndTime = endTime.toLocaleTimeString('ko-KR', {hour: '2-digit', minute:'2-digit'});
              
              $("#resTime").text(formattedStartTime + ' ~ ' + formattedEndTime);
              $("#resPurpose").text(reservation.PURPOSE);
              
              // 상태 표시
              let statusText = '';
              let statusClass = '';
              let displayStatus = '';
              
              // 상태 결정 로직
              if (reservation.RES_STATUS === 'PENDING') {
                  displayStatus = 'PENDING';
                  statusText = '승인 대기';
                  statusClass = 'status-pending';
              } else if (reservation.RES_STATUS === 'APPROVED') {
                  displayStatus = 'APPROVED';
                  statusText = '승인 완료';
                  statusClass = 'status-active';
              } else if (reservation.RES_STATUS === 'CANCELED') {
                  displayStatus = 'CANCELED';
                  statusText = '취소됨';
                  statusClass = 'status-inactive';
              } else if (reservation.RES_STATUS === 'ACTIVE') {
                  // ACTIVE 상태인 경우 CREATE_DATE와 UPDATE_DATE를 비교해서 처리
                  const createDate = new Date(reservation.CREATE_DATE).getTime();
                  const updateDate = reservation.UPDATE_DATE ? new Date(reservation.UPDATE_DATE).getTime() : 0;
                  
                  if (!reservation.UPDATE_DATE || createDate === updateDate) {
                      displayStatus = 'PENDING';
                      statusText = '승인 대기';
                      statusClass = 'status-pending';
                  } else {
                      displayStatus = 'APPROVED';
                      statusText = '승인 완료';
                      statusClass = 'status-active';
                  }
              }
              
              $("#resStatus").html('<span class="status-badge ' + statusClass + '">' + statusText + '</span>');
              
              // 날짜 정보
              if (reservation.CREATE_DATE) {
                const createDate = new Date(reservation.CREATE_DATE);
                $("#resCreateDate").text(createDate.toLocaleDateString('ko-KR') + ' ' + createDate.toLocaleTimeString('ko-KR'));
              } else {
                $("#resCreateDate").text('-');
              }
              
              if (reservation.UPDATE_DATE) {
                const updateDate = new Date(reservation.UPDATE_DATE);
                $("#resUpdateDate").text(updateDate.toLocaleDateString('ko-KR') + ' ' + updateDate.toLocaleTimeString('ko-KR'));
              } else {
                $("#resUpdateDate").text('-');
              }
              
              // 액션 버튼 추가 - 모달에서는 승인/거절만 표시
              $("#reservationActions").empty();
              
              if (displayStatus === 'PENDING') {
                $("#reservationActions").append('<button type="button" class="btn btn-success approve-reservation-modal" data-id="' + reservation.RESERVATION_NO + '">승인</button> ');
                $("#reservationActions").append('<button type="button" class="btn btn-danger cancel-reservation-modal" data-id="' + reservation.RESERVATION_NO + '">승인 거절</button> ');
              } else if (displayStatus === 'APPROVED') {
                $("#reservationActions").append('<button type="button" class="btn btn-danger cancel-reservation-modal" data-id="' + reservation.RESERVATION_NO + '">승인 취소</button> ');
              }
              
              $("#reservationActions").append('<button type="button" class="btn btn-secondary modal-close-btn">닫기</button>');
              
              // 모달 표시
              $("#reservationModal").show();
            } else {
              alertify.error(response.message || "예약 정보를 불러올 수 없습니다.");
            }
          },
          error: function(xhr, status, error) {
            alertify.error("예약 정보를 불러오는 중 오류가 발생했습니다.");
          }
        });
      });
      
      // 예약 승인 버튼 클릭 (테이블에서)
      $(document).on('click', '.approve-reservation', function() {
        const reservationNo = $(this).data('id');
        updateReservationStatus(reservationNo, 'APPROVED');
      });
      
      // 예약 취소 버튼 클릭 (테이블에서)
      $(document).on('click', '.cancel-reservation', function() {
        const reservationNo = $(this).data('id');
        if (confirm("정말로 이 예약 요청을 거절하시겠습니까?")) {
          updateReservationStatus(reservationNo, 'CANCELED');
        }
      });
      
      // 예약 승인 버튼 클릭 (모달에서)
      $(document).on('click', '.approve-reservation-modal', function() {
        const reservationNo = $(this).data('id');
        updateReservationStatus(reservationNo, 'APPROVED', true);
      });
      
      // 예약 취소 버튼 클릭 (모달에서)
      $(document).on('click', '.cancel-reservation-modal', function() {
        const reservationNo = $(this).data('id');
        if (confirm("정말로 이 예약 요청을 거절하시겠습니까?")) {
          updateReservationStatus(reservationNo, 'CANCELED', true);
        }
      });
      
      // 시설 카드 업데이트 함수
      function updateFacilityCards(facilities) {
        const container = $("#facilityCardContainer");
        container.empty();
        
        if (facilities.length === 0) {
          container.html('<div class="empty-state"><i class="fas fa-search"></i><p>검색 결과가 없습니다.</p></div>');
          return;
        }
        
        facilities.forEach(function(facility) {
          let statusBadge = '';
          if (facility.facilityStatus === 'ACTIVE') {
            statusBadge = '<span class="status-badge status-active">사용 가능</span>';
          } else if (facility.facilityStatus === 'MAINTENANCE') {
            statusBadge = '<span class="status-badge status-pending">점검 중</span>';
          } else {
            statusBadge = '<span class="status-badge status-inactive">사용 불가</span>';
          }
          
          let typeIcon = '';
          switch(facility.facilityType) {
            case 'MEETING_ROOM':
              typeIcon = '<i class="fas fa-chalkboard-teacher"></i>';
              break;
            case 'EQUIPMENT':
              typeIcon = '<i class="fas fa-tools"></i>';
              break;
            case 'VEHICLE':
              typeIcon = '<i class="fas fa-car"></i>';
              break;
            default:
              typeIcon = '<i class="fas fa-building"></i>';
          }
          
          let capacityHtml = '';
          if (facility.capacity && facility.capacity > 0) {
            capacityHtml = '<p><i class="fas fa-users"></i> 최대 ' + facility.capacity + '명 수용 가능</p>';
          }
          
          let locationHtml = '';
          if (facility.facilityLocation) {
            locationHtml = '<p><i class="fas fa-map-marker-alt"></i> ' + facility.facilityLocation + '</p>';
          }
          
          let descriptionHtml = '';
          if (facility.description) {
            descriptionHtml = '<p><i class="fas fa-info-circle"></i> ' + facility.description + '</p>';
          }
          
          const card = $('<div class="facility-card" data-type="' + facility.facilityType + '" data-status="' + facility.facilityStatus + '">' +
            '<div class="facility-icon-container">' + typeIcon + statusBadge + '</div>' +
            '<div class="facility-content">' +
            '<h3 class="facility-name">' + facility.facilityName + '</h3>' +
            '<div class="facility-info">' + capacityHtml + locationHtml + descriptionHtml + '</div>' +
            '<div class="facility-status">' +
            '<div class="action-buttons">' +
            '<button class="btn btn-sm btn-primary edit-facility" data-id="' + facility.facilityNo + '"><i class="fas fa-edit"></i> 수정</button>' +
            '<button class="btn btn-sm btn-danger delete-facility" data-id="' + facility.facilityNo + '"><i class="fas fa-trash"></i> 삭제</button>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>');
          
          container.append(card);
        });
        
        // 필터링 적용
        filterFacilityCards();
      }
      
      // 시설 카드 필터링 함수
      function filterFacilityCards() {
        const typeFilter = $("#facilityTypeFilter").val();
        const statusFilter = $("#facilityStatusFilter").val();
        
        $(".facility-card").each(function() {
          const type = $(this).data('type');
          const status = $(this).data('status');
          
          let show = true;
          
          if (typeFilter && type !== typeFilter) {
            show = false;
          }
          
          if (statusFilter && status !== statusFilter) {
            show = false;
          }
          
          $(this).toggle(show);
        });
        
        // 필터링 결과가 없을 경우 메시지 표시
        if ($(".facility-card:visible").length === 0) {
          if ($("#facilityCardContainer .empty-state").length === 0) {
            $("#facilityCardContainer").append('<div class="empty-state"><i class="fas fa-filter"></i><p>필터링 결과가 없습니다.</p></div>');
          }
        } else {
          $("#facilityCardContainer .empty-state").remove();
        }
      }
      
      // 예약 검색 및 테이블 업데이트 함수
      function searchReservations() {
        const facilityNo = $("#facilityFilter").val();
        const status = $("#statusFilter").val();
        const date = $("#dateFilter").val();
        const keyword = $("#reserverSearch").val().trim();
        
        // AJAX로 예약 검색 요청
        $.ajax({
          url: '${pageContext.request.contextPath}/getReservations',
          type: 'GET',
          data: {
            facilityNo: facilityNo || null,
            status: status || null,
            date: date || null,
            keyword: keyword || null
          },
          dataType: 'json',
          success: function(response) {
            if (response.success) {
              updateReservationTable(response.reservations);
            } else {
              alertify.error(response.message || "예약 검색에 실패했습니다.");
            }
          },
          error: function(xhr, status, error) {
            alertify.error("예약 검색 중 오류가 발생했습니다.");
          }
        });
      }
      
      // 예약 테이블 업데이트 함수
      function updateReservationTable(reservations) {
        const tbody = $("#reservationsTable tbody");
        tbody.empty();
        
        if (!reservations || reservations.length === 0) {
          tbody.html('<tr><td colspan="7" class="text-center">검색 결과가 없습니다.</td></tr>');
          return;
        }
        
        reservations.forEach(function(reservation) {
          // 예약 상태에 따른 뱃지 설정
          let statusBadge = '';
          let displayStatus = '';
          
          // 상태 결정 로직
          if (reservation.RES_STATUS === 'PENDING') {
              displayStatus = 'PENDING';
              statusBadge = '<span class="status-badge status-pending">승인 대기</span>';
          } else if (reservation.RES_STATUS === 'APPROVED') {
              displayStatus = 'APPROVED';
              statusBadge = '<span class="status-badge status-active">승인 완료</span>';
          } else if (reservation.RES_STATUS === 'CANCELED') {
              displayStatus = 'CANCELED';
              statusBadge = '<span class="status-badge status-inactive">취소됨</span>';
          } else if (reservation.RES_STATUS === 'ACTIVE') {
              // ACTIVE 상태인 경우 CREATE_DATE와 UPDATE_DATE를 비교해서 처리
              const createDate = new Date(reservation.CREATE_DATE).getTime();
              const updateDate = reservation.UPDATE_DATE ? new Date(reservation.UPDATE_DATE).getTime() : 0;
              
              if (!reservation.UPDATE_DATE || createDate === updateDate) {
                  displayStatus = 'PENDING';
                  statusBadge = '<span class="status-badge status-pending">승인 대기</span>';
              } else {
                  displayStatus = 'APPROVED';
                  statusBadge = '<span class="status-badge status-active">승인 완료</span>';
              }
          }
          
          // 액션 버튼 설정
          let actionButtons = '<button class="btn btn-sm btn-secondary view-reservation" data-id="' + reservation.RESERVATION_NO + '"><i class="fas fa-eye"></i></button> ';
          
          // PENDING 상태일 때만 승인 버튼 표시
          if (displayStatus === 'PENDING') {
            actionButtons += '<button class="btn btn-sm btn-success approve-reservation" data-id="' + reservation.RESERVATION_NO + '"><i class="fas fa-check"></i></button> ';
          }
          
          // PENDING 또는 APPROVED 상태일 때만 취소 버튼 표시
          if (displayStatus === 'PENDING' || displayStatus === 'APPROVED') {
            actionButtons += '<button class="btn btn-sm btn-danger cancel-reservation" data-id="' + reservation.RESERVATION_NO + '"><i class="fas fa-times"></i></button>';
          }
          
          // 예약 시간 포맷팅
          const startTime = new Date(reservation.START_TIME);
          const endTime = new Date(reservation.END_TIME);
          const formattedStartTime = startTime.toLocaleDateString('ko-KR') + ' ' + startTime.toLocaleTimeString('ko-KR', {hour: '2-digit', minute:'2-digit'});
          const formattedEndTime = endTime.toLocaleTimeString('ko-KR', {hour: '2-digit', minute:'2-digit'});
          
          // 행 생성
          const row = $('<tr data-reservation-id="' + reservation.RESERVATION_NO + '">' +
            '<td>' + (reservation.RESERVATION_NO || '') + '</td>' +
            '<td>' + (reservation.FACILITY_NAME || '') + '</td>' +
            '<td>' + (reservation.RESERVER_NAME || '') + ' (' + (reservation.DEPT_NAME || '') + ')</td>' +
            '<td>' + formattedStartTime + ' ~ ' + formattedEndTime + '</td>' +
            '<td>' + (reservation.PURPOSE || '') + '</td>' +
            '<td>' + statusBadge + '</td>' +
            '<td><div class="action-buttons">' + actionButtons + '</div></td>' +
            '</tr>');
          
          tbody.append(row);
        });
      }
      
      // 예약 상태 업데이트 함수
      function updateReservationStatus(reservationNo, status, closeModal = false) {
        // AJAX로 예약 상태 변경 요청
        $.ajax({
          url: '${pageContext.request.contextPath}/updateReservationStatus',
          type: 'POST',
          data: {
            reservationNo: reservationNo,
            status: status
          },
          dataType: 'json',
          success: function(response) {
            if (response.success) {
              let message = "";
              if (status === 'APPROVED') {
                message = "예약이 승인되었습니다.";
              } else if (status === 'CANCELED') {
                message = "예약이 거절되었습니다.";
              } else {
                message = "예약 상태가 변경되었습니다.";
              }
              
              alertify.success(message);
              
              // UI 즉시 업데이트
              updateReservationStatusInUI(reservationNo, status);
              
              if (closeModal) {
                $("#reservationModal").hide();
              }
            } else {
              alertify.error(response.message || "예약 상태 변경에 실패했습니다.");
            }
          },
          error: function(xhr, status, error) {
            alertify.error("예약 상태 변경 중 오류가 발생했습니다.");
          }
        });
      }
      
	// UI에서 예약 상태 즉시 업데이트 함수
	function updateReservationStatusInUI(reservationNo, status) {
	  // 테이블에서 해당 예약 행 찾기
	  const row = $("tr[data-reservation-id='" + reservationNo + "']");
	  
	  if (row.length > 0) {
	    // 상태 뱃지 컬럼 업데이트
	    const statusCell = row.find("td:eq(5)");
	    let badgeHtml = '';
	    
	    switch(status) {
	      case 'APPROVED':
	        badgeHtml = '<span class="status-badge status-active">승인 완료</span>';
	        break;
	      case 'CANCELED':
	        badgeHtml = '<span class="status-badge status-inactive">취소됨</span>';
	        break;
	      case 'PENDING':
	        badgeHtml = '<span class="status-badge status-pending">승인 대기</span>';
	        break;
	    }
	    
	    statusCell.html(badgeHtml);
	    
	    // 액션 버튼 업데이트
	    const actionCell = row.find("td:eq(6) .action-buttons");
	    let actionButtons = '<button class="btn btn-sm btn-secondary view-reservation" data-id="' + reservationNo + '"><i class="fas fa-eye"></i></button> ';
	    
	    if (status === 'PENDING') {
	      actionButtons += '<button class="btn btn-sm btn-success approve-reservation" data-id="' + reservationNo + '"><i class="fas fa-check"></i></button> ';
	      actionButtons += '<button class="btn btn-sm btn-danger cancel-reservation" data-id="' + reservationNo + '"><i class="fas fa-times"></i></button>';
	    } else if (status === 'APPROVED') {
	      actionButtons += '<button class="btn btn-sm btn-danger cancel-reservation" data-id="' + reservationNo + '"><i class="fas fa-times"></i></button>';
	    }
	    
	    actionCell.html(actionButtons);
	  } else {
	    console.log("예약 행을 찾을 수 없어 테이블을 다시 로드합니다.");
	    // 행을 찾지 못한 경우 전체 테이블 다시 로드
	    searchReservations();
	  }
	}
      
      // 페이지 로드 시 예약 목록 초기화
      if (tabParam === 'reservationList') {
        searchReservations();
      }
    });
</script>

</body>
</html>