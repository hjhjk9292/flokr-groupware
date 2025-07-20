<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>수신 문서함 | Flokr</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalWaitingList.css">
</head>
<body>
    <!-- header.jsp include -->
    <jsp:include page="../common/header.jsp" />

    <div class="apwaiting-container">
        <div class="apwaiting-header">
            <h1 class="apwaiting-title">
                <i class="fas fa-inbox"></i>
                수신 문서함
                <span class="apwaiting-title-badge">${urgentCount}건 대기중</span>
            </h1>
            <p class="apwaiting-subtitle">
                내가 결재해야 할 문서 목록입니다. 
                <span class="apwaiting-count">총 ${pageInfo.listCount}건</span>
            </p>
            
            <c:if test="${urgentCount > 0}">
                <div class="apwaiting-urgent-notice">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span>긴급 결재가 ${urgentCount}건 있습니다. 빠른 처리 부탁드립니다.</span>
                </div>
            </c:if>
            
            <div class="apwaiting-actions">
                <form class="apwaiting-search-form" method="get" action="searchDocuments.ap" onsubmit="return validateSearchForm();">
				    <input type="hidden" name="boxType" value="waiting">
				    <select name="searchType" class="apwaiting-search-select">
				        <option value="">전체</option>
				        <option value="title" ${searchType eq 'title' ? 'selected' : ''}>제목</option>
				        <option value="drafter" ${searchType eq 'drafter' ? 'selected' : ''}>기안자</option>
				        <option value="form" ${searchType eq 'form' ? 'selected' : ''}>양식</option>
				    </select>
				    <input type="text" name="keyword" class="apwaiting-search-input" 
				           placeholder="검색어를 입력하세요" value="${keyword}">
				    <button type="submit" class="apwaiting-search-btn">
				        <i class="fas fa-search"></i> 검색
				    </button>
				</form>
            </div>
        </div>
        
        <div class="apwaiting-table-container">
            <table class="apwaiting-table">
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>양식</th>
                        <th>제목</th>
                        <th>기안자</th>
                        <th>요청일</th>
                        <th>처리 기한</th>
                        <th>긴급도</th>
                        <th>결재</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="doc" items="${documentList}" varStatus="status">
                        <tr>
                            <td>${pageInfo.listCount - ((pageInfo.currentPage - 1) * pageInfo.boardLimit + status.index)}</td>
                            <td>${doc.formName}</td>
                            <td>
                                <a href="documentDetail.ap?docNo=${doc.docNo}" class="apwaiting-doc-title">
                                    ${doc.title}
                                </a>
                            </td>
                            <td>
                                <div class="apwaiting-drafter">
                                    <span class="apwaiting-drafter-name">${doc.drafterName}</span>
                                    <span class="apwaiting-drafter-dept">${doc.drafterDeptName}</span>
                                </div>
                            </td>
                            <td>
                                <fmt:formatDate value="${doc.requestedDate}" pattern="MM-dd HH:mm"/>
                            </td>
                            <td>
                                <c:set var="now" value="<%= new java.util.Date() %>"/>
							    <c:set var="isOverdue" value="${doc.requestedDate != null && ((now.time - doc.requestedDate.time) / (1000 * 60 * 60 * 24)) > 3}"/>
							    <span class="apwaiting-deadline ${isOverdue ? 'overdue' : ''}">
							        <c:choose>
							            <c:when test="${doc.requestedDate != null}">
							                <fmt:formatDate value="${doc.requestedDate}" pattern="MM-dd"/> 까지
							                <c:if test="${isOverdue}">(기한 초과)</c:if>
							            </c:when>
							            <c:otherwise>-</c:otherwise>
							        </c:choose>
							    </span>
                            </td>
                            <td class="apwaiting-action-cell">
							    <button class="apwaiting-action-btn apwaiting-approve-btn" 
							            onclick="processApproval(${doc.docNo}, 'approve')">
							        <i class="fas fa-check"></i> 승인
							    </button>
							    <button class="apwaiting-action-btn apwaiting-reject-btn" 
							            onclick="processApproval(${doc.docNo}, 'reject')">
							        <i class="fas fa-times"></i> 반려
							    </button>
							</td>
							
                        </tr>
                    </c:forEach>
                    <c:if test="${empty documentList}">
                        <tr>
                            <td colspan="8">
                                <div class="apwaiting-empty">
                                    <i class="far fa-clock"></i>
                                    <p>결재 대기 중인 문서가 없습니다.</p>
                                    <p style="color: #999; font-size: 13px; margin-top: 5px;">모든 결재가 완료되었습니다.</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        
        <c:if test="${pageInfo.listCount > 0}">
		    <div class="apwaiting-pagination">
		        <c:if test="${pageInfo.currentPage <= 1}">
		            <a href="#" class="apwaiting-page-link disabled">
		                <i class="fas fa-angle-double-left"></i>
		            </a>
		            <a href="#" class="apwaiting-page-link disabled">
		                <i class="fas fa-angle-left"></i>
		            </a>
		        </c:if>
		        <c:if test="${pageInfo.currentPage > 1}">
		            <a href="searchDocuments.ap?boxType=waiting&page=1&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apwaiting-page-link">
		                <i class="fas fa-angle-double-left"></i>
		            </a>
		            <a href="searchDocuments.ap?boxType=waiting&page=${pageInfo.currentPage - 1}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apwaiting-page-link">
		                <i class="fas fa-angle-left"></i>
		            </a>
		        </c:if>
		        
		        <c:forEach var="p" begin="${pageInfo.startPage}" end="${pageInfo.endPage}">
		            <a href="searchDocuments.ap?boxType=waiting&page=${p}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" 
		               class="apwaiting-page-link ${p == pageInfo.currentPage ? 'active' : ''}">${p}</a>
		        </c:forEach>
		        
		        <c:if test="${pageInfo.currentPage >= pageInfo.maxPage}">
		            <a href="#" class="apwaiting-page-link disabled">
		                <i class="fas fa-angle-right"></i>
		            </a>
		            <a href="#" class="apwaiting-page-link disabled">
		                <i class="fas fa-angle-double-right"></i>
		            </a>
		        </c:if>
		        <c:if test="${pageInfo.currentPage < pageInfo.maxPage}">
		            <a href="searchDocuments.ap?boxType=waiting&page=${pageInfo.currentPage + 1}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apwaiting-page-link">
		                <i class="fas fa-angle-right"></i>
		            </a>
		            <a href="searchDocuments.ap?boxType=waiting&page=${pageInfo.maxPage}&searchType=${searchType}&keyword=${keyword}&dateFrom=${dateFrom}&dateTo=${dateTo}&statusFilter=${statusFilter}" class="apwaiting-page-link">
		                <i class="fas fa-angle-double-right"></i>
		            </a>
		        </c:if>
		    </div>
		    <div class="apwaiting-page-info">
		        전체 ${pageInfo.listCount}건 중 ${pageInfo.currentPage}/${pageInfo.maxPage} 페이지
		    </div>
		</c:if>
   </div>
   
   <script>
	    function processApproval(docNo, action) {
	        // 모달 생성 및 스타일링
	        if (!document.getElementById('approval-modal')) {
	            const modalHTML = `
	                <div id="approval-modal" class="apdetail-comment-modal">
	                    <div class="apdetail-comment-container">
	                        <div class="apdetail-comment-header">
	                            <span id="approval-modal-title">결재 의견</span>
	                            <button class="apdetail-btn-outline" onclick="closeApprovalModal()">
	                                <i class="fas fa-times"></i>
	                            </button>
	                        </div>
	                        <div class="apdetail-comment-body">
	                            <form id="approval-modal-form" class="apdetail-comment-form">
	                                <input type="hidden" name="docNo" id="approval-doc-no">
	                                <input type="hidden" name="action" id="approval-action">
	                                <textarea name="comment" class="apdetail-comment-textarea" 
	                                    placeholder="의견을 입력하세요" id="approval-comment-text"></textarea>
	                                <div class="apdetail-comment-footer">
	                                    <button type="button" class="apdetail-btn apdetail-btn-outline" onclick="closeApprovalModal()">
	                                        취소
	                                    </button>
	                                    <button type="button" class="apdetail-btn" id="approval-submit-btn" onclick="submitApprovalAction()">
	                                        제출
	                                    </button>
	                                </div>
	                            </form>
	                        </div>
	                    </div>
	                </div>
	            `;
	            
	            // 모달 스타일 추가
	            const styleElement = document.createElement('style');
	            styleElement.textContent = `
	                .apdetail-comment-modal {
	                    position: fixed;
	                    top: 0;
	                    left: 0;
	                    width: 100%;
	                    height: 100%;
	                    background: rgba(0,0,0,0.5);
	                    display: flex;
	                    align-items: center;
	                    justify-content: center;
	                    z-index: 1000;
	                    opacity: 0;
	                    visibility: hidden;
	                    transition: all 0.3s;
	                }
	                
	                .apdetail-comment-modal.active {
	                    opacity: 1;
	                    visibility: visible;
	                }
	                
	                .apdetail-comment-container {
	                    background: white;
	                    width: 500px;
	                    border-radius: 12px;
	                    overflow: hidden;
	                    transform: scale(0.8);
	                    transition: transform 0.3s;
	                }
	                
	                .apdetail-comment-modal.active .apdetail-comment-container {
	                    transform: scale(1);
	                }
	                
	                .apdetail-comment-header {
	                   padding: 15px 20px;
	                   border-bottom: 1px solid #eee;
	                   font-size: 16px;
	                   font-weight: 600;
	                   display: flex;
	                   justify-content: space-between;
	                   align-items: center;
	                }
	                
	                .apdetail-comment-body {
	                   padding: 20px;
	                }
	                
	                .apdetail-comment-form {
	                   display: flex;
	                   flex-direction: column;
	                   gap: 15px;
	                }
	                
	                .apdetail-comment-textarea {
	                   width: 100%;
	                   min-height: 100px;
	                   padding: 10px;
	                   border: 1px solid #ddd;
	                   border-radius: 6px;
	                   resize: vertical;
	                   font-size: 14px;
	                }
	                
	                .apdetail-comment-footer {
	                   display: flex;
	                   justify-content: flex-end;
	                   gap: 10px;
	                }
	                
	                .apdetail-btn {
	                    padding: 10px 20px;
	                    border-radius: 6px;
	                    font-size: 14px;
	                    font-weight: 500;
	                    cursor: pointer;
	                    border: none;
	                    transition: all 0.3s;
	                }
	                
	                .apdetail-btn-outline {
	                    background: transparent;
	                    border: 1px solid #ddd;
	                    color: #666;
	                }
	                
	                .apdetail-btn-outline:hover {
	                    background: #f8f9fa;
	                }
	                
	                .apdetail-btn-approve {
	                    background: #28a745;
	                    color: white;
	                }
	                
	                .apdetail-btn-reject {
	                    background: #dc3545;
	                    color: white;
	                }
	            `;
	            
	            document.head.appendChild(styleElement);
	            document.body.insertAdjacentHTML('beforeend', modalHTML);
	            
	            // 모달 외부 클릭 시 닫기
	            document.getElementById('approval-modal').addEventListener('click', function(e) {
	                if (e.target === this) {
	                    closeApprovalModal();
	                }
	            });
	        }
	        
	        // 승인/반려에 따라 모달 설정
	        if (action === 'approve') {
	            document.getElementById('approval-modal-title').textContent = '결재 승인';
	            document.getElementById('approval-comment-text').placeholder = '의견을 입력하세요 (선택사항)';
	            document.getElementById('approval-comment-text').required = false;
	            document.getElementById('approval-submit-btn').textContent = '승인';
	            document.getElementById('approval-submit-btn').className = 'apdetail-btn apdetail-btn-approve';
	        } else if (action === 'reject') {
	            document.getElementById('approval-modal-title').textContent = '결재 반려';
	            document.getElementById('approval-comment-text').placeholder = '반려 사유를 입력하세요 (필수)';
	            document.getElementById('approval-comment-text').required = true;
	            document.getElementById('approval-submit-btn').textContent = '반려';
	            document.getElementById('approval-submit-btn').className = 'apdetail-btn apdetail-btn-reject';
	        }
	        
	        // 문서 번호와 액션 설정
	        document.getElementById('approval-doc-no').value = docNo;
	        document.getElementById('approval-action').value = action;
	        
	        // 모달 표시
	        document.getElementById('approval-modal').classList.add('active');
	    }
	
	    // 모달 닫기
	    function closeApprovalModal() {
	        document.getElementById('approval-modal').classList.remove('active');
	        document.getElementById('approval-comment-text').value = '';
	    }
	
	    // 승인/반려 액션 제출
	    function submitApprovalAction() {
	        const docNo = document.getElementById('approval-doc-no').value;
	        const action = document.getElementById('approval-action').value;
	        const comment = document.getElementById('approval-comment-text').value;
	        
	        // 반려 시 사유 필수 체크
	        if (action === 'reject' && (!comment || comment.trim() === '')) {
	            alert('반려 사유는 필수 입력 항목입니다.');
	            return;
	        }
	        
	        // AJAX 요청 URL 설정
	        const url = action === 'approve' ? 'directApprove.ap' : 'directReject.ap';
	        
	        // AJAX로 처리 요청
	        $.ajax({
	            url: url,
	            type: 'POST',
	            data: {
	                docNo: docNo,
	                comment: comment
	            },
	            success: function(response) {
	                if (response.success) {
	                    alert(response.message);
	                    // 모달 닫기
	                    closeApprovalModal();
	                    // 페이지 새로고침 (캐시 무시)
	                    location.reload();
	                } else {
	                    alert(response.message);
	                }
	            },
	            error: function() {
	                alert('서버 오류가 발생했습니다.');
	            },
	            complete: function() {
	                console.log('요청 완료');
	                // 1초 후 다시 한번 새로고침 (이중 안전장치)
	                setTimeout(function() {
	                    location.reload(true);
	                }, 1000);
	            }
	        });
	    }
	    
	    // 검색폼 유효성 검사 (기존 코드 유지)
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