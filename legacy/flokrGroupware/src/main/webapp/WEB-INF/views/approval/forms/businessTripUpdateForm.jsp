<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- documentData.data에서 출장신청서 정보 추출 -->
<c:set var="businessTripData" value="${documentData.data}" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/businessTripUpdateForm.css">

<div class="businesstrip-update-form">
    <div class="apupdate-section">
        <h3 class="apupdate-section-title">출장 신청 정보</h3>
        
        <!-- 출장 목적 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">출장 목적</label>
            <input type="text" name="purpose" id="trip-purpose" class="apupdate-input" 
                   value="${businessTripData.purpose}" required>
        </div>
        
        <!-- 출장 구분 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">출장 구분</label>
            <div class="btr-checkbox-container">
                <div class="btr-checkbox-item">
                    <input type="radio" name="tripType" value="국내 출장(당일)" id="trip-type-1" 
                           class="btr-checkbox" ${businessTripData.tripType == '국내 출장(당일)' ? 'checked' : ''}>
                    <label for="trip-type-1">국내 출장(당일)</label>
                </div>
                <div class="btr-checkbox-item">
                    <input type="radio" name="tripType" value="국내 출장(숙박)" id="trip-type-2" 
                           class="btr-checkbox" ${businessTripData.tripType == '국내 출장(숙박)' ? 'checked' : ''}>
                    <label for="trip-type-2">국내 출장(숙박)</label>
                </div>
                <div class="btr-checkbox-item">
                    <input type="radio" name="tripType" value="해외 출장" id="trip-type-3" 
                           class="btr-checkbox" ${businessTripData.tripType == '해외 출장' ? 'checked' : ''}>
                    <label for="trip-type-3">해외 출장</label>
                </div>
            </div>
        </div>
        
        <!-- 출장지 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">출장지</label>
            <input type="text" name="location" id="trip-location" class="apupdate-input" 
                   value="${businessTripData.location}" required>
        </div>
        
        <!-- 출장 기간 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">출장 기간</label>
            <div class="btr-date-group">
                <input type="date" name="startDate" id="trip-start-date" 
                       value="${businessTripData.startDate}" onchange="calculateTripDays()">
                ~
                <input type="date" name="endDate" id="trip-end-date" 
                       value="${businessTripData.endDate}" onchange="calculateTripDays()">
                <span id="trip-days" style="margin-left:10px; color: #64748b;">(총 ${businessTripData.days}일)</span>
            </div>
        </div>
        
        <!-- 출장자 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">출장자</label>
            <div id="trip-members" class="btr-member-list">
                <c:choose>
                    <c:when test="${not empty businessTripData.members}">
                        <c:forEach var="member" items="${businessTripData.members}" varStatus="status">
                            <div class="btr-member-badge">
                                ${member.name} (${member.dept})
                                <input type="hidden" class="trip-member-input" value="${member.name},${member.dept}">
                                <c:if test="${status.index > 0}">
                                    <button type="button" onclick="this.parentElement.remove()">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </c:if>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <!-- 기본적으로 로그인한 사용자를 첫 번째 출장자로 추가 -->
                        <div class="btr-member-badge">
                            ${loginUser.empName} (${loginUser.deptName})
                            <input type="hidden" class="trip-member-input" value="${loginUser.empName},${loginUser.deptName}">
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="btr-member-form">
                <input type="text" id="member-name" placeholder="이름" class="apupdate-input" style="flex: 1;">
                <input type="text" id="member-dept" placeholder="부서" class="apupdate-input" style="flex: 1;">
                <button type="button" onclick="addTripMember()" class="apupdate-btn apupdate-btn-outline">추가</button>
            </div>
        </div>
        
        <!-- 긴급 연락처 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">긴급 연락처</label>
            <input type="text" name="emergencyContact" id="emergency-contact" class="apupdate-input" 
                   value="${businessTripData.emergencyContact}" required>
        </div>
    </div>
</div>

<script>
    // 출장신청서 전용 JavaScript
    document.addEventListener('DOMContentLoaded', function() {
        // 날짜 계산기 연결
        document.getElementById('trip-start-date').addEventListener('change', calculateTripDays);
        document.getElementById('trip-end-date').addEventListener('change', calculateTripDays);
        
        // 이미 날짜가 있다면 계산 실행
        if (document.getElementById('trip-start-date').value && document.getElementById('trip-end-date').value) {
            calculateTripDays();
        }
    });
    
    // 출장일수 계산 함수
    function calculateTripDays() {
        const startDate = document.getElementById('trip-start-date').value;
        const endDate = document.getElementById('trip-end-date').value;
        
        if (!startDate || !endDate) {
            document.getElementById('trip-days').textContent = '(총 0일)';
            return;
        }
        
        const date1 = new Date(startDate);
        const date2 = new Date(endDate);
        
        if (isNaN(date1.getTime()) || isNaN(date2.getTime())) {
            document.getElementById('trip-days').textContent = '(총 0일)';
            return;
        }
        
        const timeDiff = Math.abs(date2.getTime() - date1.getTime());
        const dayDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24)) + 1;
        
        document.getElementById('trip-days').textContent = '(총 ' + dayDiff + '일)';
        
        console.log('출장 일수 계산됨:', startDate, '~', endDate, '=', dayDiff, '일');
    }
    
    // 출장자 추가 함수
    function addTripMember() {
        const nameInput = document.getElementById('member-name');
        const deptInput = document.getElementById('member-dept');
        const name = nameInput.value.trim();
        const dept = deptInput.value.trim();
        
        if (!name || !dept) {
            alert('이름과 부서를 모두 입력해주세요.');
            return;
        }
        
        const membersList = document.getElementById('trip-members');
        const memberBadge = document.createElement('div');
        memberBadge.className = 'btr-member-badge';
        
        memberBadge.innerHTML = name + ' (' + dept + ')' +
            '<input type="hidden" class="trip-member-input" value="' + name + ',' + dept + '">' +
            '<button type="button" onclick="this.parentElement.remove()">' +
            '<i class="fas fa-times"></i>' +
            '</button>';
        
        membersList.appendChild(memberBadge);
        
        nameInput.value = '';
        deptInput.value = '';
        
        console.log('출장자 추가됨: ' + name + ', ' + dept);
    }
    
    // 출장신청서 데이터 수집 함수
    function collectBusinessTripData() {
        // 출장 유형 가져오기
        let tripType = '';
        document.querySelectorAll('input[name="tripType"]').forEach(radio => {
            if (radio.checked) tripType = radio.value;
        });
        
        // 출장자 정보 수집
        const tripMembers = [];
        document.querySelectorAll('.trip-member-input').forEach(input => {
            const [name, dept] = input.value.split(',');
            tripMembers.push({ name, dept });
        });
        
        // 문서 데이터 생성
        const content = {
            formType: "businessTrip",
            data: {
                // 기본 정보
                purpose: document.getElementById('trip-purpose').value,
                tripType: tripType,
                location: document.getElementById('trip-location').value,
                startDate: document.getElementById('trip-start-date').value,
                endDate: document.getElementById('trip-end-date').value,
                days: document.getElementById('trip-days').textContent.replace(/[()총일]/g, '').trim(),
                
                // 출장자 정보
                members: tripMembers,
                
                // 추가 정보
                emergencyContact: document.getElementById('emergency-contact').value,
                
                // 기안자 정보
                drafterInfo: {
                    empName: loginUser.empName,
                    deptName: loginUser.deptName,
                    positionName: loginUser.positionName
                }
            },
            version: 1,
            requestedDate: new Date().toISOString(),
            status: "Y"
        };
        
        return content;
    }
    
    // 출장신청서 유효성 검증 함수
    function validateBusinessTripForm() {
        if (!document.getElementById('trip-purpose').value.trim()) {
            alert('출장 목적을 입력해주세요.');
            return false;
        }
        
        if (!document.querySelector('input[name="tripType"]:checked')) {
            alert('출장 구분을 선택해주세요.');
            return false;
        }
        
        if (!document.getElementById('trip-location').value.trim()) {
            alert('출장지를 입력해주세요.');
            return false;
        }
        
        if (!document.getElementById('trip-start-date').value) {
            alert('출장 시작일을 입력해주세요.');
            return false;
        }
        
        if (!document.getElementById('trip-end-date').value) {
            alert('출장 종료일을 입력해주세요.');
            return false;
        }
        
        const tripMembers = document.querySelectorAll('.trip-member-input');
        if (tripMembers.length === 0) {
            alert('최소 한 명 이상의 출장자가 필요합니다.');
            return false;
        }
        
        if (!document.getElementById('emergency-contact').value.trim()) {
            alert('긴급 연락처를 입력해주세요.');
            return false;
        }
        
        return true;
    }
</script>