/**
 * 출장신청서 양식 처리 스크립트 - JSP에 없는 함수들만 포함
 */

// 문서 내용 수집 함수
function collectDocumentContent() {
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
        
        // DEFAULT 값 추가
        version: 1,
        requestedDate: new Date().toISOString(),
        status: "Y"
    };
    
    // JSON 문자열로 변환하여 hidden input에 저장
    document.getElementById('doc-content-data').value = JSON.stringify(content);
}

// 유효성 검증 함수 (최소)
function validateFormMinimum() {
    // 필수 항목만 검증 (임시저장용)
    if (!document.getElementById('trip-purpose').value.trim()) {
        alert('출장 목적을 입력해주세요.');
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
    
    return true;
}

// 유효성 검증 함수 (전체)
function validateForm() {
    // 기본 검증
    if (!validateFormMinimum()) {
        return false;
    }
    
    // 출장 유형 검증
    let tripTypeChecked = false;
    document.querySelectorAll('input[name="tripType"]').forEach(radio => {
        if (radio.checked) tripTypeChecked = true;
    });
    
    if (!tripTypeChecked) {
        alert('출장 구분을 선택해주세요.');
        return false;
    }
    
    // 결재선 검증
    if (approvalLines.length === 0) {
        alert('결재선을 지정해주세요.');
        return false;
    }
    
    // 출장자 검증
    const memberBadges = document.querySelectorAll('.trip-member-input');
    if (memberBadges.length === 0) {
        alert('최소 한 명 이상의 출장자가 필요합니다.');
        return false;
    }
    
    // 긴급 연락처 검증
    if (!document.getElementById('emergency-contact').value.trim()) {
        alert('긴급 연락처를 입력해주세요.');
        return false;
    }
    
    return true;
}

// 임시저장
function saveAsDraft() {
    // 최소한의 유효성 검증
    if (!validateFormMinimum()) {
        return;
    }
    
    // 문서 내용 수집
    collectDocumentContent();
    
    // 임시저장 액션 설정 및 제출
    document.getElementById('form-action').value = 'save';
    document.getElementById('businessTripForm').submit();
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
   document.getElementById('businessTripForm').submit();
}

// 이전 페이지로
function goBack() {
   if (confirm('작성 중인 내용이 저장되지 않을 수 있습니다. 정말 나가시겠습니까?')) {
       location.href = 'selectForm.ap';
   }
}

// 수정 화면용 함수들 추가
// 출장신청서 데이터 수집 함수 (수정 화면용)
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

// 출장신청서 유효성 검증 함수 (수정 화면용)
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

// 출장일수 계산 함수 - 수정 화면에서 사용
function calculateTripDays() {
   var startDate = document.getElementById('trip-start-date').value;
   var endDate = document.getElementById('trip-end-date').value;
   
   if (!startDate || !endDate) {
       document.getElementById('trip-days').textContent = '(총 0일)';
       return;
   }
   
   var date1 = new Date(startDate);
   var date2 = new Date(endDate);
   
   if (isNaN(date1.getTime()) || isNaN(date2.getTime())) {
       document.getElementById('trip-days').textContent = '(총 0일)';
       return;
   }
   
   var timeDiff = Math.abs(date2.getTime() - date1.getTime());
   var dayDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24)) + 1;
   
   document.getElementById('trip-days').textContent = '(총 ' + dayDiff + '일)';
   
   console.log('출장 일수 계산됨:', startDate, '~', endDate, '=', dayDiff, '일');
}

// 출장자 추가 함수 - 수정 화면에서 사용
function addTripMember() {
   var nameInput = document.getElementById('member-name');
   var deptInput = document.getElementById('member-dept');
   
   var name = nameInput.value.trim();
   var dept = deptInput.value.trim();
   
   if (!name || !dept) {
       alert('이름과 부서를 모두 입력해주세요.');
       return;
   }
   
   // HTML 방식으로 직접 추가 (전통적인 방식)
   var memberList = document.getElementById('trip-members');
   
   // div 요소 생성
   var memberBadge = document.createElement('div');
   memberBadge.className = 'btr-member-badge';
   
   // 텍스트 노드 추가
   var textNode = document.createTextNode(name + ' (' + dept + ')');
   memberBadge.appendChild(textNode);
   
   // hidden input 추가
   var hiddenInput = document.createElement('input');
   hiddenInput.type = 'hidden';
   hiddenInput.className = 'trip-member-input';
   hiddenInput.value = name + ',' + dept;
   memberBadge.appendChild(hiddenInput);
   
   // 삭제 버튼 추가
   var deleteBtn = document.createElement('button');
   deleteBtn.type = 'button';
   deleteBtn.onclick = function() { this.parentElement.remove(); };
   
   // 아이콘 추가
   var icon = document.createElement('i');
   icon.className = 'fas fa-times';
   deleteBtn.appendChild(icon);
   
   memberBadge.appendChild(deleteBtn);
   
   // 최종 추가
   memberList.appendChild(memberBadge);
   
   // 입력 필드 초기화
   nameInput.value = '';
   deptInput.value = '';
   
   console.log('출장자 추가됨: ' + name + ', ' + dept);
}