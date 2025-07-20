<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
            일정 상세
        </h3>
        <button type="button" class="schedule-modal-close" onclick="document.getElementById('schedule-detail-modal').style.display='none';">×</button>
    </div>
    
    <!-- 모달 내용 -->
    <div class="schedule-modal-body">
        <!-- 일정 제목 및 중요도 -->
        <div class="schedule-detail-group">
            <div class="schedule-detail-title-area">
                <h2 class="schedule-detail-title">${schedule.scheduleTitle}</h2>
                
                <c:if test="${!empty schedule.important}">
                    <span class="schedule-detail-important ${schedule.important.toLowerCase()}">
                        <c:choose>
                            <c:when test="${schedule.important eq 'HIGH'}">높음</c:when>
                            <c:when test="${schedule.important eq 'NORMAL'}">보통</c:when>
                            <c:when test="${schedule.important eq 'LOW'}">낮음</c:when>
                        </c:choose>
                    </span>
                </c:if>
            </div>
        </div>
        
        <!-- 일정 유형 -->
        <div class="schedule-detail-section">
            <div class="schedule-detail-label">일정 유형</div>
            <div class="schedule-detail-value">
                <span class="schedule-detail-type ${schedule.scheduleType.toLowerCase()}">
                    <c:choose>
                        <c:when test="${schedule.scheduleType eq 'PERSONAL'}">개인 일정</c:when>
                        <c:when test="${schedule.scheduleType eq 'TEAM'}">팀 일정</c:when>
                        <c:when test="${schedule.scheduleType eq 'COMPANY'}">회사 일정</c:when>
                        <c:when test="${schedule.scheduleType eq 'OTHER'}">기타 일정</c:when>
                    </c:choose>
                </span>
            </div>
        </div>
        
        <!-- 일정 기간 -->
        <div class="schedule-detail-section">
            <div class="schedule-detail-label">일정 기간</div>
            <div class="schedule-detail-value">
                <fmt:formatDate value="${schedule.startDate}" pattern="yyyy년 MM월 dd일 HH:mm" /> ~ 
                <fmt:formatDate value="${schedule.endDate}" pattern="yyyy년 MM월 dd일 HH:mm" />
            </div>
        </div>
        
        <!-- 장소 -->
        <c:if test="${!empty schedule.location}">
            <div class="schedule-detail-section">
                <div class="schedule-detail-label">장소</div>
                <div class="schedule-detail-value">${schedule.location}</div>
            </div>
        </c:if>
        
        <!-- 설명 -->
        <div class="schedule-detail-section">
            <div class="schedule-detail-label">설명</div>
            <div class="schedule-detail-description" style="text-align: left !important; text-align-last: left !important; display: block;">
                <c:choose>
                    <c:when test="${!empty schedule.description}">
                        <span style="text-align: left !important; margin-left: 0; padding-left: 0; display: block;">${schedule.description}</span>
                    </c:when>
                    <c:otherwise>
                        <span class="schedule-detail-empty" style="text-align: left !important; margin-left: 0; padding-left: 0; display: block;">내용 없음</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>       
        
        <!-- 참석자 목록 - 항상 표시 -->
        <div class="schedule-detail-section">
            <div class="schedule-detail-label">참석자</div>
            <div class="schedule-detail-attendee-list">
                <c:choose>
                    <c:when test="${!empty attendees}">
                        <c:forEach items="${attendees}" var="attendee">
                            <span class="schedule-detail-attendee-item">
                                ${attendee.empName} (${attendee.deptName})
                                <c:if test="${attendee.empNo eq loginUser.empNo}">
                                	<span class="schedule-detail-current-user">(나)</span>
                                </c:if>
                            </span>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="schedule-detail-empty">참석자 없음</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <!-- 작성자 정보 섹션 -->
		<div class="schedule-detail-section">
		    <div class="schedule-detail-label">작성자</div>
		    <div class="schedule-detail-value">
		        ${schedule.empName} 
		        <span class="schedule-detail-date">
		        (작성일: <fmt:formatDate value="${schedule.createDate}" pattern="yyyy-MM-dd HH:mm" />
		    	<c:if test="${schedule.updateDate != null && schedule.updateDate.time != schedule.createDate.time}">
                	| 최종 수정: <fmt:formatDate value="${schedule.updateDate}" pattern="yyyy-MM-dd HH:mm" />
            	</c:if>)
		    	</span>
		    </div>
		</div>		        
    </div>
    
    
    <!-- 모달 하단 버튼 - 닫기 버튼 제거 -->
    <div class="schedule-modal-footer">
        <c:if test="${loginUser.empNo eq schedule.createEmpNo}">
            <button type="button" class="schedule-modal-btn schedule-modal-btn-edit" onclick="editSchedule(${schedule.scheduleNo})">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
                수정
            </button>
            <button type="button" class="schedule-modal-btn schedule-modal-btn-delete" onclick="deleteSchedule(${schedule.scheduleNo})">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    <line x1="10" y1="11" x2="10" y2="17"></line>
                    <line x1="14" y1="11" x2="14" y2="17"></line>
                </svg>
                삭제
            </button>
        </c:if>
    </div>
</div>

<script>
// 일정 수정 페이지로 이동
function editSchedule(scheduleNo) {
	console.log("수정 버튼 클릭: 일정 번호 " + scheduleNo);
	
	try {
        // 수정 폼 페이지로 이동 (scheduleNo를 파라미터로 전달)
        location.href = 'updateForm.sc?scheduleNo=' + scheduleNo;
    } catch (e) {
        console.error("수정 페이지 이동 중 오류 발생:", e);
        alert("수정 페이지로 이동 중 오류가 발생했습니다.");
    }
}

// 일정 삭제
function deleteSchedule(scheduleNo) {
    if (confirm('정말 이 일정을 삭제하시겠습니까?')) {
        location.href = 'delete.sc?scheduleNo=' + scheduleNo;
    }
}

// 페이지 로드 후 설명 영역 왼쪽 정렬 강제 적용
document.addEventListener('DOMContentLoaded', function() {
    // 설명 영역 찾기
    var descElements = document.querySelectorAll('.schedule-detail-description, .schedule-detail-empty');
    // 모든 설명 요소에 왼쪽 정렬 적용
    descElements.forEach(function(el) {
        el.style.textAlign = 'left';
        el.style.textAlignLast = 'left';
    });
});
</script>