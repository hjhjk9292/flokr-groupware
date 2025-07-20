<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!-- JSP 페이지 상단에 추가 -->
<%
  if(session.getAttribute("loginUser") != null) {
    com.kh.flokrGroupware.employee.model.vo.Employee emp = (com.kh.flokrGroupware.employee.model.vo.Employee)session.getAttribute("loginUser");
  }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>전자결재 대시보드 | Flokr</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalMain.css">
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />

    <!-- 사용자 정보를 저장할 숨겨진 요소 -->
    <div id="userDataContainer" 
         data-emp-name="${loginUser.empName}" 
         data-dept-name="${loginUser.deptName}" 
         style="display: none;"></div>

    <div class="apv-main-container">
        <main class="apv-main-dashboard">
            <h1 class="apv-main-page-title">
                전자 결재
                <div class="apv-main-sub-title">문서 결재와 관련된 작업을 처리하세요.</div>
            </h1>
            
            <!-- 통계 카드 영역 -->
			<div class="apv-main-stats-container">
			    <!-- 결재 대기 카드 -->
			    <div class="apv-main-stat-card" onclick="location.href='waitingList.ap'">
			        <div class="apv-main-stat-card-header">
			            <div class="apv-main-stat-card-icon waiting">
			                <i class="far fa-clock" style="font-size: 24px;"></i>
			            </div>
			            <div class="apv-main-stat-card-title">결재 대기</div>
			        </div>
			        <div class="apv-main-stat-card-value">${stats.waitingCount}<span class="apv-main-stat-card-unit">건</span></div>
			    </div>
			    
			    <!-- 진행 결재 건수 카드 - 상신 문서함으로 연결 -->
			    <div class="apv-main-stat-card" onclick="location.href='requestedList.ap?fromDashboard=pending'">
			        <div class="apv-main-stat-card-header">
			            <div class="apv-main-stat-card-icon processing">
			                <i class="far fa-file-alt" style="font-size: 24px;"></i>
			            </div>
			            <div class="apv-main-stat-card-title">진행 결재 건수</div>
			        </div>
			        <div class="apv-main-stat-card-value">${stats.processingCount}<span class="apv-main-stat-card-unit">건</span></div>
			    </div>
			    
			    <!-- 결재 완료 카드 - 완료 문서함으로 연결 -->
			    <div class="apv-main-stat-card" onclick="location.href='completedList.ap'">
			        <div class="apv-main-stat-card-header">
			            <div class="apv-main-stat-card-icon approved">
			                <i class="far fa-check-circle" style="font-size: 24px;"></i>
			            </div>
			            <div class="apv-main-stat-card-title">승인한 문서</div>
			        </div>
			        <div class="apv-main-stat-card-value">${stats.approvedCount}<span class="apv-main-stat-card-unit">건</span></div>
			    </div>
			    
			    <!-- 결재 반려 카드 - 완료 문서함으로 연결 -->
			    <div class="apv-main-stat-card" onclick="location.href='completedList.ap'">
			        <div class="apv-main-stat-card-header">
			            <div class="apv-main-stat-card-icon rejected">
			                <i class="far fa-times-circle" style="font-size: 24px;"></i>
			            </div>
			            <div class="apv-main-stat-card-title">반려한 문서</div>
			        </div>
			        <div class="apv-main-stat-card-value">${stats.rejectedCount}<span class="apv-main-stat-card-unit">건</span></div>
			    </div>
			</div>
            
            <!-- 수신 문서함 영역 -->
            <div class="apv-main-approval-list-container">
                <div class="apv-main-approval-list-header">
                    <h2 class="apv-main-approval-list-title" onclick="location.href='waitingList.ap'">수신 문서함</h2>
                    <div class="apv-main-approval-list-actions">
                        <button class="apv-main-btn apv-main-btn-primary" id="apv-main-createApprovalBtn" onclick="showModal()">
                            <i class="fas fa-plus"></i> 결재 작성
                        </button>
                    </div>
                </div>
                
                <!-- 결재 목록 테이블 -->
                <div class="apv-main-table-container">
                    <table class="apv-main-approval-table">
                        <thead>
                            <tr>
                                <th>문서번호</th>
                                <th>상신날짜</th>
                                <th>사용 양식</th>
                                <th>제목</th>
                                <th>요청자</th>
                                <th>결재상태</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${waitingDocuments}">
                                <tr>
                                    <td>${doc.docNo}</td>
                                    <td><fmt:formatDate value="${doc.requestedDate}" pattern="yyyy-MM-dd"/></td>
                                    <td>${doc.formName}</td>
                                    <td><a href="documentDetail.ap?docNo=${doc.docNo}" class="apv-main-doc-title">${doc.title}</a></td>
                                    <td>${doc.drafterName} / ${doc.drafterDeptName}</td>
                                    <td>
                                        <span class="apv-main-status-badge apv-main-status-waiting">대기</span>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty waitingDocuments}">
                                <tr>
                                    <td colspan="6">
                                        <div class="apv-main-empty-state">
                                            <i class="far fa-file-alt"></i>
                                            <div class="apv-main-empty-state-text">대기 중인 결재 문서가 없습니다.</div>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <!-- 내가 올린 문서함 영역 -->
            <div class="apv-main-approval-list-container">
                <div class="apv-main-approval-list-header">
                    <h2 class="apv-main-approval-list-title" onclick="location.href='requestedList.ap'">내가 올린 문서함</h2>
                </div>
                
                <!-- 결재 목록 테이블 -->
                <div class="apv-main-table-container">
                    <table class="apv-main-approval-table">
                        <thead>
                            <tr>
                                <th>문서번호</th>
                                <th>상신날짜</th>
                                <th>사용 양식</th>
                                <th>제목</th>
                                <th>결재상태</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${myDocuments}">
                                <tr>
                                    <td>${doc.docNo}</td>
                                    <td><fmt:formatDate value="${doc.requestedDate}" pattern="yyyy-MM-dd"/></td>
                                    <td>${doc.formName}</td>
                                    <td><a href="documentDetail.ap?docNo=${doc.docNo}" class="apv-main-doc-title">${doc.title}</a></td>
                                    <td>
                                        <span class="apv-main-status-badge 
                                            <c:choose>
                                                <c:when test="${doc.docStatus == 'REQUESTED'}">apv-main-status-processing</c:when>
                                                <c:when test="${doc.docStatus == 'APPROVED'}">apv-main-status-approved</c:when>
                                                <c:when test="${doc.docStatus == 'REJECTED'}">apv-main-status-rejected</c:when>
                                            </c:choose>">
                                            <c:choose>
                                                <c:when test="${doc.docStatus == 'REQUESTED'}">진행중</c:when>
                                                <c:when test="${doc.docStatus == 'APPROVED'}">승인</c:when>
                                                <c:when test="${doc.docStatus == 'REJECTED'}">반려</c:when>
                                            </c:choose>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty myDocuments}">
                                <tr>
                                    <td colspan="5">
                                        <div class="apv-main-empty-state">
                                            <i class="far fa-file-alt"></i>
                                            <div class="apv-main-empty-state-text">상신한 문서가 없습니다.</div>
                                            <button class="apv-main-btn apv-main-btn-primary" id="apv-main-createApprovalBtn2">
                                                <i class="fas fa-plus"></i> 결재 작성하기
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <!-- 임시저장함 영역 -->
            <div class="apv-main-approval-list-container">
                <div class="apv-main-approval-list-header">
                    <h2 class="apv-main-approval-list-title" onclick="location.href='draftList.ap'">임시저장함</h2>
                </div>
                
                <!-- 결재 목록 테이블 -->
                <div class="apv-main-table-container">
                    <table class="apv-main-approval-table">
                        <thead>
                            <tr>
                                <th>저장일자</th>
                                <th>사용 양식</th>
                                <th>제목</th>
                                <th>상태</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${draftDocuments}">
                                <tr>
                                    <td><fmt:formatDate value="${doc.createDate}" pattern="yyyy-MM-dd"/></td>
                                    <td>${doc.formName}</td>
                                    <td><a href="updateDocument.ap?docNo=${doc.docNo}" class="apv-main-doc-title">${doc.title}</a></td>
                                    <td><span class="apv-main-status-badge apv-main-status-draft">임시저장</span></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty draftDocuments}">
                                <tr>
                                    <td colspan="4">
                                        <div class="apv-main-empty-state">
                                            <i class="far fa-file-alt"></i>
                                            <div class="apv-main-empty-state-text">임시저장한 문서가 없습니다.</div>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <!-- 완료 문서함 영역 -->
            <div class="apv-main-approval-list-container">
                <div class="apv-main-approval-list-header">
                    <h2 class="apv-main-approval-list-title" onclick="location.href='completedList.ap'">완료 문서함</h2>
                </div>
                
                <div class="apv-main-table-container">
                    <table class="apv-main-approval-table">
                        <thead>
                            <tr>
                                <th>문서번호</th>
                                <th>완료날짜</th>
                                <th>사용 양식</th>
                                <th>제목</th>
                                <th>기안자</th>
                                <th>결과</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${completedDocuments}">
                                <tr>
                                    <td>${doc.docNo}</td>
                                    <td><fmt:formatDate value="${doc.completedDate}" pattern="yyyy-MM-dd"/></td>
                                    <td>${doc.formName}</td>
                                    <td><a href="documentDetail.ap?docNo=${doc.docNo}" class="apv-main-doc-title">${doc.title}</a></td>
                                    <td>${doc.drafterName} / ${doc.drafterDeptName}</td>
                                    <td>
                                        <span class="apv-main-status-badge 
                                            <c:choose>
                                                <c:when test="${doc.docStatus == 'APPROVED'}">apv-main-status-approved</c:when>
                                                <c:when test="${doc.docStatus == 'REJECTED'}">apv-main-status-rejected</c:when>
                                            </c:choose>">
                                            <c:choose>
                                                <c:when test="${doc.docStatus == 'APPROVED'}">승인</c:when>
                                                <c:when test="${doc.docStatus == 'REJECTED'}">반려</c:when>
                                            </c:choose>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty completedDocuments}">
                                <tr>
                                    <td colspan="6">
                                        <div class="apv-main-empty-state">
                                            <i class="far fa-check-circle"></i>
                                            <div class="apv-main-empty-state-text">완료된 문서가 없습니다.</div>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- 결재 양식 선택 모달 -->
    <div class="apv-main-modal-overlay" id="apv-main-approvalFormModal">
        <div class="apv-main-modal-container">
            <div class="apv-main-modal-header">
                <h3 class="apv-main-modal-title">결재 양식 선택</h3>
                <button class="apv-main-modal-close" id="apv-main-closeFormModal"><i class="fas fa-times"></i></button>
            </div>
            <div class="apv-main-modal-body">
                <div style="display: flex; gap: 20px; height: 400px;">
                    <!-- 왼쪽 영역: 양식 목록 -->
                    <div style="flex: 1; border-right: 1px solid #eee; padding-right: 20px; overflow-y: auto;">
                        <div style="margin-bottom: 15px;">
                            <input type="text" placeholder="양식명 또는 설명으로 검색" style="width: 100%; padding: 8px; border-radius: 4px; border: 1px solid #ddd;" id="formSearchInput">
                        </div>
                        <ul style="list-style: none; padding: 0; margin: 0;" id="formListContainer">
                            <!-- 양식 목록은 AJAX로 동적으로 로드 -->
                        </ul>
                    </div>
                    
                    <!-- 오른쪽 영역: 선택한 양식 정보 -->
                    <div style="flex: 1.5; padding-left: 20px;" id="formPreviewContainer">
                        <p style="color: #999; text-align: center; margin-top: 50px;">양식을 선택해주세요.</p>
                    </div>
                </div>
            </div>
            <div class="apv-main-modal-footer">
                <button class="apv-main-btn apv-main-btn-outline" id="apv-main-cancelFormSelection">취소</button>
                <button class="apv-main-btn apv-main-btn-primary" id="apv-main-selectForm" disabled>선택</button>
            </div>
        </div>
    </div>

    <script>
    function showModal() {
        console.log("모달 표시 버튼 클릭됨");
        $('#apv-main-approvalFormModal').addClass('active');
        loadApprovalForms(); // 모달이 열릴 때 양식 목록 로드
        
        // 디버깅을 위해 모달의 가시성 상태 확인
        setTimeout(function() {
            console.log("모달 요소:", $('#apv-main-approvalFormModal'));
            console.log("모달 가시성:", $('#apv-main-approvalFormModal').css('visibility'));
            console.log("모달 opacity:", $('#apv-main-approvalFormModal').css('opacity'));
            console.log("active 클래스 포함 여부:", $('#apv-main-approvalFormModal').hasClass('active'));
        }, 100);
    }
    
    $(document).ready(function() {
        // 양식 선택 관련 변수
        let selectedFormNo = null;
        
     // DOM에서 사용자 데이터 읽어오기
        const userDataElement = document.getElementById('userDataContainer');
        const loginUserData = {
            empName: userDataElement.getAttribute('data-emp-name') || '사용자',
            deptName: userDataElement.getAttribute('data-dept-name') || '부서'
        };
        
        console.log("DOM에서 읽어온 사용자 데이터:", loginUserData);
        
        // 결재 작성 버튼 클릭 시 모달 표시 및 양식 로드
        $('#apv-main-createApprovalBtn, #apv-main-createApprovalBtn2').on('click', function() {
            loadApprovalForms();
            $('#apv-main-approvalFormModal').addClass('active');
        });
        
        // 모달 닫기
        $('#apv-main-closeFormModal, #apv-main-cancelFormSelection').on('click', function() {
            $('#apv-main-approvalFormModal').removeClass('active');
            resetFormSelection();
        });
        
        // 모달 외부 클릭 시 닫기
        $('#apv-main-approvalFormModal').on('click', function(e) {
            if (e.target === this) {
                $(this).removeClass('active');
                resetFormSelection();
            }
        });
        
        // 양식 선택 완료
        $('#apv-main-selectForm').on('click', function() {
            if (selectedFormNo) {            
                window.location.href = 'writeDocument.ap?formNo=' + selectedFormNo;
            } else {
                alert("양식을 선택해주세요.");
            }
        });
        
        // 검색 기능
        $('#formSearchInput').on('input', function() {
            const keyword = $(this).val().toLowerCase();
            $('#formListContainer li').each(function() {
                const formName = $(this).find('div:first').text().toLowerCase();
                const description = $(this).find('div:last').text().toLowerCase();
                
                if (formName.includes(keyword) || description.includes(keyword)) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        });
        
        // 통계 카드 클릭 이벤트
        $('.apv-main-stat-card').on('click', function() {
            const iconClass = $(this).find('.apv-main-stat-card-icon');
            if (iconClass.hasClass('waiting')) {
                window.location.href = 'waitingList.ap';
            } else if (iconClass.hasClass('processing')) {
                window.location.href = 'requestedList.ap?fromDashboard=pending';
            } else if (iconClass.hasClass('approved') || iconClass.hasClass('rejected')) {
                window.location.href = 'completedList.ap';
            }
        });
        
        // 문서함 제목 클릭 이벤트
        $('.apv-main-approval-list-title').on('click', function() {
            const titleText = $(this).text().trim();
            if (titleText.includes('수신 문서함')) {
                window.location.href = 'waitingList.ap';
            } else if (titleText.includes('내가 올린 문서함')) {
                window.location.href = 'requestedList.ap';
            } else if (titleText.includes('임시저장함')) {
                window.location.href = 'draftList.ap';
            } else if (titleText.includes('완료 문서함')) {
                window.location.href = 'completedList.ap';
            }
        });
        
        // 양식 목록 로드
        function loadApprovalForms() {
            $.ajax({
                url: 'formList.ap',
                type: 'GET',
                dataType: 'json',
                success: function(data) {
                    console.log("서버에서 받은 데이터:", data);
                    if (data && Array.isArray(data)) {
                        displayFormList(data);
                    } else {
                        console.error("비정상 응답:", data);
                        alert('양식 목록을 불러오는 중 오류가 발생했습니다.');
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Ajax 오류:", xhr.status, error);
                    console.error("응답 텍스트:", xhr.responseText);
                    alert('양식 목록을 불러오는 중 오류가 발생했습니다.');
                }
            });
        }
        
        // 양식 목록 표시
        function displayFormList(forms) {
            const container = $('#formListContainer');
            if (container.length === 0) return;
            
            container.empty();
            
            if (!Array.isArray(forms) || forms.length === 0) {
                container.html('<li>사용 가능한 양식이 없습니다.</li>');
                return;
            }
            
            forms.forEach(function(form) {
                const li = $('<li>')
                    .css({
                        'padding': '12px',
                        'border-radius': '4px',
                        'margin-bottom': '8px',
                        'cursor': 'pointer',
                        'border': '1px solid #eee'
                    })
                    .attr('data-form-no', form.formNo);
                
                const nameDiv = $('<div>')
                    .css({
                        'font-weight': '500',
                        'color': '#333',
                        'margin-bottom': '5px'
                    })
                    .text(form.formName || '이름 없음');
                
                const descDiv = $('<div>')
                    .css({
                        'font-size': '12px',
                        'color': '#777'
                    })
                    .text(form.description || '설명 없음');
                
                li.append(nameDiv, descDiv);
                
                // 클릭 이벤트 추가
                li.on('click', function() {
                    selectForm(form.formNo, this);
                });
                
                container.append(li);
            });
        }
        
        // 양식 선택
        function selectForm(formNo, element) {
            // 이전 선택 해제
            $('#formListContainer li').css({
                'background-color': '',
                'border-left-width': '1px',
                'border-left-color': '#eee'
            });
            
            // 현재 선택 표시
            $(element).css({
                'background-color': '#f0f5ff',
                'border-left-width': '3px',
                'border-left-color': '#003561'
            });
            
            selectedFormNo = formNo;
            
            // 선택 버튼 활성화
            $('#apv-main-selectForm').prop('disabled', false);
            
            // 양식 미리보기 로드
            loadFormPreview(formNo);
        }
        
        // 양식 미리보기 로드
        function loadFormPreview(formNo) {
            $.ajax({
                url: 'formDetail.ap',
                type: 'GET',
                data: { formNo: formNo },
                dataType: 'json',
                success: function(data) {
                    console.log("양식 상세 데이터:", data);
                    displayFormPreview(data);
                },
                error: function(xhr, status, error) {
                    console.error("양식 상세 오류:", status, error);
                    alert('양식 정보를 불러오는 중 오류가 발생했습니다.');
                }
            });
        }
        
     // 양식 미리보기 표시 - 전통적인 문자열 연결 방식으로 수정
        function displayFormPreview(form) {
            const container = $('#formPreviewContainer');
            if (container.length === 0) {
                console.log("formPreviewContainer 요소를 찾을 수 없음");
                return;
            }
            
            console.log("HTML 삽입 전 #formPreviewContainer 내용:", container.html());
            
            // 데이터 확인
            if (!form || typeof form !== 'object') {
                container.html('<p>양식 정보를 불러올 수 없습니다.</p>');
                return;
            }
            
            // 로그인 사용자 정보
			const userDataElement = document.getElementById('userDataContainer');
			console.log("userDataElement:", userDataElement);
			console.log("data-emp-name 속성:", userDataElement.getAttribute('data-emp-name'));
			console.log("data-dept-name 속성:", userDataElement.getAttribute('data-dept-name'));
			
			const loginUser = {
			    empName: userDataElement.getAttribute('data-emp-name') || '사용자',
			    deptName: userDataElement.getAttribute('data-dept-name') || '부서'
			};
			            
            console.log("폼 미리보기에 사용될 사용자 정보:", loginUser);
            
            // 하드코딩으로 테스트
            const formTitle = form.formName || '양식 이름 없음';
            const formDescription = form.description || '설명이 없습니다.';
            
            // 전통적인 문자열 연결 방식
            var html = '';
            html += '<h4 style="margin-top: 0; margin-bottom: 20px; color: #333;">' + formTitle + '</h4>';
            html += '<div style="background-color: #f9fafb; border-radius: 8px; padding: 20px; margin-bottom: 20px;">';
            html += '    <div style="margin-bottom: 15px;">';
            html += '        <div style="font-size: 13px; color: #777; margin-bottom: 5px;">양식 설명</div>';
            html += '        <div style="font-size: 14px; color: #333;">' + formDescription + '</div>';
            html += '    </div>';
            html += '    <div style="margin-bottom: 15px;">';
            html += '        <div style="font-size: 13px; color: #777; margin-bottom: 5px;">기안자</div>';
            html += '        <div style="font-size: 14px; color: #333;">' + loginUser.empName + '</div>';
            html += '    </div>';
            html += '    <div>';
            html += '        <div style="font-size: 13px; color: #777; margin-bottom: 5px;">기안자 부서</div>';
            html += '        <div style="font-size: 14px; color: #333;">' + loginUser.deptName + '</div>';
            html += '    </div>';
            html += '</div>';
            
            container.html(html);
            
            console.log("HTML 삽입 후 #formPreviewContainer 내용:", container.html());
            
            // 추가 확인: 특정 요소에 직접 접근해서 텍스트 확인
            setTimeout(function() {
                console.log("0.5초 후 #formPreviewContainer의 h4 내용:", 
                    $('#formPreviewContainer h4').text());
            }, 500);
        }
        
        // 선택 초기화
        function resetFormSelection() {
            console.log("resetFormSelection 함수 호출됨");
            selectedFormNo = null;
            $('#apv-main-selectForm').prop('disabled', true);
            $('#formPreviewContainer').html('<p style="color: #999; text-align: center; margin-top: 50px;">양식을 선택해주세요.</p>');
            $('#formSearchInput').val('');
        }
    });
    </script>
</body>
</html>