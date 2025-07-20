<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>상신 문서함 | Flokr</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalRequestedList.css">
</head>
<body>
   <!-- header.jsp include -->
   <jsp:include page="../common/header.jsp" />

   <div class="aprequest-container">
       <div class="aprequest-header">
           <h1 class="aprequest-title">
               <i class="fas fa-paper-plane"></i>
               상신 문서함
           </h1>
           <p class="aprequest-subtitle">
               내가 결재 요청한 문서 목록입니다. 
               <span class="aprequest-count">총 ${pageInfo.listCount}건</span>
           </p>
           
           <div class="aprequest-actions">
               <form class="aprequest-search-form" method="get" action="searchDocuments.ap" onsubmit="return validateSearchForm();">
			    <input type="hidden" name="boxType" value="requested">
			    <select name="searchType" class="aprequest-search-select">
			        <option value="">전체</option>
			        <option value="title" ${searchType eq 'title' ? 'selected' : ''}>제목</option>
			        <option value="form" ${searchType eq 'form' ? 'selected' : ''}>양식</option>
			    </select>
			    <input type="text" name="keyword" class="aprequest-search-input" 
			           placeholder="검색어를 입력하세요" value="${keyword}">
			    <button type="submit" class="aprequest-search-btn">
			        <i class="fas fa-search"></i> 검색
			    </button>
			</form>
           </div>
           
           <div class="aprequest-filter-section">
			    <span class="aprequest-filter-label">상태별 필터:</span>
			    <select name="statusFilter" class="aprequest-filter-select" onchange="filterByStatus(this.value)">
			        <option value="" ${empty statusFilter ? 'selected' : ''}>전체</option>
			        <option value="REQUESTED" ${statusFilter eq 'REQUESTED' ? 'selected' : ''}>진행중</option>
			        <option value="APPROVED" ${statusFilter eq 'APPROVED' ? 'selected' : ''}>승인</option>
			        <option value="REJECTED" ${statusFilter eq 'REJECTED' ? 'selected' : ''}>반려</option>
			    </select>
			</div>
       </div>
       
       <div class="aprequest-table-container">
           <table class="aprequest-table">
               <thead>
                   <tr>
                       <th>번호</th>
                       <th>양식</th>
                       <th>제목</th>
                       <th>상신일시</th>
                       <th>결재 진행</th>
                       <th>상태</th>
                       <th>결과</th>
                   </tr>
               </thead>
               <tbody>
                   <c:forEach var="doc" items="${documentList}" varStatus="status">
                       <tr>
                           <td>${pageInfo.listCount - ((pageInfo.currentPage - 1) * pageInfo.boardLimit + status.index)}</td>
                           <td>${doc.formName}</td>
                           <td>
                               <a href="documentDetail.ap?docNo=${doc.docNo}" class="aprequest-doc-title">
                                   ${doc.title}
                               </a>
                           </td>
                           <td>
                               <fmt:formatDate value="${doc.requestedDate}" pattern="yyyy-MM-dd HH:mm"/>
                           </td>
                           <td>
                               <div class="aprequest-progress">
                                   <div class="aprequest-progress-bar">
                                       <div class="aprequest-progress-fill" 
                                            style="width: ${doc.docStatus == 'APPROVED' ? '100' : (doc.docStatus == 'REJECTED' ? '100' : '50')}%"></div>
                                   </div>
                                   <span>
                                   		<c:choose>
									        <c:when test="${doc.docStatus == 'APPROVED'}">완료</c:when>
									        <c:when test="${doc.docStatus == 'REJECTED'}">반려</c:when>
									        <c:otherwise>진행중</c:otherwise>
									    </c:choose>
                                   </span>
                               </div>
                           </td>
                           <td>
                               <span class="aprequest-status-badge 
                                   <c:choose>
                                       <c:when test="${doc.docStatus == 'REQUESTED'}">aprequest-status-requested</c:when>
                                       <c:when test="${doc.docStatus == 'APPROVED'}">aprequest-status-approved</c:when>
                                       <c:when test="${doc.docStatus == 'REJECTED'}">aprequest-status-rejected</c:when>
                                   </c:choose>">
                                   <c:choose>
                                       <c:when test="${doc.docStatus == 'REQUESTED'}">결재진행중</c:when>
                                       <c:when test="${doc.docStatus == 'APPROVED'}">결재완료</c:when>
                                       <c:when test="${doc.docStatus == 'REJECTED'}">결재반려</c:when>
                                   </c:choose>
                               </span>
                           </td>
                           <td>
                               <c:choose>
                                   <c:when test="${doc.docStatus == 'APPROVED'}">
                                       <fmt:formatDate value="${doc.completedDate}" pattern="MM-dd HH:mm"/>
                                   </c:when>
                                   <c:when test="${doc.docStatus == 'REJECTED'}">
                                       <span style="color: #f44336;">
                                           반려됨
                                       </span>
                                   </c:when>
                                   <c:otherwise>
                                       <span style="color: #666;">
                                           처리 대기중
                                       </span>
                                   </c:otherwise>
                               </c:choose>
                           </td>
                       </tr>
                   </c:forEach>
                   <c:if test="${empty documentList}">
                       <tr>
                           <td colspan="7">
                               <div class="aprequest-empty">
                                   <i class="far fa-paper-plane"></i>
                                   <p>상신한 문서가 없습니다.</p>
                               </div>
                           </td>
                       </tr>
                   </c:if>
               </tbody>
           </table>
       </div>
       
       <c:if test="${pageInfo.listCount > 0}">
           <div class="aprequest-pagination">
               <c:if test="${pageInfo.currentPage <= 1}">
                   <a href="#" class="aprequest-page-link disabled">
                       <i class="fas fa-angle-double-left"></i>
                   </a>
                   <a href="#" class="aprequest-page-link disabled">
                       <i class="fas fa-angle-left"></i>
                   </a>
               </c:if>
               <c:if test="${pageInfo.currentPage > 1}">
                   <a href="requestedList.ap?page=1" class="aprequest-page-link">
                       <i class="fas fa-angle-double-left"></i>
                   </a>
                   <a href="requestedList.ap?page=${pageInfo.currentPage - 1}" class="aprequest-page-link">
                       <i class="fas fa-angle-left"></i>
                   </a>
               </c:if>
               
               <c:forEach var="p" begin="${pageInfo.startPage}" end="${pageInfo.endPage}">
                   <a href="requestedList.ap?page=${p}" 
                      class="aprequest-page-link ${p == pageInfo.currentPage ? 'active' : ''}">${p}</a>
               </c:forEach>
               
               <c:if test="${pageInfo.currentPage >= pageInfo.maxPage}">
                   <a href="#" class="aprequest-page-link disabled">
                       <i class="fas fa-angle-right"></i>
                   </a>
                   <a href="#" class="aprequest-page-link disabled">
                       <i class="fas fa-angle-double-right"></i>
                   </a>
               </c:if>
               <c:if test="${pageInfo.currentPage < pageInfo.maxPage}">
                   <a href="requestedList.ap?page=${pageInfo.currentPage + 1}" class="aprequest-page-link">
                       <i class="fas fa-angle-right"></i>
                   </a>
                   <a href="requestedList.ap?page=${pageInfo.maxPage}" class="aprequest-page-link">
                       <i class="fas fa-angle-double-right"></i>
                   </a>
               </c:if>
           </div>
           <div class="aprequest-page-info">
               전체 ${pageInfo.listCount}건 중 ${pageInfo.currentPage}/${pageInfo.maxPage} 페이지
           </div>
       </c:if>
   </div>
   
   <script>
	    // 상태 필터링 함수
	    function filterByStatus(status) {
	        // URL 파라미터 객체 생성
	        let searchParams = new URLSearchParams(window.location.search);
	        
	     	// 전체 필터를 선택했거나 상태가 없으면 fromDashboard 파라미터 제거
	        if (!status || status === "") {
	            searchParams.delete('statusFilter');
	            searchParams.delete('fromDashboard'); // 중요: 전체 필터 선택 시 fromDashboard 제거
	        } else {
	            searchParams.set('statusFilter', status);
	            // 상태가 명시적으로 지정된 경우에도 fromDashboard 제거
	            searchParams.delete('fromDashboard');
	        }
	        
	        // 현재 페이지 초기화
	        searchParams.set('page', '1');
	        
	        // 리다이렉트
	        window.location.href = 'requestedList.ap?' + searchParams.toString();
	    }
	    
	    // 검색폼 유효성 검사
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