<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/attendanceMain.css">
</head>
<body>

	<jsp:include page="../common/header.jsp" />

	<br>
	<div class="outer">
		<div class="attendance-container">
	        <!-- 왼쪽 사이드바 -->
	        <div class="attendance-sidebar">
	            <div class="sidebar-title">근태 관리</div>
	            <div class="sidebar-subtitle">출퇴근과 근무 기록을 간편하게 관리하세요.</div>
	            
	            <div class="today-date">2025년 5월 20일 (화)</div>
	            
	            <div class="time-display"></div>
	            
	            <div class="status-row">
				    <span class="status-label">업무 상태</span>
				    <c:choose>
				        <c:when test="${attendance.clockOutTime != null}">
				            <span class="status-value">-</span>
				        </c:when>
				        <c:when test="${attendance.attStatus == 'NORMAL'}">
				            <span class="status-value status-green">업무 중</span>
				        </c:when>
				        <c:when test="${attendance.attStatus == 'REMOTE'}">
				            <span class="status-value status-blue">재택 근무 중</span>
				        </c:when>
				        <c:otherwise>
				            <span class="status-value">-</span>
				        </c:otherwise>
				    </c:choose>
				</div>

	            <div class="status-row">
				    <span class="status-label">출근 시간</span>
				    <span class="status-value">
				        <c:choose>
				            <c:when test="${not empty attendance.clockInTime}">
				                <fmt:formatDate value="${attendance.clockInTime}" pattern="HH:mm:ss" />
				            </c:when>
				            <c:otherwise>-</c:otherwise>
				        </c:choose>
				    </span>
				</div>
	            
	            
				<div class="status-row">
				    <span class="status-label">퇴근 시간</span>
				    <span class="status-value">
				        <c:choose>
				            <c:when test="${not empty attendance.clockOutTime}">
				                <fmt:formatDate value="${attendance.clockOutTime}" pattern="HH:mm:ss" />
				            </c:when>
				            <c:otherwise>-</c:otherwise>
				        </c:choose>
				    </span>
				</div>
	            
	            <div class="status-row">
	                <span class="status-label">업무 형태</span>
	                <span class="status-value work-type">
                        <c:choose>
                            <c:when test="${attendance.attStatus == 'NORMAL'}">사무실 근무</c:when>
                            <c:when test="${attendance.attStatus == 'REMOTE'}">재택 근무</c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </span>
	            </div>
	            
	            <c:set var="baseSeconds" value="${weekDuration.seconds}" />
				<c:set var="clockInTimeMillis" value="${attendance.clockInTime.time}" />
	            
	            <div class="status-row">
				    <span class="status-label">주간 누적 근무 시간</span>
					<span class="status-value">
				        <span id="liveWorkTime"></span>
				    </span>
				</div>
	            
	            <div class="button-group">
	                <button class="btn btn-clock-in">출근하기</button>
	                <button class="btn btn-clock-out">퇴근하기</button>
	            </div>
	            
	            <div class="nav-buttons">
				    <div class="nav-btn home" data-type="REMOTE">🏠 HOME</div>
				    <div class="nav-btn office active" data-type="NORMAL">🏢 OFFICE</div>
				</div>

	        </div>
	        
	        <!-- 오른쪽 메인 컨텐츠 -->
	        <div class="attendance-content">
	            <div class="content-header">
	                <div class="content-title">나의 근태 현황</div>
	                
	                <div class="month-selector">
					  <span class="nav-arrow" id="prevMonth">〈</span>
					  <div class="month-display" id="monthText">${currentYear}. <span id="monthNum">${currentMonth < 10 ? "0" + currentMonth : currentMonth}</span></div>
					  <span class="nav-arrow" id="nextMonth">〉</span>
					</div>
	                
	                <c:set var="baseMonthSeconds" value="${monthDuration.seconds}" />
					<c:set var="baseMonthOverSeconds" value="${monthOverDuration.seconds}" />
	                
	                <div class="time-summary">
	                    <div class="time-card">
						  <div class="time-card-title">이번주 누적</div>
						  <div class="time-card-value" id="liveWeekTotal"></div>
						</div>
						
						<div class="time-card">
						  <div class="time-card-title">이번주 초과</div>
						  <div class="time-card-value" id="liveWeekOver"></div>
						</div>
						
						<div class="time-card">
						  <div class="time-card-title">이번주 잔여</div>
						  <div class="time-card-value" id="liveWeekRemain"></div>
						</div>
						
						<div class="time-card">
						  <div class="time-card-title">이번달 누적</div>
						  <div class="time-card-value" id="liveMonthTotal"></div>
						</div>
						
						<div class="time-card">
						  <div class="time-card-title">이번달 연장</div>
						  <div class="time-card-value" id="liveMonthOver"></div>
						</div>
	                </div>
	            </div>
	            
	            <!-- 주간 근무 현황 -->
	            <div class="weekly-summary">
	                <c:forEach var="week" items="${weeklySummaries}">
					    <div class="week-row ${week.weekNumber == currentWeek ? 'expanded' : ''}" data-week="${week.weekNumber}">
					        <div>
					            <span class="week-number">${week.weekNumber}주차 (${week.startDate} ~ ${week.endDate})</span>
					            <span class="week-plus">+</span>
					        </div>
					        <div class="week-time">
							  누적 근무시간 
							  ${fn:substringBefore(week.totalSeconds / 3600, '.')}H 
							  ${fn:substringBefore((week.totalSeconds % 3600) / 60, '.')}M 
							  ${week.totalSeconds % 60}S |
							  초과 근무시간 
							  ${fn:substringBefore(week.overtimeSeconds / 3600, '.')}H 
							  ${fn:substringBefore((week.overtimeSeconds % 3600) / 60, '.')}M 
							  ${week.overtimeSeconds % 60}S
							</div>
					    </div>
					    
					    <!-- 상세 정보 표시 -->
						  <div class="attendance-detail" data-detail="${week.weekNumber}" style="${week.weekNumber == currentWeek ? '' : 'display:none'}">
						    <div class="attendance-header">
						      <div class="attendance-column">일자</div>
						      <div class="attendance-column">업무시작</div>
						      <div class="attendance-column">업무종료</div>
						      <div class="attendance-column">총근무시간</div>
						    </div>
						
						    <c:forEach var="att" items="${weeklyAttendanceMap[week.weekNumber]}">
						      <div class="attendance-row">
						        <div class="attendance-cell"><fmt:formatDate value="${att.attendanceDate}" pattern="yyyy-MM-dd" /></div>
						        <div class="attendance-cell">
						          <c:choose>
						            <c:when test="${att.clockInTime != null}">
						              <fmt:formatDate value="${att.clockInTime}" pattern="HH:mm:ss" />
						            </c:when>
						            <c:otherwise>-</c:otherwise>
						          </c:choose>
						        </div>
						        <div class="attendance-cell">
						          <c:choose>
						            <c:when test="${att.clockOutTime != null}">
						              <fmt:formatDate value="${att.clockOutTime}" pattern="HH:mm:ss" />
						            </c:when>
						            <c:otherwise>-</c:otherwise>
						          </c:choose>
						        </div>
						        <div class="attendance-cell">
						          <c:choose>
								  <c:when test="${att.clockInTime != null && att.clockOutTime != null}">
								    <c:set var="totalSec" value="${(att.clockOutTime.time - att.clockInTime.time) / 1000}" />
								    <c:set var="h" value="${fn:substringBefore(totalSec / 3600, '.')}" />
									<c:set var="m" value="${fn:substringBefore((totalSec % 3600) / 60, '.')}" />
									<c:set var="s" value="${fn:substringBefore(totalSec % 60, '.')}" />
								    ${h}H ${m}M ${s}S
								  </c:when>
								  <c:otherwise>-</c:otherwise>
								</c:choose>
						        </div>
						      </div>
						    </c:forEach>
						  </div>
					</c:forEach>
	            </div>
	        </div>
	    </div>
    </div>
    
<script>
//근태 관리 시스템 전체 JavaScript 코드
document.addEventListener('DOMContentLoaded', function () {
    // 현재 시간 표시 함수
    function updateTime() {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        
        document.querySelector('.time-display').textContent = hours + ":" + minutes + ":" + seconds;
    }
    
    // 1초마다 시간 업데이트
    setInterval(updateTime, 1000);
    updateTime(); // 초기 시간 설정
    
    // 스타일 추가
    const style = document.createElement('style');
    style.textContent = `
        .attendance-detail {
            overflow: hidden;
            transition: max-height 0.3s ease-in-out;
            max-height: 0;
        }
        
        .attendance-detail.show {
            max-height: 500px; /* 충분히 큰 값 */
        }
        
        .no-records-message {
            padding: 15px;
            text-align: center;
            color: #888;
            font-style: italic;
            background-color: #f9f9f9;
            border-radius: 4px;
            margin: 10px 0;
        }
    `;
    document.head.appendChild(style);
    
    // 기록이 없는 주차에 메시지 추가
    document.querySelectorAll('.attendance-detail').forEach(detail => {
        const rows = detail.querySelectorAll('.attendance-row');
        // 헤더만 있고 실제 데이터 행이 없는 경우
        if (rows.length === 0) {
            const message = document.createElement('div');
            message.className = 'no-records-message';
            message.textContent = '이 주에 등록된 근무 기록이 없습니다.';
            
            const header = detail.querySelector('.attendance-header');
            if (header) {
                header.after(message);
            } else {
                detail.appendChild(message);
            }
        }
    });
    
    // 모든 토글 초기 상태 설정
    document.querySelectorAll('.attendance-detail').forEach(detail => {
        // detail에 새 스타일 적용
        detail.style.display = 'none';
        detail.classList.remove('show');
    });
    
    // 주간 근무 요약 토글 기능
    document.querySelectorAll('.week-row').forEach(row => {
        // 기존 이벤트 리스너 제거 및 재설정
        const newRow = row.cloneNode(true);
        row.parentNode.replaceChild(newRow, row);
        
        newRow.addEventListener('click', function() {
            const weekId = this.getAttribute('data-week');
            const detail = document.querySelector(`.attendance-detail[data-detail="${weekId}"]`);
            const plusSign = this.querySelector('.week-plus');
            
            // 토글 상태 확인
            const isCurrentlyExpanded = this.classList.contains('expanded');
            
            if (isCurrentlyExpanded) {
                // 닫기
                this.classList.remove('expanded');
                if (plusSign) plusSign.textContent = '+';
                
                if (detail) {
                    detail.classList.remove('show');
                    setTimeout(function() {
                        detail.style.display = 'none';
                    }, 300); // 트랜지션 종료 후 숨김
                }
            } else {
                // 모든 주차 닫기
                document.querySelectorAll('.week-row').forEach(r => {
                    r.classList.remove('expanded');
                    const sign = r.querySelector('.week-plus');
                    if (sign) sign.textContent = '+';
                });
                
                document.querySelectorAll('.attendance-detail').forEach(d => {
                    d.classList.remove('show');
                    d.style.display = 'none';
                });
                
                // 선택한 주차 열기
                this.classList.add('expanded');
                if (plusSign) plusSign.textContent = '−';
                
                if (detail) {
                    detail.style.display = 'block';
                    // display:block 적용 후 약간의 지연을 두고 show 클래스 추가
                    setTimeout(function() {
                        detail.classList.add('show');
                    }, 10);
                }
            }
        });
    });
    
    // 현재 주차 초기 표시
    const currentWeek = parseInt('${currentWeek}', 10);
    const currentWeekRow = document.querySelector(`.week-row[data-week="${currentWeek}"]`);
    const currentWeekDetail = document.querySelector(`.attendance-detail[data-detail="${currentWeek}"]`);
    
    if (currentWeekRow && currentWeekDetail) {
        currentWeekRow.classList.add('expanded');
        const plusSign = currentWeekRow.querySelector('.week-plus');
        if (plusSign) plusSign.textContent = '−';
        
        // 초기 화면에서는 애니메이션 없이 바로 표시
        currentWeekDetail.style.display = 'block';
        setTimeout(function() {
            currentWeekDetail.classList.add('show');
        }, 10);
    }
    
    // 출퇴근 버튼 초기 상태 설정
    const clockIn = '${attendance.clockInTime}' !== '';
    const clockOut = '${attendance.clockOutTime}' !== '';

    const btnIn = document.querySelector('.btn-clock-in');
    const btnOut = document.querySelector('.btn-clock-out');

    if (!clockIn) {
        btnIn.disabled = false;
        btnOut.disabled = true;
    } else if (!clockOut) {
        btnIn.disabled = true;
        btnOut.disabled = false;
    } else {
        btnIn.disabled = true;
        btnOut.disabled = true;
    }
    
    // HOME/OFFICE 초기 상태 설정
    const homeBtn = document.querySelector('.nav-btn.home');
    const officeBtn = document.querySelector('.nav-btn.office');
    
    if ('${attendance.attStatus}' === 'REMOTE') {
        homeBtn.classList.add('active');
        officeBtn.classList.remove('active');
    } else {
        // 기본값 또는 NORMAL인 경우
        homeBtn.classList.remove('active');
        officeBtn.classList.add('active');
    }
    
    // 근무 시간 관련 업데이트 - 개선된 버전
    const weekTotalEl = document.getElementById("liveWeekTotal");
    const weekOverEl = document.getElementById("liveWeekOver");
    const weekRemainEl = document.getElementById("liveWeekRemain");
    const monthTotalEl = document.getElementById("liveMonthTotal");
    const monthOverEl = document.getElementById("liveMonthOver");
    const totalEl = document.getElementById("liveWorkTime");

    const clockInExists = '${attendance.clockInTime}' !== '';
    const hasClockOut = '${attendance.clockOutTime}' !== '';

    // 디버깅을 위한 로그 추가
    console.log("출근 여부: ", clockInExists);
    console.log("퇴근 여부: ", hasClockOut);

    // 기본값 설정
    if (!clockInExists) {
        totalEl.textContent = "00H 00M 00S";
        weekTotalEl.textContent = "00H 00M 00S";
        weekOverEl.textContent = "00H 00M 00S";
        weekRemainEl.textContent = "35H 00M 00S";
        monthTotalEl.textContent = '${month.h}H ${month.m}M ${month.s}S';
        monthOverEl.textContent = '${monthOver.h}H ${monthOver.m}M ${monthOver.s}S';
        return;
    }

    // 모든 시간 값을 초 단위로 변환하여 계산
    // weekDuration.seconds 값이 정의되지 않았을 경우를 대비해 기본값 0 설정
    let baseSeconds = parseInt('${weekDuration.seconds}', 10) || 0;
    let baseMonthSeconds = parseInt('${baseMonthSeconds}', 10) || 0; 
    let baseMonthOverSeconds = parseInt('${baseMonthOverSeconds}', 10) || 0;

    // 디버깅 로그
    console.log("기본 주간 초: ", baseSeconds);
    console.log("기본 월간 초: ", baseMonthSeconds);
    console.log("기본 월간 초과 초: ", baseMonthOverSeconds);

    // 출근 시간을 JavaScript Date 객체로 변환
    let clockInTime = parseInt('${attendance.clockInTime.time}', 10);
    console.log("출근 시간 밀리초: ", clockInTime);
    
    // 표준 근무 시간 (35시간을 초로 변환)
    const standard = 126000; // 35 * 60 * 60

    // 시간 포맷 함수 (개선 버전)
    function format(s) {
        // NaN 체크 및 숫자가 아닌 경우 처리
        if (isNaN(s) || typeof s !== 'number') {
            console.error("유효하지 않은 시간 값: ", s);
            s = 0;
        }
        
        // 음수 처리 추가
        s = Math.max(0, s);
        
        const h = String(Math.floor(s / 3600)).padStart(2, '0');
        const m = String(Math.floor((s % 3600) / 60)).padStart(2, '0');
        const sec = String(Math.floor(s % 60)).padStart(2, '0');
        return h + "H " + m + "M " + sec + "S";
    }

    // 퇴근하지 않은 경우 실시간 업데이트
    if (!hasClockOut && clockInTime > 0) {
        console.log("실시간 업데이트 모드 활성화");
        
        function updateLiveAll() {
            try {
                const now = Date.now();
                // 현재 시간과 출근 시간의 차이를 초 단위로 계산
                const elapsed = Math.floor((now - clockInTime) / 1000);
                console.log("경과 시간(초): ", elapsed);
                
                const total = baseSeconds + elapsed;
                console.log("총 누적 시간(초): ", total);

                // 각 요소 업데이트
                totalEl.textContent = format(total);
                weekTotalEl.textContent = format(total);

                if (total > standard) {
                    const over = total - standard;
                    weekOverEl.textContent = format(over);
                    weekRemainEl.textContent = "00H 00M 00S";
                    monthTotalEl.textContent = format(baseMonthSeconds + elapsed);
                    monthOverEl.textContent = format(baseMonthOverSeconds + over);
                } else {
                    weekOverEl.textContent = "00H 00M 00S";
                    weekRemainEl.textContent = format(standard - total);
                    monthTotalEl.textContent = format(baseMonthSeconds + elapsed);
                    monthOverEl.textContent = format(baseMonthOverSeconds);
                }
            } catch (error) {
                console.error("실시간 업데이트 중 오류 발생: ", error);
            }
        }

        // 초기 호출 및 1초마다 업데이트
        updateLiveAll();
        const intervalId = setInterval(updateLiveAll, 1000);
        
        // 페이지 언로드 시 인터벌 정리
        window.addEventListener('beforeunload', function() {
            clearInterval(intervalId);
        });
    } else {
        console.log("정적 시간 표시 모드");
        // 이미 퇴근한 경우 정적 값 표시
        totalEl.textContent = format(baseSeconds);
        weekTotalEl.textContent = format(baseSeconds);
        
        // 초과 근무 시간 계산
        const overTime = Math.max(0, baseSeconds - standard);
        weekOverEl.textContent = format(overTime);
        
        // 잔여 시간 계산
        const remainTime = Math.max(0, standard - baseSeconds);
        weekRemainEl.textContent = format(remainTime);
        
        // 월간 누적 및 초과 시간
        monthTotalEl.textContent = format(baseMonthSeconds);
        monthOverEl.textContent = format(baseMonthOverSeconds);
    }
    
    // 월 선택 기능 개선
    const monthText = document.getElementById('monthText');
    
    let year = parseInt('${currentYear}', 10);
    let month = parseInt('${currentMonth}', 10);
    
    // 초기 월 표시 포맷 수정
    updateMonthDisplay();
    
    // 이벤트 리스너 한 번만 등록되도록
    const prevBtn = document.getElementById('prevMonth');
    const nextBtn = document.getElementById('nextMonth');
    
    const newPrevBtn = prevBtn.cloneNode(true);
    const newNextBtn = nextBtn.cloneNode(true);
    
    prevBtn.parentNode.replaceChild(newPrevBtn, prevBtn);
    nextBtn.parentNode.replaceChild(newNextBtn, nextBtn);
    
    // 월 이동 기능 (AJAX 요청 추가)
    newPrevBtn.addEventListener('click', () => {
        month -= 1;
        if (month < 1) {
            month = 12;
            year -= 1;
        }
        updateMonthDisplay();
        fetchMonthData(year, month);
    });

    newNextBtn.addEventListener('click', () => {
        month += 1;
        if (month > 12) {
            month = 1;
            year += 1;
        }
        updateMonthDisplay();
        fetchMonthData(year, month);
    });

    function updateMonthDisplay() {
        const formattedMonth = month < 10 ? '0' + month : month;
        monthText.innerHTML = year + ". <span id='monthNum'>" + formattedMonth + "</span>";
    }
    
    // 월 데이터 가져오는 함수 (AJAX)
    function fetchMonthData(year, month) {
        // 이 부분은 백엔드에 월별 데이터를 요청하는 AJAX 코드로 구현해야 함
        // 예시: 
        /*
        fetch(`${pageContext.request.contextPath}/attendance/getMonthData?year=${year}&month=${month}`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })
        .then(res => res.json())
        .then(data => {
            // 받아온 데이터로 UI 업데이트
            // 주차별 데이터 업데이트
            // 월간 누적/초과 시간 업데이트
        })
        .catch(error => {
            console.error("월 데이터 가져오기 실패:", error);
        });
        */
        
        // 미구현 상태에서는 안내 메시지
        console.log(`${year}년 ${month}월 데이터 요청 기능 필요`);
    }
});

// 출근 버튼 이벤트
document.querySelector('.btn-clock-in').addEventListener('click', function () {
    // 현재 활성화된 버튼(HOME 또는 OFFICE)의 데이터 타입 가져오기
    const activeBtn = document.querySelector('.nav-btn.active');
    const type = activeBtn ? activeBtn.getAttribute('data-type') : 'NORMAL';

    fetch('${pageContext.request.contextPath}/attendance/clockIn', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({type: type})
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            location.reload(); // 새로고침하여 값 반영
        } else {
            alert('출근 처리 중 오류가 발생했습니다.');
        }
    })
    .catch(error => {
        console.error("출근 처리 중 오류 발생:", error);
        alert('출근 처리 중 오류가 발생했습니다.');
    });
});

// 퇴근 버튼 이벤트
document.querySelector('.btn-clock-out').addEventListener('click', function () {
    if (!confirm('정말 퇴근 처리하시겠습니까?')) {
        return;
    }

    fetch('${pageContext.request.contextPath}/attendance/clockOut', { 
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            location.reload();
        } else {
            alert('퇴근 처리 중 오류가 발생했습니다.');
        }
    })
    .catch(error => {
        console.error("퇴근 처리 중 오류 발생:", error);
        alert('퇴근 처리 중 오류가 발생했습니다.');
    });
});

// HOME/OFFICE 버튼 클릭 이벤트
document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', function () {
        const type = this.getAttribute('data-type');
        const isClockIn = '${attendance.clockInTime}' !== '';
        const hasClockOut = '${attendance.clockOutTime}' !== '';
        
        // 버튼 활성화 시각적 변경
        document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        
        // 출근 중이고 퇴근 안한 상태일 때만 API 호출하여 업무 형태 변경
        if (isClockIn && !hasClockOut) {
            updateWorkType(type);
        }
    });
});

// 업무 형태 변경 함수
function updateWorkType(type) {
    fetch('${pageContext.request.contextPath}/attendance/updateType', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({type: type})
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // 업무 상태 텍스트 업데이트
            const statusValue = document.querySelector('.status-value:not(.work-type)');
            if (statusValue) {
                if (type === 'NORMAL') {
                    statusValue.className = 'status-value status-green';
                    statusValue.textContent = '업무 중';
                    document.querySelector('.work-type').textContent = '사무실 근무';
                } else {
                    statusValue.className = 'status-value status-blue';
                    statusValue.textContent = '재택 근무 중';
                    document.querySelector('.work-type').textContent = '재택 근무';
                }
            }
        } else {
            alert('업무 형태 변경 중 오류가 발생했습니다.');
        }
    })
    .catch(error => {
        console.error('업무 형태 변경 중 오류 발생:', error);
        alert('업무 형태 변경 중 오류가 발생했습니다.');
    });
}
</script>
</body>
</html>