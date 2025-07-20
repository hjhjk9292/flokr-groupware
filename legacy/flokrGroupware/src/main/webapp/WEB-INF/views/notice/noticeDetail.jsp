<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${notice.noticeTitle} - 공지사항</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/noticeDetail.css">
  <!-- jQuery 라이브러리 -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
  <!-- Font Awesome CDN -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <!-- Alertify -->
  <script src="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/alertify.min.js"></script>
  <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/alertify.min.css"/>
  <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/themes/default.min.css"/>
</head>
<body>
  <jsp:include page="../common/header.jsp"/>
  
  <main class="notice-container">
    <div class="notice-header">
      <h1>공지사항</h1>
      
      <div class="notice-actions">
        <a href="${pageContext.request.contextPath}/noticeList" class="btn btn-secondary">
          <i class="fas fa-list"></i> 목록으로
        </a>
        
        <c:if test="${loginUser.isAdmin eq 'Y'}">
          <a href="${pageContext.request.contextPath}/noticeUpdate/${notice.noticeNo}" class="btn btn-primary">
            <i class="fas fa-edit"></i> 수정
          </a>
          <button type="button" class="btn btn-danger" id="deleteBtn">
            <i class="fas fa-trash-alt"></i> 삭제
          </button>
        </c:if>
      </div>
    </div>
    
    <!-- 공지사항 상세 정보 -->
    <div class="notice-detail">
      <div class="notice-detail-header">
        <div class="notice-detail-title">
          <c:if test="${notice.isMandatory eq 1}">
            <span class="notice-mandatory">필독</span>
          </c:if>
          ${notice.noticeTitle}
        </div>
        
        <div class="notice-info">
          <div class="notice-info-item">
            <i class="fas fa-user"></i>
            <span>${notice.noticeWriter} (${notice.deptName})</span>
          </div>
          <div class="notice-info-item">
            <i class="fas fa-calendar-alt"></i>
            <span><fmt:formatDate value="${notice.createDate}" pattern="yyyy-MM-dd HH:mm" /></span>
          </div>
          <div class="notice-info-item">
            <i class="fas fa-tag"></i>
            <span>
              <c:choose>
                <c:when test="${notice.category eq 'GENERAL'}">일반</c:when>
                <c:when test="${notice.category eq 'EVENT'}">행사</c:when>
                <c:when test="${notice.category eq 'SYSTEM'}">시스템</c:when>
                <c:when test="${notice.category eq 'HR'}">인사</c:when>
                <c:otherwise>${notice.category}</c:otherwise>
              </c:choose>
            </span>
          </div>
          <div class="notice-info-item">
            <i class="fas fa-eye"></i>
            <span>${notice.viewCount}</span>
          </div>
        </div>
      </div>
      
      <div class="notice-detail-content">
        <div class="notice-content">${notice.noticeContent}</div>
      </div>
    </div>
    
    <!-- 목록으로 버튼 (하단) -->
    <div class="notice-list-btn">
      <a href="${pageContext.request.contextPath}/noticeList" class="btn btn-secondary">
        <i class="fas fa-list"></i> 목록으로
      </a>
    </div>
  </main>
  
  <!-- 삭제 확인 모달 -->
  <div class="modal-backdrop" id="deleteModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>공지사항 삭제</h3>
      </div>
      <div class="modal-body">
        <p>이 공지사항을 삭제하시겠습니까?</p>
        <p>삭제된 공지사항은 복구할 수 없습니다.</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" id="cancelBtn">취소</button>
        <button type="button" class="btn btn-danger" id="confirmDeleteBtn">삭제</button>
      </div>
    </div>
  </div>
  
  <script>
    $(document).ready(function() {
      // 삭제 버튼 클릭 시 모달 표시
      $('#deleteBtn').click(function() {
        $('#deleteModal').css('display', 'flex');
      });
      
      // 모달 취소 버튼
      $('#cancelBtn').click(function() {
        $('#deleteModal').css('display', 'none');
      });
      
      // 모달 외부 클릭 시 닫기
      $('#deleteModal').click(function(e) {
        if (e.target === this) {
          $(this).css('display', 'none');
        }
      });
      
      // 삭제 확인 버튼
      $('#confirmDeleteBtn').click(function() {
        // AJAX 삭제 요청
        $.ajax({
          url: '${pageContext.request.contextPath}/noticeDeleteAjax',
          type: 'POST',
          data: { noticeNo: ${notice.noticeNo} },
          success: function(response) {
            if (response.success) {
              alertify.success(response.message);
              setTimeout(function() {
                location.href = '${pageContext.request.contextPath}/noticeList';
              }, 1500);
            } else {
              alertify.error(response.message);
              $('#deleteModal').css('display', 'none');
            }
          },
          error: function(xhr, status, error) {
            alertify.error('공지사항 삭제 중 오류가 발생했습니다.');
            $('#deleteModal').css('display', 'none');
          }
        });
      });
    });
  </script>
</body>
</html>