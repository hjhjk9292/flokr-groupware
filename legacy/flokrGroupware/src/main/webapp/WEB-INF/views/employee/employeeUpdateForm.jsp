<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>사원 정보 수정</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/employeeUpdateForm.css">
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
        <div class="update-form">
            <h2 class="form-title">사원 정보 수정</h2>
            
            <form action="${pageContext.request.contextPath}/employeeUpdate" method="post" id="updateForm">
                <input type="hidden" name="empNo" value="${employee.empNo}">
                <input type="hidden" name="empId" value="${employee.empId}">
                <input type="hidden" name="isAdmin" value="${employee.isAdmin}">
                
                <div class="form-row">
                    <div class="form-group col-md-6">
                        <label for="empName">이름<span class="required-mark">*</span></label>
                        <input type="text" class="form-control" id="empName" name="empName" value="${employee.empName}" required>
                    </div>
                    
                    <div class="form-group col-md-6">
                        <label for="empId-display">사번</label>
                        <input type="text" class="form-control" id="empId-display" value="${employee.empId}" disabled>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group col-md-6">
                        <label for="email">이메일<span class="required-mark">*</span></label>
                        <input type="email" class="form-control" id="email" name="email" value="${employee.email}" required>
                    </div>
                    
                    <div class="form-group col-md-6">
                        <label for="phone">전화번호</label>
                        <div class="phone-group">
                            <c:set var="phoneArr" value="${employee.phone.split('-')}"/>
                            <input type="text" class="form-control phone-input" id="phone1" name="phone1" maxlength="3" value="${phoneArr[0]}">
                            <span class="phone-separator">-</span>
                            <input type="text" class="form-control phone-input" id="phone2" name="phone2" maxlength="4" value="${phoneArr[1]}">
                            <span class="phone-separator">-</span>
                            <input type="text" class="form-control phone-input" id="phone3" name="phone3" maxlength="4" value="${phoneArr[2]}">
                        </div>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group col-md-6">
                        <label for="deptNo">부서<span class="required-mark">*</span></label>
                        <select class="form-control" id="deptNo" name="deptNo" required>
                            <option value="">부서 선택</option>
                            <c:forEach var="dept" items="${deptList}">
                                <option value="${dept.deptNo}" ${dept.deptNo eq employee.deptNo ? 'selected' : ''}>${dept.deptName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="form-group col-md-6">
                        <label for="positionNo">직급<span class="required-mark">*</span></label>
                        <select class="form-control" id="positionNo" name="positionNo" required>
                            <option value="">직급 선택</option>
                            <c:forEach var="position" items="${positionList}">
                                <option value="${position.positionNo}" ${position.positionNo eq employee.positionNo ? 'selected' : ''}>${position.positionName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="hireDate">입사일<span class="required-mark">*</span></label>
                    <fmt:formatDate value="${employee.hireDate}" pattern="yyyy-MM-dd" var="formattedHireDate" />
					<input type="date" id="hireDate" name="hireDate" class="form-control" value="${formattedHireDate}">
                </div>
                
                <div class="form-group">
                    <label>계정 상태</label>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="status" id="statusActive" value="Y" ${employee.status eq 'Y' ? 'checked' : ''}>
                        <label class="form-check-label" for="statusActive">
                            활성
                        </label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="status" id="statusInactive" value="N" ${employee.status eq 'N' ? 'checked' : ''}>
                        <label class="form-check-label" for="statusInactive">
                            비활성 (퇴사)
                        </label>
                    </div>
                </div>
                
                <div class="action-buttons">
                    <button type="submit" class="btn btn-primary">저장</button>
                    <a href="${pageContext.request.contextPath}/employeeDetail/${employee.empNo}" class="btn btn-secondary">취소</a>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        $(document).ready(function() {
            // 숫자만 입력 가능하게 제한
            $(".phone-input").on("input", function() {
                $(this).val($(this).val().replace(/[^0-9]/g, ""));
            });
            
            // 폼 제출 전 유효성 검사
            $("#updateForm").submit(function(e) {
                var phone1 = $("#phone1").val();
                var phone2 = $("#phone2").val();
                var phone3 = $("#phone3").val();
                
                // 전화번호 유효성 검사 (모두 입력하거나 모두 비워야 함)
                if((phone1 != "" || phone2 != "" || phone3 != "") && 
                   (phone1 == "" || phone2 == "" || phone3 == "")) {
                    alert("전화번호를 완전히 입력하거나 비워두세요.");
                    return false;
                }
                
                if(phone1 != "" && (phone1.length < 2 || phone1.length > 3)) {
                    alert("전화번호 첫 번째 부분은 2~3자리여야 합니다.");
                    return false;
                }
                
                if(phone2 != "" && (phone2.length < 3 || phone2.length > 4)) {
                    alert("전화번호 두 번째 부분은 3~4자리여야 합니다.");
                    return false;
                }
                
                if(phone3 != "" && phone3.length != 4) {
                    alert("전화번호 세 번째 부분은 4자리여야 합니다.");
                    return false;
                }
                
                return true;
            });
        });
    </script>
</body>
</html>