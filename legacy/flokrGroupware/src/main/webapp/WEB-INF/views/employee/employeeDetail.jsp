<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>사원 상세 정보</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/employeeDetail.css">
    <!-- jQuery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <jsp:include page="../common/header.jsp"/>
    
    <div class="container">
        <div class="employee-detail">
            <div class="detail-header">
                <c:choose>
                    <c:when test="${not empty employee.profileImgPath}">
                        <img src="${employee.profileImgPath}" alt="프로필 이미지" class="profile-image">
                    </c:when>
                    <c:otherwise>
                        <svg class="profile-image" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
				            <circle cx="12" cy="7" r="5" fill="#E2E8F0"/>
				            <path d="M3 19c0-3.314 4.03-6 9-6s9 2.686 9 6v1H3v-1z" fill="#E2E8F0"/>
				        </svg>
                    </c:otherwise>
                </c:choose>
                
                <div>
                    <h1 class="employee-name">${employee.empName}</h1>
                    <p class="employee-position">${employee.deptName} / ${employee.positionName}</p>
                    <p class="employee-id">사번: ${employee.empId}</p>
                </div>
            </div>
            
            <div class="detail-section">
                <h3 class="section-title">기본 정보</h3>
                <div class="info-row">
                    <div class="info-label">이메일</div>
                    <div class="info-value">${employee.email}</div>
                </div>
                <div class="info-row">
                    <div class="info-label">전화번호</div>
                    <div class="info-value">${employee.phone}</div>
                </div>
                <div class="info-row">
                    <div class="info-label">입사일</div>
                    <td><fmt:formatDate value="${employee.hireDate}" pattern="yyyy-MM-dd"/></td>
                </div>
                <div class="info-row">
                    <div class="info-label">상태</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${employee.status eq 'Y'}">
                                <span class="status-badge status-active">재직</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-badge status-inactive">퇴사</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <c:if test="${not empty employee.signatureImgPath}">
                <div class="detail-section">
                    <h3 class="section-title">전자 서명</h3>
                    <img src="${employee.signatureImgPath}" alt="전자 서명" class="signature-image">
                </div>
            </c:if>
            
            <div class="detail-section">
                <h3 class="section-title">시스템 정보</h3>
                <div class="info-row">
                    <div class="info-label">마지막 로그인</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${not empty employee.lastLoginDate}">
                                <fmt:formatDate value="${employee.lastLoginDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </c:when>
                            <c:otherwise>
                                로그인 기록 없음
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="info-row">
                    <div class="info-label">계정 생성일</div>
                    <div class="info-value"><fmt:formatDate value="${employee.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                </div>
                <div class="info-row">
                    <div class="info-label">최종 수정일</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${not empty employee.updateDate}">
                                <fmt:formatDate value="${employee.updateDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </c:when>
                            <c:otherwise>
                                수정 이력 없음
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <!-- 버튼 영역 -->
			<div class="button-area">
			    <button type="button" class="btn btn-primary" id="resetPasswordBtn" data-empno="${employee.empNo}">
			        <i class="fas fa-key"></i> 비밀번호 초기화
			    </button>
			    <a href="${pageContext.request.contextPath}/employeeUpdate/${employee.empNo}" class="btn btn-info">
			        <i class="fas fa-edit"></i> 수정
			    </a>
			    <c:if test="${employee.status eq 'Y'}">
			        <button type="button" class="btn btn-danger" id="deleteEmployeeBtn" data-empno="${employee.empNo}" data-empname="${employee.empName}">
			            <i class="fas fa-user-slash"></i> 퇴사 처리
			        </button>
			    </c:if>
			    <a href="${pageContext.request.contextPath}/employeeList" class="btn btn-secondary">
			        <i class="fas fa-list"></i> 목록으로
			    </a>
			</div>
        </div>
    </div>
    
<script>
    $(document).ready(function() {
        // 비밀번호 초기화 버튼
        $("#resetPasswordBtn").click(function() {
            var empNo = $(this).data("empno");
            var $button = $(this);
            
            if(confirm("해당 사원의 비밀번호를 초기화하시겠습니까?")) {
                // 로딩 표시 추가
                $button.html('<i class="fas fa-spinner fa-spin"></i> 처리 중...');
                $button.prop('disabled', true);
                
                $.ajax({
                    url: "${pageContext.request.contextPath}/resetPassword",
                    type: "POST",
                    data: { empNo: empNo },
                    success: function(response) {
                        if(response.success) {
                            alert(response.message);
                        } else {
                            alert(response.message || "비밀번호 초기화에 실패했습니다.");
                        }
                        // 버튼 원상복구
                        $button.html('<i class="fas fa-key"></i> 비밀번호 초기화');
                        $button.prop('disabled', false);
                    },
                    error: function(xhr, status, error) {
                        alert("비밀번호 초기화 중 오류가 발생했습니다: " + error);
                        // 버튼 원상복구
                        $button.html('<i class="fas fa-key"></i> 비밀번호 초기화');
                        $button.prop('disabled', false);
                    }
                });
            }
        });
        
        // 퇴사 처리 버튼
        $("#deleteEmployeeBtn").click(function() {
            var empNo = $(this).data("empno");
            var empName = $(this).data("empname");
            var $button = $(this);
            
            if(confirm(empName + " 님을 퇴사 처리하시겠습니까?\n처리 후에는 로그인이 불가능합니다.")) {
                // 로딩 표시 추가
                $button.html('<i class="fas fa-spinner fa-spin"></i> 처리 중...');
                $button.prop('disabled', true);
                
                $.ajax({
                    url: "${pageContext.request.contextPath}/employeeDeleteAjax",
                    type: "POST",
                    data: { empNo: empNo },
                    success: function(response) {
                        if(response.success) {
                            alert("퇴사 처리가 완료되었습니다.");
                            window.location.href = "${pageContext.request.contextPath}/employeeList";
                        } else {
                            alert(response.message || "퇴사 처리에 실패했습니다.");
                            // 버튼 원상복구
                            $button.html('<i class="fas fa-user-slash"></i> 퇴사 처리');
                            $button.prop('disabled', false);
                        }
                    },
                    error: function(xhr, status, error) {
                        alert("퇴사 처리 중 오류가 발생했습니다: " + error);
                        // 버튼 원상복구
                        $button.html('<i class="fas fa-user-slash"></i> 퇴사 처리');
                        $button.prop('disabled', false);
                    }
                });
            }
        });
    });
</script>
</body>
</html>