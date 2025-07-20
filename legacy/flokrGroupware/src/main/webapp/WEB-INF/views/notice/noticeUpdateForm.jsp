<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>공지사항 수정</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/noticeUpdateForm.css">
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
      <div class="notice-title">
        <h1>공지사항 수정</h1>
        <p>공지사항 내용을 수정하세요.</p>
      </div>
    </div>
    
    <form id="noticeForm" action="${pageContext.request.contextPath}/noticeUpdate" method="post">
      <input type="hidden" name="noticeNo" value="${notice.noticeNo}">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
      
      <div class="notice-form">
        <!-- 제목 -->
        <div class="form-group">
          <label for="noticeTitle" class="form-label">제목</label>
          <input type="text" class="form-control" id="noticeTitle" name="noticeTitle" placeholder="공지사항 제목을 입력하세요" value="${notice.noticeTitle}" required>
        </div>
        
        <!-- 분류 -->
        <div class="form-group">
          <label for="category" class="form-label">분류</label>
          <select class="form-select" id="category" name="category" required>
            <option value="">선택하세요</option>
            <option value="GENERAL" ${notice.category eq 'GENERAL' ? 'selected' : ''}>일반</option>
            <option value="EVENT" ${notice.category eq 'EVENT' ? 'selected' : ''}>행사</option>
            <option value="SYSTEM" ${notice.category eq 'SYSTEM' ? 'selected' : ''}>시스템</option>
            <option value="HR" ${notice.category eq 'HR' ? 'selected' : ''}>인사</option>
          </select>
        </div>
        
        <!-- 필독 여부 -->
        <div class="form-group">
          <div class="form-check">
            <input type="checkbox" class="form-check-input" id="isMandatory" name="isMandatory" value="1" ${notice.isMandatory eq 1 ? 'checked' : ''}>
            <label for="isMandatory" class="form-check-label">필독 공지로 설정</label>
          </div>
          <p class="form-text">필독 공지는 항상 상단에 표시되며 강조됩니다.</p>
        </div>
        
        <!-- 내용 -->
        <div class="form-group">
          <label for="noticeContent" class="form-label">내용</label>
          <textarea class="form-control" id="noticeContent" name="noticeContent" placeholder="공지사항 내용을 입력하세요" required>${notice.noticeContent}</textarea>
        </div>
        
        <!-- 알림 발송 여부 -->
        <div class="form-group">
          <div class="form-check">
            <input type="checkbox" class="form-check-input" id="sendNotification" name="sendNotification" value="true">
            <label for="sendNotification" class="form-check-label">전체 직원에게 알림 발송</label>
          </div>
          <p class="form-text">체크하면 공지사항 수정 시 모든 직원에게 알림이 전송됩니다.</p>
        </div>
      </div>
      
      <!-- 버튼 -->
      <div class="form-actions">
        <a href="${pageContext.request.contextPath}/noticeDetail/${notice.noticeNo}" class="btn btn-secondary">
          <i class="fas fa-times"></i> 취소
        </a>
        <button type="submit" class="btn btn-primary" id="submitBtn">
          <i class="fas fa-check"></i> 수정하기
        </button>
      </div>
    </form>
  </main>
  
  <script>
    $(document).ready(function() {
    	
    });
  </script>
</body>
</html>