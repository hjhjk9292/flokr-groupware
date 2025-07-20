<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Flokr 그룹웨어 로그인</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/loginForm.css">
<!-- jQuery 라이브러리 -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<!-- JavaScript (Alertify) -->
<script src="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/alertify.min.js"></script>
<!-- CSS (Alertify) -->
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/alertify.min.css"/>
<!-- Default theme (Alertify) -->
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.14.0/build/css/themes/default.min.css"/>
<!-- Font Awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <!-- 세션에 메시지가 있다면 alertify로 표시하고 세션에서 제거 -->
    <c:if test="${not empty alertMsg}">
        <script>
            alertify.alert("${alertMsg}");
        </script>
        <c:remove var="alertMsg" scope="session"/>
    </c:if>

    <div class="login-container">
        <div class="login-logo">
            <img src="${pageContext.request.contextPath}/resources/images/Flokr_logo.png" alt="Flokr">
        </div>
        
        <h1 class="login-title">로그인</h1>
        <p class="login-subtitle">계정 정보를 입력하여 로그인하세요</p>
        
        <form action="login.me" method="post">
            <div class="input-group">
                <label class="input-label">아이디</label>
                <div class="input-addon">
                    <i class="fas fa-user input-icon"></i>
                    <input type="text" name="empId" class="input-field input-with-icon" placeholder="아이디를 입력하세요" required>
                </div>
            </div>
            
            <div class="input-group">
                <label class="input-label">비밀번호</label>
                <div class="input-addon">
                    <i class="fas fa-lock input-icon"></i>
                    <input type="password" name="passwordHash" class="input-field input-with-icon" placeholder="비밀번호를 입력하세요" required>
                </div>
            </div>
            
            <div class="login-options">
                <label class="remember-me">
                    <input type="checkbox" name="remember"> 아이디 저장
                </label>
            </div>
            
            <button type="submit" class="login-button">로그인</button>
        </form>
        
        <div class="login-footer">
            © 2025 Flokr Groupware. All rights reserved.<br>
            <small>문의: support@flokr.com / 02-1234-5678</small>
        </div>
    </div>

    <script>
        // 아이디 저장 기능
        $(document).ready(function() {
            // 저장된 쿠키값 가져와서 ID 칸에 넣기
            var userInputId = getCookie("userInputId");
            $("input[name='empId']").val(userInputId); 
            
            if($("input[name='empId']").val() != ""){ // 그 전에 ID를 저장해서 처음 페이지 로딩 시 입력란에 저장된 ID가 표시된 상태라면,
                $("input[name='remember']").prop("checked", true); // ID 저장하기를 체크 상태로 두기
            }
            
            $("input[name='remember']").change(function(){ // 체크박스에 변화가 있다면,
                if($(this).is(":checked")){ // ID 저장하기 체크했을 때,
                    var userInputId = $("input[name='empId']").val();
                    setCookie("userInputId", userInputId, 7); // 7일 동안 쿠키 보관
                }else{ // ID 저장하기 체크 해제 시,
                    deleteCookie("userInputId");
                }
            });
            
            // ID 저장하기를 체크한 상태에서 ID를 입력하는 경우, 쿠키 저장
            $("input[name='empId']").keyup(function(){ // ID 입력 칸에 ID를 입력할 때,
                if($("input[name='remember']").is(":checked")){ // ID 저장하기를 체크한 상태라면,
                    var userInputId = $("input[name='empId']").val();
                    setCookie("userInputId", userInputId, 7); // 7일 동안 쿠키 보관
                }
            });
        });

        function setCookie(cookieName, value, exdays){
            var exdate = new Date();
            exdate.setDate(exdate.getDate() + exdays);
            var cookieValue = escape(value) + ((exdays==null) ? "" : "; expires=" + exdate.toGMTString());
            document.cookie = cookieName + "=" + cookieValue;
        }

        function deleteCookie(cookieName){
            var expireDate = new Date();
            expireDate.setDate(expireDate.getDate() - 1);
            document.cookie = cookieName + "= " + "; expires=" + expireDate.toGMTString();
        }

        function getCookie(cookieName) {
            cookieName = cookieName + '=';
            var cookieData = document.cookie;
            var start = cookieData.indexOf(cookieName);
            var cookieValue = '';
            if(start != -1){
                start += cookieName.length;
                var end = cookieData.indexOf(';', start);
                if(end == -1)end = cookieData.length;
                cookieValue = cookieData.substring(start, end);
            }
            return unescape(cookieValue);
        }
    </script>
</body>
</html>