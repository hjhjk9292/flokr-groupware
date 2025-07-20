<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Flokr - 일정관리</title>
    
    <!-- FullCalendar CSS -->
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.css" rel="stylesheet">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/scheduleCalendar.css">
    

    
    
</head>
<body class="calendar-wrapper">
    <jsp:include page="../common/header.jsp"/>
        
    <div class="calendar-content-container">
        <div class="calendar-header">
            <div style="display: flex; align-items: center;">
                <button class="calendar-nav-button" id="calendar-prev-button" style="font-size: 24px; margin-right: 5px;">
                    <span>&#8249;</span>
                </button>
                <h4 id="calendar-current-month-year" style="margin: 0; font-size: 20px; font-weight: 500;">2024년 4월</h4>
                <button class="calendar-nav-button" id="calendar-next-button" style="font-size: 24px; margin-left: 5px;">
                    <span>&#8250;</span>
                </button>
                <button class="calendar-today-button" id="calendar-today-button" style="margin-left: 10px; background-color: #dbebfb; color: #114d79; border-radius: 4px; font-size: 14px; font-weight: bold; padding: 5px 12px;">오늘</button>
	            <button id="add-schedule-btn" class="calendar-today-button" style="margin-left: 10px; background-color: white; color: #003561; border: 1px solid #003561; border-radius: 4px; font-size: 14px; font-weight: bold; padding: 5px 12px; transition: background-color 0.2s ease;">
				    <span style="font-size: 16px; margin-right: 4px;">+</span> 새 일정 등록
				</button>            
            </div>
            <div class="calendar-navigation">
                <div class="calendar-view-controls">
                    <button class="calendar-view-button active" id="calendar-month-view">월</button>
                    <button class="calendar-view-button" id="calendar-week-view">주</button>
                    <button class="calendar-view-button" id="calendar-day-view">일</button>
                </div>
            </div>
        </div>
        
        <div class="calendar-main-container">
            <div id="calendar-main"></div>
        </div>
    </div>
    
    <!-- 일정 상세 모달 컨테이너 -->
	<div id="schedule-detail-modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); z-index: 1000;"></div>
    
    <!-- 날짜별 일정 목록 모달 -->
    <div id="dateSchedulesModal" class="schedule-modal">
        <div class="schedule-modal-content">
            <div class="schedule-modal-header">
                <h3 class="schedule-modal-title">
                    <span class="schedule-modal-title-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                    </span>
                    <span id="selectedDateTitle">2024년 4월 15일</span> 일정
                </h3>
                <!--  <button type="button" class="schedule-modal-close" onclick="closeDateSchedulesModal()">×</button>-->
            </div>
            <div class="schedule-modal-body">
                <div id="dateSchedulesList" class="date-schedules-list">
                    <!-- 일정 목록이 여기에 동적으로 추가 -->
                </div>
                <div id="noSchedulesMessage" class="no-schedules-message" style="display: none;">
                    <p>이 날짜에 등록된 일정이 없습니다.</p>
                </div>
            </div>
            <div class="schedule-modal-footer">
                <button type="button" class="schedule-modal-btn schedule-modal-btn-add" onclick="addScheduleForDate()">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    새 일정 추가
                </button>
                <button type="button" class="schedule-modal-btn schedule-modal-btn-back" onclick="closeDateSchedulesModal()">
                    닫기
                </button>
            </div>
        </div>
    </div>
    
	<!-- FullCalendar JS -->
	<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/locales/ko.js"></script>
	    
	<script>
	    document.addEventListener('DOMContentLoaded', function() {
	        console.log("DOM loaded, initializing calendar...");
	        
	        // 캘린더 요소 가져오기
	        var calendarEl = document.getElementById('calendar-main');
	        
	        // FullCalendar가 정의되었는지 확인
	        if (typeof FullCalendar === 'undefined') {
	            console.error("FullCalendar is not defined! Check library loading.");
	            calendarEl.innerHTML = '<div style="padding: 20px; text-align: center; color: red;">캘린더 라이브러리를 로드할 수 없습니다. 페이지를 새로고침 해주세요.</div>';
	            return;
	        }
	        
	        // FullCalendar 초기화
	        var calendar = new FullCalendar.Calendar(calendarEl, {
	            headerToolbar: false, // 커스텀 헤더 사용
	            locale: 'ko', // 한국어 설정
	            initialView: 'dayGridMonth', // 초기 뷰 (월간)
	            height: 'auto',
	            contentHeight: 700,
	            dayMaxEvents: true, // 이벤트가 많을 경우 "more" 링크 표시
	            weekNumbers: false,
	            navLinks: false,
	            fixedWeekCount: false,
	            showNonCurrentDates: true,
	            firstDay: 0, // 일요일부터 시작
	            
	            // DB에서 데이터를 동적으로 가져오기
	            events: function(info, successCallback, failureCallback) {
	                // AJAX 요청으로 서버에서 데이터 가져오기
	                $.ajax({
	                    url: 'getSchedules.sc', // 실제 컨트롤러 URL로 변경
	                    type: 'GET',
	                    dataType: 'json',
	                    data: {
	                        start: info.startStr, // 시작 날짜
	                        end: info.endStr      // 종료 날짜
	                    },
	                    success: function(result) { 
	                        
	                        // 서버에서 받은 일정 데이터 처리
	                        successCallback(result);  // 서버에서 이미 FullCalendar 형식으로 변환된 데이터를 받기 때문에 바로 사용
	                    },
	                    error: function(jqXHR, textStatus, errorThrown) {
	                        failureCallback(errorThrown);
	                        console.error("일정 데이터를 불러오는데 실패했습니다.", errorThrown);
	                        
	                        // 실패 시 빈 배열을 전달
	                        successCallback([]);
	                        
	                        // 사용자에게 오류 메시지 표시
	                        alert("일정을 불러오는데 실패했습니다. 페이지를 새로고침하거나 나중에 다시 시도해주세요.");
	                    }
	                });
	            },
	            
	            // 날짜 클릭 이벤트 - 해당 날짜에 색상 적용 및 동그라미 추가
	            dateClick: function(info) {
	                // 모든 셀의 배경색 초기화
	                document.querySelectorAll('.fc-day').forEach(function(cell) {
	                    cell.classList.remove('fc-day-selected');
	                    cell.style.backgroundColor = '';
	                });
	                document.querySelectorAll('.fc-daygrid-day-number').forEach(function(dateEl) {
	                    dateEl.classList.remove('date-circle');
	                });
	                
	                // 선택한 날짜에 배경색 적용
	                info.dayEl.classList.add('fc-day-selected');
	                info.dayEl.style.backgroundColor = 'rgba(0, 53, 97, 0.1)';
	                
	                // 선택한 날짜의 번호에 동그라미 스타일 적용
	                var dateEl = info.dayEl.querySelector('.fc-daygrid-day-number');
	                if(dateEl) {
	                    dateEl.classList.add('date-circle');
	                }
	                
	                console.log('날짜 클릭: ' + info.dateStr);
	                
	                // 선택한 날짜의 일정 상세 정보를 가져오는 AJAX 요청
	                $.ajax({
	                    url: 'getDaySchedules.sc',
	                    type: 'GET',
	                    data: {
	                        date: info.dateStr
	                    },
	                    success: function(result) {
	                        // 해당 날짜의 일정 목록 모달 표시
	                        showDateSchedulesModal(info.dateStr, result);
	                    }, 
	                    error: function(xhr, status, error) {
	                        console.error('일정 목록을 불러오는데 실패했습니다:', error);
	                        // 에러가 발생해도 빈 일정 목록으로 모달 표시
	                        showDateSchedulesModal(info.dateStr, []);
	                    }
	                });
	            },
	            
	            // 이벤트(일정) 클릭 시 처리
	            eventClick: function(info) {
	                console.log('일정 클릭:', info.event.id, info.event.title);
	                
	                // 일정 상세 정보 모달 표시
	                openScheduleDetail(info.event.id);
	            },
	            
	            // 달력 렌더링 후 추가 작업
	            datesSet: function(info) {
	                // 현재 날짜(오늘)를 가져오기
	                var today = new Date();
	                var year = today.getFullYear();
	                var month = today.getMonth() + 1;
	                var day = today.getDate();
	                
	                // ISO 형식의 오늘 날짜 문자열 생성 (YYYY-MM-DD)
	                var todayStr = year + '-' + 
	                              (month < 10 ? '0' + month : month) + '-' + 
	                              (day < 10 ? '0' + day : day);
	                
	                // 약간의 지연 후 적용 (렌더링 완료 후)
	                setTimeout(function() {
	                    // 모든 날짜 셀의 선택 초기화
	                    document.querySelectorAll('.fc-day').forEach(function(cell) {
	                        cell.classList.remove('fc-day-selected');
	                        cell.style.backgroundColor = '';
	                    });
	                    document.querySelectorAll('.fc-daygrid-day-number').forEach(function(dateEl) {
	                        dateEl.classList.remove('date-circle');
	                    });
	                    
	                    // 오늘 날짜 셀 찾기 및 강조 표시
	                    var todayEl = document.querySelector('.fc-day[data-date="' + todayStr + '"]');
	                    if (todayEl) {
	                        todayEl.classList.add('fc-day-selected');
	                        todayEl.style.backgroundColor = 'rgba(0, 53, 97, 0.1)';
	                        
	                        // 오늘 날짜 번호에 동그라미 추가
	                        var todayNumEl = todayEl.querySelector('.fc-daygrid-day-number');
	                        if (todayNumEl) {
	                            todayNumEl.classList.add('date-circle');
	                        }
	                        
	                        console.log('오늘 날짜 강조:', todayStr);
	                    }
	                }, 100);
	            },
	            
	            eventContent: function(info) {
	                // 일정 유형에 따른 색상 설정
	                var bgColor, textColor, borderColor;
	                var scheduleType = info.event.extendedProps.scheduleType || 'PERSONAL';
	                
	                switch(scheduleType.toUpperCase()) {
	                    case 'PERSONAL':
	                        bgColor = 'rgba(219, 234, 254, 0.8)';
	                        textColor = '#003561';
	                        borderColor = '#003561';
	                        break;
	                    case 'TEAM':
	                        bgColor = 'rgba(209, 250, 229, 0.8)';
	                        textColor = '#27ae60';
	                        borderColor = '#27ae60';
	                        break;
	                    case 'COMPANY':
	                        bgColor = 'rgba(255, 237, 213, 0.8)';
	                        textColor = '#f39c12';
	                        borderColor = '#f39c12';
	                        break;
	                    case 'OTHER':
	                        bgColor = 'rgba(237, 233, 254, 0.8)';
	                        textColor = '#8e44ad';
	                        borderColor = '#8e44ad';
	                        break;
	                    default:
	                        bgColor = 'rgba(219, 234, 254, 0.8)';
	                        textColor = '#003561';
	                        borderColor = '#003561';
	                }
	                
	                // 컨테이너 생성 및 스타일 직접 적용
	                var container = document.createElement('div');
	                container.style.cssText = `
	                    display: flex;
	                    align-items: center;
	                    width: 100%;
	                    height: 100%;
	                    padding: 3px 8px 3px 0;
	                    margin: 0;
	                    background-color: ${bgColor};
	                    position: relative;
	                    box-sizing: border-box;
	                    border: none !important;
	                    overflow: hidden;
	                `;
	                
	                // 추가해야 할 스타일: 다른 테두리 제거 및 세로줄 중복 방지
	                container.style.borderTop = 'none';
	                container.style.borderRight = 'none';
	                container.style.borderBottom = 'none';
	                container.style.position = 'relative';
	                container.style.boxSizing = 'border-box';
	                container.style.width = '100%'; // 너비 100%로 설정
	                
	                // 제목 요소
	                var titleEl = document.createElement('span');
	                titleEl.style.color = textColor;
	                titleEl.style.flex = '1';
	                titleEl.style.fontWeight = '500';
	                titleEl.style.backgroundColor = bgColor; // 여기 bgColor는 rgba로 설정한 것
	                
	                
	                // 중요도 HIGH인 경우에만 노란색 별표 추가 
	                var titleContent = '';
	                if (info.event.extendedProps && info.event.extendedProps.important) {
	                    var importanceVal = info.event.extendedProps.important.toUpperCase();
	                    
	                    if (importanceVal === 'HIGH') {
	                        // 중요 일정에만 별표 추가 (텍스트로 추가)
	                        titleContent += '<span style="color: #facc15; font-size: 12px; margin-right: 4px; display: inline-block;">★</span>';
	                    }
	                }
	                
	                // 제목 텍스트 추가
	                titleContent += info.event.title || '';
	                titleEl.innerHTML = titleContent;
	                
	                container.appendChild(titleEl);
	                
	                return { domNodes: [container] };
	            },
	        });
	        
	        try {
	            // 캘린더 렌더링
	            calendar.render();
	            
	            // 현재 표시하는 월/년 업데이트 함수
	            function updateMonthYearTitle() {
	                var date = calendar.getDate();
	                var year = date.getFullYear();
	                var month = date.getMonth() + 1;
	                document.getElementById('calendar-current-month-year').textContent = year + '년 ' + month + '월';
	            }
	            
	            // 초기 월/년 표시
	            updateMonthYearTitle();
	            
	            // 이전 달 버튼 이벤트
	            document.getElementById('calendar-prev-button').addEventListener('click', function() {
	                calendar.prev();
	                updateMonthYearTitle();
	            });
	            
	            // 다음 달 버튼 이벤트
	            document.getElementById('calendar-next-button').addEventListener('click', function() {
	                calendar.next();
	                updateMonthYearTitle();
	            });
	            
	            // 오늘 버튼 이벤트
	            document.getElementById('calendar-today-button').addEventListener('click', function() {
	                calendar.today();
	                updateMonthYearTitle();
	            });
	            
	            // 뷰 전환 버튼 이벤트
	            document.getElementById('calendar-month-view').addEventListener('click', function() {
	                changeView('calendar-month-view', 'dayGridMonth');
	            });
	            
	            document.getElementById('calendar-week-view').addEventListener('click', function() {
	                changeView('calendar-week-view', 'timeGridWeek');
	            });
	            
	            document.getElementById('calendar-day-view').addEventListener('click', function() {
	                changeView('calendar-day-view', 'timeGridDay');
	            });
	            
	            // 뷰 변경 함수
	            function changeView(buttonId, viewName) {
	                // 모든 버튼 비활성화
	                document.querySelectorAll('.calendar-view-button').forEach(function(btn) {
	                    btn.classList.remove('active');
	                });
	                
	                // 선택한 버튼 활성화
	                document.getElementById(buttonId).classList.add('active');
	                
	                // 캘린더 뷰 변경
	                calendar.changeView(viewName);
	                updateMonthYearTitle();
	            }
	        } catch (error) {
	            console.error("캘린더 렌더링 오류:", error);
	            calendarEl.innerHTML = '<div style="padding: 20px; text-align: center; color: red;">캘린더를 로드하는 중 오류가 발생했습니다: ' + error.message + '</div>';
	        }
	        
	        // calendar-current-month-year 요소에 클릭 이벤트 추가
	        document.getElementById('calendar-current-month-year').addEventListener('click', function(e) {
	            e.preventDefault();
	            
	            // 이미 열려있는 선택기가 있으면 닫기
	            const existingPicker = document.getElementById('month-year-picker');
	            if (existingPicker) {
	                existingPicker.remove();
	                return;
	            }
	            
	            // 현재 날짜 구하기
	            const currentDate = calendar.getDate();
	            const currentYear = currentDate.getFullYear();
	            const currentMonth = currentDate.getMonth();
	            
	            // 월/연도 선택기 컨테이너 생성
	            const picker = document.createElement('div');
	            picker.id = 'month-year-picker';
	            picker.style.position = 'absolute';
	            picker.style.top = (this.offsetTop + this.offsetHeight + 5) + 'px';
	            picker.style.left = this.offsetLeft + 'px';
	            
	            // 연도 선택 드롭다운
	            const yearSelect = document.createElement('select');
	            yearSelect.style.margin = '0 5px 10px 0';
	            yearSelect.style.padding = '5px';
	            yearSelect.style.width = '120px';
	            
	            // 연도 옵션 추가 (현재 연도 ±5년)
	            for (let y = currentYear - 5; y <= currentYear + 5; y++) {
	                const option = document.createElement('option');
	                option.value = y;
	                option.textContent = y + '년';
	                option.selected = y === currentYear;
	                yearSelect.appendChild(option);
	            }
	            
	            // 월 선택 드롭다운
	            const monthSelect = document.createElement('select');
	            monthSelect.style.margin = '0 0 10px 5px';
	            monthSelect.style.padding = '5px';
	            monthSelect.style.width = '100px';
	            
	            // 월 옵션 추가
	            const months = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
	            months.forEach((month, index) => {
	                const option = document.createElement('option');
	                option.value = index;
	                option.textContent = month;
	                option.selected = index === currentMonth;
	                monthSelect.appendChild(option);
	            });
	            
	            // 선택 버튼
	            const selectBtn = document.createElement('button');
	            selectBtn.textContent = '선택';
	            selectBtn.style.marginRight = '5px';
	            selectBtn.style.padding = '6px 12px';
	            selectBtn.style.backgroundColor = '#003561';
	            selectBtn.style.color = '#fff';
	            selectBtn.style.border = 'none';
	            selectBtn.style.borderRadius = '4px';
	            selectBtn.style.cursor = 'pointer';
	            
	            // 취소 버튼
	            const cancelBtn = document.createElement('button');
	            cancelBtn.textContent = '취소';
	            cancelBtn.style.padding = '6px 12px';
	            cancelBtn.style.backgroundColor = '#e9e9e9';
	            cancelBtn.style.color = '#333';
	            cancelBtn.style.border = 'none';
	            cancelBtn.style.borderRadius = '4px';
	            cancelBtn.style.cursor = 'pointer';
	            
	            // 버튼 컨테이너
	            const buttonContainer = document.createElement('div');
	            buttonContainer.style.textAlign = 'center';
	            buttonContainer.appendChild(selectBtn);
	            buttonContainer.appendChild(cancelBtn);
	            
	            // 모든 요소를 피커에 추가
	            picker.appendChild(yearSelect);
	            picker.appendChild(monthSelect);
	            picker.appendChild(buttonContainer);
	            
	            // 선택 버튼 클릭 이벤트
	            selectBtn.addEventListener('click', function() {
	                const year = parseInt(yearSelect.value);
	                const month = parseInt(monthSelect.value);
	                const newDate = new Date(year, month, 1);
	                
	                // 새 날짜로 캘린더 이동
	                calendar.gotoDate(newDate);
	                updateMonthYearTitle();
	                
	                // 선택기 제거
	                picker.remove();
	            });
	            
	            // 취소 버튼 클릭 이벤트
	            cancelBtn.addEventListener('click', function() {
	                picker.remove();
	            });
	            
	            // 다른 곳 클릭 시 닫기
	            document.addEventListener('click', function closeOnClickOutside(e) {
	                if (!picker.contains(e.target) && e.target !== document.getElementById('calendar-current-month-year')) {
	                    picker.remove();
	                    document.removeEventListener('click', closeOnClickOutside);
	                }
	            });
	            
	            // 피커를 DOM에 추가
	            document.body.appendChild(picker);
	        });
	     
	        // 새 일정 등록 버튼 호버 효과
	        const addScheduleBtn = document.getElementById('add-schedule-btn');
	        addScheduleBtn.addEventListener('mouseenter', function() {
	            this.style.backgroundColor = 'rgba(0, 53, 97, 0.1)'; // 테마 색상의 투명도를 낮춘 배경색
	        });
	        addScheduleBtn.addEventListener('mouseleave', function() {
	            this.style.backgroundColor = 'white';
	        });
	
	        // 새 일정 등록 버튼 클릭 이벤트
	        addScheduleBtn.addEventListener('click', function() {
	            // 일정 등록 페이지로 이동
	            location.href = 'enrollForm.sc';
	        });
	    });  
	    
	    // 전역 변수 - 선택된 날짜 저장
	    let selectedDate = null;
	
	 // 일정 상세 정보 모달을 여는 함수
	    function openScheduleDetail(scheduleNo) {
	        // 모달 엘리먼트 가져오기
	        const modalEl = document.getElementById('schedule-detail-modal');
	        
	        // 일정 상세 모달 열기
	        $.ajax({
	            url: 'detailModal.sc',
	            type: 'GET',
	            data: {
	                scheduleNo: scheduleNo
	            },
	            success: function(response) {
	                console.log('일정 상세 정보 로드 성공');
	                
	                // 모달 컨테이너 초기화 및 설정
	                modalEl.innerHTML = '';
	                modalEl.innerHTML = response;
	                
	                // 모달 표시 및 중앙 정렬
	                modalEl.style.display = 'flex';
	                modalEl.style.justifyContent = 'center';
	                modalEl.style.alignItems = 'center';
	                
	             	// 모달 로드 후 수정/삭제 버튼에 직접 이벤트 리스너 추가
	                const editBtn = modalEl.querySelector('.schedule-modal-btn-edit');
	                if (editBtn) {
	                    editBtn.addEventListener('click', function() {
	                        console.log("수정 버튼 클릭: 일정 번호 " + scheduleNo);
	                        try {
	                            location.href = 'updateForm.sc?scheduleNo=' + scheduleNo;
	                        } catch (e) {
	                            console.error("수정 페이지 이동 중 오류 발생:", e);
	                            alert("수정 페이지로 이동 중 오류가 발생했습니다.");
	                        }
	                    });
	                }
	                
	                const deleteBtn = modalEl.querySelector('.schedule-modal-btn-delete');
	                if (deleteBtn) {
	                    deleteBtn.addEventListener('click', function() {
	                        if (confirm('정말 이 일정을 삭제하시겠습니까?')) {
	                            location.href = 'delete.sc?scheduleNo=' + scheduleNo;
	                        }
	                    });
	                }
	                
	                // 모달 외부 클릭 시 닫기 이벤트 설정
	                modalEl.addEventListener('click', function handleModalClick(e) {
	                    if (e.target === modalEl) {
	                        modalEl.style.display = 'none';
	                        modalEl.removeEventListener('click', handleModalClick);
	                    }
	                });
	                
	                // 설명 영역 왼쪽 정렬 강제 적용
	                const descriptionElement = modalEl.querySelector('.schedule-detail-description');
	                if (descriptionElement) {
	                    descriptionElement.style.textAlign = 'left';
	                    descriptionElement.style.textAlignLast = 'left';
	                    descriptionElement.style.display = 'block';
	                    
	                    // 하위의 모든 span 요소에도 왼쪽 정렬 적용
	                    const spans = descriptionElement.querySelectorAll('span');
	                    spans.forEach(span => {
	                        span.style.textAlign = 'left';
	                        span.style.marginLeft = '0';
	                        span.style.paddingLeft = '0';
	                        span.style.display = 'block';
	                    });
	                }
	                
	                // 빈 설명 영역도 왼쪽 정렬 적용
	                const emptyElement = modalEl.querySelector('.schedule-detail-empty');
	                if (emptyElement) {
	                    emptyElement.style.textAlign = 'left';
	                    emptyElement.style.textAlignLast = 'left';
	                    emptyElement.style.marginLeft = '0';
	                    emptyElement.style.paddingLeft = '0';
	                    emptyElement.style.display = 'block';
	                }
	            },
	            error: function(xhr, status, error) {
	                console.error('일정 상세 정보를 불러오는데 실패했습니다:', error);
	                alert('일정 정보를 불러오는데 실패했습니다.');
	            }
	        });
	    }
	
	    // 날짜별 일정 목록 모달 표시 함수
	    function showDateSchedulesModal(dateStr, schedules) {
	        selectedDate = dateStr;
	        
	        // 날짜 포맷팅 (예: 2024년 4월 15일)
	        const date = new Date(dateStr);
	        const formattedDate = date.getFullYear() + '년 ' + 
	                            (date.getMonth() + 1) + '월 ' + 
	                            date.getDate() + '일';
	        
	        // 모달 제목 설정
	        document.getElementById('selectedDateTitle').textContent = formattedDate;
	        
	        // 일정 목록 컨테이너
	        const schedulesList = document.getElementById('dateSchedulesList');
	        schedulesList.innerHTML = ''; // 목록 초기화
	        
	        // 일정이 없는 경우 메시지 표시
	        if (!schedules || schedules.length === 0) {
	            document.getElementById('noSchedulesMessage').style.display = 'block';
	            schedulesList.style.display = 'none';
	        } else {
	            document.getElementById('noSchedulesMessage').style.display = 'none';
	            schedulesList.style.display = 'block';
	            
	            // 각 일정을 목록에 추가
	            schedules.forEach(function(schedule) {
	                const scheduleItem = document.createElement('div');
	                scheduleItem.className = 'date-schedule-item type-' + schedule.scheduleType.toLowerCase();
	                scheduleItem.setAttribute('data-schedule-id', schedule.scheduleNo);
	                
	                // 일정 클릭 시 상세 모달 열기
	                scheduleItem.addEventListener('click', function() {
	                    // 날짜별 일정 목록 모달 닫기
	                    closeDateSchedulesModal();
	                    
	                    // 일정 상세 모달 열기
	                    openScheduleDetail(schedule.scheduleNo);
	                });
	                
	                // 일정 제목 영역
	                const titleDiv = document.createElement('div');
	                titleDiv.className = 'date-schedule-title';
	                
	                // 중요도에 따른 표시
	                if (schedule.important) {
	                    const importanceSpan = document.createElement('span');
	                    importanceSpan.className = 'date-schedule-importance ' + schedule.important.toLowerCase();
	                    titleDiv.appendChild(importanceSpan);
	                }
	                
	                // 일정 제목
	                titleDiv.innerHTML += schedule.scheduleTitle;
	                
	                // 일정 유형 뱃지
	                const typeBadge = document.createElement('span');
	                typeBadge.className = 'date-schedule-type-badge ' + schedule.scheduleType.toLowerCase();
	                
	                let typeText = '';
	                switch (schedule.scheduleType) {
	                    case 'PERSONAL': typeText = '개인'; break;
	                    case 'TEAM': typeText = '팀'; break;
	                    case 'COMPANY': typeText = '회사'; break;
	                    default: typeText = '기타'; break;
	                }
	                
	                typeBadge.textContent = typeText;
	                titleDiv.appendChild(typeBadge);
	                
	                // 일정 시간
	                const timeDiv = document.createElement('div');
	                timeDiv.className = 'date-schedule-time';
	                
	                // 시간 포맷팅
	                let timeText = '';
	                if (schedule.allDay) {
	                    timeText = '종일';
	                } else {
	                    const startTime = new Date(schedule.startDate).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
	                    const endTime = new Date(schedule.endDate).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
	                    timeText = startTime + ' ~ ' + endTime;
	                }
	                
	                timeDiv.textContent = timeText;
	                
	                // 장소 정보
	                const locationDiv = document.createElement('div');
	                locationDiv.className = 'date-schedule-location';
	                
	                if (schedule.location) {
	                    locationDiv.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>' + schedule.location;
	                }
	                
	                // 요소 조합
	                scheduleItem.appendChild(titleDiv);
	                scheduleItem.appendChild(timeDiv);
	                
	                if (schedule.location) {
	                    scheduleItem.appendChild(locationDiv);
	                }
	                
	                schedulesList.appendChild(scheduleItem);
	            });
	        }
	        
	        // 모달 표시
	        document.getElementById('dateSchedulesModal').style.display = 'flex';
	        
	        // 모달 외부 클릭 시 닫기 이벤트 추가
	        document.getElementById('dateSchedulesModal').addEventListener('click', function(e) {
	            if (e.target === this) {
	                closeDateSchedulesModal();
	            }
	        });
	    }
	
	    // 날짜별 일정 목록 모달 닫기 함수
	    function closeDateSchedulesModal() {
	        document.getElementById('dateSchedulesModal').style.display = 'none';
	        selectedDate = null;
	    }
	
	    // 선택한 날짜에 새 일정 추가 함수
	    function addScheduleForDate() {
	        if (selectedDate) {
	            // 일정 등록 페이지로 이동하면서 선택한 날짜 정보 전달
	            location.href = 'enrollForm.sc?date=' + selectedDate;
	        }
	    }
	</script>

</body>
</html>