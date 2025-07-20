<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="today" value="${now}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Flokr</title>
<%-- FullCalendar CSS --%>
<link href='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/main.min.css' rel='stylesheet' />
<!-- main CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/userMain.css">
<!-- Material Icons 추가 -->
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"rel="stylesheet">
</head>
<body>

    <jsp:include page="common/header.jsp"/>

    <div class="outer">
        <%-- 왼쪽 컬럼 --%>
        <div class="s01">
            <div class="section part01">
                <div id="p01-profile">
                    <div class="profile-image-container" style="display: flex; justify-content: center; align-items: center;">
				        <c:choose>
						    <c:when test="${not empty loginUser.profileImgPath}">
								<div style="width: 100%; height: 100%; border-radius: 50%; overflow: hidden;">
								    <!--<img src="${loginUser.profileImgPath}" alt="프로필" style="width: 100%; height: 100%; object-fit: cover;">-->
								</div>
					        </c:when>
				            <c:otherwise>
				                <%-- 스크립틀릿으로 색상 계산 --%>
				                <% 
				                    String[] colors = {"#4285f4", "#34a853", "#ea4335", "#fbbc05", "#9c27b0"};
				                    String empName = ((com.kh.flokrGroupware.employee.model.vo.Employee)session.getAttribute("loginUser")).getEmpName();
				                    char firstChar = empName.charAt(0);
				                    int colorIndex = Math.abs(firstChar) % 5;
				                    pageContext.setAttribute("bgColor", colors[colorIndex]);
				                    pageContext.setAttribute("firstChar", String.valueOf(firstChar));
				                %>
				                
				                <div style="width: 90px; height: 90px; border-radius: 50%; background-color: ${bgColor}; display: flex; justify-content: center; align-items: center; border: 1px solid #eee;">
				                    <span style="color: white; font-size: 36px; font-family: 'Noto Sans KR', sans-serif;">${firstChar}</span>
				                </div>
				            </c:otherwise>
				        </c:choose>
				        <span class="status-indicator online"></span>
				    </div>
				    <p class="profile-name">${loginUser.empName}</p>
				    <p class="profile-title">${loginUser.deptName}	${loginUser.positionName}</p>
                    <div class="profile-buttons">
                        <button class="btn-small btn-home">HOME</button>
                        <button class="btn-small btn-office active">OFFICE</button>
                    </div>
                </div>
                <div id="p01-onoffbtn">
                    <p class="current-date"><fmt:formatDate value="${today}" pattern="yyyy년 MM월 dd일" /></p>
                    <div class="time-info-box">
					    <div class="time-section">
					        <span class="time-label">출근 시간</span>
					        <span class="time-value" id="clockInTime">-- : --</span> <!-- 출근 시간 -->
					    </div>
					    <div class="time-section">
					        <span class="time-label">퇴근 시간</span>
					        <span class="time-value" id="clockOutTime">-- : --</span> <!-- 퇴근 시간 -->
					    </div>
					</div>
                    <div class="action-buttons">
                        <button class="btn-large btn-checkin">출근</button>
                        <button class="btn-large btn-checkout">퇴근</button>
                    </div>
                </div>
            </div>
            <div class="section part02">
                <div class="sub-title">업무 목록</div>
                <div id="work-list">
                    <table>
                        <thead>
                            <tr>
                                <th>업무 제목</th>
                                <th>생성일</th>
                                <th>마감일</th>
                                <th>상태</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%-- 업무 목록 데이터 (예시) --%>
                            <tr>
                                <td>사용자 데이터 시각화</td>
                                <td>2025/05/19</td>
                                <td>2025/05/22</td>
                                <td><span class="status status-inprogress">진행중</span></td>
                            </tr>
                            <tr>
                                <td>리액트 컴포넌트 개발</td>
                                <td>2025/05/17</td>
                                <td>2025/05/23</td>
                                <td><span class="status status-inprogress">진행중</span></td>
                            </tr>
                            <tr>
                                <td>스프링부트 API 개발 가이드</td>
                                <td>2025/05/18</td>
                                <td>2025/05/25</td>
                                <td><span class="status status-completed">완료</span></td>
                            </tr>
                            <tr>
                                <td>UI 디자인 원칙 및 컴포넌트</td>
                                <td>2025/05/11</td>
                                <td>2025/05/21</td>
                                <td><span class="status status-inprogress">진행중</span></td>
                            </tr>
                            <tr>
                                <td>사용자 인터페이스 디자인</td>
                                <td>2025/05/13</td>
                                <td>2025/05/20</td>
                                <td><span class="status status-completed">완료</span></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <%-- 가운데 컬럼 --%>
        <div class="s02">
            <div class="section part03">
                <%-- 달력 제목은 FullCalendar에서 생성 --%>
                <div id="main-calender" style="height: 100%; width: 100%;">
                	<!-- 동적 일정 캘린더로 대체 ❤️ -->
                    <div id="user-sc-calendar-container" style="height: 100%; width: 100%;"></div>
                </div>
            </div>
            <div class="section part04">
                <div class="sub-title">회사 공지</div>
                <div id="notice-list">
                     <table>
			            <thead>
			                <tr>
			                    <th width="60%">TITLE</th> <%-- 제목 컬럼명 수정 --%>
			                    <th width="20%">WRITER</th>
			                    <th width="20%">DATE</th>
			                </tr>
			            </thead>
			            <tbody id="notice-list-body">
			                <%-- 공지사항 데이터가 여기에 동적으로 로드됩니다 --%>
			            </tbody>
                    </table>
                </div>
            </div>
        </div>

        <%-- 오른쪽 컬럼 (팀원 주소록) --%>
        <div class="section s03">
            <div class="sub-title">팀원 주소록</div>
            <div id="work-address">
                <table>
                    <colgroup> <%-- 컬럼 너비 정의 --%>
                       <col style="width: auto;">  <%-- 이름/이미지 --%>
                       <col style="width: 70px;">  <%-- 등급 (Label) --%>
                       <col style="width: 50px;">  <%-- 체크 --%>
                       <col style="width: 30px;">  <%-- 옵션 --%>
                    </colgroup>
                    <thead>
                      <tr>
                        <th>NAME</th>
                        <th>GRADE</th>
                        <th>CHAT</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      <%-- Pedro --%>
                      <tr>
                        <td>
                          <div class="addr-name-cell"> <%-- 셀 내부 Flex 컨테이너 --%>
                            <img src="" alt="팀원" class="addr-profile-img">
                            <div class="addr-text">
                              <span class="addr-name">Pedro</span>
                              <span class="addr-subtitle">Developer</span> <%-- 직책 예시 --%>
                            </div>
                          </div>
                        </td>
                        <td><span class="addr-grade-label">팀원</span></td> <%-- 등급 예시 --%>
                        <td><span class="material-icons" style="font-size: 20px;">sms</span></td>
                        <td><span class="addr-options-icon">⋮</span></td>
                      </tr>
                      <%-- Ryan --%>
                      <tr>
                         <td>
                          <div class="addr-name-cell">
                            <img src="https://via.placeholder.com/32/33FF57/FFFFFF?text=R" alt="Ryan profile" class="addr-profile-img">
                            <div class="addr-text">
                              <span class="addr-name">Ryan</span>
                              <span class="addr-subtitle">Designer</span> <%-- 직책 예시 --%>
                            </div>
                          </div>
                        </td>
                        <td><span class="addr-grade-label">팀원</span></td> <%-- 등급 예시 --%>
                        <td><span class="material-icons" style="font-size: 20px;">sms</span></td>
                        <td><span class="addr-options-icon">⋮</span></td>
                      </tr>
                      <%-- Brian --%>
                       <tr>
                         <td>
                          <div class="addr-name-cell">
                            <img src="https://via.placeholder.com/32/3357FF/FFFFFF?text=B" alt="Brian profile" class="addr-profile-img">
                            <div class="addr-text">
                              <span class="addr-name">Brian</span>
                              <span class="addr-subtitle">Manager</span> <%-- 직책 예시 --%>
                            </div>
                          </div>
                        </td>
                        <td><span class="addr-grade-label">팀장</span></td> <%-- 등급 예시 --%>
                        <td><span class="material-icons" style="font-size: 20px;">sms</span></td>
                        <td><span class="addr-options-icon">⋮</span></td>
                      </tr>
                    </tbody>
                  </table>
            </div>
        </div>
    </div>
    
    <!-- 일정 상세 모달 컨테이너 추가 ❤️ -->
    <div id="user-sc-schedule-detail-modal"></div>
    
    <!-- 일정 목록 모달 컨테이너 추가 ❤️ -->
    <div id="user-sc-common-modal" class="user-sc-modal-overlay"></div>

    <%-- FullCalendar JS --%>
    <script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js'></script>
    
    <!-- 실제 일정 캘린더 코드 추가 ❤️ -->
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        var calendarEl = document.getElementById('user-sc-calendar-container');
        if (!calendarEl) { 
            console.error("달력 컨테이너 없음"); 
            return; 
        }
        
        // 캘린더 초기화
        var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            firstDay: 1, // 월요일부터 시작
            height: 'auto', // 자동 높이
            aspectRatio: 1.35, // 가로/세로 비율
            dayMinHeight: 45,     // 모든 날짜 셀에 최소 높이 적용
            dayMaxEventRows: 1, // 날짜당 최대 이벤트 행 수 제한
            fixedWeekCount: false, // 월의 주 수에 따라 높이 자동 조절
            //fixedWeekCount: false, // 월의 주 수에 따라 높이 자동 조절
            //rowHeight: 40, // 각 행의 높이를 일정하게 설정
            showNonCurrentDates: true, // 이전/다음 달의 날짜 표시
            headerToolbar: { 
                left: '', 
                center: 'title', 
                right: 'prev,next' // 이전, 다음 버튼만 표시
            },
            
         	// 월 변경 이벤트 리스너 추가
            datesSet: function(info) {
                // 화면이 변경될 때마다 스타일 다시 적용
                setTimeout(applyCleanCalendarStyles, 0);
            },
            
            // 동적 일정 데이터 로드
            events: function(info, successCallback, failureCallback) {
                $.ajax({
                    url: 'getSchedules.sc', // 일정 데이터를 가져오는 컨트롤러 URL
                    type: 'GET',
                    dataType: 'json',
                    data: {
                        start: info.startStr, // 시작 날짜
                        end: info.endStr      // 종료 날짜
                    },
                    success: function(result) {
                        // 서버에서 받은 일정 데이터 처리
                        successCallback(result); // 서버에서 이미 FullCalendar 형식으로 변환된 데이터를 받음
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        failureCallback(errorThrown);
                        console.error("일정 데이터를 불러오는데 실패했습니다.", errorThrown);
                    }
                });
            },
            
            // 이벤트 렌더링 - 일정 페이지 스타일 적용
            eventContent: function(arg) {
                if (arg.event.display === 'background') {
                    return {}; // 배경 이벤트는 내용을 비움
                }
                
                // 일정 유형에 따른 색상 설정
                let bgColor, textColor, borderColor;
                let scheduleType = arg.event.extendedProps.scheduleType || 'PERSONAL';
                
                switch(scheduleType.toUpperCase()) {
                    case 'PERSONAL':
                        bgColor = 'rgba(219, 234, 254, 0.8)'; // 파스텔 파란색
                        textColor = '#003561';
                        borderColor = '#003561';
                        break;
                    case 'TEAM':
                        bgColor = 'rgba(209, 250, 229, 0.8)'; // 파스텔 녹색
                        textColor = '#27ae60';
                        borderColor = '#27ae60';
                        break;
                    case 'COMPANY':
                        bgColor = 'rgba(255, 237, 213, 0.8)'; // 파스텔 주황색
                        textColor = '#f39c12';
                        borderColor = '#f39c12';
                        break;
                    default:
                        bgColor = 'rgba(237, 233, 254, 0.8)'; // 파스텔 보라색
                        textColor = '#8e44ad';
                        borderColor = '#8e44ad';
                }
                
                // 매우 간결한 컨텐츠 생성
                let content = document.createElement('div');
                content.style.fontSize = '11px';
                content.style.padding = '1px 3px';
                content.style.whiteSpace = 'nowrap';
                content.style.overflow = 'hidden';
                content.style.textOverflow = 'ellipsis';
                content.style.backgroundColor = bgColor;
                content.style.color = textColor;
                content.style.borderLeft = '2px solid ' + borderColor;
                content.style.borderRadius = '2px';
                
                
             	// 직접 이벤트 리스너 추가
                content.addEventListener('mouseover', function() {
                    this.style.transform = 'translateX(2px)';
                    this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
                    this.style.borderLeftWidth = '4px';
                    this.style.cursor = 'pointer';
                });
                
                content.addEventListener('mouseout', function() {
                    this.style.transform = '';
                    this.style.boxShadow = '';
                    this.style.borderLeftWidth = '2px';
                });
                
                // 중요도 HIGH인 경우 간결하게 별표 추가
                if (arg.event.extendedProps.important && arg.event.extendedProps.important.toUpperCase() === 'HIGH') {
                    content.innerHTML = '★ ' + arg.event.title;
                } else {
                    content.textContent = arg.event.title;
                }
                
                return { domNodes: [content] };
            },
            
            // 날짜 클릭 - 해당 날짜의 일정 목록 모달
            dateClick: function(info) {
                // 날짜 클릭 시 해당 날짜의 일정 목록 모달 띄우기
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
            
            // 일정 클릭 - 특정 일정 상세 정보 모달
            eventClick: function(info) {
                console.log('일정 클릭:', info.event.id, info.event.title);
                
                // 일정 상세 정보 모달 표시
                openScheduleDetail(info.event.id);
            },
            
            // 렌더링 완료 후 추가 스타일 적용
            viewDidMount: function() {
                setTimeout(applyCleanCalendarStyles, 0);
            }
        });
        
        // 렌더링
        calendar.render();

        // 깔끔한 달력 스타일 적용 함수
        function applyCleanCalendarStyles() {
            // 1. 전체 캘린더 컨테이너 스타일
            document.querySelectorAll('.fc').forEach(function(el) {
                el.style.fontFamily = 'Arial, sans-serif';
                el.style.fontSize = '13px';
                el.style.borderRadius = '8px';
                el.style.overflow = 'hidden';
                el.style.border = 'none';
                el.style.boxShadow = '0 2px 5px rgba(0, 0, 0, 0.08)';
                el.style.width = '100%'; // 전체 너비 사용
            });
            
            // 2. 헤더 스타일 적용
            document.querySelectorAll('.fc-toolbar-title').forEach(function(el) {
                el.style.fontSize = '16px';
                el.style.fontWeight = '600';
                el.style.color = '#333';
                el.style.margin = '0';
                el.style.padding = '15px 0 10px 0';
            });
            
            // 3. 버튼 스타일 적용
            document.querySelectorAll('.fc-button').forEach(function(el) {
                el.style.backgroundColor = '#fff';
                el.style.color = '#666';
                el.style.border = '1px solid #ddd';
                el.style.boxShadow = 'none';
                el.style.padding = '4px 8px';
                el.style.borderRadius = '4px';
                el.style.fontSize = '12px';
                el.style.fontWeight = 'normal';
            });
            
            // 4. 요일 헤더 스타일 적용
            document.querySelectorAll('.fc-col-header-cell').forEach(function(el, index) {
                el.style.padding = '8px 0';
                el.style.fontWeight = '500';
                el.style.fontSize = '12px';
                el.style.backgroundColor = '#fff';
                el.style.borderWidth = '0';
                el.style.textAlign = 'center';
                
                var a = el.querySelector('a');
                if (a) {
                    // 일요일은 파란색, 토요일은 빨간색 (두 번째 이미지처럼)
                    if (index === 6) { // 일요일 (firstDay가 1이므로 인덱스 6이 일요일)
                        a.style.color = '#1a73e8';
                    } else if (index === 5) { // 토요일
                        a.style.color = '#1a73e8';
                    } else { // 평일
                        a.style.color = '#333';
                    }
                    a.style.textDecoration = 'none';
                    a.style.fontWeight = '600';
                }
            });
            
         	// 4-1. 스크롤 영역 숨기기
            document.querySelectorAll('.fc-scroller').forEach(function(el) {
                el.style.overflow = 'hidden'; // 스크롤바 제거
                el.style.height = 'auto !important'; // 자동 높이
            });
         	
         	// 4-2. 테이블 컨테이너 영역 조정
            document.querySelectorAll('.fc-daygrid-body').forEach(function(el) {
                el.style.width = '100% !important'; // 전체 너비
                el.style.height = 'auto !important'; // 자동 높이
                el.style.overflow = 'hidden'; // 스크롤바 제거
            });
            
            // 5. 날짜 셀 스타일 적용
            document.querySelectorAll('.fc-daygrid-day').forEach(function(el) {
                el.style.borderColor = '#f0f0f0'; // 매우 연한 구분선
                el.style.height = '42px'; // 셀 높이 고정
                el.style.minHeight = '42px'; // 최소 높이도 설정
                el.style.maxHeight = '42px'; // 최대 높이 제한
                el.style.cursor = 'pointer'; // 클릭 가능 표시
                el.style.boxSizing = 'border-box'; // 테두리 포함 크기 계산
                
                var number = el.querySelector('.fc-daygrid-day-number');
                if (number) {
                    number.style.fontSize = '12px';
                    number.style.fontWeight = '400';
                    number.style.color = '#333';
                    number.style.padding = '3px';
                    number.style.textAlign = 'center';
                    number.style.textDecoration = 'none';
                    number.style.float = 'none'; // 중앙 정렬을 위해
                    number.style.display = 'block';
                }
            });
            
         	// 테이블 레이아웃 수정
            document.querySelectorAll('.fc-scrollgrid-sync-table').forEach(function(el) {
                el.style.tableLayout = 'fixed'; // 테이블 레이아웃을 fixed로 설정
                el.style.width = '100%'; // 전체 너비 사용
                el.style.height = 'auto'; // 자동 높이
            })
            
            // 테이블에 강제 너비 적용
		    document.querySelectorAll('.fc-scrollgrid').forEach(function(el) {
		        el.style.width = '100%'; // 테이블 전체 너비 사용
		    });
            
         	// 행(주) 높이를 동일하게 설정
            document.querySelectorAll('.fc-scrollgrid-sync-table tbody tr').forEach(function(el) {
                el.style.height = '45px'; // 모든 행의 높이를 동일하게 설정
                el.style.minHeight = '45px'; // 최소 높이도 설정
            });
         	
         	// 셀에 강제 높이 적용
            document.querySelectorAll('.fc-daygrid-day').forEach(function(el) {
                el.style.height = '45px'; // 높이 증가
                el.style.minHeight = '45px';
                el.style.boxSizing = 'border-box'; // 테두리와 패딩을 포함한 크기 계산
            });
            
            // 6. 비활성 날짜 스타일 적용
            document.querySelectorAll('.fc-day-other .fc-daygrid-day-number').forEach(function(el) {
                el.style.color = '#bbb';
                el.style.opacity = '0.7';
            });
            
            // 7. 이벤트 컨테이너 마진 조정
            document.querySelectorAll('.fc-daygrid-event-harness').forEach(function(el) {
                el.style.margin = '1px 0 0 0';
            });
            
            // 8. 오늘 날짜 스타일 적용
            document.querySelectorAll('.fc-day-today').forEach(function(el) {
                el.style.backgroundColor = 'rgba(66, 133, 244, 0.05)';
                
                // 오늘 날짜에 동그라미 표시 (두 번째 이미지처럼)
                var dateNum = el.querySelector('.fc-daygrid-day-number');
                if (dateNum) {
                	dateNum.style.backgroundColor = '#dbebfb'
                    dateNum.style.borderRadius = '50%';
                    dateNum.style.width = '24px';
                    dateNum.style.height = '24px';
                    dateNum.style.lineHeight = '24px';
                    dateNum.style.margin = '0 auto';
                    dateNum.style.padding = '0';
                    dateNum.style.display = 'flex';
                    dateNum.style.alignItems = 'center';
                    dateNum.style.justifyContent = 'center';
                }
            });
            
            // 9. 이벤트 행 높이 제한
            document.querySelectorAll('.fc-daygrid-day-events').forEach(function(el) {
                el.style.margin = '0';
                el.style.padding = '0 2px';
                el.style.maxHeight = '16px';
            });
        }

        // 일정 상세 정보 모달을 여는 함수
        function openScheduleDetail(scheduleNo) {
            // 모달 엘리먼트 가져오기
            const modalEl = document.getElementById('user-sc-schedule-detail-modal');
            
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
                    
                 	// 수정/삭제 버튼 숨기기 (간단한 선택자만 사용)
                    modalEl.querySelectorAll('.schedule-modal-btn-edit').forEach(function(btn) {
                        btn.style.display = 'none';
                    });
                    
                    modalEl.querySelectorAll('.schedule-modal-btn-delete').forEach(function(btn) {
                        btn.style.display = 'none';
                    });
                    
                    modalEl.querySelectorAll('.schedule-modal-actions').forEach(function(container) {
                        container.style.display = 'none';
                    });
                    
                 // 참석자 및 작성자 스타일 적용 (간단한 접근 방식)
                    setTimeout(function() {
                        // 참석자 스타일 적용
                        var attendeeLabels = modalEl.querySelectorAll('.schedule-detail-label');
                        attendeeLabels.forEach(function(label) {
                            if (label.textContent.includes('참석자')) {
                                var value = label.nextElementSibling;
                                if (value) {
                                    var spans = value.querySelectorAll('span');
                                    spans.forEach(function(span) {
                                        // 전체 텍스트를 하나의 스타일로 적용
                                        span.style.backgroundColor = '#e0e7ff';
                                        span.style.color = '#3730a3';
                                        span.style.padding = '3px 8px';
                                        span.style.borderRadius = '4px';
                                        span.style.fontSize = '13px';
                                        span.style.marginRight = '5px';
                                        span.style.display = 'inline-block';
                                        span.style.marginBottom = '5px';
                                    });
                                }
                            }
                            
                            // 작성자 스타일 적용
                            if (label.textContent.includes('작성자')) {
                                var value = label.nextElementSibling;
                                if (value) {
                                    var text = value.textContent;
                                    var idx = text.indexOf('(');
                                    if (idx > -1) {
                                        // 이름과 날짜 분리
                                        var name = text.substring(0, idx).trim();
                                        var date = text.substring(idx);
                                        
                                        // HTML 내용 변경
                                        value.innerHTML = '<span>' + 
                                                      name + '</span>' + 
                                                      '<span style="color:#6c757d; font-size:0.85em; margin-left:8px;">' + 
                                                      date + '</span>';
                                    }
                                }
                            }
                        });
                    }); 
                    
                 	// X 버튼에 이벤트 리스너 추가 - 직접적인 DOM 조작 방식
                    var closeButtons = modalEl.querySelectorAll('.schedule-modal-close');
                    for (var i = 0; i < closeButtons.length; i++) {
                        closeButtons[i].addEventListener('click', function() {
                            console.log('X 버튼 클릭');
                            modalEl.style.display = 'none';
                        });
                    }
                    
                    // 모달 외부 클릭 시 닫기 이벤트 설정
                    modalEl.addEventListener('click', function handleModalClick(e) {
                        if (e.target === modalEl) {
                            modalEl.style.display = 'none';
                            modalEl.removeEventListener('click', handleModalClick);
                        }
                    });
                },
                error: function(xhr, status, error) {
                    console.error('일정 상세 정보를 불러오는데 실패했습니다:', error);
                    alert('일정 정보를 불러오는데 실패했습니다.');
                }
            });
        }

        // 날짜별 일정 목록 모달 표시 함수
        function showDateSchedulesModal(dateStr, schedules) {
            window.selectedDate = dateStr;
            
            // 날짜 포맷팅 (예: 2024년 4월 15일)
            const date = new Date(dateStr);
            const formattedDate = date.getFullYear() + '년 ' + 
                                (date.getMonth() + 1) + '월 ' + 
                                date.getDate() + '일';
            
            // 일정 목록 모달 표시 (html 모달 컨테이너 필요)
            const modalEl = document.getElementById('user-sc-common-modal');
            
            // 모달 컨텐츠 생성 - 백틱/템플릿 리터럴 대신 문자열 연결 사용
            let modalContent = '<div class="schedule-modal-content">';
            modalContent += '<div class="schedule-modal-header">';
            modalContent += '<h3 class="schedule-modal-title">';
            modalContent += '<span class="schedule-modal-title-icon">';
            modalContent += '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">';
            modalContent += '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>';
            modalContent += '<line x1="16" y1="2" x2="16" y2="6"></line>';
            modalContent += '<line x1="8" y1="2" x2="8" y2="6"></line>';
            modalContent += '<line x1="3" y1="10" x2="21" y2="10"></line>';
            modalContent += '</svg>';
            modalContent += '</span>';
            modalContent += '<span id="selectedDateTitle">' + formattedDate + '</span> 일정';
            modalContent += '</h3>';
            //modalContent += '<button type="button" class="schedule-modal-close">×</button>';
            modalContent += '</div>';
            modalContent += '<div class="schedule-modal-body">';
            modalContent += '<div id="dateSchedulesList" class="date-schedules-list">';
            
            // 일정이 없는 경우 메시지 표시
            if (!schedules || schedules.length === 0) {
                modalContent += '<div class="no-schedules-message">';
                modalContent += '<p>이 날짜에 등록된 일정이 없습니다.</p>';
                modalContent += '</div>';
            } else {
                // 각 일정을 목록에 추가
                schedules.forEach(function(schedule) {
                    let typeClass = '';
                    let typeText = '';
                    
                    switch (schedule.scheduleType) {
                        case 'PERSONAL': 
                            typeClass = 'personal'; 
                            typeText = '개인'; 
                            break;
                        case 'TEAM': 
                            typeClass = 'team'; 
                            typeText = '팀'; 
                            break;
                        case 'COMPANY': 
                            typeClass = 'company'; 
                            typeText = '회사'; 
                            break;
                        default: 
                            typeClass = 'other'; 
                            typeText = '기타'; 
                            break;
                    }
                    
                    // 중요도에 따른 클래스
                    let importanceClass = schedule.important ? schedule.important.toLowerCase() : '';
                    
                    // 일정 시간 포맷팅
                    let timeText = '';
                    if (schedule.allDay === 'Y') {
                        timeText = '종일';
                    } else {
                        // 시간만 표시
                        const startTime = new Date(schedule.startDate).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
                        const endTime = new Date(schedule.endDate).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
                        timeText = startTime + ' ~ ' + endTime;
                    }
                    
                    // 일정 항목 HTML - 백틱/템플릿 리터럴 대신 문자열 연결 사용
                    modalContent += '<div class="date-schedule-item type-' + typeClass + '" onclick="openScheduleDetail(' + schedule.scheduleNo + ')">';
                    modalContent += '<div class="date-schedule-title">';
                    
                    if (importanceClass === 'high') {
                        modalContent += '<span class="date-schedule-importance high"></span>';
                    }
                    
                    modalContent += schedule.scheduleTitle;
                    modalContent += '<span class="date-schedule-type-badge ' + typeClass + '">' + typeText + '</span>';
                    modalContent += '</div>';
                    modalContent += '<div class="date-schedule-time">' + timeText + '</div>';
                    
                    if (schedule.location) {
                        modalContent += '<div class="date-schedule-location">';
                        modalContent += '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">';
                        modalContent += '<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>';
                        modalContent += '<circle cx="12" cy="10" r="3"></circle>';
                        modalContent += '</svg> ';
                        modalContent += schedule.location;
                        modalContent += '</div>';
                    }
                    
                    modalContent += '</div>';
                });
            }
            
            // 모달 푸터 추가
            modalContent += '</div>'; // dateSchedulesList 닫기
            modalContent += '</div>'; // modal-body 닫기
            modalContent += '<div class="schedule-modal-footer">';
            modalContent += '<button type="button" class="schedule-modal-btn schedule-modal-btn-add" onclick="addScheduleForDate()">';
            modalContent += '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">';
            modalContent += '<line x1="12" y1="5" x2="12" y2="19"></line>';
            modalContent += '<line x1="5" y1="12" x2="19" y2="12"></line>';
            modalContent += '</svg>';
            modalContent += '새 일정 추가';
            modalContent += '</button>';
            modalContent += '<button type="button" class="schedule-modal-btn schedule-modal-btn-back" onclick="closeModal()">닫기</button>';
            modalContent += '</div>'; // modal-footer 닫기
            modalContent += '</div>'; // modal-content 닫기
            
            // 모달에 컨텐츠 삽입
            modalEl.innerHTML = modalContent;
            
            // 모달 표시
            modalEl.classList.add('active');
            modalEl.style.display = 'flex';
            
         	// X 버튼에 이벤트 리스너 추가
            const closeButton = modalEl.querySelector('.schedule-modal-close');
            if (closeButton) {
                closeButton.onclick = function() {
                    closeModal();
                };
            }
            
            // 모달 외부 클릭 시 닫기 이벤트 추가
            modalEl.addEventListener('click', function(e) {
                if (e.target === modalEl) {
                    closeModal();
                }
            });
        }
    });

    // 전역 함수 선언
    window.closeModal = function() {
        document.getElementById('user-sc-common-modal').style.display = 'none';
        document.getElementById('user-sc-common-modal').classList.remove('active');
        document.getElementById('user-sc-schedule-detail-modal').style.display = 'none';
        window.selectedDate = null;
    };

    window.addScheduleForDate = function() {
        if (window.selectedDate) {
            // 일정 등록 페이지로 이동하면서 선택한 날짜 정보 전달
            location.href = 'enrollForm.sc?date=' + window.selectedDate;
        }
    };

    // 전역 변수 선언
    window.selectedDate = null;

    // 일정 상세 정보 모달을 여는 전역 함수
    window.openScheduleDetail = function(scheduleNo) {
        // 모달 엘리먼트 가져오기
        const modalEl = document.getElementById('user-sc-schedule-detail-modal');
        
        // 일정 상세 모달 열기
        $.ajax({
            url: 'detailModal.sc',
            type: 'GET',
            data: {
                scheduleNo: scheduleNo
            },// 일정 상세 모달 열기
            success: function(response) {
                console.log('일정 상세 정보 로드 성공');
                
                // 모달 컨테이너 초기화 및 설정
                modalEl.innerHTML = '';
                modalEl.innerHTML = response;
                
                // 모달 표시 및 중앙 정렬
                modalEl.style.display = 'flex';
                modalEl.style.justifyContent = 'center';
                modalEl.style.alignItems = 'center';
                
             	// X 버튼에 이벤트 리스너 추가
                const closeButton = modalEl.querySelector('.schedule-modal-close');
                if (closeButton) {
                    closeButton.onclick = function() {
                        closeModal();
                    };
                }
                
                // 모달 외부 클릭 시 닫기 이벤트 설정
                modalEl.addEventListener('click', function handleModalClick(e) {
                    if (e.target === modalEl) {
                        modalEl.style.display = 'none';
                        modalEl.removeEventListener('click', handleModalClick);
                    }
                });
            },
            error: function(xhr, status, error) {
                console.error('일정 상세 정보를 불러오는데 실패했습니다:', error);
                alert('일정 정보를 불러오는데 실패했습니다.');
            }
        });
    };
    
 // 페이지 완전 로딩 후 추가 실행
    window.addEventListener('load', function() {
      setTimeout(function() {
        var lastRow = document.querySelector('.fc-scrollgrid-sync-table tbody tr:last-child');
        if (lastRow) {
          // 마지막 행의 모든 자식 요소에 스타일 적용
          var elements = lastRow.querySelectorAll('*');
          elements.forEach(function(el) {
            el.style.height = '45px';
            el.style.minHeight = '45px';
          });
          console.log('마지막 행 스타일 강제 적용 완료');
        }
      }, 500); // 0.5초 지연
    });
    </script>
    <!-- 공지사항 AJAX 호출을 위한 스크립트 추가 -->
	<script>
	$(document).ready(function() {
	    // 최근 공지사항 가져오기
	    $.ajax({
	        url: "${pageContext.request.contextPath}/recentNotices",
	        type: "GET",
	        data: { limit: 3 }, // 표시할 공지사항 수 (필요에 따라 조정)
	        success: function(notices) {
	            // 결과를 받아서 테이블에 표시
	            var tbody = $("#notice-list-body");
	            tbody.empty(); // 기존 내용 제거
	
	            if (notices && notices.length > 0) {
	                // 공지사항이 있는 경우
	                $.each(notices, function(index, notice) {
	                    var row = $("<tr>");
	                    
	                    // 제목 (링크 포함)
	                    var titleCell = $("<td>");
	                    var titleLink = $("<a>").attr("href", "${pageContext.request.contextPath}/noticeDetail/" + notice.noticeNo)
	                                         .text(notice.noticeTitle)
	                                         .css("color", "#333")
	                                         .css("text-decoration", "none");
	                    titleCell.append(titleLink);
	                    
	                    // 작성자
	                    var writerCell = $("<td>").text(notice.empName || "관리자");
	                    
	                    // 날짜 (yyyy-mm-dd 형식으로 변환)
	                    var date = new Date(notice.createDate);
	                    var dateStr = date.getFullYear() + "-" + 
	                                 padZero(date.getMonth() + 1) + "-" + 
	                                 padZero(date.getDate());
	                    var dateCell = $("<td>").text(dateStr);
	                    
	                    // 행에 셀 추가
	                    row.append(titleCell).append(writerCell).append(dateCell);
	                    tbody.append(row);
	                });
	            } else {
	                // 공지사항이 없는 경우
	                tbody.html('<tr><td colspan="3" class="text-center">등록된 공지사항이 없습니다.</td></tr>');
	            }
	        },
	        error: function(xhr, status, error) {
	            console.error("공지사항 불러오기 오류:", error);
	            var tbody = $("#notice-list-body");
	            tbody.html('<tr><td colspan="3" class="text-center">공지사항을 불러오는 중 오류가 발생했습니다.</td></tr>');
	        }
	    });
	    
	    // 날짜 포맷팅용 함수 (1 -> 01)
	    function padZero(num) {
	        return num < 10 ? "0" + num : num;
	    }
	});
	
	// 출근 버튼 클릭 시
	document.querySelector(".btn-checkin").addEventListener("click", function() {
	    fetch('/attendance/clockIn', {
	        method: 'POST',
	        headers: {
	            'Content-Type': 'application/json'
	        },
	        body: JSON.stringify({ type: 'NORMAL' })  // 'NORMAL' 또는 'REMOTE' 근무 유형
	    })
	    .then(response => response.json())
	    .then(data => {
	        if (data.success) {
	            alert("출근 완료!");
	            updateClockInTime();  // 출근 시간 갱신 함수 호출
	        } else {
	            alert("출근 처리 실패");
	        }
	    })
	    .catch(error => {
	        console.error("Error:", error);
	    });
	});

	// 출근 시간 업데이트 함수
	function updateClockInTime() {
	    fetch('/attendance/getClockInTime')  // 서버에서 출근 시간 가져오기
	        .then(response => response.json())
	        .then(data => {
	            const clockInElement = document.getElementById('clockInTime');
	            if (data.success && data.clockInTime) {
	                const clockInTime = new Date(data.clockInTime);  // 출근 시간
	                clockInElement.textContent = `${clockInTime.getHours()} : ${clockInTime.getMinutes()}`;
	            }
	        })
	        .catch(error => {
	            console.error("출근 시간 업데이트 오류:", error);
	        });
	}

	</script>
</body>
</html>