<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- documentData.data에서 휴가신청서 정보 추출 -->
<c:set var="vacationData" value="${documentData.data}" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/vacationUpdateForm.css">


<div class="vacation-update-form">
    <div class="apupdate-section">
        <h3 class="apupdate-section-title">휴가 신청 정보</h3>
        
        <!-- 휴가 종류 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">휴가 종류</label>
            <div class="vac-checkbox-container">
                <div class="vac-checkbox-item">
                    <input type="radio" name="vacationType" value="연차" id="vacation-annual" 
                           class="vac-checkbox" ${vacationData.vacationType == '연차' ? 'checked' : ''}>
                    <label for="vacation-annual">연차</label>
                </div>
                <div class="vac-checkbox-item">
                    <input type="radio" name="vacationType" value="반차" id="vacation-half" 
                           class="vac-checkbox" ${vacationData.vacationType == '반차' ? 'checked' : ''}>
                    <label for="vacation-half">반차</label>
                </div>
                <div class="vac-checkbox-item">
                    <input type="radio" name="vacationType" value="병가" id="vacation-sick" 
                           class="vac-checkbox" ${vacationData.vacationType == '병가' ? 'checked' : ''}>
                    <label for="vacation-sick">병가</label>
                </div>
                <div class="vac-checkbox-item">
                    <input type="radio" name="vacationType" value="특별휴가" id="vacation-special" 
                           class="vac-checkbox" ${vacationData.vacationType == '특별휴가' ? 'checked' : ''}>
                    <label for="vacation-special">특별휴가</label>
                </div>
            </div>
        </div>
        
        <!-- 휴가 기간 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">휴가 기간</label>
            <div class="vac-date-group">
                <input type="date" name="startDate" id="vacation-start-date" 
                       value="${vacationData.startDate}" required onchange="calculateVacationDays()">
                ~
                <input type="date" name="endDate" id="vacation-end-date" 
                       value="${vacationData.endDate}" required onchange="calculateVacationDays()">
                <span class="day-count" id="vacation-days">(총 ${vacationData.days}일)</span>
            </div>
        </div>
        
        <!-- 연차 현황 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">연차 현황</label>
            <div class="vac-annual-info">
                총 <input type="text" id="total-days" value="${vacationData.totalDays}">일 중 
                사용 <input type="text" id="used-days" value="${vacationData.usedDays}">일,  
                잔여 <span id="remaining-days">${vacationData.remainingDays}</span>일
            </div>
        </div>
        
        <!-- 휴가 사유 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">휴가 사유</label>
            <textarea name="reason" id="vacation-reason" class="apupdate-input" rows="4" required>${vacationData.reason}</textarea>
        </div>
        
        <!-- 비상 연락처 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">비상 연락처</label>
            <input type="text" name="emergencyContact" id="emergency-contact" 
                   value="${vacationData.emergencyContact}" class="apupdate-input" required>
        </div>
    </div>
</div>

<script>
    // 휴가신청서 전용 JavaScript
    document.addEventListener('DOMContentLoaded', function() {
        // 초기 연차 현황 계산
        updateRemainingDays();
        
        // 페이지 로드 시 즉시 계산 실행 (이미 날짜가 있다면)
        calculateVacationDays();
        
        // 휴가 종류 선택 이벤트 연결
        document.querySelectorAll('input[name="vacationType"]').forEach(function(radio) {
            radio.addEventListener('change', handleVacationTypeChange);
        });
        
        // 연차 입력 필드 이벤트 연결
        document.getElementById('total-days').addEventListener('input', updateRemainingDays);
        document.getElementById('used-days').addEventListener('input', updateRemainingDays);
    });
    
    // 휴가 종류 변경 처리
    function handleVacationTypeChange() {
        // 선택된 휴가 종류 가져오기
        const vacationType = document.querySelector('input[name="vacationType"]:checked').value;
        
        // 반차인 경우 시작일과 종료일 동일하게 설정
        if (vacationType === '반차') {
            const startDate = document.getElementById('vacation-start-date').value;
            if (startDate) {
                document.getElementById('vacation-end-date').value = startDate;
            }
            calculateVacationDays();
        }
    }
    
    // 휴가일수 계산 함수
    function calculateVacationDays() {
        var startDate = document.getElementById('vacation-start-date').value;
        var endDate = document.getElementById('vacation-end-date').value;
        
        if (!startDate || !endDate) {
            document.getElementById('vacation-days').textContent = '(총 0일)';
            return;
        }
        
        var date1 = new Date(startDate);
        var date2 = new Date(endDate);
        
        if (isNaN(date1.getTime()) || isNaN(date2.getTime())) {
            document.getElementById('vacation-days').textContent = '(총 0일)';
            return;
        }
        
        // 반차 처리
        const vacationType = document.querySelector('input[name="vacationType"]:checked');
        if (vacationType && vacationType.value === '반차') {
            document.getElementById('vacation-days').textContent = '(총 0.5일)';
            return;
        }
        
        // 일반 휴가 처리
        var timeDiff = Math.abs(date2.getTime() - date1.getTime());
        var dayDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24)) + 1;
        
        document.getElementById('vacation-days').textContent = '(총 ' + dayDiff + '일)';
        
        console.log('휴가 일수 계산됨:', startDate, '~', endDate, '=', dayDiff, '일');
    }
    
    // 잔여일수 계산 함수
    function updateRemainingDays() {
        var totalDays = parseInt(document.getElementById('total-days').value) || 0;
        var usedDays = parseFloat(document.getElementById('used-days').value) || 0;
        var remainingDays = totalDays - usedDays;
        
        document.getElementById('remaining-days').textContent = remainingDays.toFixed(1);
    }
    
    // 휴가신청서 데이터 수집 함수
    function collectVacationData() {
        // 휴가 유형 가져오기
        let vacationType = '';
        document.querySelectorAll('input[name="vacationType"]').forEach(radio => {
            if (radio.checked) vacationType = radio.value;
        });
        
        // 문서 데이터 생성
        const content = {
            formType: "vacation",
            data: {
                // 휴가 정보
                vacationType: vacationType,
                startDate: document.getElementById('vacation-start-date').value,
                endDate: document.getElementById('vacation-end-date').value,
                days: document.getElementById('vacation-days').textContent.replace(/[()총일]/g, '').trim(),
                
                // 연차 정보
                totalDays: document.getElementById('total-days').value,
                usedDays: document.getElementById('used-days').value,
                remainingDays: document.getElementById('remaining-days').textContent,
                
                // 추가 정보
                reason: document.getElementById('vacation-reason').value,
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
    
    // 휴가신청서 유효성 검증 함수
    function validateVacationForm() {
        if (!document.querySelector('input[name="vacationType"]:checked')) {
            alert('휴가 종류를 선택해주세요.');
            return false;
        }
        
        if (!document.getElementById('vacation-start-date').value) {
            alert('휴가 시작일을 선택해주세요.');
            return false;
        }
        
        if (!document.getElementById('vacation-end-date').value) {
            alert('휴가 종료일을 선택해주세요.');
            return false;
        }
        
        if (!document.getElementById('vacation-reason').value.trim()) {
            alert('휴가 사유를 입력해주세요.');
            return false;
        }
        
        if (!document.getElementById('emergency-contact').value.trim()) {
            alert('비상 연락처를 입력해주세요.');
            return false;
        }
        
        return true;
    }
</script>