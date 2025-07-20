<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>완료 문서함 | Flokr</title>    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalCompletedList.css">
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />

    <div class="apcomplete-container">
        <div class="apcomplete-header">
            <h1 class="apcomplete-title">
                <i class="fas fa-check-circle"></i>
                완료 문서함
            </h1>
            <p class="apcomplete-subtitle">
                승인 또는 반려 완료된 문서 목록입니다. 
                <span class="apcomplete-count">총 ${pageInfo.listCount}건</span>
            </p>
            
            <div class="apcomplete-stats">
                <div class="apcomplete-stat-card">
                    <div class="apcomplete-stat-label">총 처리</div>
                    <div class="apcomplete-stat-value apcomplete-stat-total">${pageInfo.listCount}</div>
                </div>
                <div class="apcomplete-stat-card">
                    <div class="apcomplete-stat-label">승인 완료</div>
                    <div class="apcomplete-stat-value apcomplete-stat-approved">${empty stats ? 0 : (empty stats.approvedCount ? 0 : stats.approvedCount)}</div>
                </div>
                <div class="apcomplete-stat-card">
                    <div class="apcomplete-stat-label">반려</div>
                    <div class="apcomplete-stat-value apcomplete-stat-rejected">${empty stats ? 0 : (empty stats.rejectedCount ? 0 : stats.rejectedCount)}</div>
                </div>
                <!-- 평균 처리 시간 카드를 처리 효율성 카드로 변경 -->
			    <div class="apcomplete-stat-card">
			        <div class="apcomplete-stat-label apcomplete-tooltip">처리 효율성
				        <span class="apcomplete-tooltip-text">
				            <strong>신속 처리</strong>: 24시간 이내 완료된 문서 비율<br>
				            <strong>정상 처리</strong>: 1-3일 내 완료된 문서 비율<br>
				            <strong>지연 처리</strong>: 3일 이상 소요된 문서 비율<br>
				            현재 가장 높은 비율의 처리 상태를 보여줍니다.
				        </span>
				    </div>
			        <div class="apcomplete-stat-value ${stats.processingEfficiency.status eq '신속 처리' ? 'apcomplete-stat-fast' : 
			                (stats.processingEfficiency.status eq '정상 처리' ? 'apcomplete-stat-normal' : 
			                 'apcomplete-stat-slow')}">
			            ${empty stats ? '-' : (empty stats.processingEfficiency ? '-' : stats.processingEfficiency.status)}
			            <c:if test="${not empty stats and not empty stats.processingEfficiency and not empty stats.processingEfficiency.percentage}">
			                <span class="apcomplete-stat-percentage">${stats.processingEfficiency.percentage}%</span>
			            </c:if>
			        </div>
			    </div>
            </div>
            
            <div class="apcomplete-actions">
                <form class="apcomplete-search-form" method="get" action="searchDocuments.ap" onsubmit="return validateSearchForm();">
			        <input type="hidden" name="boxType" value="completed">
			        
			        <!-- 날짜 범위 -->
			        <div class="apcomplete-date-range">
			            <input type="date" name="dateFrom" class="apcomplete-date-input" value="${dateFrom}">
			            <span>~</span>
			            <input type="date" name="dateTo" class="apcomplete-date-input" value="${dateTo}">
			        </div>
			        
			        <!-- 검색 필드 -->
			        <div class="apcomplete-search-container">
			            <select name="searchType" class="apcomplete-search-select">
			                <option value="">전체</option>
			                <option value="title" ${searchType eq 'title' ? 'selected' : ''}>제목</option>
			                <option value="drafter" ${searchType eq 'drafter' ? 'selected' : ''}>기안자</option>
			                <option value="form" ${searchType eq 'form' ? 'selected' : ''}>양식</option>
			            </select>
			            <input type="text" name="keyword" class="apcomplete-search-input" 
			                   placeholder="검색어를 입력하세요" value="${keyword}">
			            <button type="submit" class="apcomplete-search-btn">
			                <i class="fas fa-search"></i> 검색
			            </button>
			        </div>
			    </form>
            </div>
            
            <div class="apcomplete-filter-section">
    <span class="apcomplete-filter-label">상태별 필터:</span>
    <!-- ❤️ 필터 선택 상태 유지를 위한 selected 속성 추가 -->
    <select name="statusFilter" class="apcomplete-filter-select" onchange="filterByStatus(this.value)">
        <option value="">전체</option>
        <option value="APPROVED" ${statusFilter eq 'APPROVED' ? 'selected' : ''}>승인</option>
        <option value="REJECTED" ${statusFilter eq 'REJECTED' ? 'selected' : ''}>반려</option>
    </select>
    <span class="apcomplete-filter-label">기간:</span>
    <!-- ❤️ 기간 필터 선택 상태 유지를 위한 selected 속성 추가 -->
    <select name="periodFilter" class="apcomplete-filter-select" onchange="filterByPeriod(this.value)">
        <option value="">전체</option>
        <option value="today" ${periodFilter eq 'today' ? 'selected' : ''}>오늘</option>
        <option value="week" ${periodFilter eq 'week' ? 'selected' : ''}>1주일</option>
        <option value="month" ${periodFilter eq 'month' ? 'selected' : ''}>1개월</option>
        <option value="3month" ${periodFilter eq '3month' ? 'selected' : ''}>3개월</option>
    </select>
</div>
        </div>
        
        <div class="apcomplete-table-container">
            <table class="apcomplete-table">
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>양식</th>
                        <th>제목</th>
                        <th>기안자</th>
                        <th>상신일</th>
                        <th>완료일</th>
                        <th>처리 시간</th>
                        <th>결과</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="doc" items="${documentList}" varStatus="status">
                        <tr>
                            <td>${pageInfo.listCount - ((pageInfo.currentPage - 1) * pageInfo.boardLimit + status.index)}</td>
                            <td>${doc.formName}</td>
                            <td>
                                <a href="documentDetail.ap?docNo=${doc.docNo}" class="apcomplete-doc-title">
                                    ${doc.title}
                                </a>
                            </td>
                            <td>
                                <div class="apcomplete-drafter">
                                    <span class="apcomplete-drafter-name">${doc.drafterName}</span>
                                    <span class="apcomplete-drafter-dept">${doc.drafterDeptName}</span>
                                </div>
                            </td>
                            <td>
                                <fmt:formatDate value="${doc.requestedDate}" pattern="MM-dd HH:mm"/>
                            </td>
                            <td>
                                <fmt:formatDate value="${doc.completedDate}" pattern="MM-dd HH:mm"/>
                            </td>
                            <td>
                                <c:choose>
							        <c:when test="${doc.completedDate != null && doc.requestedDate != null}">
							            <c:set var="processMilli" value="${doc.completedDate.time - doc.requestedDate.time}" />
							            <c:set var="processHours" value="${processMilli / (1000 * 60 * 60)}" />
							            <span class="apcomplete-duration 
							                <c:choose>
							                    <c:when test="${processHours <= 2}">apcomplete-fast</c:when>
							                    <c:when test="${processHours >= 24}">apcomplete-slow</c:when>
							                </c:choose>">
							                <c:choose>
							                    <c:when test="${processHours < 1}">1시간 이내</c:when>
							                    <c:when test="${processHours < 24}">약 ${Math.round(processHours)}시간</c:when>
							                    <c:otherwise>약 ${Math.round(processHours/24)}일</c:otherwise>
							                </c:choose>
							            </span>
							        </c:when>
							        <c:otherwise>
							            <span class="apcomplete-duration">-</span>
							        </c:otherwise>
							    </c:choose>
                            </td>
                            <td>
                                <span class="apcomplete-status-badge 
                                    <c:choose>
                                        <c:when test="${doc.docStatus == 'APPROVED'}">apcomplete-status-approved</c:when>
                                        <c:when test="${doc.docStatus == 'REJECTED'}">apcomplete-status-rejected</c:when>
                                    </c:choose>">
                                    <c:choose>
                                        <c:when test="${doc.docStatus == 'APPROVED'}">승인</c:when>
                                        <c:when test="${doc.docStatus == 'REJECTED'}">반려</c:when>
                                    </c:choose>
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty documentList}">
                        <tr>
                            <td colspan="8">
                                <div class="apcomplete-empty">
                                    <i class="far fa-check-circle"></i>
                                    <p>완료된 문서가 없습니다.</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        
        <c:if test="${pageInfo.listCount > 0}">
    <div class="apcomplete-pagination">
        <c:if test="${pageInfo.currentPage <= 1}">
            <a href="#" class="apcomplete-page-link disabled">
                <i class="fas fa-angle-double-left"></i>
            </a>
            <a href="#" class="apcomplete-page-link disabled">
                <i class="fas fa-angle-left"></i>
            </a>
        </c:if>
        <c:if test="${pageInfo.currentPage > 1}">
            <!-- ❤️ 페이지네이션 링크에 필터 파라미터 추가 -->
            <a href="completedList.ap?page=1${statusFilter != null ? '&statusFilter='.concat(statusFilter) : ''}${periodFilter != null ? '&periodFilter='.concat(periodFilter) : ''}${dateFrom != null ? '&dateFrom='.concat(dateFrom) : ''}${dateTo != null ? '&dateTo='.concat(dateTo) : ''}" class="apcomplete-page-link">
                <i class="fas fa-angle-double-left"></i>
            </a>
            <a href="completedList.ap?page=${pageInfo.currentPage - 1}${statusFilter != null ? '&statusFilter='.concat(statusFilter) : ''}${periodFilter != null ? '&periodFilter='.concat(periodFilter) : ''}${dateFrom != null ? '&dateFrom='.concat(dateFrom) : ''}${dateTo != null ? '&dateTo='.concat(dateTo) : ''}" class="apcomplete-page-link">
                <i class="fas fa-angle-left"></i>
            </a>
        </c:if>
        
        <c:forEach var="p" begin="${pageInfo.startPage}" end="${pageInfo.endPage}">
            <!-- ❤️ 페이지 번호 링크에 필터 파라미터 추가 -->
            <a href="completedList.ap?page=${p}${statusFilter != null ? '&statusFilter='.concat(statusFilter) : ''}${periodFilter != null ? '&periodFilter='.concat(periodFilter) : ''}${dateFrom != null ? '&dateFrom='.concat(dateFrom) : ''}${dateTo != null ? '&dateTo='.concat(dateTo) : ''}" 
               class="apcomplete-page-link ${p == pageInfo.currentPage ? 'active' : ''}">${p}</a>
        </c:forEach>
        
        <c:if test="${pageInfo.currentPage >= pageInfo.maxPage}">
            <a href="#" class="apcomplete-page-link disabled">
                <i class="fas fa-angle-right"></i>
            </a>
            <a href="#" class="apcomplete-page-link disabled">
                <i class="fas fa-angle-double-right"></i>
            </a>
        </c:if>
        <c:if test="${pageInfo.currentPage < pageInfo.maxPage}">
            <!-- ❤️ 페이지네이션 링크에 필터 파라미터 추가 -->
            <a href="completedList.ap?page=${pageInfo.currentPage + 1}${statusFilter != null ? '&statusFilter='.concat(statusFilter) : ''}${periodFilter != null ? '&periodFilter='.concat(periodFilter) : ''}${dateFrom != null ? '&dateFrom='.concat(dateFrom) : ''}${dateTo != null ? '&dateTo='.concat(dateTo) : ''}" class="apcomplete-page-link">
                <i class="fas fa-angle-right"></i>
            </a>
            <a href="completedList.ap?page=${pageInfo.maxPage}${statusFilter != null ? '&statusFilter='.concat(statusFilter) : ''}${periodFilter != null ? '&periodFilter='.concat(periodFilter) : ''}${dateFrom != null ? '&dateFrom='.concat(dateFrom) : ''}${dateTo != null ? '&dateTo='.concat(dateTo) : ''}" class="apcomplete-page-link">
                <i class="fas fa-angle-double-right"></i>
            </a>
        </c:if>
    </div>
    <div class="apcomplete-page-info">
        전체 ${pageInfo.listCount}건 중 ${pageInfo.currentPage}/${pageInfo.maxPage} 페이지
    </div>
</c:if>
    </div>
    
    <script>
    function filterByStatus(status) {
        // URL 파라미터 객체 생성
        let searchParams = new URLSearchParams(window.location.search);
        
        // 상태 필터 설정 (빈 값이면 파라미터 제거)
        if (status) {
            searchParams.set('statusFilter', status);
        } else {
            searchParams.delete('statusFilter');
        }
        
        // 현재 페이지 초기화
        searchParams.set('page', '1');
        
        // 리다이렉트
        window.location.href = 'completedList.ap?' + searchParams.toString();
    }
    
    // 기간별 필터 함수 수정
    function filterByPeriod(period) {
        // URL 파라미터 객체 생성
        let searchParams = new URLSearchParams(window.location.search);
        
        // 기간 필터 설정
        if (period) {
            searchParams.set('periodFilter', period);
        } else {
            // 기간 필터가 없으면 관련 파라미터 모두 제거
            searchParams.delete('periodFilter');
            searchParams.delete('dateFrom');
            searchParams.delete('dateTo');
        }
        
        // 현재 페이지 초기화
        searchParams.set('page', '1');
        
        // 리다이렉트
        window.location.href = 'completedList.ap?' + searchParams.toString();
    }
	    
	    function updateUrl(params) {
	        const urlParams = new URLSearchParams(window.location.search);
	        
	        Object.keys(params).forEach(key => {
	            if (params[key]) {
	                urlParams.set(key, params[key]);
	            } else {
	                urlParams.delete(key);
	            }
	        });
	        
	        window.location.href = 'completedList.ap?' + urlParams.toString();
	    }
	    
	    // 검색폼 유효성 검사
	    function validateSearchForm() {
	        const searchType = document.querySelector('select[name="searchType"]').value;
	        const keyword = document.querySelector('input[name="keyword"]').value.trim();
	        const dateFrom = document.querySelector('input[name="dateFrom"]').value;
	        const dateTo = document.querySelector('input[name="dateTo"]').value;
	        
	        // 검색어가 없어도 날짜 범위가 있으면 검색 허용
	        if (dateFrom || dateTo) {
	            return true;
	        }
	        
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