/**
 * 재택근무신청서 양식 처리 스크립트
 */

// 유효성 검증 함수
function validateForm() {
    // 재택근무 유형 검증
    var workTypeSelected = false;
    var radioButtons = document.getElementsByName('workType');
    for (var i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].checked) {
            workTypeSelected = true;
            break;
        }
    }
    
    if (!workTypeSelected) {
        alert('재택근무 유형을 선택해주세요.');
        return false;
    }
    
    // 재택근무 기간 검증
    if (!document.getElementById('startDate').value) {
        alert('시작일을 입력해주세요.');
        document.getElementById('startDate').focus();
        return false;
    }
    
    if (!document.getElementById('endDate').value) {
        alert('종료일을 입력해주세요.');
        document.getElementById('endDate').focus();
        return false;
    }
    
    // 재택근무 사유 검증
    if (!document.getElementById('reason').value.trim()) {
        alert('재택근무 사유를 입력해주세요.');
        document.getElementById('reason').focus();
        return false;
    }
    
    // 업무 계획 검증
    if (!document.getElementById('workPlan').value.trim()) {
        alert('업무 계획을 입력해주세요.');
        document.getElementById('workPlan').focus();
        return false;
    }
    
    // 비상 연락처 검증
    if (!document.getElementById('contact').value.trim()) {
        alert('비상 연락처를 입력해주세요.');
        document.getElementById('contact').focus();
        return false;
    }
    
    // 결재선 검증
    if (approvalLines.length === 0) {
        alert('결재선을 지정해주세요.');
        return false;
    }
    
    return true;
}

// 문서 내용 수집 함수
function collectDocumentContent() {
    // 재택근무 유형 가져오기
    var workType = '';
    var radioButtons = document.getElementsByName('workType');
    for (var i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].checked) {
            workType = radioButtons[i].value;
            break;
        }
    }
    
    // 근무 환경 가져오기
    var workEnv = [];
    var checkboxes = document.getElementsByName('workEnv[]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked) {
            workEnv.push(checkboxes[i].value);
        }
    }
    
    // 문서 데이터 생성
    var content = {
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
        
        // DEFAULT 값 추가
        version: 1,
        requestedDate: new Date().toISOString(),
        status: "Y"
    };
    
    // JSON 문자열로 변환하여 hidden input에 저장
    document.getElementById('doc-content-data').value = JSON.stringify(content);
}

// 임시저장
function saveAsDraft() {
    // 최소한의 유효성 검증
    if (!document.getElementById('startDate').value) {
        alert('시작일을 입력해주세요.');
        return;
    }
    
    // 문서 내용 수집
    collectDocumentContent();
    
    // 임시저장 액션 설정 및 제출
    document.getElementById('form-action').value = 'save';
    document.getElementById('remoteWorkForm').submit();
}

// 결재요청
function submitDocument() {
    // 전체 유효성 검증
    if (!validateForm()) {
        return;
    }
    
    // 문서 내용 수집
    collectDocumentContent();
    
    // 결재요청 액션 설정 및 제출
    document.getElementById('form-action').value = 'submit';
    document.getElementById('remoteWorkForm').submit();
}

// 이전 페이지로
function goBack() {
    if (confirm('작성 중인 내용이 저장되지 않을 수 있습니다. 정말 나가시겠습니까?')) {
        location.href = 'selectForm.ap';
    }
}

// 수정 화면용 함수들 추가
// 재택근무신청서 데이터 수집 함수 (수정 화면용)
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

// 재택근무신청서 유효성 검증 함수 (수정 화면용)
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