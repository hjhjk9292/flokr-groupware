<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- documentData.data에서 재택근무신청서 정보 추출 -->
<c:set var="remoteWorkData" value="${documentData.data}" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/remoteWorkUpdateForm.css">


<div class="remotework-update-form">
    <div class="apupdate-section">
        <h3 class="apupdate-section-title">재택근무 신청 정보</h3>
        
        <!-- 재택근무 유형 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">재택근무 유형</label>
            <div class="rwf-checkbox-container">
                <div class="rwf-checkbox-item">
                    <input type="radio" name="workType" value="정기 재택근무(주 2~3일)" id="workType1" 
                           class="rwf-checkbox" ${remoteWorkData.workType == '정기 재택근무(주 2~3일)' ? 'checked' : ''}>
                    <label for="workType1">정기 재택근무(주 2~3일)</label>
                </div>
                <div class="rwf-checkbox-item">
                    <input type="radio" name="workType" value="비정기 재택근무(특정일)" id="workType2" 
                           class="rwf-checkbox" ${remoteWorkData.workType == '비정기 재택근무(특정일)' ? 'checked' : ''}>
                    <label for="workType2">비정기 재택근무(특정일)</label>
                </div>
                <div class="rwf-checkbox-item">
                    <input type="radio" name="workType" value="전일 재택근무(특별사유)" id="workType3" 
                           class="rwf-checkbox" ${remoteWorkData.workType == '전일 재택근무(특별사유)' ? 'checked' : ''}>
                    <label for="workType3">전일 재택근무(특별사유)</label>
                </div>
            </div>
        </div>
        
        <!-- 재택근무 기간 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">재택근무 기간</label>
            <div class="rwf-date-group">
                <input type="date" name="startDate" id="startDate" 
                       value="${remoteWorkData.startDate}" onchange="calculateDays()">
                ~
                <input type="date" name="endDate" id="endDate" 
                       value="${remoteWorkData.endDate}" onchange="calculateDays()">
                <span id="dayCount" style="margin-left:10px; color: #64748b;">(총 ${remoteWorkData.days}일)</span>
            </div>
        </div>
        
        <!-- 재택근무 사유 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">재택근무 사유</label>
            <textarea name="reason" id="reason" class="apupdate-input" rows="4" required>${remoteWorkData.reason}</textarea>
        </div>
        
        <!-- 업무 계획 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">업무 계획</label>
            <textarea name="workPlan" id="workPlan" class="apupdate-input" rows="4" required>${remoteWorkData.workPlan}</textarea>
        </div>
        
        <!-- 근무 환경 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">근무 환경</label>
            <div class="rwf-checkbox-container">
                <c:set var="workEnvList" value="${fn:split(remoteWorkData.workEnvironments, ', ')}" />
                <div class="rwf-checkbox-item">
                    <input type="checkbox" name="workEnv[]" value="업무용 노트북 보유" id="env1" class="rwf-checkbox"
                           <c:forEach var="env" items="${workEnvList}">
                               <c:if test="${env == '업무용 노트북 보유'}">checked</c:if>
                           </c:forEach>>
                    <label for="env1">업무용 노트북 보유</label>
                </div>
                <div class="rwf-checkbox-item">
                    <input type="checkbox" name="workEnv[]" value="안정적인 인터넷 환경" id="env2" class="rwf-checkbox"
                           <c:forEach var="env" items="${workEnvList}">
                               <c:if test="${env == '안정적인 인터넷 환경'}">checked</c:if>
                           </c:forEach>>
                    <label for="env2">안정적인 인터넷 환경</label>
                </div>
                <div class="rwf-checkbox-item">
                    <input type="checkbox" name="workEnv[]" value="화상회의 장비" id="env3" class="rwf-checkbox"
                           <c:forEach var="env" items="${workEnvList}">
                               <c:if test="${env == '화상회의 장비'}">checked</c:if>
                           </c:forEach>>
                    <label for="env3">화상회의 장비</label>
                </div>
                <div class="rwf-checkbox-item">
                    <input type="checkbox" name="workEnv[]" value="VPN 접속 가능" id="env4" class="rwf-checkbox"
                           <c:forEach var="env" items="${workEnvList}">
                               <c:if test="${env == 'VPN 접속 가능'}">checked</c:if>
                           </c:forEach>>
                    <label for="env4">VPN 접속 가능</label>
                </div>
            </div>
        </div>
        
        <!-- 비상 연락처 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">비상 연락처</label>
            <input type="text" name="contact" id="contact" class="apupdate-input" 
                   value="${remoteWorkData.contact}" required>
        </div>
    </div>
</div>

<script>
    // 재택근무신청서 전용 JavaScript
    document.addEventListener('DOMContentLoaded', function() {
        // 날짜 필드에 이벤트 핸들러 직접 설정
        document.getElementById('startDate').addEventListener('change', calculateDays);
        document.getElementById('endDate').addEventListener('change', calculateDays);
        
        // 이미 날짜가 있다면 계산 실행
        if (document.getElementById('startDate').value && document.getElementById('endDate').value) {
            calculateDays();
        }
    });
    
    // 근무일수 계산 함수
    function calculateDays() {
        var startDate = document.getElementById('startDate').value;
        var endDate = document.getElementById('endDate').value;
        
        if (!startDate || !endDate) {
            document.getElementById('dayCount').textContent = '(총 0일)';
            return;
        }
        
        var date1 = new Date(startDate);
        var date2 = new Date(endDate);
        
        if (isNaN(date1.getTime()) || isNaN(date2.getTime())) {
            document.getElementById('dayCount').textContent = '(총 0일)';
            return;
        }
        
        var timeDiff = Math.abs(date2.getTime() - date1.getTime());
        var dayDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24)) + 1;
        
        document.getElementById('dayCount').textContent = '(총 ' + dayDiff + '일)';
        
        console.log('재택근무 일수 계산됨:', startDate, '~', endDate, '=', dayDiff, '일');
    }
    
    // 재택근무신청서 데이터 수집 함수
    function collectRemoteWorkData() {
        // 재택근무 유형 가져오기
        let workType = '';
        document.querySelectorAll('input[name="workType"]').forEach(radio => {
            if (radio.checked) workType = radio.value;
        });
        
        // 근무 환경 가져오기
        const workEnv = [];
        document.querySelectorAll('input[name="workEnv[]"]:checked').forEach(checkbox => {
            workEnv.push(checkbox.value);
        });
        
        // 문서 데이터 생성
        const content = {
            formType: "remoteWork",
            data: {
                workType: workType,
                startDate: document.getElementById('startDate').value,
                endDate: document.getElementById('endDate').value,
                days: document.getElementById('dayCount').textContent.replace(/[()총일]/g, '').trim(),
                reason: document.getElementById('reason').value,
                workPlan: document.getElementById('workPlan').value,
                workEnvironments: workEnv.join(', '),
                contact: document.getElementById('contact').value,
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
    
    // 재택근무신청서 유효성 검증 함수
    function validateRemoteWorkForm() {
        // 재택근무 유형 검증
        if (!document.querySelector('input[name="workType"]:checked')) {
            alert('재택근무 유형을 선택해주세요.');
            return false;
        }
        
        // 재택근무 기간 검증
        if (!document.getElementById('startDate').value) {
            alert('시작일을 입력해주세요.');
            return false;
        }
        
        if (!document.getElementById('endDate').value) {
            alert('종료일을 입력해주세요.');
            return false;
        }
        
        // 재택근무 사유 검증
        if (!document.getElementById('reason').value.trim()) {
            alert('재택근무 사유를 입력해주세요.');
            return false;
        }
        
        // 업무 계획 검증
        if (!document.getElementById('workPlan').value.trim()) {
            alert('업무 계획을 입력해주세요.');
            return false;
        }
        
        // 비상 연락처 검증
        if (!document.getElementById('contact').value.trim()) {
            alert('비상 연락처를 입력해주세요.');
            return false;
        }
        
        return true;
    }
</script>