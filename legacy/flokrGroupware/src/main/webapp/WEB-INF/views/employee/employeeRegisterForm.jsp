<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>사원 등록</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/employeeRegisterForm.css">
<!-- jQuery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<!-- Date Picker -->
<link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
</head>
<body>
    <jsp:include page="../common/header.jsp"/>
    
    <div class="register-container">
        <h2 class="register-title">사원 등록</h2>
        
        <form action="${pageContext.request.contextPath}/insertEmployee" method="post" id="employeeForm">
            <div class="register-row">
                <div class="register-col">
                    <div class="register-group">
                        <label class="register-label">이름</label>
                        <input type="text" name="empName" class="register-input" required>
                    </div>
                </div>
                <div class="register-col">
                    <div class="register-group">
                        <label class="register-label">입사일</label>
                        <input type="date" name="hireDate" class="register-input" required>
                    </div>
                </div>
            </div>
            
            <div class="register-row">
                <div class="register-col">
                    <div class="register-group">
                        <label class="register-label">전화번호</label>
                        <div class="phone-input-group">
                            <input type="text" name="phone1" class="phone-input" maxlength="3" value="010">
                            <span class="phone-separator">-</span>
                            <input type="text" name="phone2" class="phone-input" maxlength="4">
                            <span class="phone-separator">-</span>
                            <input type="text" name="phone3" class="phone-input" maxlength="4">
                        </div>
                    </div>
                </div>
                <div class="register-col">
                    <div class="register-group">
                        <!-- 이메일 사번으로 자동 생성됨을 안내 -->
                        <label class="register-label">이메일</label>
                        <div class="email-info">이메일은 사번@flokr.com 형식으로 자동 생성됩니다.</div>
                    </div>
                </div>
            </div>
            
            <div class="register-row">
                <div class="register-col">
                    <div class="register-group">
                        <label class="register-label">부서</label>
                        <select name="deptNo" id="deptNo" class="register-select" required>
                            <option value="">부서 선택</option>
                            <c:forEach var="dept" items="${deptList}">
                                <option value="${dept.deptNo}">${dept.deptName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="register-col">
                    <div class="register-group">
                        <label class="register-label">직급</label>
                        <select name="positionNo" class="register-select" required>
                            <option value="">직급 선택</option>
                            <c:forEach var="position" items="${positionList}">
                                <option value="${position.positionNo}">${position.positionName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>
            
            <div class="register-group">
                <p class="password-hint" id="passwordHint">초기 비밀번호는 사번 + "init"으로 자동 생성됩니다.</p>
                <p class="auto-generate-info">사번은 부서코드+연도+순번으로 자동 생성됩니다.</p>
            </div>
            
            <!-- 관리자 여부 필드 제거, 기본값 N으로 설정 -->
            <input type="hidden" name="isAdmin" value="N">
            
            <div class="register-btn-container">
                <button type="submit" class="register-btn-primary">등록하기</button>
            </div>
        </form>
    </div>

    <script>
        $(document).ready(function() {
            // 숫자만 입력되도록 제한
            $(".phone-input").on("input", function() {
                $(this).val($(this).val().replace(/[^0-9]/g, ""));
            });
            
            // 현재 년도 가져오기 (2자리)
            var currentYear = new Date().getFullYear().toString().substr(2);
            
            // 부서 선택 시 사번 예시 표시
            $("#deptNo").change(function() {
                var deptNo = $(this).val();
                if(deptNo) {
                    // 서버에서 해당 부서의 마지막 사번 조회
                    $.ajax({
                        url: "${pageContext.request.contextPath}/getLastEmpId",
                        data: {deptNo: deptNo, yearPrefix: currentYear},
                        success: function(data) {
                            // data는 마지막 순번 (예: 001, 002 등)
                            var newSequence = parseInt(data || "0") + 1;
                            var sequenceStr = String(newSequence).padStart(3, '0');
                            var newEmpId = deptNo + currentYear + sequenceStr;
                            
                            // 초기 비밀번호 힌트 업데이트
                            $("#passwordHint").text("초기 비밀번호는 사번 + \"init\"으로 자동 생성됩니다. 예: " + newEmpId + "init");
                            $(".auto-generate-info").text("사번은 부서코드+연도+순번으로 자동 생성됩니다. 예: " + newEmpId);
                            $(".email-info").text("이메일은 " + newEmpId + "@flokr.com 형식으로 자동 생성됩니다.");
                        }
                    });
                }
            });
            
            // 폼 제출 전 유효성 검사
            $("#employeeForm").submit(function(e) {
                // 부서 선택 확인
                if(!$("#deptNo").val()) {
                    alert("부서를 선택해주세요.");
                    e.preventDefault();
                    return false;
                }
                
                // 전화번호 유효성 검사
                var phone2 = $("input[name='phone2']").val();
                var phone3 = $("input[name='phone3']").val();
                
                if(phone2 && (phone2.length < 3 || phone2.length > 4)) {
                    alert("전화번호 두번째 자리는 3~4자리여야 합니다.");
                    e.preventDefault();
                    return false;
                }
                
                if(phone3 && phone3.length !== 4) {
                    alert("전화번호 세번째 자리는 4자리여야 합니다.");
                    e.preventDefault();
                    return false;
                }
                
                return true;
            });
        });
    </script>
</body>
</html>