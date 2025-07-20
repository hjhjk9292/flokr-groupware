<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- documentData.data에서 지출결의서 정보 추출 -->
<c:set var="expenseData" value="${documentData.data}" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/expenseUpdateForm.css">

<div class="expense-update-form">
    <div class="apupdate-section">
        <h3 class="apupdate-section-title">지출 결의서 정보</h3>
        
        <!-- 지출 유형 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">지출 유형</label>
            <div class="exp-checkbox-container">
                <div class="exp-checkbox-item">
                    <input type="radio" name="expenseType" value="개인비용 청구" id="expense-type-1" 
                           class="exp-checkbox" ${expenseData.expenseType == '개인비용 청구' ? 'checked' : ''}>
                    <label for="expense-type-1">개인비용 청구</label>
                </div>
                <div class="exp-checkbox-item">
                    <input type="radio" name="expenseType" value="법인카드 사용" id="expense-type-2" 
                           class="exp-checkbox" ${expenseData.expenseType == '법인카드 사용' ? 'checked' : ''}>
                    <label for="expense-type-2">법인카드 사용</label>
                </div>
                <div class="exp-checkbox-item">
                    <input type="radio" name="expenseType" value="회의비" id="expense-type-3" 
                           class="exp-checkbox" ${expenseData.expenseType == '회의비' ? 'checked' : ''}>
                    <label for="expense-type-3">회의비</label>
                </div>
                <div class="exp-checkbox-item">
                    <input type="radio" name="expenseType" value="업무 출장비" id="expense-type-4" 
                           class="exp-checkbox" ${expenseData.expenseType == '업무 출장비' ? 'checked' : ''}>
                    <label for="expense-type-4">업무 출장비</label>
                </div>
                <div class="exp-checkbox-item">
                    <input type="radio" name="expenseType" value="기타" id="expense-type-5" 
                           class="exp-checkbox" ${expenseData.expenseType == '기타' ? 'checked' : ''}>
                    <label for="expense-type-5">기타</label>
                </div>
            </div>
        </div>
        
        <!-- 계좌정보 -->
        <div class="apupdate-form-group" id="account-container" 
             style="${expenseData.expenseType != '개인비용 청구' ? 'display:none;' : ''}">
            <label class="apupdate-label">계좌정보</label>
            <input type="text" name="accountInfo" id="account-info" 
                   value="${expenseData.accountInfo}" class="apupdate-input" 
                   placeholder="예: 하나은행 123-456789-01234 (예금주: 홍길동)">
        </div>
        
        <!-- 지출 내역 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">지출 내역</label>
            <table class="exp-table" id="expense-table">
                <thead>
                    <tr>
                        <th width="20%">지출일자</th>
                        <th width="40%">지출내역</th>
                        <th width="30%">금액(원)</th>
                        <th width="10%"></th>
                    </tr>
                </thead>
                <tbody id="expense-body">
                    <c:choose>
                        <c:when test="${not empty expenseData.expenses}">
                            <c:forEach var="expense" items="${expenseData.expenses}" varStatus="status">
                                <tr>
                                    <td>
                                        <input type="date" class="expense-date" value="${expense.date}">
                                    </td>
                                    <td>
                                        <input type="text" class="expense-detail" value="${expense.detail}" placeholder="지출내역">
                                    </td>
                                    <td>
                                        <input type="number" class="expense-amount" value="${expense.amount}" 
                                               placeholder="금액" min="0" onchange="calculateTotal()">
                                    </td>
                                    <td>
                                        <button type="button" class="exp-delete-btn" onclick="deleteExpenseRow(this)">
                                            <i class="fas fa-times"></i>
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>
                                    <input type="date" class="expense-date">
                                </td>
                                <td>
                                    <input type="text" class="expense-detail" placeholder="지출내역">
                                </td>
                                <td>
                                    <input type="number" class="expense-amount" placeholder="금액" min="0" onchange="calculateTotal()">
                                </td>
                                <td>
                                    <button type="button" class="exp-delete-btn" onclick="deleteExpenseRow(this)">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="2" style="text-align: right;">합계</td>
                        <td id="expense-total" class="exp-text-right">
                            <c:choose>
                                <c:when test="${not empty expenseData.total}">${expenseData.total}</c:when>
                                <c:otherwise>0</c:otherwise>
                            </c:choose>
                        </td>
                        <td></td>
                    </tr>
                </tfoot>
            </table>
            <button type="button" class="exp-add-btn" onclick="addExpenseRow()">
                <i class="fas fa-plus"></i> 항목 추가
            </button>
        </div>
        
        <!-- 지출 내용 -->
        <div class="apupdate-form-group">
            <label class="apupdate-label">지출 내용</label>
            <textarea name="description" id="expense-description" class="apupdate-input" rows="4" required>${expenseData.description}</textarea>
        </div>
    </div>
</div>

<script>
    // 지출결의서 전용 JavaScript
    document.addEventListener('DOMContentLoaded', function() {
        // 초기 합계 계산
        calculateTotal();
        
        // 지출 유형 변경 이벤트 연결
        document.querySelectorAll('input[name="expenseType"]').forEach(function(radio) {
            radio.addEventListener('change', handleExpenseTypeChange);
        });
    });
    
    // 지출 유형 변경 처리
    function handleExpenseTypeChange() {
        const selectedType = document.querySelector('input[name="expenseType"]:checked').value;
        const accountContainer = document.getElementById('account-container');
        
        if (selectedType === '개인비용 청구') {
            accountContainer.style.display = 'block';
        } else {
            accountContainer.style.display = 'none';
        }
    }
    
    // 지출 항목 추가 함수
    function addExpenseRow() {
        var tbody = document.getElementById('expense-body');
        var newRow = document.createElement('tr');
        
        newRow.innerHTML = `
            <td>
                <input type="date" class="expense-date">
            </td>
            <td>
                <input type="text" class="expense-detail" placeholder="지출내역">
            </td>
            <td>
                <input type="number" class="expense-amount" placeholder="금액" min="0" onchange="calculateTotal()">
            </td>
            <td>
                <button type="button" class="exp-delete-btn" onclick="deleteExpenseRow(this)">
                    <i class="fas fa-times"></i>
                </button>
            </td>
        `;
        
        tbody.appendChild(newRow);
    }
    
    // 지출 항목 삭제 함수
    function deleteExpenseRow(button) {
        var tbody = document.getElementById('expense-body');
        if (tbody.rows.length <= 1) {
            alert('최소 하나의 지출 항목은 필요합니다.');
            return;
        }
        
        var row = button.closest('tr');
        row.remove();
        calculateTotal();
    }
    
    // 총액 계산 함수
    function calculateTotal() {
        var amountInputs = document.querySelectorAll('.expense-amount');
        var total = 0;
        
        for (var i = 0; i < amountInputs.length; i++) {
            var value = parseInt(amountInputs[i].value) || 0;
            total += value;
        }
        
        document.getElementById('expense-total').textContent = total.toLocaleString('ko-KR');
    }
    
    // 지출결의서 데이터 수집 함수
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
   
   // 지출결의서 유효성 검증 함수
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
</script>