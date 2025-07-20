<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>시설 예약</title>
  
  <!-- jQuery 라이브러리 -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
  
  <!-- Bootstrap CDN -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
  
  <!-- Font Awesome Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  
  <!-- Flatpickr 날짜 선택기 -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/ko.js"></script>
  
  <!-- Alertify -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/css/alertify.min.css">
  <script src="https://cdn.jsdelivr.net/npm/alertifyjs@1.13.1/build/alertify.min.js"></script>
  
  <style> 
    .facility-container {
      padding: 20px;
      background-color: #f5f7fb;
    }
    
    .user-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .user-header h1 {
      color: #003561;
      font-size: 24px;
      margin: 0;
    }
    
    .user-header h1 i {
      margin-right: 10px;
    }
    
    .tab-container {
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      margin-bottom: 20px;
    }
    
    .facility-nav-tabs {
      display: flex;
      border-bottom: 1px solid #ddd;
      padding: 0;
      list-style: none;
      margin: 0;
    }
    
    .facility-nav-tabs .facility-nav-item {
      margin-bottom: -1px;
    }
    
    .facility-nav-tabs .facility-nav-link {
      display: block;
      padding: 15px 20px;
      text-decoration: none;
      color: #666;
      border: 1px solid transparent;
      border-top-left-radius: 8px;
      border-top-right-radius: 8px;
      transition: all 0.3s ease;
      cursor: pointer;
    }
    
    .facility-nav-tabs .facility-nav-link:hover {
      color: #003561;
      border-color: transparent;
    }
    
    .facility-nav-tabs .facility-nav-link.active {
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
    
    .facility-actions {
      display: flex;
      justify-content: center;
    }
    
    .facility-table {
      width: 100%;
      border-collapse: collapse;
      background-color: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    
    .facility-table th, .facility-table td {
      padding: 12px 15px;
      text-align: left;
      border-bottom: 1px solid #eee;
    }
    
    .facility-table th {
      background-color: #f5f7fb;
      font-weight: 600;
      color: #555;
    }
    
    .facility-table tr:last-child td {
      border-bottom: none;
    }
    
    .facility-table tr:hover td {
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
    
    .date-time-picker {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
    
    .date-time-picker .time-picker {
      flex: 1;
      min-width: 120px;
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
  </style>
</head>
<body>

<!-- 헤더 포함 -->
<jsp:include page="../common/header.jsp" />

<div class="facility-container">
  <div class="user-header">
    <h1><i class="fas fa-calendar-check"></i> 시설 예약</h1>
  </div>
  
  <div class="tab-container">
    <!-- 탭 네비게이션 -->
    <ul class="facility-nav-tabs" id="facilityTabs">
      <li class="facility-nav-item">
        <a class="facility-nav-link active" href="#facilityList">시설 목록</a>
      </li>
      <li class="facility-nav-item">
        <a class="facility-nav-link" href="#myReservations">내 예약</a>
      </li>
    </ul>
    
    <!-- 탭 컨텐츠 -->
    <div class="tab-content">
      <!-- 시설 목록 탭 -->
      <div class="tab-pane active" id="facilityList">
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
              <label for="dateFilter">날짜 선택</label>
              <input type="text" id="dateFilter" class="form-control date-picker" placeholder="예약 날짜 선택...">
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
        </div>
        
        <!-- 시설 카드 목록 -->
        <div class="facility-grid" id="facilityCardContainer">
          <!-- 자바스크립트로 동적으로 로드됩니다 -->
          <div class="empty-state">
            <i class="fas fa-spinner fa-spin"></i>
            <p>시설 목록을 불러오는 중입니다...</p>
          </div>
        </div>
      </div>
      
      <!-- 내 예약 탭 -->
      <div class="tab-pane" id="myReservations">
        <!-- 필터링 영역 -->
        <div class="filter-container">
          <div class="filter-row">
            <div class="filter-item">
              <label for="myStatusFilter">예약 상태</label>
              <select id="myStatusFilter" class="form-control">
                <option value="">전체</option>
                <option value="PENDING">승인 대기</option>
                <option value="APPROVED">승인 완료</option>
                <option value="CANCELED">취소됨</option>
              </select>
            </div>
            <div class="filter-item">
              <label for="myDateFilter">날짜 선택</label>
              <input type="text" id="myDateFilter" class="form-control date-picker" placeholder="날짜 선택...">
            </div>
            <div class="filter-item">
              <label for="myFacilityFilter">시설 선택</label>
              <select id="myFacilityFilter" class="form-control">
                <option value="">전체 시설</option>
                <!-- 자바스크립트로 동적으로 로드됩니다 -->
              </select>
            </div>
          </div>
        </div>
        
        <!-- 예약 목록 테이블 -->
        <div class="table-responsive">
          <table class="facility-table" id="myReservationsTable">
            <thead>
              <tr>
                <th>예약번호</th>
                <th>시설명</th>
                <th>예약 시간</th>
                <th>사용 목적</th>
                <th>예약 상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <!-- 자바스크립트로 동적으로 로드됩니다 -->
              <tr>
                <td colspan="6" class="text-center">예약 내역을 불러오는 중입니다...</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- 예약 모달 -->
<div class="modal" id="reservationModal">
  <div class="modal-content">
    <span class="modal-close">&times;</span>
    <h2 class="modal-title" id="reservationModalTitle">시설 예약</h2>
    <form id="reservationForm">
      <input type="hidden" id="facilityId" name="facilityNo">
      
      <div class="form-group">
        <label for="facilityNameDisplay">시설명</label>
        <input type="text" id="facilityNameDisplay" class="form-control" readonly style="background-color: #f9f9f9;">
      </div>
      
      <div class="form-group">
        <label for="reservationDate">예약 날짜</label>
        <input type="text" id="reservationDate" name="reservationDate" class="form-control date-picker" placeholder="날짜 선택" required>
      </div>
      
      <div class="form-group">
        <label>예약 시간</label>
        <div class="row">
          <div class="col-5">
            <select id="startTime" name="startTime" class="form-control" required>
              <option value="">시작 시간</option>
              <option value="09:00">09:00</option>
              <option value="10:00">10:00</option>
              <option value="11:00">11:00</option>
              <option value="12:00">12:00</option>
              <option value="13:00">13:00</option>
              <option value="14:00">14:00</option>
              <option value="15:00">15:00</option>
              <option value="16:00">16:00</option>
              <option value="17:00">17:00</option>
              <option value="18:00">18:00</option>
            </select>
          </div>
          <div class="col-2 text-center d-flex align-items-center justify-content-center">
            <span>~</span>
          </div>
          <div class="col-5">
            <select id="endTime" name="endTime" class="form-control" required>
              <option value="">종료 시간</option>
              <option value="10:00">10:00</option>
              <option value="11:00">11:00</option>
              <option value="12:00">12:00</option>
              <option value="13:00">13:00</option>
              <option value="14:00">14:00</option>
              <option value="15:00">15:00</option>
              <option value="16:00">16:00</option>
              <option value="17:00">17:00</option>
              <option value="18:00">18:00</option>
              <option value="19:00">19:00</option>
            </select>
          </div>
        </div>
      </div>
      
      <div class="form-group">
        <label for="purpose">사용 목적</label>
        <textarea id="purpose" name="purpose" class="form-control" rows="3" required placeholder="사용 목적을 입력해주세요"></textarea>
      </div>
      
      <div class="btn-container">
        <button type="button" class="btn btn-secondary modal-close-btn">취소</button>
        <button type="submit" class="btn btn-primary">예약 신청</button>
      </div>
    </form>
  </div>
</div>

<!-- 예약 상세 정보 모달 -->
<div class="modal" id="reservationDetailModal">
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
    </div>
    <div id="reservationActions" class="btn-container">
      <!-- 버튼들은 JavaScript에서 동적으로 추가 -->
    </div>
  </div>
</div>

<script>
$(document).ready(function() {
  // URL에서 탭 매개변수 가져오기
  const urlParams = new URLSearchParams(window.location.search);
  const tabParam = urlParams.get('tab');
  
  // 날짜 선택기 초기화
  initDatePickers();
  
  // 모달 관련 이벤트 초기화
  initModalEvents();
  
  // 탭 관련 이벤트 초기화
  initTabEvents();
  
  // 시설 목록 이벤트 초기화
  initFacilityEvents();
  
  // 예약 관련 이벤트 초기화
  initReservationEvents();
  
  // 만약 탭 매개변수가 있으면 해당 탭 활성화
  if (tabParam === 'myReservations') {
    $("#facilityTabs .facility-nav-link[href='#myReservations']").trigger('click');
  } else {
    // 시설 목록 탭이 기본값
    loadFacilities();
  }
  
  // 날짜 선택기 초기화 함수
  function initDatePickers() {
    flatpickr(".date-picker", {
      locale: "ko",
      dateFormat: "Y-m-d",
      minDate: "today",
      onChange: function(selectedDates, dateStr, instance) {
        // 날짜 변경 시 필터 적용
        if (instance.element.id === 'dateFilter') {
          loadFacilities();
        } else if (instance.element.id === 'myDateFilter') {
          loadMyReservations();
        }
      }
    });
  }
  
  // 모달 이벤트 초기화 함수
  function initModalEvents() {
    
    // 모달 닫기 버튼 이벤트
    $(document).on('click', '.modal-close, .modal-close-btn', function() {
      const modalId = $(this).closest('.modal').attr('id');
      $('#' + modalId).hide();
    });
    
    // 백그라운드 클릭시 모달 닫기 (옵션)
    $(document).on('click', '.modal', function(e) {
      if (e.target === this) {
        $(this).hide();
      }
    });
    
    // ESC 키로 모달 닫기
    $(document).keydown(function(e) {
      if (e.key === "Escape") {
        $('.modal:visible').hide();
      }
    });
  }
  
  // 탭 이벤트 초기화 함수
  function initTabEvents() {
    // jQuery 방식으로 변경된 탭 전환 코드
    $("#facilityTabs .facility-nav-link").on('click', function(e) {
      e.preventDefault();
      
      // 모든 탭 비활성화
      $("#facilityTabs .facility-nav-link").removeClass("active");
      $(".tab-pane").removeClass("active");
      
      // 클릭한 탭 활성화
      $(this).addClass("active");
      
      // 해당 탭 내용 표시 - jQuery 방식으로 변경
      var targetId = $(this).attr("href");
      $(targetId).addClass("active");
      
      // URL 업데이트 (탭 변경 시)
      const tabName = targetId.substring(1); // #facilityList -> facilityList
      const url = new URL(window.location);
      url.searchParams.set('tab', tabName);
      window.history.pushState({}, '', url);
      
      // 탭 변경 시 데이터 로드
      if (targetId === '#facilityList') {
        loadFacilities();
      } else if (targetId === '#myReservations') {
        loadMyReservations();
        loadFacilitiesForFilter();
      }
    });
  }
  
  // 시설 관련 이벤트 초기화
  function initFacilityEvents() {
    // 시설 검색 버튼 클릭
    $("#searchFacilityBtn").click(function() {
      loadFacilities();
    });
    
    // 시설 검색창 엔터 키 이벤트
    $("#facilitySearch").keypress(function(e) {
      if (e.which === 13) {
        loadFacilities();
        return false;
      }
    });
    
    // 시설 필터링 (유형 선택 변경 시)
    $("#facilityTypeFilter").change(function() {
      loadFacilities();
    });
    
    // 시설 예약 버튼 클릭
    $(document).on('click', '.reserve-facility', function() {
      const facilityNo = $(this).data('id');
      openReservationModal(facilityNo);
    });
  }
  
  // 예약 관련 이벤트 초기화
  function initReservationEvents() {
    // 예약 상태 필터 변경 이벤트
    $("#myStatusFilter").change(function() {
      loadMyReservations();
    });
    
    // 시설 필터 변경 이벤트
    $("#myFacilityFilter").change(function() {
      loadMyReservations();
    });
    
    // 내 예약 상세 보기 버튼 클릭
    $(document).on('click', '.view-my-reservation', function() {
      const reservationNo = $(this).data('id');
      openReservationDetailModal(reservationNo);
    });
    
	// 내 예약 취소 버튼 클릭 (테이블에서)
    $(document).on('click', '.cancel-my-reservation', function() {
      const reservationNo = $(this).data('id');
      if (confirm("정말로 이 예약을 취소하시겠습니까?")) {
        cancelReservation(reservationNo);
      }
      });
    
    // 내 예약 취소 버튼 클릭 (모달에서)
    $(document).on('click', '.cancel-my-reservation-modal', function() {
      const reservationNo = $(this).data('id');
      if (confirm("정말로 이 예약을 취소하시겠습니까?")) {
        cancelReservation(reservationNo, true);
      }
    });
    
    // 예약 수정 버튼 클릭 (모달에서)
    $(document).on('click', '.edit-my-reservation-modal', function() {
      const reservationNo = $(this).data('id');
      openEditReservationModal(reservationNo);
    });
    
    // 예약 폼 제출
    $("#reservationForm").submit(function(e) {
      e.preventDefault();
      submitReservationForm();
    });
  }
  
  // 예약 모달 열기
  function openReservationModal(facilityNo) {    
    // 편집 모드 초기화
    $("#reservationForm").removeData('edit-id');
    $("#reservationModalTitle").text("시설 예약");
    
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
          $("#facilityNameDisplay").val(facility.facilityName);
          
          // 폼 초기화 - 오늘 날짜 기본 설정
          const today = new Date();
          const formattedDate = today.getFullYear() + '-' + 
                        String(today.getMonth() + 1).padStart(2, '0') + '-' + 
                        String(today.getDate()).padStart(2, '0');
          $("#reservationDate").val(formattedDate);
          $("#startTime").val("");
          $("#endTime").val("");
          $("#purpose").val("");
          
          // 모달 표시
          $("#reservationModal").show();
        } else {
          alertify.error(response.message || "시설 정보를 불러올 수 없습니다.");
        }
      },
      error: function(xhr, status, error) {
        alertify.error("시설 정보를 불러오는 중 오류가 발생했습니다.");
      }
    });
  }
  
  // 예약 수정 모달 열기
  function openEditReservationModal(reservationNo) {
    $("#reservationDetailModal").hide();
    
    $.ajax({
      url: '${pageContext.request.contextPath}/getReservationDetail',
      type: 'GET',
      data: { reservationNo: reservationNo },
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          const reservation = response.reservation;
          
          // 시설 정보 가져오기
          $.ajax({
            url: '${pageContext.request.contextPath}/getFacilityDetail',
            type: 'GET',
            data: { facilityNo: reservation.FACILITY_NO },
            dataType: 'json',
            success: function(facilityResponse) {
              if (facilityResponse.success) {
                const facility = facilityResponse.facility;
                
                // 예약 수정 모달 데이터 채우기
                $("#facilityId").val(facility.facilityNo);
                $("#facilityNameDisplay").val(facility.facilityName);
                
                // 날짜와 시간 설정
                const startTime = new Date(reservation.START_TIME);
                const endTime = new Date(reservation.END_TIME);
                
                // 날짜
                const year = startTime.getFullYear();
                const month = String(startTime.getMonth() + 1).padStart(2, '0');
                const day = String(startTime.getDate()).padStart(2, '0');
                const formattedDate = `${year}-${month}-${day}`;
                
                // 시간
                const startHour = String(startTime.getHours()).padStart(2, '0');
                const startMinute = String(startTime.getMinutes()).padStart(2, '0');
                const endHour = String(endTime.getHours()).padStart(2, '0');
                const endMinute = String(endTime.getMinutes()).padStart(2, '0');
                
                $("#reservationDate").val(formattedDate);
                $("#startTime").val(`${startHour}:${startMinute}`);
                $("#endTime").val(`${endHour}:${endMinute}`);
                $("#purpose").val(reservation.PURPOSE);
                
                // 모달 타이틀 변경
                $("#reservationModalTitle").text("예약 수정");
                
                // 수정중인 예약 ID 저장
                $("#reservationForm").data('edit-id', reservationNo);
                
                // 모달 표시
                $("#reservationModal").show();
              } else {
                alertify.error(facilityResponse.message || "시설 정보를 불러올 수 없습니다.");
              }
            },
            error: function(xhr, status, error) {
              alertify.error("시설 정보를 불러오는 중 오류가 발생했습니다.");
            }
          });
        } else {
          alertify.error(response.message || "예약 정보를 불러올 수 없습니다.");
        }
      },
      error: function(xhr, status, error) {
        alertify.error("예약 정보를 불러오는 중 오류가 발생했습니다.");
      }
    });
  }
  
  // 예약 상세 정보 모달 열기
  function openReservationDetailModal(reservationNo) {
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
          $("#resReserver").text(reservation.RESERVER_NAME);
          $("#resDept").text(reservation.DEPT_NAME);
          
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
          
          // 개선된 상태 표시 로직
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
            // ACTIVE 상태인 경우 CREATE_DATE와 UPDATE_DATE를 비교
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
          
          // 액션 버튼 추가
          $("#reservationActions").empty();
          
          // 수정/취소 버튼 추가 (승인 대기 중이거나, 승인 완료된 경우에만)
          if (displayStatus === 'PENDING' || displayStatus === 'APPROVED') {
            $("#reservationActions").append('<button type="button" class="btn btn-primary edit-my-reservation-modal" data-id="' + reservation.RESERVATION_NO + '">예약 수정</button> ');
            $("#reservationActions").append('<button type="button" class="btn btn-danger cancel-my-reservation-modal" data-id="' + reservation.RESERVATION_NO + '">예약 취소</button> ');
          }
          
          $("#reservationActions").append('<button type="button" class="btn btn-secondary modal-close-btn">닫기</button>');
          
          // 모달 표시
          $("#reservationDetailModal").show();
        } else {
          alertify.error(response.message || "예약 정보를 불러올 수 없습니다.");
        }
      },
      error: function(xhr, status, error) {
        alertify.error("예약 정보를 불러오는 중 오류가 발생했습니다.");
      }
    });
  }
  
  // 예약 폼 제출 처리
  function submitReservationForm() {
    // 폼 데이터 수집
    const facilityNo = $("#facilityId").val();
    const reservationDate = $("#reservationDate").val();
    const startTime = $("#startTime").val();
    const endTime = $("#endTime").val();
    const purpose = $("#purpose").val().trim();
    
    // 필수 필드 검증
    if (!reservationDate || !startTime || !endTime || !purpose) {
      alertify.error("모든 필수 항목을 입력해주세요.");
      return;
    }
    
    // 시간 유효성 확인 (간단히 문자열 비교)
    if (startTime >= endTime) {
      alertify.error("종료 시간은 시작 시간보다 이후여야 합니다.");
      return;
    }
    
    // 영업 시간 확인 (9시~19시)
    const startTimeHour = parseInt(startTime.split(':')[0]);
    const endTimeHour = parseInt(endTime.split(':')[0]);
    
    if (startTimeHour < 9 || endTimeHour > 19) {
      alertify.error("예약 가능 시간은 09:00 ~ 19:00 입니다.");
      return;
    }
    
    // 수정 중인 예약 ID 가져오기
    const editReservationId = $("#reservationForm").data('edit-id');
    const isEditing = !!editReservationId;
    
    // AJAX로 예약 요청 또는 수정
    $.ajax({
      url: isEditing ? 
        '${pageContext.request.contextPath}/updateReservation' : 
        '${pageContext.request.contextPath}/createReservation',
      type: 'POST',
      data: {
        reservationNo: isEditing ? editReservationId : null,
        facilityNo: facilityNo,
        reservationDate: reservationDate,
        startTime: startTime,
        endTime: endTime,
        purpose: purpose
      },
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          alertify.success(response.message || (isEditing ? "예약이 수정되었습니다." : "예약이 신청되었습니다."));
          $("#reservationModal").hide();
          
          // 내 예약 탭으로 전환 및 목록 업데이트
          $("#facilityTabs .facility-nav-link[href='#myReservations']").click();
          
          // 편집 모드 초기화
          $("#reservationForm").removeData('edit-id');
        } else {
          alertify.error(response.message || (isEditing ? "예약 수정에 실패했습니다." : "예약 신청에 실패했습니다."));
        }
      },
      error: function(xhr, status, error) {
        alertify.error((isEditing ? "예약 수정" : "예약 신청") + " 중 오류가 발생했습니다.");
        console.error("응답 텍스트:", xhr.responseText);
      }
    });
  }
  
  // 예약 취소 함수
  function cancelReservation(reservationNo, closeModal = false) {
    $.ajax({
      url: '${pageContext.request.contextPath}/cancelReservation',
      type: 'POST',
      data: { 
        reservationNo: reservationNo,
        status: 'CANCELED' // 명시적으로 상태 전달
      },
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          alertify.success(response.message || "예약이 취소되었습니다.");
          
          if (closeModal) {
            $("#reservationDetailModal").hide();
          }
          
          // 내 예약 목록 업데이트
          loadMyReservations();
        } else {
          alertify.error(response.message || "예약 취소에 실패했습니다.");
        }
      },
      error: function(xhr, status, error) {
        alertify.error("예약 취소 중 오류가 발생했습니다.");
      }
    });
  }
  
  // 시설 목록 로드 함수
  function loadFacilities() {
    const typeFilter = $("#facilityTypeFilter").val();
    const dateFilter = $("#dateFilter").val();
    const keyword = $("#facilitySearch").val().trim();
    
    // 로딩 상태 표시
    $("#facilityCardContainer").html('<div class="empty-state"><i class="fas fa-spinner fa-spin"></i><p>시설 목록을 불러오는 중입니다...</p></div>');
    
    $.ajax({
      url: '${pageContext.request.contextPath}/getAvailableFacilities',
      type: 'GET',
      data: {
        type: typeFilter || null,
        date: dateFilter || null,
        keyword: keyword || null
      },
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          updateFacilityCards(response.facilities);
        } else {
          alertify.error(response.message || "시설 목록을 불러올 수 없습니다.");
          $("#facilityCardContainer").html('<div class="empty-state"><i class="fas fa-exclamation-circle"></i><p>시설 목록을 불러오는 중 오류가 발생했습니다.</p></div>');
        }
      },
      error: function(xhr, status, error) {
        alertify.error("시설 목록을 불러오는 중 오류가 발생했습니다.");
        $("#facilityCardContainer").html('<div class="empty-state"><i class="fas fa-exclamation-circle"></i><p>시설 목록을 불러오는 중 오류가 발생했습니다.</p></div>');
      }
    });
  }
  
  // 필터용 시설 목록 로드 함수
  function loadFacilitiesForFilter() {
    $.ajax({
      url: '${pageContext.request.contextPath}/getAvailableFacilities',
      type: 'GET',
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          // 시설 필터 옵션 업데이트
          const filterSelect = $("#myFacilityFilter");
          filterSelect.empty();
          filterSelect.append('<option value="">전체 시설</option>');
          
          response.facilities.forEach(function(facility) {
            filterSelect.append('<option value="' + facility.facilityNo + '">' + facility.facilityName + '</option>');
          });
        }
      }
    });
  }
  
  // 내 예약 목록 로드 함수
  function loadMyReservations() {
    const statusFilter = $("#myStatusFilter").val();
    const dateFilter = $("#myDateFilter").val();
    const facilityFilter = $("#myFacilityFilter").val();
    
    // 로딩 상태 표시
    $("#myReservationsTable tbody").html('<tr><td colspan="6" class="text-center"><i class="fas fa-spinner fa-spin"></i> 예약 내역을 불러오는 중입니다...</td></tr>');
    
    $.ajax({
      url: '${pageContext.request.contextPath}/getMyReservations',
      type: 'GET',
      data: {
        status: statusFilter || null,
        date: dateFilter || null,
        facilityNo: facilityFilter || null
      },
      dataType: 'json',
      success: function(response) {
        if (response.success) {
          updateMyReservationsTable(response.reservations);
        } else {
          alertify.error(response.message || "예약 목록을 불러올 수 없습니다.");
          $("#myReservationsTable tbody").html('<tr><td colspan="6" class="text-center">예약 내역을 불러오는 중 오류가 발생했습니다.</td></tr>');
        }
      },
      error: function(xhr, status, error) {
        alertify.error("예약 목록을 불러오는 중 오류가 발생했습니다.");
        $("#myReservationsTable tbody").html('<tr><td colspan="6" class="text-center">예약 내역을 불러오는 중 오류가 발생했습니다.</td></tr>');
      }
    });
  }
  
  // 시설 카드 업데이트 함수
  function updateFacilityCards(facilities) {
    const container = $("#facilityCardContainer");
    container.empty();
    
    if (!facilities || facilities.length === 0) {
      container.html('<div class="empty-state"><i class="fas fa-calendar-times"></i><p>사용 가능한 시설이 없습니다.</p></div>');
      return;
    }
    
    facilities.forEach(function(facility) {
      // 사용 가능한 시설만 표시
      if (facility.facilityStatus === 'ACTIVE') {
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
        
        const card = $('<div class="facility-card" data-type="' + facility.facilityType + '">' +
                '<div class="facility-icon-container">' + typeIcon + 
                '<span class="status-badge status-active">예약 가능</span></div>' +
                '<div class="facility-content">' +
                '<h3 class="facility-name">' + facility.facilityName + '</h3>' +
                '<div class="facility-info">' + capacityHtml + locationHtml + descriptionHtml + '</div>' +
                '<div class="facility-actions">' +
                '<button class="btn btn-primary reserve-facility" data-id="' + facility.facilityNo + '"><i class="fas fa-calendar-plus"></i> 예약하기</button>' +
                '</div>' +
                '</div>' +
                '</div>');
              
              container.append(card);
            }
          });
        }
        
        // 내 예약 테이블 업데이트 함수
        function updateMyReservationsTable(reservations) {
          const tbody = $("#myReservationsTable tbody");
          tbody.empty();
          
          if (!reservations || reservations.length === 0) {
            tbody.html('<tr><td colspan="6" class="text-center">예약 내역이 없습니다.</td></tr>');
            return;
          }
          
          reservations.forEach(function(reservation) {
            // 예약 상태에 따른 뱃지 설정
            let statusBadge = '';
            let displayStatus = '';
            
            // 개선된 상태 표시 로직
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
              // ACTIVE 상태인 경우 CREATE_DATE와 UPDATE_DATE를 비교
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
            
            // 예약 시간 포맷팅
            const startTime = new Date(reservation.START_TIME);
            const endTime = new Date(reservation.END_TIME);
            const formattedStartTime = startTime.toLocaleDateString('ko-KR') + ' ' + startTime.toLocaleTimeString('ko-KR', {hour: '2-digit', minute:'2-digit'});
            const formattedEndTime = endTime.toLocaleTimeString('ko-KR', {hour: '2-digit', minute:'2-digit'});
            
            // 액션 버튼 설정
            let actionButtons = '<button class="btn btn-sm btn-secondary view-my-reservation" data-id="' + reservation.RESERVATION_NO + '"><i class="fas fa-eye"></i></button> ';
            
            if (displayStatus === 'PENDING' || displayStatus === 'APPROVED') {
              actionButtons += '<button class="btn btn-sm btn-danger cancel-my-reservation" data-id="' + reservation.RESERVATION_NO + '"><i class="fas fa-times"></i></button>';
            }
            
            // 행 생성
            const row = $('<tr data-reservation-id="' + reservation.RESERVATION_NO + '">' +
              '<td>' + reservation.RESERVATION_NO + '</td>' +
              '<td>' + reservation.FACILITY_NAME + '</td>' +
              '<td>' + formattedStartTime + ' ~ ' + formattedEndTime + '</td>' +
              '<td>' + reservation.PURPOSE + '</td>' +
              '<td>' + statusBadge + '</td>' +
              '<td><div class="action-buttons">' + actionButtons + '</div></td>' +
              '</tr>');
            
            tbody.append(row);
          });
        }
 });
 </script>

 </body>
 </html>