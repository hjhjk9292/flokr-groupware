<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>문서 수정 | Flokr</title>    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalDocumentUpdate.css">
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />

    <div class="apupdate-container">
        <form id="apupdate-document-form" method="post" action="updateDocument.ap" enctype="multipart/form-data">
            <input type="hidden" name="docNo" value="${document.docNo}">
            <input type="hidden" name="formNo" value="${document.formNo}">
            <input type="hidden" name="lines" id="apupdate-lines-data">
            <input type="hidden" name="action" id="apupdate-action">
            <input type="hidden" name="deleteFileNo" id="apupdate-delete-file-no">
            <input type="hidden" name="docContent" id="docContent-hidden">
            
            <div class="apupdate-form">
                <div class="apupdate-header">
                    <h1 class="apupdate-title">문서 수정</h1>
                    <div class="apupdate-doc-info">
                        <span>문서번호: ${document.docNo}</span>
                        <span>|</span>
                        <span class="apupdate-status-badge 
                            <c:if test="${document.docStatus == 'REJECTED'}">apupdate-status-rejected</c:if>">
                            <c:choose>
                                <c:when test="${document.docStatus == 'DRAFT'}">임시저장</c:when>
                                <c:when test="${document.docStatus == 'REJECTED'}">반려</c:when>
                            </c:choose>
                        </span>
                        <span>|</span>
                        <span>최종 수정: <fmt:formatDate value="${document.updateDate}" pattern="yyyy-MM-dd"/></span>
                    </div>
                </div>
                
                <!-- 기본 정보 섹션 -->
                <div class="apupdate-section">
                    <h3 class="apupdate-section-title">
                        <i class="fas fa-info-circle"></i>
                        기본 정보
                    </h3>
                    <div class="apupdate-form-group">
                        <label class="apupdate-label">문서 제목</label>
                        <input type="text" name="title" class="apupdate-input" value="${document.title}" required>
                    </div>
                </div>
                
                <c:if test="${document.docStatus == 'DRAFT' || document.docStatus == 'REJECTED'}">
                    <!-- 결재선 섹션 (임시저장 또는 반려 상태일 때만 수정 가능) -->
                    <div class="apupdate-section">
                        <h3 class="apupdate-section-title">
                            <i class="fas fa-route"></i>
                            결재선 설정
                        </h3>
                        <div class="apupdate-line-section">
                            <div class="apupdate-line-list" id="apupdate-approval-lines">
                                <c:if test="${empty lines}">
                                    <div class="apupdate-empty-line">
                                        <i class="fas fa-plus"></i>
                                        결재자를 추가해주세요
                                    </div>
                                </c:if>
                                <c:forEach var="line" items="${lines}" varStatus="status">
                                    <div class="apupdate-line-item">
                                        <span class="apupdate-line-order">${status.count}</span>
                                        <div class="apupdate-line-info">
                                            <span class="apupdate-line-name">${line.approverName} ${line.positionName}</span>
                                            <span class="apupdate-line-dept">${line.deptName}</span>
                                        </div>
                                        <button type="button" class="apupdate-remove-btn" onclick="removeApprovalLine(${status.index})">
                                            <i class="fas fa-times"></i>
                                        </button>
                                    </div>
                                </c:forEach>
                            </div>
                            <button type="button" class="apupdate-add-line-btn" onclick="openApprovalLineModal()">
                                <i class="fas fa-plus"></i> 결재자 추가
                            </button>
                        </div>
                    </div>
                </c:if>
                
                <!-- 문서 내용 섹션 -->
                <div class="apupdate-section">
                    <h3 class="apupdate-section-title">
                        <i class="fas fa-edit"></i>
                        문서 내용
                    </h3>
                    
                    <!-- 양식별 동적 include -->
                    <div class="apupdate-content">
                        <c:choose>
                            <c:when test="${document.formNo == 1}">
                                <jsp:include page="forms/vacationUpdateForm.jsp" />
                            </c:when>
                            <c:when test="${document.formNo == 2}">
                                <jsp:include page="forms/expenseUpdateForm.jsp" />
                            </c:when>
                            <c:when test="${document.formNo == 3}">
                                <jsp:include page="forms/remoteWorkUpdateForm.jsp" />
                            </c:when>
                            <c:when test="${document.formNo == 4}">
                                <jsp:include page="forms/businessTripUpdateForm.jsp" />
                            </c:when>
                            <c:otherwise>
                                <!-- 기본 문서 편집기 -->
                                <div class="apupdate-form-group">
                                    <div id="doc-content-editor" contenteditable="true" class="apupdate-input doc-content-field">
                                        <c:choose>
                                            <c:when test="${not empty document.docContent}">
                                                ${document.docContent}
                                            </c:when>
                                            <c:otherwise>
                                                <span class="placeholder">문서 내용을 입력하세요</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                
                <!-- 첨부파일 섹션 (단일 파일) -->
                <div class="apupdate-section">
                    <h3 class="apupdate-section-title">
                        <i class="fas fa-paperclip"></i>
                        첨부파일
                    </h3>
                    <div class="apupdate-attachment">
                        <c:if test="${attachment != null}">
                            <div class="apupdate-current-file">
                                <div class="apupdate-file-item">
                                    <i class="fas fa-file"></i>
                                    <span>${attachment.originalFilename}</span>
                                    <span style="color: #666; font-size: 12px;">(File)</span>
                                    <a href="downloadAttachment.ap?attachmentNo=${attachment.attachmentNo}" 
                                       style="color: #003561; margin-left: auto;">
                                        <i class="fas fa-download"></i>
                                   </a>
                                   <button type="button" class="apupdate-remove-btn" onclick="removeCurrentFile(${attachment.attachmentNo})">
                                       <i class="fas fa-times"></i>
                                   </button>
                               </div>
                           </div>
                       </c:if>
                       
                       <input type="file" id="apupdate-file-input" name="attachFile" class="apupdate-file-input">
                       <div class="apupdate-file-upload" onclick="document.getElementById('apupdate-file-input').click()">
                           <i class="fas fa-cloud-upload-alt" style="font-size: 24px; color: #999; margin-bottom: 10px;"></i>
                           <p style="color: #666;">
                               <c:if test="${attachment != null}">새 파일 업로드 (기존 파일 교체)</c:if>
                               <c:if test="${attachment == null}">파일을 선택하거나 드래그하여 업로드하세요</c:if>
                           </p>
                           <p style="color: #999; font-size: 12px;">최대 10MB</p>
                       </div>
                   </div>
               </div>
           </div>
       </form>
   </div>
   
   <!-- 액션 바 -->
   <div class="apupdate-action-bar">
       <div class="apupdate-action-container">
           <div class="apupdate-info">
               <c:if test="${document.docStatus == 'REJECTED'}">
                   <span style="color: #f44336;">반려 사유: ${rejectedComment}</span>
               </c:if>
           </div>
           <div class="apupdate-btn-group">
               <button class="apupdate-btn apupdate-btn-outline" onclick="goBack()">취소</button>
               <button class="apupdate-btn apupdate-btn-primary" onclick="updateDraft()">
                   <i class="fas fa-save"></i> 임시저장
               </button>
               <c:if test="${document.docStatus == 'DRAFT' || document.docStatus == 'REJECTED'}">
                   <button class="apupdate-btn apupdate-btn-secondary" onclick="submitDocumentDirect()">
					    <i class="fas fa-paper-plane"></i> 결재요청
					</button>
               </c:if>
           </div>
       </div>
   </div>
   
   <script>
    // JSP EL 태그를 사용하는 초기화 코드 (JSP에 유지)
    const loginUser = {
	    empName: '<c:out value="${loginUser.empName}" default=""/>',
	    deptName: '<c:out value="${loginUser.deptName}" default=""/>',
	    positionName: '<c:out value="${loginUser.positionName}" default=""/>'
	};

    // 결재선 데이터 초기화
    let approvalLines = [
    	<c:if test="${not empty lines}">
	        <c:forEach var="line" items="${lines}" varStatus="status">
	            {
	                empNo: <c:out value="${line.approverEmpNo}" default="0"/>,
	                empName: '<c:out value="${line.approverName}" default=""/>',
	                deptName: '<c:out value="${line.deptName}" default=""/>',
	                positionName: '<c:out value="${line.positionName}" default=""/>'
	            }${!status.last ? ',' : ''}
	        </c:forEach>
	    </c:if>
    ];
    
    // 첨부파일 처리
    let selectedFile = null;
    let deleteFileNo = null;
    
    document.getElementById('apupdate-file-input').addEventListener('change', function(e) {
        handleFileSelect(e.target.files[0]);
    });
    
    // 드래그 앤 드롭
    const fileUpload = document.querySelector('.apupdate-file-upload');
    
    fileUpload.addEventListener('dragover', function(e) {
        e.preventDefault();
        this.classList.add('dragover');
    });
    
    fileUpload.addEventListener('dragleave', function(e) {
        e.preventDefault();
        this.classList.remove('dragover');
    });
    
    fileUpload.addEventListener('drop', function(e) {
        e.preventDefault();
        this.classList.remove('dragover');
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect(files[0]);
        }
    });
    
    function handleFileSelect(file) {
        if (file.size > 10 * 1024 * 1024) {
            alert('파일 크기는 10MB를 초과할 수 없습니다.');
            return;
        }
        
        selectedFile = file;
        
        // 기존 파일이 있으면 삭제 표시
        if (document.querySelector('.apupdate-current-file')) {
            removeCurrentFile();
        }
        
        // 새 파일 표시
        const currentFileSection = document.querySelector('.apupdate-attachment');
        const newFileItem = document.createElement('div');
        newFileItem.className = 'apupdate-current-file';
        
        var fileItemHtml = '<div class="apupdate-file-item">';
        fileItemHtml += '<i class="fas fa-file"></i>';
        fileItemHtml += '<span>' + file.name + '</span>';
        fileItemHtml += '<span style="color: #666; font-size: 12px;">(File)</span>';
        fileItemHtml += '<span style="color: #28a745; margin-left: auto; font-size: 12px;">새 파일</span>';
        fileItemHtml += '<button type="button" class="apupdate-remove-btn" onclick="removeNewFile()">';
        fileItemHtml += '<i class="fas fa-times"></i>';
        fileItemHtml += '</button>';
        fileItemHtml += '</div>';
        
        newFileItem.innerHTML = fileItemHtml;
        currentFileSection.insertBefore(newFileItem, currentFileSection.firstChild);
    }
    
    function removeCurrentFile(attachmentNo) {
        if (attachmentNo) {
            deleteFileNo = attachmentNo;
            document.getElementById('apupdate-delete-file-no').value = attachmentNo;
        }
        
        const currentFile = document.querySelector('.apupdate-current-file');
        if (currentFile) {
            currentFile.remove();
        }
    }
    
    function removeNewFile() {
        selectedFile = null;
        document.getElementById('apupdate-file-input').value = '';
        const currentFile = document.querySelector('.apupdate-current-file');
        if (currentFile) {
            currentFile.remove();
        }
    }
    
    // 결재선 모달 열기
    function openApprovalLineModal() {
        window.open('approvalLineModal.ap', 'approvalLineModal', 'width=800,height=600,scrollbars=yes');
    }
    
    // 결재선 추가 함수 - 전통적인 방식으로 수정
    window.addApprovalLine = function(emp) {
        // 중복 확인
        var isDuplicate = false;
        for (var i = 0; i < approvalLines.length; i++) {
            if (approvalLines[i].empNo === emp.empNo) {
                isDuplicate = true;
                break;
            }
        }
        
        if (isDuplicate) {
            alert('이미 결재선에 포함된 사원입니다.');
            return;
        }
        
        // 결재선에 추가
        approvalLines.push({
            empNo: emp.empNo,
            empName: emp.empName,
            deptName: emp.deptName || '',
            positionName: emp.positionName || ''
        });
        
        // 화면 업데이트
        updateApprovalLineDisplay();
    };
    
    // 결재선 화면 업데이트 - 전통적인 방식으로 수정
    function updateApprovalLineDisplay() {
        var container = document.getElementById('apupdate-approval-lines');
        
        // 컨테이너 초기화
        container.innerHTML = '';
        
        if (approvalLines.length === 0) {
            // 결재선이 비어있을 때
            var emptyDiv = document.createElement('div');
            emptyDiv.className = 'apupdate-empty-line';
            
            var icon = document.createElement('i');
            icon.className = 'fas fa-plus';
            emptyDiv.appendChild(icon);
            
            var textNode = document.createTextNode(' 결재자를 추가해주세요');
            emptyDiv.appendChild(textNode);
            
            container.appendChild(emptyDiv);
        } else {
            // 결재선에 결재자가 있을 때
            for (var i = 0; i < approvalLines.length; i++) {
                var line = approvalLines[i];
                
                // 결재선 항목 요소 생성
                var lineItem = document.createElement('div');
                lineItem.className = 'apupdate-line-item';
                
                // 순서 표시
                var orderSpan = document.createElement('span');
                orderSpan.className = 'apupdate-line-order';
                orderSpan.textContent = (i + 1);
                lineItem.appendChild(orderSpan);
                
                // 결재자 정보 컨테이너
                var infoDiv = document.createElement('div');
                infoDiv.className = 'apupdate-line-info';
                
                // 이름과 직급
                var nameSpan = document.createElement('span');
                nameSpan.className = 'apupdate-line-name';
                nameSpan.textContent = line.empName + ' ' + (line.positionName || '');
                infoDiv.appendChild(nameSpan);
                
                // 부서명
                var deptSpan = document.createElement('span');
                deptSpan.className = 'apupdate-line-dept';
                deptSpan.textContent = line.deptName || '';
                infoDiv.appendChild(deptSpan);
                
                lineItem.appendChild(infoDiv);
                
                // 삭제 버튼
                var removeBtn = document.createElement('button');
                removeBtn.type = 'button';
                removeBtn.className = 'apupdate-remove-btn';
                removeBtn.setAttribute('onclick', 'removeApprovalLine(' + i + ')');
                
                var removeIcon = document.createElement('i');
                removeIcon.className = 'fas fa-times';
                removeBtn.appendChild(removeIcon);
                
                lineItem.appendChild(removeBtn);
                
                // 컨테이너에 추가
                container.appendChild(lineItem);
            }
        }
        
        // hidden input에 결재선 데이터 설정
        document.getElementById('apupdate-lines-data').value = JSON.stringify(approvalLines);
        
        // 콘솔에 로그 출력 (디버깅용)
        console.log('결재선 업데이트됨:', approvalLines);
    }
    
 // 결재자 제거 함수 - 전통적인 방식으로 수정
    function removeApprovalLine(index) {
        // 배열에서 해당 인덱스 항목 제거
        approvalLines.splice(index, 1);
        
        // 화면 업데이트
        updateApprovalLineDisplay();
    }
    
    // 문서 초기화 및 placeholder 효과 추가
    document.addEventListener('DOMContentLoaded', function() {
        const contentEditor = document.getElementById('doc-content-editor');
        
        if (contentEditor) {
            // 초기 상태 확인
            if (!contentEditor.textContent.trim()) {
                contentEditor.innerHTML = '<span class="placeholder">문서 내용을 입력하세요</span>';
            }
            
            contentEditor.addEventListener('focus', function() {
                if (contentEditor.innerHTML === '<span class="placeholder">문서 내용을 입력하세요</span>') {
                    contentEditor.innerHTML = '';
                }
            });
            
            contentEditor.addEventListener('blur', function() {
                if (!contentEditor.textContent.trim()) {
                    contentEditor.innerHTML = '<span class="placeholder">문서 내용을 입력하세요</span>';
                }
            });
        }
        
        // 페이지 로드 시 기존 결재선 표시
        if (approvalLines.length > 0) {
            updateApprovalLineDisplay();
        }
    });
    
    // 양식별 데이터 수집 함수
    function collectDocumentData() {
        const formNo = ${document.formNo};
        let documentData = {};
        
        switch(formNo) {
            case 1: // 휴가신청서
                documentData = collectVacationData();
                break;
            case 2: // 지출결의서
                documentData = collectExpenseData();
                break;
            case 3: // 재택근무신청서
                documentData = collectRemoteWorkData();
                break;
            case 4: // 출장신청서
                documentData = collectBusinessTripData();
                break;
            default:
                // 기본 에디터의 경우
                const contentEditor = document.getElementById('doc-content-editor');
                if (contentEditor) {
                    const content = contentEditor.innerHTML;
                    if (content === '<span class="placeholder">문서 내용을 입력하세요</span>') {
                        documentData = { content: '' };
                    } else {
                        documentData = { content: content };
                    }
                }
        }
        
        // JSON 문자열로 변환하여 hidden input에 저장
        document.getElementById('docContent-hidden').value = JSON.stringify(documentData);
    }
    
    // 기본 에디터에서 내용 가져오기
    function getDefaultContentValue() {
        const contentEditor = document.getElementById('doc-content-editor');
        const content = contentEditor.innerHTML;
        
        // placeholder 텍스트일 경우 빈 값으로 처리
        if (content === '<span class="placeholder">문서 내용을 입력하세요</span>' || !content.trim()) {
            alert('문서 내용을 입력해주세요.');
            return '';
        }
        
        return content;
    }
    
    // 폼 유효성 검증 함수
    function validateForm() {
        const title = document.querySelector('input[name="title"]').value.trim();
        if (!title) {
            alert('문서 제목을 입력해주세요.');
            return false;
        }
        
        // 양식 번호에 따라 다른 검증 로직 적용
        const formNo = ${document.formNo};
        let isValid = true;
        
        switch(formNo) {
            case 1: // 휴가신청서
                isValid = validateVacationForm();
                break;
            case 2: // 지출결의서
                isValid = validateExpenseForm();
                break;
            case 3: // 재택근무신청서
                isValid = validateRemoteWorkForm();
                break;
            case 4: // 출장신청서
                isValid = validateBusinessTripForm();
                break;
            default:
                // 기본 문서 내용 검증
                const content = getDefaultContentValue();
                isValid = content !== '';
        }
        
        return isValid;
    }
    
    // 폼 제출 관련 함수
    function updateDraft() {
        if (!validateForm()) return;
        
        collectDocumentData();
        document.getElementById('apupdate-action').value = 'save';
        document.getElementById('apupdate-document-form').submit();
    }
    
    function submitDocument() {
        if (!validateForm()) return;
        
        if (approvalLines.length === 0) {
            alert('결재선을 설정해주세요.');
            return;
        }
        
        collectDocumentData();
        document.getElementById('apupdate-action').value = 'submit';
        document.getElementById('apupdate-document-form').submit();
    }
    
    function goBack() {
        if (confirm('수정 중인 내용이 저장되지 않을 수 있습니다. 정말 나가시겠습니까?')) {
            history.back();
        }
    }
 </script>
 <script>
// 결재요청 직접 처리 함수 - 기존 함수 대신 사용
function submitDocumentDirect() {
    // 유효성 검증
    const title = document.querySelector('input[name="title"]').value.trim();
    if (!title) {
        alert('문서 제목을 입력해주세요.');
        return;
    }
    
    // 결재선 확인
    if (approvalLines.length === 0) {
        alert('결재선을 설정해주세요.');
        return;
    }
    
    // 직접 데이터 수집
    try {
        // 양식별 데이터 수집
        const formNo = ${document.formNo};
        let data = {};
        
        switch(formNo) {
            case 1: // 휴가신청서
                // 휴가 유형 가져오기
                let vacationType = '';
                document.querySelectorAll('input[name="vacationType"]').forEach(radio => {
                    if (radio.checked) vacationType = radio.value;
                });
                
                data = {
                    formType: "vacation",
                    data: {
                        vacationType: vacationType,
                        startDate: document.getElementById('vacation-start-date').value,
                        endDate: document.getElementById('vacation-end-date').value,
                        days: document.getElementById('vacation-days').textContent.replace(/[()총일]/g, '').trim(),
                        totalDays: document.getElementById('total-days').value,
                        usedDays: document.getElementById('used-days').value,
                        remainingDays: document.getElementById('remaining-days').textContent,
                        reason: document.getElementById('vacation-reason').value,
                        emergencyContact: document.getElementById('emergency-contact').value,
                        drafterInfo: {
                            empName: loginUser.empName,
                            deptName: loginUser.deptName,
                            positionName: loginUser.positionName
                        }
                    }
                };
                break;
                
            case 2: // 지출결의서
                // 지출 유형 가져오기
                let expenseType = '';
                document.querySelectorAll('input[name="expenseType"]').forEach(radio => {
                    if (radio.checked) expenseType = radio.value;
                });
                
                // 지출 내역 수집
                const expenses = [];
                const expenseTableRows = document.querySelectorAll('#expense-body tr');
                expenseTableRows.forEach(row => {
                    const dateInput = row.querySelector('.expense-date');
                    const detailInput = row.querySelector('.expense-detail');
                    const amountInput = row.querySelector('.expense-amount');
                    
                    if (dateInput && detailInput && amountInput) {
                        expenses.push({
                            date: dateInput.value,
                            detail: detailInput.value,
                            amount: amountInput.value
                        });
                    }
                });
                
                data = {
                    formType: "expense",
                    data: {
                        expenseType: expenseType,
                        accountInfo: document.getElementById('account-info') ? document.getElementById('account-info').value : '',
                        expenses: expenses,
                        total: document.getElementById('expense-total') ? document.getElementById('expense-total').textContent.replace(/,/g, '') : '0',
                        description: document.getElementById('expense-description') ? document.getElementById('expense-description').value : '',
                        drafterInfo: {
                            empName: loginUser.empName,
                            deptName: loginUser.deptName,
                            positionName: loginUser.positionName
                        }
                    }
                };
                break;
                
            case 3: // 재택근무신청서
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
                
                data = {
                    formType: "remoteWork",
                    data: {
                        workType: workType,
                        startDate: document.getElementById('startDate') ? document.getElementById('startDate').value : '',
                        endDate: document.getElementById('endDate') ? document.getElementById('endDate').value : '',
                        days: document.getElementById('dayCount') ? document.getElementById('dayCount').textContent.replace(/[()총일]/g, '').trim() : '0',
                        reason: document.getElementById('reason') ? document.getElementById('reason').value : '',
                        workPlan: document.getElementById('workPlan') ? document.getElementById('workPlan').value : '',
                        workEnvironments: workEnv.join(', '),
                        contact: document.getElementById('contact') ? document.getElementById('contact').value : '',
                        drafterInfo: {
                            empName: loginUser.empName,
                            deptName: loginUser.deptName,
                            positionName: loginUser.positionName
                        }
                    }
                };
                break;
                
            case 4: // 출장신청서
                // 출장 유형 가져오기
                let tripType = '';
                document.querySelectorAll('input[name="tripType"]').forEach(radio => {
                    if (radio.checked) tripType = radio.value;
                });
                
                // 출장자 정보 수집
                const tripMembers = [];
                document.querySelectorAll('.trip-member-input').forEach(input => {
                    if (input.value) {
                        const [name, dept] = input.value.split(',');
                        tripMembers.push({ name, dept });
                    }
                });
                
                data = {
                    formType: "businessTrip",
                    data: {
                        purpose: document.getElementById('trip-purpose') ? document.getElementById('trip-purpose').value : '',
                        tripType: tripType,
                        location: document.getElementById('trip-location') ? document.getElementById('trip-location').value : '',
                        startDate: document.getElementById('trip-start-date') ? document.getElementById('trip-start-date').value : '',
                        endDate: document.getElementById('trip-end-date') ? document.getElementById('trip-end-date').value : '',
                        days: document.getElementById('trip-days') ? document.getElementById('trip-days').textContent.replace(/[()총일]/g, '').trim() : '0',
                        members: tripMembers,
                        emergencyContact: document.getElementById('emergency-contact') ? document.getElementById('emergency-contact').value : '',
                        drafterInfo: {
                            empName: loginUser.empName,
                            deptName: loginUser.deptName,
                            positionName: loginUser.positionName
                        }
                    }
                };
                break;
                
            default:
                // 기본 에디터의 경우
                const contentEditor = document.getElementById('doc-content-editor');
                if (contentEditor) {
                    const content = contentEditor.innerHTML;
                    if (content === '<span class="placeholder">문서 내용을 입력하세요</span>') {
                        data = { content: '' };
                    } else {
                        data = { content: content };
                    }
                }
        }
        
        // 직접 hidden input 설정
        console.log("수집된 문서 데이터:", data);
        
        // 모든 가능한 ID에 데이터 저장 시도
        if (document.getElementById('docContent-hidden')) {
            document.getElementById('docContent-hidden').value = JSON.stringify(data);
        }
        
        if (document.getElementById('doc-content-data')) {
            document.getElementById('doc-content-data').value = JSON.stringify(data);
        }
        
        // 모든 가능한 액션 필드 설정 시도
        if (document.getElementById('apupdate-action')) {
            document.getElementById('apupdate-action').value = 'submit';
        }
        
        if (document.getElementById('form-action')) {
            document.getElementById('form-action').value = 'submit';
        }
        
        // 폼 제출
        console.log("폼 제출 준비 완료");
        if (document.getElementById('apupdate-document-form')) {
            document.getElementById('apupdate-document-form').submit();
        } else if (document.getElementById('vacationForm')) {
            document.getElementById('vacationForm').submit();
        } else if (document.getElementById('expenseForm')) {
            document.getElementById('expenseForm').submit();
        } else if (document.getElementById('remoteWorkForm')) {
            document.getElementById('remoteWorkForm').submit();
        } else if (document.getElementById('businessTripForm')) {
            document.getElementById('businessTripForm').submit();
        } else {
            alert("폼을 찾을 수 없습니다.");
        }
    } catch(e) {
        console.error("결재요청 중 오류 발생:", e);
        alert("결재요청 중 오류가 발생했습니다: " + e.message);
    }
}
</script>

 <!-- 양식별 스크립트 추가 -->
 <script src="${pageContext.request.contextPath}/resources/js/vacationForm.js"></script>
 <script src="${pageContext.request.contextPath}/resources/js/expenseForm.js"></script>
 <script src="${pageContext.request.contextPath}/resources/js/remoteWorkForm.js"></script>
 <script src="${pageContext.request.contextPath}/resources/js/businessTripForm.js"></script>
 <script>
// 페이지 로드 시 한 번만 실행
(function() {
    // 각 양식별 데이터 수집 및 유효성 검증 함수를 안전하게 감싸는 래퍼
    // 이미 문서가 로드된 후에 실행되므로 DOMContentLoaded 이벤트는 필요 없음
    
    // 기존 함수 백업
    const originalUpdateDraft = window.updateDraft;
    const originalSubmitDocumentDirect = window.submitDocumentDirect;
    
    // updateDraft 함수 래핑 (임시저장용)
    window.updateDraft = function() {
        try {
            // 제목 유효성 검증
            const title = document.querySelector('input[name="title"]').value.trim();
            if (!title) {
                alert('문서 제목을 입력해주세요.');
                return;
            }
            
            // 양식별 데이터 수집 - 기존 함수 호출 시 발생하는 오류 처리
            try {
                collectDocumentData();
                document.getElementById('apupdate-action').value = 'save';
                document.getElementById('apupdate-document-form').submit();
            } catch (error) {
                console.error('데이터 수집 중 오류 발생:', error);
                
                // 오류 발생 시 안전한 대체 방법 사용
                safeCollectDocumentData();
                document.getElementById('apupdate-action').value = 'save';
                document.getElementById('apupdate-document-form').submit();
            }
        } catch (error) {
            console.error('임시저장 처리 중 오류:', error);
            alert('임시저장 중 오류가 발생했습니다. 필요한 정보를 확인하세요.');
        }
    };
    
    // submitDocumentDirect 함수 래핑 (결재요청용)
    window.submitDocumentDirect = function() {
        try {
            // 제목 유효성 검증
            const title = document.querySelector('input[name="title"]').value.trim();
            if (!title) {
                alert('문서 제목을 입력해주세요.');
                return;
            }
            
            // 결재선 확인
            if (approvalLines.length === 0) {
                alert('결재선을 설정해주세요.');
                return;
            }
            
            // 양식별 데이터 수집 - 기존 함수 호출 시 발생하는 오류 처리
            try {
                collectDocumentData();
                document.getElementById('apupdate-action').value = 'submit';
                document.getElementById('apupdate-document-form').submit();
            } catch (error) {
                console.error('데이터 수집 중 오류 발생:', error);
                
                // 오류 발생 시 안전한 대체 방법 사용
                safeCollectDocumentData();
                document.getElementById('apupdate-action').value = 'submit';
                document.getElementById('apupdate-document-form').submit();
            }
        } catch (error) {
            console.error('결재요청 처리 중 오류:', error);
            alert('결재요청 중 오류가 발생했습니다. 필요한 정보를 확인하세요.');
        }
    };
    
    // 안전한 데이터 수집 함수 (기존 함수가 실패할 경우 대체용)
    function safeCollectDocumentData() {
        const formNo = ${document.formNo}; // JSP EL을 통해 양식 번호 가져오기
        let documentData = {};
        
        // 양식별 데이터 수집
        switch (formNo) {
            case 1: // 휴가신청서
                documentData = collectSafeVacationData();
                break;
            case 2: // 지출결의서
                documentData = collectSafeExpenseData();
                break;
            case 3: // 재택근무신청서
                documentData = collectSafeRemoteWorkData();
                break;
            case 4: // 출장신청서
                documentData = collectSafeBusinessTripData();
                break;
            default:
                // 기본 에디터
                const contentEditor = document.getElementById('doc-content-editor');
                if (contentEditor) {
                    const content = contentEditor.innerHTML;
                    if (content === '<span class="placeholder">문서 내용을 입력하세요</span>') {
                        documentData = { content: '' };
                    } else {
                        documentData = { content: content };
                    }
                }
        }
        
        // 데이터 저장
        document.getElementById('docContent-hidden').value = JSON.stringify(documentData);
    }
    
    // 안전한 휴가신청서 데이터 수집 함수
    function collectSafeVacationData() {
        // 값을 안전하게 가져오는 헬퍼 함수
        function getValue(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.value : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        function getText(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.textContent : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        // 휴가 유형 가져오기
        let vacationType = '';
        try {
            const checkedRadio = document.querySelector('input[name="vacationType"]:checked');
            if (checkedRadio) vacationType = checkedRadio.value;
        } catch (e) {
            console.error('휴가 유형 가져오기 오류:', e);
        }
        
        return {
            formType: "vacation",
            data: {
                vacationType: vacationType,
                startDate: getValue('#vacation-start-date', ''),
                endDate: getValue('#vacation-end-date', ''),
                days: getText('#vacation-days', '0').replace(/[()총일]/g, '').trim(),
                totalDays: getValue('#total-days', '0'),
                usedDays: getValue('#used-days', '0'),
                remainingDays: getText('#remaining-days', '0'),
                reason: getValue('#vacation-reason', ''),
                emergencyContact: getValue('#emergency-contact', ''),
                drafterInfo: {
                    empName: loginUser.empName || '',
                    deptName: loginUser.deptName || '',
                    positionName: loginUser.positionName || ''
                }
            }
        };
    }
    
    // 안전한 지출결의서 데이터 수집 함수
    function collectSafeExpenseData() {
        // 값을 안전하게 가져오는 헬퍼 함수
        function getValue(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.value : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        function getText(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.textContent : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        // 지출 유형 가져오기
        let expenseType = '';
        try {
            const checkedRadio = document.querySelector('input[name="expenseType"]:checked');
            if (checkedRadio) expenseType = checkedRadio.value;
        } catch (e) {
            console.error('지출 유형 가져오기 오류:', e);
        }
        
        // 지출 내역 수집 (안전하게)
        const expenses = [];
        try {
            const rows = document.querySelectorAll('#expense-body tr');
            if (rows && rows.length > 0) {
                rows.forEach(row => {
                    try {
                        const dateInput = row.querySelector('.expense-date');
                        const detailInput = row.querySelector('.expense-detail');
                        const amountInput = row.querySelector('.expense-amount');
                        
                        if (dateInput && detailInput && amountInput) {
                            expenses.push({
                                date: dateInput.value || '',
                                detail: detailInput.value || '',
                                amount: amountInput.value || '0'
                            });
                        }
                    } catch (e) {
                        console.error('지출항목 데이터 수집 오류:', e);
                    }
                });
            }
        } catch (e) {
            console.error('지출 내역 수집 오류:', e);
        }
        
        return {
            formType: "expense",
            data: {
                expenseType: expenseType,
                accountInfo: getValue('#account-info', ''),
                expenses: expenses,
                total: getText('#expense-total', '0').replace(/,/g, ''),
                description: getValue('#expense-description', ''),
                drafterInfo: {
                    empName: loginUser.empName || '',
                    deptName: loginUser.deptName || '',
                    positionName: loginUser.positionName || ''
                }
            }
        };
    }
    
    // 안전한 재택근무신청서 데이터 수집 함수
    function collectSafeRemoteWorkData() {
        // 값을 안전하게 가져오는 헬퍼 함수
        function getValue(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.value : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        function getText(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.textContent : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        // 재택근무 유형 가져오기
        let workType = '';
        try {
            const checkedRadio = document.querySelector('input[name="workType"]:checked');
            if (checkedRadio) workType = checkedRadio.value;
        } catch (e) {
            console.error('재택근무 유형 가져오기 오류:', e);
        }
        
        // 근무 환경 가져오기
        const workEnv = [];
        try {
            const checkedBoxes = document.querySelectorAll('input[name="workEnv[]"]:checked');
            if (checkedBoxes && checkedBoxes.length > 0) {
                checkedBoxes.forEach(checkbox => {
                    if (checkbox && checkbox.value) workEnv.push(checkbox.value);
                });
            }
        } catch (e) {
            console.error('근무 환경 가져오기 오류:', e);
        }
        
        return {
            formType: "remoteWork",
            data: {
                workType: workType,
                startDate: getValue('#startDate', ''),
                endDate: getValue('#endDate', ''),
                days: getText('#dayCount', '0').replace(/[()총일]/g, '').trim(),
                reason: getValue('#reason', ''),
                workPlan: getValue('#workPlan', ''),
                workEnvironments: workEnv.join(', '),
                contact: getValue('#contact', ''),
                drafterInfo: {
                    empName: loginUser.empName || '',
                    deptName: loginUser.deptName || '',
                    positionName: loginUser.positionName || ''
                }
            }
        };
    }
    
    // 안전한 출장신청서 데이터 수집 함수
    function collectSafeBusinessTripData() {
        // 값을 안전하게 가져오는 헬퍼 함수
        function getValue(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.value : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        function getText(selector, defaultValue) {
            try {
                const element = document.querySelector(selector);
                return element ? element.textContent : defaultValue;
            } catch (e) {
                return defaultValue;
            }
        }
        
        // 출장 유형 가져오기
        let tripType = '';
        try {
            const checkedRadio = document.querySelector('input[name="tripType"]:checked');
            if (checkedRadio) tripType = checkedRadio.value;
        } catch (e) {
            console.error('출장 유형 가져오기 오류:', e);
        }
        
        // 출장자 정보 수집
        const tripMembers = [];
        try {
            const memberInputs = document.querySelectorAll('.trip-member-input');
            if (memberInputs && memberInputs.length > 0) {
                memberInputs.forEach(input => {
                    if (input && input.value) {
                        const parts = input.value.split(',');
                        tripMembers.push({
                            name: parts[0] || '',
                            dept: parts[1] || ''
                        });
                    }
                });
            }
        } catch (e) {
            console.error('출장자 정보 수집 오류:', e);
        }
        
        return {
            formType: "businessTrip",
            data: {
                purpose: getValue('#trip-purpose', ''),
                tripType: tripType,
                location: getValue('#trip-location', ''),
                startDate: getValue('#trip-start-date', ''),
                endDate: getValue('#trip-end-date', ''),
                days: getText('#trip-days', '0').replace(/[()총일]/g, '').trim(),
                members: tripMembers,
                emergencyContact: getValue('#emergency-contact', ''),
                drafterInfo: {
                    empName: loginUser.empName || '',
                    deptName: loginUser.deptName || '',
                    positionName: loginUser.positionName || ''
                }
            }
        };
    }
})();
</script>
 </body>
 </html>