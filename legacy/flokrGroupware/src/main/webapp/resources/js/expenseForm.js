/**
 * 지출결의서 양식 처리 스크립트
 */

// 유효성 검증 함수
function validateForm() {
    // 지출 유형 검증
    var expenseTypeSelected = false;
    var radioButtons = document.getElementsByName('expenseType');
    for (var i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].checked) {
            expenseTypeSelected = true;
            break;
        }
    }
    
    if (!expenseTypeSelected) {
        alert('지출 유형을 선택해주세요.');
        return false;
    }
    
    // 계좌정보 검증 (개인비용 청구인 경우만)
    var expenseType = '';
    for (var i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].checked) {
            expenseType = radioButtons[i].value;
            break;
        }
    }
    
    if (expenseType === '개인비용 청구' && !document.getElementById('account-info').value.trim()) {
        alert('계좌정보를 입력해주세요.');
        document.getElementById('account-info').focus();
        return false;
    }
    
    // 지출 내역 검증
    var hasValidExpense = false;
    var rows = document.getElementById('expense-body').querySelectorAll('tr');
    
    for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var dateInput = row.querySelector('.expense-date');
        var detailInput = row.querySelector('.expense-detail');
        var amountInput = row.querySelector('.expense-amount');
        
        if (dateInput.value && detailInput.value.trim() && amountInput.value) {
            hasValidExpense = true;
            break;
        }
    }
    
    if (!hasValidExpense) {
        alert('최소 하나 이상의 유효한 지출 항목을 입력해주세요.');
        return false;
    }
    
    // 지출 내용 검증
    if (!document.getElementById('expense-description').value.trim()) {
        alert('지출 내용을 입력해주세요.');
        document.getElementById('expense-description').focus();
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
    // 지출 유형 가져오기
    var expenseType = '';
    var radioButtons = document.getElementsByName('expenseType');
    for (var i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].checked) {
            expenseType = radioButtons[i].value;
            break;
        }
    }
    
    // 지출 내역 수집
    var expenses = [];
    var rows = document.getElementById('expense-body').querySelectorAll('tr');
    
    for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var dateInput = row.querySelector('.expense-date');
        var detailInput = row.querySelector('.expense-detail');
        var amountInput = row.querySelector('.expense-amount');
        
        if (dateInput.value || detailInput.value || amountInput.value) {
            expenses.push({
                date: dateInput.value,
                detail: detailInput.value,
                amount: amountInput.value
            });
        }
    }
    
    // 문서 데이터 생성
    var content = {
        formType: "expense",
        data: {
            expenseType: expenseType,
            accountInfo: document.getElementById('account-info').value,
            expenses: expenses,
            total: document.getElementById('expense-total').textContent.replace(/,/g, ''),
            description: document.getElementById('expense-description').value,
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
    var hasAnyData = false;
    
    // 지출 내역 중 하나라도 값이 있는지 확인
    var rows = document.getElementById('expense-body').querySelectorAll('tr');
    
    for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var dateInput = row.querySelector('.expense-date');
        var detailInput = row.querySelector('.expense-detail');
        var amountInput = row.querySelector('.expense-amount');
        
        if (dateInput.value || detailInput.value.trim() || amountInput.value) {
            hasAnyData = true;
            break;
        }
    }
    
    if (!hasAnyData) {
        alert('최소 하나 이상의 지출 항목을 작성해주세요.');
        return;
    }
    
    // 문서 내용 수집
    collectDocumentContent();
    
    // 임시저장 액션 설정 및 제출
    document.getElementById('form-action').value = 'save';
    document.getElementById('expenseForm').submit();
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
    document.getElementById('expenseForm').submit();
}

// 이전 페이지로
function goBack() {
    if (confirm('작성 중인 내용이 저장되지 않을 수 있습니다. 정말 나가시겠습니까?')) {
        location.href = 'selectForm.ap';
    }
}

// 수정 화면용 함수들 추가
// 지출결의서 데이터 수집 함수 (수정 화면용)
function collectExpenseData() {
    // 지출 유형 가져오기
    let expenseType = '';
    document.querySelectorAll('input[name="expenseType"]').forEach(radio => {
        if (radio.checked) expenseType = radio.value;
    });
    
    // 지출 내역 수집
    const expenses = [];
    const rows = document.getElementById('expense-body').querySelectorAll('tr');
    
    for (let i = 0; i < rows.length; i++) {
        const row = rows[i];
        const dateInput = row.querySelector('.expense-date');
        const detailInput = row.querySelector('.expense-detail');
        const amountInput = row.querySelector('.expense-amount');
        
        if (dateInput.value || detailInput.value || amountInput.value) {
            expenses.push({
                date: dateInput.value,
                detail: detailInput.value,
                amount: amountInput.value
            });
        }
    }
    
    // 문서 데이터 생성
    const content = {
        formType: "expense",
        data: {
            expenseType: expenseType,
            accountInfo: document.getElementById('account-info').value,
            expenses: expenses,
            total: document.getElementById('expense-total').textContent.replace(/,/g, ''),
            description: document.getElementById('expense-description').value,
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

// 지출결의서 유효성 검증 함수 (수정 화면용)
function validateExpenseForm() {
    // 지출 유형 검증
    if (!document.querySelector('input[name="expenseType"]:checked')) {
        alert('지출 유형을 선택해주세요.');
        return false;
    }
    
    // 계좌정보 검증 (개인비용 청구인 경우만)
    const expenseType = document.querySelector('input[name="expenseType"]:checked').value;
    if (expenseType === '개인비용 청구' && !document.getElementById('account-info').value.trim()) {
        alert('계좌정보를 입력해주세요.');
        return false;
    }
    
    // 지출 내역 검증
    let hasValidExpense = false;
    const rows = document.getElementById('expense-body').querySelectorAll('tr');
    
    for (let i = 0; i < rows.length; i++) {
        const row = rows[i];
        const dateInput = row.querySelector('.expense-date');
        const detailInput = row.querySelector('.expense-detail');
        const amountInput = row.querySelector('.expense-amount');
        
        if (dateInput.value && detailInput.value.trim() && amountInput.value) {
            hasValidExpense = true;
            break;
        }
    }
    
    if (!hasValidExpense) {
        alert('최소 하나 이상의 유효한 지출 항목을 입력해주세요.');
        return false;
    }
    
    // 지출 내용 검증
    if (!document.getElementById('expense-description').value.trim()) {
        alert('지출 내용을 입력해주세요.');
        return false;
    }
    
    return true;
}