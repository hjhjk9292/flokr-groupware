<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>사원 목록</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/employeeList.css">
    <!-- jQuery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <jsp:include page="../common/header.jsp"/>
    
    <div class="employee-container">
        <div class="page-title">
            <h2>사원 목록</h2>
            <p>등록된 사원 정보를 조회하고 관리할 수 있습니다.</p>
        </div>
        
        <!-- 검색 필터 -->
        <div class="search-filter">
            <form action="${pageContext.request.contextPath}/employeeList" method="get" class="form-inline">
                <div class="form-group mr-3">
                    <label for="deptNo" class="mr-2">부서</label>
                    <select name="deptNo" id="deptNo" class="form-control">
                        <option value="">전체</option>
                        <c:forEach var="dept" items="${deptList}">
                            <option value="${dept.deptNo}" ${selectedDeptNo eq dept.deptNo ? 'selected' : ''}>${dept.deptName}</option>
                        </c:forEach>
                    </select>
                </div>
                
                <div class="form-group mr-3">
                    <label for="searchType" class="mr-2">검색 조건</label>
                    <select name="searchType" id="searchType" class="form-control">
                        <option value="name" ${searchType eq 'name' ? 'selected' : ''}>이름</option>
                        <option value="id" ${searchType eq 'id' ? 'selected' : ''}>사번</option>
                        <option value="email" ${searchType eq 'email' ? 'selected' : ''}>이메일</option>
                    </select>
                </div>
                
                <div class="form-group mr-3">
                    <input type="text" name="keyword" id="keyword" class="form-control" placeholder="검색어 입력" value="${keyword}">
                </div>
                
                <div class="status-filter w-100 mt-2 mb-2">
                    <div class="custom-control custom-radio custom-control-inline">
                        <input type="radio" id="statusAll" name="statusFilter" value="all" class="custom-control-input" ${statusFilter eq 'all' ? 'checked' : ''}>
                        <label class="custom-control-label" for="statusAll">전체 보기</label>
                    </div>
                    <div class="custom-control custom-radio custom-control-inline">
                        <input type="radio" id="statusActive" name="statusFilter" value="active" class="custom-control-input" ${empty statusFilter || statusFilter eq 'active' ? 'checked' : ''}>
                        <label class="custom-control-label" for="statusActive">재직자만</label>
                    </div>
                    <div class="custom-control custom-radio custom-control-inline">
                        <input type="radio" id="statusInactive" name="statusFilter" value="inactive" class="custom-control-input" ${statusFilter eq 'inactive' ? 'checked' : ''}>
                        <label class="custom-control-label" for="statusInactive">퇴사자만</label>
                    </div>
                </div>
                
                <div class="w-100 mt-2">
                    <button type="submit" class="btn btn-primary mr-2">검색</button>
                    <button type="button" id="resetSearchBtn" class="btn btn-outline-secondary">초기화</button>
                </div>
            </form>
        </div>
        
        <!-- 사원 목록 테이블 -->
        <div class="table-container">
            <table class="employee-table">
                <thead>
                    <tr>
                        <th>프로필</th>
                        <th>사번</th>
                        <th>이름</th>
                        <th>부서</th>
                        <th>직급</th>
                        <th>이메일</th>
                        <th>전화번호</th>
                        <th>입사일</th>
                        <th>상태</th>
                        <th>관리</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty employeeList}">
                            <tr>
                                <td colspan="10" class="text-center">등록된 사원이 없습니다.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="emp" items="${employeeList}">
                                <tr>
                                    <td class="profile-cell">
									    <c:choose>
									        <c:when test="${not empty emp.profileImgPath}">
									            <img src="${pageContext.request.contextPath}${emp.profileImgPath}" alt="프로필" class="profile-img">
									        </c:when>
									        <c:otherwise>
									            <%-- 스크립틀릿으로 색상 계산 --%>
									            <% 
									                String[] colors = {"#4285f4", "#34a853", "#ea4335", "#fbbc05", "#9c27b0"};
									                String empName = ((com.kh.flokrGroupware.employee.model.vo.Employee)pageContext.getAttribute("emp")).getEmpName();
									                char firstChar = empName.charAt(0);
									                int colorIndex = Math.abs(firstChar) % 5;
									                pageContext.setAttribute("bgColor", colors[colorIndex]);
									                pageContext.setAttribute("firstChar", String.valueOf(firstChar));
									            %>
									            
									            <div style="width: 40px; height: 40px; border-radius: 50%; background-color: ${bgColor}; display: flex; justify-content: center; align-items: center; border: 1px solid #eee;">
									                <span style="color: white; font-size: 16px; font-family: 'Noto Sans KR', sans-serif;">${firstChar}</span>
									            </div>
									        </c:otherwise>
									    </c:choose>
									</td>
                                    <td>${emp.empId}</td>
                                    <td>${emp.empName}</td>
                                    <td>${emp.deptName}</td>
                                    <td>${emp.positionName}</td>
                                    <td>${emp.email}</td>
                                    <td>${emp.phone}</td>
                                    <td>
                                        <fmt:formatDate value="${emp.hireDate}" pattern="yyyy-MM-dd"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${emp.status eq 'Y'}">
                                                <span class="status-badge status-active">재직</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-inactive">퇴사</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/employeeDetail/${emp.empNo}" class="action-btn detail">
                                            <i class="fas fa-search"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/employeeUpdate/${emp.empNo}" class="action-btn edit">
                                            <i class="fas fa-edit"></i>
                                        </a>
                                        <c:if test="${emp.status eq 'Y'}">
                                            <button type="button" class="action-btn delete" data-empno="${emp.empNo}" data-empname="${emp.empName}">
                                                <i class="fas fa-user-slash"></i>
                                            </button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
        
        <!-- 페이지네이션 -->
        <div class="pagination-container">
            <ul class="pagination">
                <c:if test="${pi.currentPage > 1}">
                    <li class="page-item">
                        <a class="page-link" href="${pageContext.request.contextPath}/employeeList?currentPage=${pi.currentPage - 1}&searchType=${searchType}&keyword=${keyword}&deptNo=${selectedDeptNo}&statusFilter=${statusFilter}" aria-label="Previous">
                            <span aria-hidden="true">&laquo;</span>
                        </a>
                    </li>
                </c:if>
                
                <c:forEach var="p" begin="${pi.startPage}" end="${pi.endPage}">
                    <li class="page-item ${p == pi.currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/employeeList?currentPage=${p}&searchType=${searchType}&keyword=${keyword}&deptNo=${selectedDeptNo}&statusFilter=${statusFilter}">${p}</a>
                    </li>
                </c:forEach>
                
                <c:if test="${pi.currentPage < pi.maxPage}">
                    <li class="page-item">
                        <a class="page-link" href="${pageContext.request.contextPath}/employeeList?currentPage=${pi.currentPage + 1}&searchType=${searchType}&keyword=${keyword}&deptNo=${selectedDeptNo}&statusFilter=${statusFilter}" aria-label="Next">
                            <span aria-hidden="true">&raquo;</span>
                        </a>
                    </li>
                </c:if>
            </ul>
        </div>
        
        <!-- 신규 등록 버튼 -->
        <div class="text-right mt-3">
            <a href="${pageContext.request.contextPath}/employeeRegister" class="btn btn-primary">
                <i class="fas fa-plus mr-1"></i> 신규 등록
            </a>
        </div>
    </div>
    
<script>
    $(document).ready(function() {
        // 검색 초기화 버튼
        $("#resetSearchBtn").click(function() {
            window.location.href = "${pageContext.request.contextPath}/employeeList";
        });
        
        // AJAX 퇴사 처리
        $(document).on("click", ".action-btn.delete", function(e) {
            e.preventDefault();
            var empNo = $(this).data("empno");
            var empName = $(this).data("empname");
            var $button = $(this);
            
            if(confirm(empName + " 님을 퇴사 처리하시겠습니까?\n처리 후에는 로그인이 불가능합니다.")) {
                // 로딩 표시 추가
                $button.html('<i class="fas fa-spinner fa-spin"></i>');
                $button.prop('disabled', true);
                
                $.ajax({
                    url: "${pageContext.request.contextPath}/employeeDeleteAjax",
                    type: "POST",
                    data: { empNo: empNo },
                    success: function(response) {
                        if(response.success) {
                            alert("퇴사 처리가 완료되었습니다.");
                            window.location.reload();
                        } else {
                            alert(response.message || "퇴사 처리에 실패했습니다.");
                            // 버튼 원상복구
                            $button.html('<i class="fas fa-user-slash"></i>');
                            $button.prop('disabled', false);
                        }
                    },
                    error: function(xhr, status, error) {
                        alert("퇴사 처리 중 오류가 발생했습니다: " + error);
                        // 버튼 원상복구
                        $button.html('<i class="fas fa-user-slash"></i>');
                        $button.prop('disabled', false);
                    }
                });
            }
        });
    });
</script>
</body>
</html>