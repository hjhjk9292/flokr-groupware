<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>공지사항 목록</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/noticeList.css">
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
        <h1>공지사항</h1>
        <p>중요한 소식과 안내사항을 확인하세요.</p>
      </div>
      
      <c:if test="${loginUser.isAdmin eq 'Y'}">
        <div class="notice-actions">
          <a href="${pageContext.request.contextPath}/noticeCreate" class="btn btn-primary">
            <i class="fas fa-plus"></i> 공지사항 등록
          </a>
        </div>
      </c:if>
    </div>
    
    <!-- 검색 및 필터 -->
    <div class="notice-filters">
      <select class="notice-select" id="categoryFilter">
        <option value="">전체 분류</option>
        <option value="GENERAL" ${category eq 'GENERAL' ? 'selected' : ''}>일반</option>
        <option value="EVENT" ${category eq 'EVENT' ? 'selected' : ''}>행사</option>
        <option value="SYSTEM" ${category eq 'SYSTEM' ? 'selected' : ''}>시스템</option>
        <option value="HR" ${category eq 'HR' ? 'selected' : ''}>인사</option>
      </select>
      
      <div class="notice-search">
        <input type="text" id="searchKeyword" placeholder="검색어 입력" value="${keyword}">
        <button id="searchBtn"><i class="fas fa-search"></i></button>
      </div>
    </div>
    
    <!-- 공지사항 테이블 -->
    <div class="notice-table-container">
      <table class="notice-table">
        <thead>
          <tr>
            <th width="7%">번호</th>
            <th width="15%">분류</th>
            <th width="45%">제목</th>
            <th width="15%">작성자</th>
            <th width="10%">작성일</th>
            <th width="8%">조회수</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty noticeList}">
              <tr>
                <td colspan="6">
                  <div class="empty-notice">
                    <i class="fas fa-clipboard"></i>
                    <p>등록된 공지사항이 없습니다.</p>
                  </div>
                </td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach items="${noticeList}" var="notice">
                <tr>
                  <td>${notice.noticeNo}</td>
                  <td>
                    <c:choose>
                      <c:when test="${notice.category eq 'GENERAL'}">일반</c:when>
                      <c:when test="${notice.category eq 'EVENT'}">행사</c:when>
                      <c:when test="${notice.category eq 'SYSTEM'}">시스템</c:when>
                      <c:when test="${notice.category eq 'HR'}">인사</c:when>
                      <c:otherwise>${notice.category}</c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <a href="${pageContext.request.contextPath}/noticeDetail/${notice.noticeNo}" class="notice-title-link">
                      <c:if test="${notice.isMandatory eq 1}">
                        <span class="notice-mandatory">필독</span>
                      </c:if>
                      ${notice.noticeTitle}
                    </a>
                  </td>
                  <td>${notice.noticeWriter}</td>
                  <td>
                    <fmt:formatDate value="${notice.createDate}" pattern="yyyy-MM-dd" />
                  </td>
                  <td>${notice.viewCount}</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
    
    <!-- 페이지네이션 -->
    <c:if test="${not empty noticeList}">
      <div class="notice-pagination">
        <c:if test="${pi.currentPage > 1}">
          <a href="${pageContext.request.contextPath}/noticeList?page=1&category=${category}&keyword=${keyword}">
            <i class="fas fa-angle-double-left"></i>
          </a>
          <a href="${pageContext.request.contextPath}/noticeList?page=${pi.currentPage - 1}&category=${category}&keyword=${keyword}">
            <i class="fas fa-angle-left"></i>
          </a>
        </c:if>
        
        <c:forEach var="p" begin="${pi.startPage}" end="${pi.endPage}">
          <c:choose>
            <c:when test="${p eq pi.currentPage}">
              <a href="#" class="active">${p}</a>
            </c:when>
            <c:otherwise>
              <a href="${pageContext.request.contextPath}/noticeList?page=${p}&category=${category}&keyword=${keyword}">${p}</a>
            </c:otherwise>
          </c:choose>
        </c:forEach>
        
        <c:if test="${pi.currentPage < pi.maxPage}">
          <a href="${pageContext.request.contextPath}/noticeList?page=${pi.currentPage + 1}&category=${category}&keyword=${keyword}">
            <i class="fas fa-angle-right"></i>
          </a>
          <a href="${pageContext.request.contextPath}/noticeList?page=${pi.maxPage}&category=${category}&keyword=${keyword}">
            <i class="fas fa-angle-double-right"></i>
          </a>
        </c:if>
      </div>
    </c:if>
  </main>
  
  <script>
    $(document).ready(function() {
      // 검색 기능
      $('#searchBtn').click(function() {
        const category = $('#categoryFilter').val();
        const keyword = $('#searchKeyword').val();
        
        location.href = "${pageContext.request.contextPath}/noticeList?category=" + category + "&keyword=" + keyword;
      });
      
      // 카테고리 필터 변경 시 즉시 검색
      $('#categoryFilter').change(function() {
        const category = $(this).val();
        const keyword = $('#searchKeyword').val();
        
        location.href = "${pageContext.request.contextPath}/noticeList?category=" + category + "&keyword=" + keyword;
      });
      
      // 검색어 입력 후 엔터키 처리
      $('#searchKeyword').keypress(function(e) {
        if (e.which == 13) {
          $('#searchBtn').click();
        }
      });
    });
  </script>
</body>
</html>