<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.kh.flokrGroupware.approval.model.vo.ApprovalDoc" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>문서 상세보기 | Flokr</title>    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/approvalDocumentDetail.css">
</head>
<body>
   <!-- header.jsp include -->
   <jsp:include page="../common/header.jsp" />

   <div class="apdetail-container">
       <!-- 문서 헤더 -->
       <div class="apdetail-header">
           <div class="apdetail-status-bar">
               <span class="apdetail-doc-number">문서번호: ${document.docNo}</span>
               <span class="apdetail-status-badge 
                   <c:choose>
                       <c:when test="${document.docStatus == 'DRAFT'}">apdetail-status-draft</c:when>
                       <c:when test="${document.docStatus == 'REQUESTED'}">apdetail-status-requested</c:when>
                       <c:when test="${document.docStatus == 'APPROVED'}">apdetail-status-approved</c:when>
                       <c:when test="${document.docStatus == 'REJECTED'}">apdetail-status-rejected</c:when>
                   </c:choose>">
                   <c:choose>
                       <c:when test="${document.docStatus == 'DRAFT'}">임시저장</c:when>
                       <c:when test="${document.docStatus == 'REQUESTED'}">결재진행중</c:when>
                       <c:when test="${document.docStatus == 'APPROVED'}">결재완료</c:when>
                       <c:when test="${document.docStatus == 'REJECTED'}">결재반려</c:when>
                   </c:choose>
               </span>
           </div>
           
           <h1 class="apdetail-title">${document.title}</h1>
           
           <div class="apdetail-meta">
               <div class="apdetail-meta-item">
                   <span class="apdetail-meta-label">기안자</span>
                   <span class="apdetail-meta-value">${document.drafterName} ${document.drafterPositionName}</span>
               </div>
               <div class="apdetail-meta-item">
                   <span class="apdetail-meta-label">기안부서</span>
                   <span class="apdetail-meta-value">${document.drafterDeptName}</span>
               </div>
               <div class="apdetail-meta-item">
                   <span class="apdetail-meta-label">기안일자</span>
                   <span class="apdetail-meta-value">
                       <fmt:formatDate value="${document.createDate}" pattern="yyyy-MM-dd"/>
                   </span>
               </div>
               <div class="apdetail-meta-item">
                   <span class="apdetail-meta-label">문서종류</span>
                   <span class="apdetail-meta-value">${document.formName}</span>
               </div>
               <c:if test="${document.requestedDate != null}">
                   <div class="apdetail-meta-item">
                       <span class="apdetail-meta-label">상신일자</span>
                       <span class="apdetail-meta-value">
                           <fmt:formatDate value="${document.requestedDate}" pattern="yyyy-MM-dd"/>
                       </span>
                   </div>
               </c:if>
               <c:if test="${document.completedDate != null}">
                   <div class="apdetail-meta-item">
                       <span class="apdetail-meta-label">완료일자</span>
                       <span class="apdetail-meta-value">
                           <fmt:formatDate value="${document.completedDate}" pattern="yyyy-MM-dd"/>
                       </span>
                   </div>
               </c:if>
           </div>
       </div>
       
       <!-- 문서 내용 -->
       <div class="apdetail-content">
           <div class="apdetail-section-title">
               <i class="fas fa-file-alt"></i>
               문서 내용
           </div>
           <!-- 문서 내용 표시 부분 변경 -->
           <div class="apdetail-document-content">
               <div id="document-content">
               	<p>문서 내용을 불러오는 중...</p>
               </div>
           </div>
       </div>
       
       <!-- 결재선 -->
       <div class="apdetail-approval-line">
           <div class="apdetail-section-title">
               <i class="fas fa-route"></i>
               결재선
           </div>
           <table class="apdetail-approval-table">
               <thead>
                   <tr>
                       <th>순서</th>
                       <th>결재자</th>
                       <th>부서/직급</th>
                       <th>결재</th>
                       <th>결재일시</th>
                       <th>의견</th>
                   </tr>
               </thead>
               <tbody>
                   <c:forEach var="line" items="${lines}">
                       <tr>
                           <td>${line.approvalOrder}</td>
                           <td>${line.approverName}</td>
                           <td>${line.deptName}/${line.positionName}</td>
                           <td>
                               <div class="apdetail-approval-signature
                                   <c:choose>
                                       <c:when test="${line.lineStatus == 'APPROVED'}">approved</c:when>
                                       <c:when test="${line.lineStatus == 'REJECTED'}">rejected</c:when>
                                   </c:choose>">
                                   <c:choose>
                                       <c:when test="${line.lineStatus == 'PENDING'}">-</c:when>
                                       <c:when test="${line.lineStatus == 'WAITING'}">대기중</c:when>
                                       <c:when test="${line.lineStatus == 'APPROVED'}">
                                           <i class="fas fa-check"></i>
                                       </c:when>
                                       <c:when test="${line.lineStatus == 'REJECTED'}">
                                           <i class="fas fa-times"></i>
                                       </c:when>
                                   </c:choose>
                               </div>
                           </td>
                           <td>
                               <c:if test="${line.processedDate != null}">
                                   <fmt:formatDate value="${line.processedDate}" pattern="yyyy-MM-dd HH:mm"/>
                               </c:if>
                           </td>
                           <td>
                               <c:if test="${not empty line.approvalComment}">
                                   <div class="apdetail-approval-comment" title="${line.approvalComment}">
                                       ${line.approvalComment}
                                   </div>
                               </c:if>
                           </td>
                       </tr>
                   </c:forEach>
               </tbody>
           </table>
       </div>
       
       <!-- 첨부파일 -->
       <div class="apdetail-attachment">
           <div class="apdetail-section-title">
               <i class="fas fa-paperclip"></i>
               첨부파일
           </div>
           <div class="apdetail-attachment-list">
               <c:if test="${attachment != null}">
                   <div class="apdetail-attachment-item">
                       <i class="fas fa-file apdetail-attachment-icon"></i>
                       <div class="apdetail-attachment-info">
                           <div class="apdetail-attachment-name">${attachment.originalFilename}</div>
                           <div class="apdetail-attachment-size">File</div>
                       </div>
                       <a href="downloadAttachment.ap?attachmentNo=${attachment.attachmentNo}" 
                          class="apdetail-download-btn">
                           <i class="fas fa-download"></i> 다운로드
                       </a>
                   </div>
               </c:if>
               <c:if test="${attachment == null}">
                   <p style="color: #777; font-size: 14px; padding: 20px; text-align: center;">
                       첨부된 파일이 없습니다.
                   </p>
               </c:if>
           </div>
       </div>
   </div>
   
   <!-- 액션 바 -->
   <div class="apdetail-action-bar">
       <div class="apdetail-action-container">
           <div></div>
           <div class="apdetail-btn-group">
               <!-- 결재자 액션 버튼 -->
               <c:if test="${isCurrentApprover}">
                   <button class="apdetail-btn apdetail-btn-approve" onclick="openApprovalModal()">
                       <i class="fas fa-check"></i> 승인
                   </button>
                   <button class="apdetail-btn apdetail-btn-reject" onclick="openRejectModal()">
                       <i class="fas fa-times"></i> 반려
                   </button>
               </c:if>
               
               <!-- 기안자 액션 버튼 -->
               <c:if test="${document.drafterEmpNo == loginUser.empNo && 
                            (document.docStatus == 'DRAFT' || document.docStatus == 'REJECTED')}">
                   <button class="apdetail-btn apdetail-btn-edit" onclick="editDocument()">
                       <i class="fas fa-edit"></i> 수정
                   </button>
                   <button class="apdetail-btn apdetail-btn-delete" onclick="deleteDocument()">
                       <i class="fas fa-trash"></i> 삭제
                   </button>
               </c:if>
               
               <button class="apdetail-btn apdetail-btn-outline" onclick="goBack()">
                   <i class="fas fa-arrow-left"></i> 목록
               </button>
           </div>
       </div>
   </div>
   
   <!-- 승인/반려 의견 모달 -->
   <div class="apdetail-comment-modal" id="apdetail-comment-modal">
       <div class="apdetail-comment-container">
           <div class="apdetail-comment-header">
               <span id="apdetail-comment-title">결재 의견</span>
               <button class="apdetail-btn-outline" onclick="closeCommentModal()">
                   <i class="fas fa-times"></i>
               </button>
           </div>
           <div class="apdetail-comment-body">
               <form id="apdetail-comment-form" class="apdetail-comment-form" method="POST">
                   <input type="hidden" name="lineNo" value="${currentLine.lineNo}">
                   <textarea name="comment" class="apdetail-comment-textarea" 
                             placeholder="의견을 입력하세요" id="apdetail-comment-text"></textarea>
                   <div class="apdetail-comment-footer">
                       <button type="button" class="apdetail-btn apdetail-btn-outline" onclick="closeCommentModal()">
                           취소
                       </button>
                       <button type="submit" class="apdetail-btn" id="apdetail-submit-btn">
                           제출
                       </button>
                   </div>
               </form>
           </div>
       </div>
   </div>
   
   <script>
// docContent를 JavaScript 변수로 안전하게 설정
var rawDocContent = "";
<%
    ApprovalDoc docForScript = (ApprovalDoc)request.getAttribute("document");
    String docContentForScript = "";
   
    if (docForScript != null && docForScript.getDocContent() != null) {
        docContentForScript = docForScript.getDocContent();
        // 특수 문자 이스케이프 처리
        docContentForScript = docContentForScript
            .replace("\\", "\\\\")
            .replace("\'", "\\'")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("</", "<\\/");
        
        // EL 표현식을 이스케이프 처리
        docContentForScript = docContentForScript
            .replace("${", "\\${");
    }
%>
rawDocContent = '<%= docContentForScript %>';

// 문서 번호 및 기타 필요한 데이터 설정
var docNo = '${document.docNo}';
</script>

<!-- 외부 JS 파일 로드 -->
<script src="${pageContext.request.contextPath}/resources/js/approvalDocumentDetail.js"></script>
</body>
</html>