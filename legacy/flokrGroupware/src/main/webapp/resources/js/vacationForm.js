/**
 * 휴가신청서 양식 처리 스크립트
 */

// 유효성 검증 함수
function validateForm() {
    // 휴가 종류 검증
    var vacationTypeSelected = false;
    var radioButtons = document.getElementsByName('vacationType');
    for (var i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].checked) {
            vacationTypeSelected = true;
            break;
        }
    }
    
    if (!vacationTypeSelected) {
        alert('휴가 종류를 선택해주세요.');
        return false;
    }
    
    // 휴가 기간 검증
   if (!document.getElementById('vacation-start-date').value) {
       alert('휴가 시작일을 선택해주세요.');
       document.getElementById('vacation-start-date').focus();
       return false;
   }
   
   if (!document.getElementById('vacation-end-date').value) {
       alert('휴가 종료일을 선택해주세요.');
       document.getElementById('vacation-end-date').focus();
       return false;
   }
   
   // 휴가 사유 검증
   if (!document.getElementById('vacation-reason').value.trim()) {
       alert('휴가 사유를 입력해주세요.');
       document.getElementById('vacation-reason').focus();
       return false;
   }
   
   // 비상 연락처 검증
   if (!document.getElementById('emergency-contact').value.trim()) {
       alert('비상 연락처를 입력해주세요.');
       document.getElementById('emergency-contact').focus();
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
   // 휴가 유형 가져오기
   var vacationType = '';
   var radioButtons = document.getElementsByName('vacationType');
   for (var i = 0; i < radioButtons.length; i++) {
       if (radioButtons[i].checked) {
           vacationType = radioButtons[i].value;
           break;
       }
   }
   
   // 문서 데이터 생성
   var content = {
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
   if (!document.getElementById('vacation-start-date').value) {
       alert('휴가 시작일을 선택해주세요.');
       return;
   }
   
   // 문서 내용 수집
   collectDocumentContent();
   
   // 임시저장 액션 설정 및 제출
   document.getElementById('form-action').value = 'save';
   document.getElementById('vacationForm').submit();
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
   document.getElementById('vacationForm').submit();
}

// 이전 페이지로
function goBack() {
   if (confirm('작성 중인 내용이 저장되지 않을 수 있습니다. 정말 나가시겠습니까?')) {
       location.href = 'selectForm.ap';
   }
}

// 수정 화면용 함수들 추가
// 휴가신청서 데이터 수집 함수 (수정 화면용)
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

// 휴가신청서 유효성 검증 함수 (수정 화면용)
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