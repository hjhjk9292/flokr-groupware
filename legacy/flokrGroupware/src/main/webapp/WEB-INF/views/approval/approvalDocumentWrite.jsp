<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결재 문서 작성 | Flokr</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalDocumentWrite.css">
    <link href="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-lite.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-lite.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/lang/summernote-ko-KR.min.js"></script>
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />

    <div class="apenroll-container">
        <form id="apenroll-document-form" method="post" action="insertDocument.ap" enctype="multipart/form-data">
            <input type="hidden" name="formNo" value="${form.formNo}">
            <input type="hidden" name="lines" id="apenroll-lines-data">
            <input type="hidden" name="action" id="apenroll-action">
            
            <div class="apenroll-form">
                <div class="apenroll-header">
                    <h1 class="apenroll-title">결재 문서 작성</h1>
                    <p class="apenroll-form-name">${form.formName}</p>
                </div>
                
                <!-- 기본 정보 섹션 -->
                <div class="apenroll-section">
                    <h3 class="apenroll-section-title">
                        <i class="fas fa-info-circle"></i>
                        기본 정보
                    </h3>
                    <div class="apenroll-form-group">
                        <label class="apenroll-label">문서 제목</label>
                        <input type="text" name="title" class="apenroll-input" placeholder="문서 제목을 입력하세요" required>
                    </div>
                    <div class="apenroll-form-group">
                        <label class="apenroll-label">긴급도</label>
                        <select name="urgency" class="apenroll-input">
                            <option value="NORMAL">보통</option>
                            <option value="URGENT">긴급</option>
                            <option value="VERY_URGENT">매우 긴급</option>
                        </select>
                    </div>
                </div>
                
                <!-- 결재선 섹션 -->
                <div class="apenroll-section">
                    <h3 class="apenroll-section-title">
                        <i class="fas fa-route"></i>
                        결재선 설정
                    </h3>
                    <div class="apenroll-line-section">
                        <div class="apenroll-line-list" id="apenroll-approval-lines">
                            <div class="apenroll-empty-line">
                                <i class="fas fa-plus"></i>
                                결재자를 추가해주세요
                            </div>
                        </div>
                        <button type="button" class="apenroll-add-line-btn" onclick="openApprovalLineModal()">
                            <i class="fas fa-plus"></i> 결재자 추가
                        </button>
                    </div>
                </div>
                
                <!-- 문서 내용 섹션 -->
                <div class="apenroll-section">
                    <h3 class="apenroll-section-title">
                        <i class="fas fa-edit"></i>
                        문서 내용
                    </h3>
                    <div class="apenroll-content-editor">
                        <textarea name="docContent" id="apenroll-summernote">${form.formContent}</textarea>
                    </div>
                </div>
                
                <!-- 첨부파일 섹션 (단일 파일) -->
                <div class="apenroll-section">
                    <h3 class="apenroll-section-title">
                        <i class="fas fa-paperclip"></i>
                        첨부파일 (1개만 업로드 가능)
                    </h3>
                    <div class="apenroll-attachment">
                        <input type="file" id="apenroll-file-input" name="attachFile" class="apenroll-file-input">
                        <div class="apenroll-file-upload" onclick="document.getElementById('apenroll-file-input').click()">
                            <i class="fas fa-cloud-upload-alt" style="font-size: 24px; color: #999; margin-bottom: 10px;"></i>
                            <p style="color: #666;">파일을 선택하거나 드래그하여 업로드하세요</p>
                            <p style="color: #999; font-size: 12px;">최대 10MB</p>
                        </div>
                        <div class="apenroll-file-info" id="apenroll-file-info">
                            <div class="apenroll-file-item">
                                <i class="fas fa-file"></i>
                                <span id="apenroll-file-name"></span>
                                <span id="apenroll-file-size"></span>
                                <button type="button" class="apenroll-remove-btn" onclick="removeFile()">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
    
    <!-- 액션 바 -->
    <div class="apenroll-action-bar">
        <div class="apenroll-action-container">
            <div class="apenroll-info">
                임시저장하거나 결재를 요청하실 수 있습니다.
            </div>
            <div class="apenroll-btn-group">
                <button class="apenroll-btn apenroll-btn-outline" onclick="goBack()">취소</button>
                <button class="apenroll-btn apenroll-btn-primary" onclick="saveDraft()">
                    <i class="fas fa-save"></i> 임시저장
                </button>
                <button class="apenroll-btn apenroll-btn-secondary" onclick="submitDocument()">
                    <i class="fas fa-paper-plane"></i> 결재요청
                </button>
            </div>
        </div>
    </div>
    
    <script>
        // Summernote 초기화
        $('#apenroll-summernote').summernote({
            placeholder: '문서 내용을 입력하세요',
            tabsize: 2,
            height: 300,
            lang: 'ko-KR',
            toolbar: [
                ['style', ['style']],
                ['font', ['bold', 'underline', 'clear']],
                ['color', ['color']],
                ['para', ['ul', 'ol', 'paragraph']],
                ['table', ['table']],
                ['insert', ['link', 'picture']],
                ['view', ['fullscreen', 'codeview', 'help']]
            ]
        });
        
        // 결재선 데이터
        let approvalLines = [];
        
        // 첨부파일 처리
        let selectedFile = null;
        
        document.getElementById('apenroll-file-input').addEventListener('change', function(e) {
            handleFileSelect(e.target.files[0]);
        });
        
        // 드래그 앤 드롭
        const fileUpload = document.querySelector('.apenroll-file-upload');
        
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
           document.getElementById('apenroll-file-name').textContent = file.name;
           document.getElementById('apenroll-file-size').textContent = formatFileSize(file.size);
           document.getElementById('apenroll-file-info').style.display = 'block';
           
           // 기존 파일 선택을 덮어쓰기
           const fileInput = document.getElementById('apenroll-file-input');
           const dataTransfer = new DataTransfer();
           dataTransfer.items.add(file);
           fileInput.files = dataTransfer.files;
       }
       
       function removeFile() {
           selectedFile = null;
           document.getElementById('apenroll-file-input').value = '';
           document.getElementById('apenroll-file-info').style.display = 'none';
       }
       
       function formatFileSize(bytes) {
           if (bytes === 0) return '0 Bytes';
           
           const k = 1024;
           const sizes = ['Bytes', 'KB', 'MB', 'GB'];
           const i = Math.floor(Math.log(bytes) / Math.log(k));
           
           return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
       }
       
       // 결재선 관련 함수
       function openApprovalLineModal() {
           // 결재선 모달 열기
           window.open('approvalLineModal.ap', 'approvalLineModal', 'width=800,height=600,scrollbars=yes');
       }
       
       // 결재선 모달에서 선택된 결재자 정보를 받는 함수 (전역 함수로 선언)
       window.addApprovalLine = function(emp) {
           // 이미 존재하는지 확인
           if (approvalLines.find(line => line.empNo === emp.empNo)) {
               alert('이미 결재선에 포함된 사원입니다.');
               return;
           }
           
           approvalLines.push({
               empNo: emp.empNo,
               empName: emp.empName,
               deptName: emp.deptName,
               positionName: emp.positionName
           });
           
           updateApprovalLineDisplay();
       };
       
       function updateApprovalLineDisplay() {
    	   const container = document.getElementById('apenroll-approval-lines');
    	    
    	    if (approvalLines.length === 0) {
    	        container.innerHTML = '<div class="apenroll-empty-line"><i class="fas fa-plus"></i>결재자를 추가해주세요</div>';
    	    } else {
    	        let html = '';
    	        approvalLines.forEach(function(line, index) {
    	            html += '<div class="apenroll-line-item">';
    	            html += '<span class="apenroll-line-order">' + (index + 1) + '</span>';
    	            html += '<div class="apenroll-line-info">';
    	            html += '<span class="apenroll-line-name">' + (line.empName || '') + ' ' + (line.positionName || '') + '</span>';
    	            html += '<span class="apenroll-line-dept">' + (line.deptName || '') + '</span>';
    	            html += '</div>';
    	            html += '<button type="button" class="apenroll-remove-btn" onclick="removeApprovalLine(' + index + ')">';
    	            html += '<i class="fas fa-times"></i>';
    	            html += '</button>';
    	            html += '</div>';
    	        });
    	        container.innerHTML = html;
    	    }
    	    
    	    // hidden input에 JSON 데이터 저장
    	    document.getElementById('apenroll-lines-data').value = JSON.stringify(approvalLines);
    	}
       
       function removeApprovalLine(index) {
           approvalLines.splice(index, 1);
           updateApprovalLineDisplay();
       }
       
       // 폼 제출 관련 함수
       function saveDraft() {
           if (!validateForm()) return;
           
           document.getElementById('apenroll-action').value = 'save';
           document.getElementById('apenroll-document-form').submit();
       }
       
       function submitDocument() {
           if (!validateForm()) return;
           
           if (approvalLines.length === 0) {
               alert('결재선을 설정해주세요.');
               return;
           }
           
           document.getElementById('apenroll-action').value = 'submit';
           document.getElementById('apenroll-document-form').submit();
       }
       
       function validateForm() {
           const title = document.querySelector('input[name="title"]').value.trim();
           if (!title) {
               alert('문서 제목을 입력해주세요.');
               return false;
           }
           
           const content = $('#apenroll-summernote').summernote('code');
           if (!content || content === '<p><br></p>') {
               alert('문서 내용을 입력해주세요.');
               return false;
           }
           
           return true;
       }
       
       function goBack() {
           if (confirm('작성 중인 내용이 저장되지 않을 수 있습니다. 정말 나가시겠습니까?')) {
               history.back();
           }
       }
   </script>
</body>
</html>