<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>지출결의서 | Flokr</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/expenseForm.css">
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />
    
    <div class="ex-container">
        <!-- 로고 -->
        <div class="ex-logo">
            <img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="Flokr" 
                 width="80" height="50"
                 onerror="this.src='https://via.placeholder.com/120x30?text=FLOKR'">
        </div>
        
        <!-- 문서 헤더 -->
        <div class="ex-header">
            <h1 class="ex-title">지출 결의서</h1>
        </div>
        
        <form id="expenseForm" method="post" action="insertDocument.ap" enctype="multipart/form-data">
            <input type="hidden" name="formNo" value="${form.formNo}">
            <input type="hidden" name="title" value="지출결의서">
            <input type="hidden" name="lines" id="approval-lines-data">
            <input type="hidden" name="action" id="form-action">
            <input type="hidden" name="docContent" id="doc-content-data">
            
            <!-- 기본 정보 -->
            <div class="ex-group">
                <div class="ex-info-row">
                    <div><strong>기안자:</strong> ${loginUser.empName}</div>
                    <div><strong>부서:</strong> ${loginUser.deptName}</div>
                    <div><strong>직위:</strong> ${loginUser.positionName}</div>
                    <div><strong>기안일:</strong> ${today}</div>
                </div>
            </div>
            
            <!-- 지출 유형 -->
            <div class="ex-group">
                <label class="ex-label">지출 유형</label>
                <div class="ex-check-group">
                    <div class="ex-check-item">
                        <input type="radio" name="expenseType" value="개인비용 청구" id="expense-type-1" class="ex-check">
                        <label for="expense-type-1">개인비용 청구</label>
                    </div>
                    <div class="ex-check-item">
                        <input type="radio" name="expenseType" value="법인카드 사용" id="expense-type-2" class="ex-check">
                        <label for="expense-type-2">법인카드 사용</label>
                    </div>
                    <div class="ex-check-item">
                        <input type="radio" name="expenseType" value="회의비" id="expense-type-3" class="ex-check">
                        <label for="expense-type-3">회의비</label>
                    </div>
                    <div class="ex-check-item">
                        <input type="radio" name="expenseType" value="업무 출장비" id="expense-type-4" class="ex-check">
                        <label for="expense-type-4">업무 출장비</label>
                    </div>
                    <div class="ex-check-item">
                        <input type="radio" name="expenseType" value="기타" id="expense-type-5" class="ex-check">
                        <label for="expense-type-5">기타</label>
                    </div>
                </div>
            </div>
            
            <!-- 계좌정보 -->
            <div class="ex-group">
                <label class="ex-label">계좌정보</label>
                <input type="text" id="account-info" class="ex-control" placeholder="예: 하나은행 123-456789-01234 (예금주: 홍길동)">
            </div>
            
            <!-- 결재선 -->
            <div class="ex-approval-section">
                <label class="ex-label">결재선</label>
                <div id="approval-line-container" class="ex-approval-line">
                    <div class="ex-empty-approval">결재자를 추가해주세요</div>
                </div>
                <button type="button" class="ex-btn ex-btn-outline" onclick="openApprovalLineModal()">
                    <i class="fas fa-plus"></i> 결재자 추가
                </button>
            </div>
            
            <!-- 지출 내역 -->
            <div class="ex-group">
                <label class="ex-label">지출 내역</label>
                <table class="ex-table" id="expense-table">
                    <thead>
                        <tr>
                            <th width="20%">지출일자</th>
                            <th width="40%">지출내역</th>
                            <th width="30%">금액(원)</th>
                            <th width="10%"></th>
                        </tr>
                    </thead>
                    <tbody id="expense-body">
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
                                <button type="button" class="ex-delete-btn" onclick="deleteExpenseRow(this)">
                                    <i class="fas fa-times"></i>
                                </button>
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="2" style="text-align: right;">합계</td>
                            <td id="expense-total" class="ex-text-right">0</td>
                            <td></td>
                        </tr>
                    </tfoot>
                </table>
                <button type="button" class="ex-add-btn" onclick="addExpenseRow()">
                    <i class="fas fa-plus"></i> 항목 추가
                </button>
            </div>
            
            <!-- 지출 내용 -->
            <div class="ex-group">
                <label class="ex-label">지출 내용</label>
                <textarea id="expense-description" class="ex-control ex-textarea" placeholder="지출 내용에 대해 상세히 작성해주세요."></textarea>
            </div>
            
            <div class="ex-divider"></div>
            
            <!-- 첨부파일 -->
            <div class="ex-attach-section">
                <h3 class="ex-attach-title">첨부파일</h3>
                <input type="file" name="attachFile" id="attachFile" class="ex-control" style="padding: 10px;">
                <p style="font-size: 12px; color: #64748b; margin-top: 5px;">
                    * 영수증, 지출증빙 자료 등을 첨부해주세요.
                </p>
            </div>
            
            <!-- 액션 버튼 -->
            <div class="ex-actions">
                <button type="button" class="ex-btn ex-btn-outline" onclick="goBack()">취소</button>
                <button type="button" class="ex-btn ex-btn-secondary" onclick="saveAsDraft()">임시저장</button>
                <button type="button" class="ex-btn ex-btn-primary" onclick="submitDocument()">결재요청</button>
            </div>
        </form>
    </div>
    
    <script>
        // JSP EL 태그를 사용하는 초기화 코드 (JSP에 유지)
        const loginUser = {
            empName: '${loginUser.empName}',
            deptName: '${loginUser.deptName}',
            positionName: '${loginUser.positionName}'
        };
        
        // 결재선 관련 변수
        let approvalLines = [];
        
        // 첨부파일 처리 - 페이지 초기화 관련
        document.getElementById('attachFile').addEventListener('change', function(e) {
            const fileInput = e.target;
            if (fileInput.files.length > 0) {
                // 파일 선택됨
            }
        });
        
        // DOM 로드 완료 시 초기화 - 페이지 특정 설정
        document.addEventListener('DOMContentLoaded', function() {
            // 초기 합계 계산
            calculateTotal();
        });
        
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
                    <button type="button" class="ex-delete-btn" onclick="deleteExpenseRow(this)">
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
        
        // 결재선 모달 열기
        function openApprovalLineModal() {
            window.open('approvalLineModal.ap', 'approvalLineModal', 'width=800,height=600,scrollbars=yes');
        }
        
        // 결재선 추가 함수
        window.addApprovalLine = function(emp) {
            // 중복 체크
            if (approvalLines.find(line => line.empNo === emp.empNo)) {
                alert('이미 결재선에 포함된 사원입니다.');
                return;
            }
            
            // 결재선에 추가
            approvalLines.push({
                empNo: emp.empNo,
                empName: emp.empName,
                deptName: emp.deptName || '부서 없음',
                positionName: emp.positionName || ''
            });
            
            // 결재선 표시 업데이트
            updateApprovalLineDisplay();
            
            // hidden input에 값 설정
            document.getElementById('approval-lines-data').value = JSON.stringify(approvalLines);
        };
        
        // 결재선 표시 업데이트
        function updateApprovalLineDisplay() {
            const container = document.getElementById('approval-line-container');
            
            if (approvalLines.length === 0) {
                container.innerHTML = '<div class="ex-empty-approval">결재자를 추가해주세요</div>';
            } else {
                let html = '';
                approvalLines.forEach(function(line, index) {
                    html += '<div class="ex-approval-item">';
                    html += '<div class="ex-approval-number">' + (index + 1) + '</div>';
                    html += '<span class="ex-approval-name">' + (line.empName || '') + ' ' + (line.positionName || '') + '</span>';
                    html += '<span class="ex-approval-dept">' + (line.deptName || '') + '</span>';
                    html += '<button type="button" class="ex-approval-btn" onclick="removeApprover(' + index + ')">';
                    html += '<i class="fas fa-times"></i>';
                    html += '</button>';
                    html += '</div>';
                });
                container.innerHTML = html;
            }
            
            document.getElementById('approval-lines-data').value = JSON.stringify(approvalLines);
        }
        
        // 결재자 제거
        function removeApprover(index) {
            approvalLines.splice(index, 1);
            updateApprovalLineDisplay();
            document.getElementById('approval-lines-data').value = JSON.stringify(approvalLines);
        }
    </script>
    
    <!-- 외부 JS 파일 로드 -->
    <script src="${pageContext.request.contextPath}/resources/js/expenseForm.js"></script>
</body>
</html>