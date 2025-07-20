<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>임시저장함 | Flokr</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalDraftList.css">
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />

    <div class="apdraft-container">
        <div class="apdraft-header">
            <h1 class="apdraft-title">
                <i class="fas fa-folder-open"></i>
                임시저장함
            </h1>
            <p class="apdraft-subtitle">
                작성 중이거나 아직 상신하지 않은 문서 목록입니다. 
                <span class="apdraft-count">총 ${pageInfo.listCount}건</span>
            </p>
            
            <div class="apdraft-actions">
                <form class="apdraft-search-form" method="get" action="searchDocuments.ap" onsubmit="return validateSearchForm();">
                    <input type="hidden" name="boxType" value="draft">
                    <select name="searchType" class="apdraft-search-select">
                        <option value="">전체</option>
                        <option value="title" ${param.searchType eq 'title' ? 'selected' : ''}>제목</option>
                        <option value="form" ${param.searchType eq 'form' ? 'selected' : ''}>양식</option>
                    </select>
                    <input type="text" name="keyword" class="apdraft-search-input" 
                           placeholder="검색어를 입력하세요" value="${keyword}">
                    <button type="submit" class="apdraft-search-btn">
                        <i class="fas fa-search"></i> 검색
                    </button>
                </form>
            </div>
        </div>
        
        <div class="apdraft-table-container">
            <table class="apdraft-table">
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>양식</th>
                        <th>제목</th>
                        <th>저장일시</th>
                        <th>상태</th>
                        <th>작업</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="doc" items="${documentList}" varStatus="status">
                        <tr>
                            <td>${pageInfo.listCount - ((pageInfo.currentPage - 1) * pageInfo.boardLimit + status.index)}</td>
                            <td>${doc.formName}</td>
                            <td>
                                <a href="updateDocument.ap?docNo=${doc.docNo}" class="apdraft-doc-title">
                                    ${doc.title}
                                </a>
                            </td>
                            <td>
                                <fmt:formatDate value="${doc.updateDate}" pattern="yyyy-MM-dd HH:mm"/>
                            </td>
                            <td>
                                <span class="apdraft-status-badge apdraft-status-draft">
                                    <c:choose>
                                        <c:when test="${doc.docStatus == 'DRAFT'}">임시저장</c:when>
                                        <c:when test="${doc.docStatus == 'REJECTED'}">반려</c:when>
                                    </c:choose>
                                </span>
                            </td>
                            <td class="apdraft-actions-cell">
                                <button class="apdraft-action-btn apdraft-action-edit" 
                                        onclick="editDocument(${doc.docNo})" title="수정">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <!-- 결재요청 버튼 추가 - 임시저장함에서 바로 결재요청으로 전환할 수 있는 버튼 -->
                                <button class="apdraft-action-btn apdraft-action-submit" 
                                        onclick="submitDocument(${doc.docNo})" title="결재요청">
                                    <i class="fas fa-paper-plane"></i>
                                </button>
                                <button class="apdraft-action-btn apdraft-action-delete" 
                                        onclick="deleteDocument(${doc.docNo})" title="삭제">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty documentList}">
                        <tr>
                            <td colspan="6">
                                <div class="apdraft-empty">
                                    <i class="far fa-folder-open"></i>
                                    <p>저장된 문서가 없습니다.</p>

                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        
        <c:if test="${pageInfo.listCount > 0}">
		    <div class="apdraft-pagination">
		        <c:if test="${pageInfo.currentPage <= 1}">
		            <a href="#" class="apdraft-page-link disabled">
		                <i class="fas fa-angle-double-left"></i>
		            </a>
		            <a href="#" class="apdraft-page-link disabled">
		                <i class="fas fa-angle-left"></i>
		            </a>
		        </c:if>
		        <c:if test="${pageInfo.currentPage > 1}">
		            <a href="searchDocuments.ap?boxType=draft&page=1&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apdraft-page-link">
		                <i class="fas fa-angle-double-left"></i>
		            </a>
		            <a href="searchDocuments.ap?boxType=draft&page=${pageInfo.currentPage - 1}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apdraft-page-link">
		                <i class="fas fa-angle-left"></i>
		            </a>
		        </c:if>
		        
		        <c:forEach var="p" begin="${pageInfo.startPage}" end="${pageInfo.endPage}">
		            <a href="searchDocuments.ap?boxType=draft&page=${p}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" 
		               class="apdraft-page-link ${p == pageInfo.currentPage ? 'active' : ''}">${p}</a>
		        </c:forEach>
		        
		        <c:if test="${pageInfo.currentPage >= pageInfo.maxPage}">
		            <a href="#" class="apdraft-page-link disabled">
		                <i class="fas fa-angle-right"></i>
		            </a>
		            <a href="#" class="apdraft-page-link disabled">
		                <i class="fas fa-angle-double-right"></i>
		            </a>
		        </c:if>
		        <c:if test="${pageInfo.currentPage < pageInfo.maxPage}">
		            <a href="searchDocuments.ap?boxType=draft&page=${pageInfo.currentPage + 1}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apdraft-page-link">
		                <i class="fas fa-angle-right"></i>
		            </a>
		            <a href="searchDocuments.ap?boxType=draft&page=${pageInfo.maxPage}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apdraft-page-link">
		                <i class="fas fa-angle-double-right"></i>
		            </a>
		        </c:if>
		    </div>
		    <div class="apdraft-page-info">
		        전체 ${pageInfo.listCount}건 중 ${pageInfo.currentPage}/${pageInfo.maxPage} 페이지
		    </div>
		</c:if>
    </div>
    
    <script>
	    function editDocument(docNo) {
	        // 기존 함수 유지
	        window.location.href = 'updateDocument.ap?docNo=' + docNo;
	    }
	    
	    function deleteDocument(docNo) {
	        // 기존 함수 유지
	        if (confirm('정말 삭제하시겠습니까?\n삭제된 문서는 복구할 수 없습니다.')) {
	            const form = document.createElement('form');
	            form.method = 'POST';
	            form.action = 'deleteDocument.ap';
	            
	            const input = document.createElement('input');
	            input.type = 'hidden';
	            input.name = 'docNo';
	            input.value = docNo;
	            
	            form.appendChild(input);
	            document.body.appendChild(form);
	            form.submit();
	        }
	    }
	    
	    function submitDocument(docNo) {
	        // 기존 함수 유지
	        if (confirm('이 문서를 결재 요청하시겠습니까?\n결재선이 설정되지 않은 경우 문서 편집 화면으로 이동합니다.')) {
	            // 먼저 결재선 설정 여부 확인을 위해 문서 정보를 가져옴
	            fetch('checkApprovalLine.ap?docNo=' + docNo)
	                .then(response => response.json())
	                .then(data => {
	                    if (data.hasApprovalLine) {
	                        // 결재선이 있으면 바로 결재요청 처리
	                        const form = document.createElement('form');
	                        form.method = 'POST';
	                        form.action = 'submitDraft.ap';
	                        
	                        const input = document.createElement('input');
	                        input.type = 'hidden';
	                        input.name = 'docNo';
	                        input.value = docNo;
	                        
	                        form.appendChild(input);
	                        document.body.appendChild(form);
	                        form.submit();
	                    } else {
	                        // 결재선이 없으면 수정 화면으로 이동
	                        alert('결재선이 설정되지 않았습니다. 문서 편집 화면으로 이동합니다.');
	                        window.location.href = 'updateDocument.ap?docNo=' + docNo;
	                    }
	                })
	                .catch(error => {
	                    console.error('Error:', error);
	                    alert('문서 정보를 확인하는 중 오류가 발생했습니다. 문서 편집 화면으로 이동합니다.');
	                    window.location.href = 'updateDocument.ap?docNo=' + docNo;
	                });
	        }
	    }
	    
	    // 검색폼 유효성 검사 (새로 추가)
	    function validateSearchForm() {
	        const searchType = document.querySelector('select[name="searchType"]').value;
	        const keyword = document.querySelector('input[name="keyword"]').value.trim();
	        
	        // 전체 선택 시에는 키워드가 있어도 되고 없어도 됨
	        if (searchType === '') {
	            return true;
	        }
	        
	        // 검색 타입이 선택되었는데 키워드가 없는 경우
	        if (searchType && !keyword) {
	            alert('검색어를 입력해주세요.');
	            return false;
	        }
	        
	        return true;
	    }
	</script>
</body>
</html>