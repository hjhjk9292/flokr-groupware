<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="detail-wrapper">
  <!-- 상단 헤더 -->
  <div class="detail-header">
    <div class="title-left">
      <span class="emoji">${ task.emoji }</span>
      <h2>${ task.taskTitle }</h2>
    </div>
    <div class="header-actions">
      <button class="refresh-btn" onclick="toggleOptions(event)">
      	<span class="material-icons">
			more_horiz
		</span>
      </button>
      
      <div class="dropdown-options" id="actionMenu" style="display: none;">
	    <ul>
	      <li onclick="editTask('${task.taskNo}')">수정하기</li>
	      <li onclick="deleteTask('${task.taskNo}')">삭제하기</li>
	    </ul>
	  </div>
      
      <button class="close-btn" onclick="backToList()">
		<span class="material-icons">
			close
		</span>
      </button>
    </div>
  </div>

  <!-- 본문 -->
  <div class="detail-body">
    <!-- 왼쪽 업무 내용 -->
	<div class="left-pane">
	  <div class="rowLeft">
	    <label>업무 내용</label>
	    <div class="content-box">${ task.taskContent }</div>
	  </div>
	
	<div class="rowLeft">
	  <label>첨부파일</label>
	  <div class="attachment-box">
	    <c:if test="${not empty atmt}">
	      <a href="${pageContext.request.contextPath}/${atmt.storedFilepath}" 
	         download="${atmt.originalFilename}" 
	         class="file-link">
	         📎 ${atmt.originalFilename}
	      </a>
	    </c:if>
	    <c:if test="${empty atmt}">
	      <span class="no-file">첨부된 파일이 없습니다</span>
	    </c:if>
	  </div>
	</div>

	</div>


    <!-- 오른쪽 정보 영역 -->
    <div class="right-pane">
      <div class="rowRight">
        <label>카테고리</label>
        <select class="dropdown" disabled>
          <option selected>${ task.category }</option>
        </select>
      </div>

      <div class="rowRight">
        <label>상태</label>
        <div class="status-tags">
		  <span class="tag 요청 ${statusKor eq '요청' ? 'active' : ''}">요청</span>
		  <span class="tag 진행중 ${statusKor eq '진행중' ? 'active' : ''}">진행 중</span>
		  <span class="tag 피드백 ${statusKor eq '피드백' ? 'active' : ''}">피드백</span>
		  <span class="tag 보류 ${statusKor eq '보류' ? 'active' : ''}">보류</span>
		  <span class="tag 완료 ${statusKor eq '완료' ? 'active' : ''}">완료</span>
		</div>
      </div>

      <div class="rowRight">
        <label>마감일</label>
        <input type="date" value="${ task.dueDate }" disabled class="date-input" />
      </div>

      <div class="rowRight">
		  <label>담당자</label>
		  <div class="detail-assignees">
		    <c:choose>
		      <c:when test="${not empty assignees}">
		        <!-- 처음 3명만 표시 -->
		        <c:forEach var="assignee" items="${assignees}" varStatus="status">
		          <c:if test="${status.index < 3}">
		            <!-- 랜덤 색상 클래스 -->
		            <c:set var="colorClasses" value="red,blue,green,purple,orange,teal" />
		            <c:set var="colorArray" value="${fn:split(colorClasses, ',')}" />
		            <c:set var="randomColorIndex" value="${status.index % fn:length(colorArray)}" />
		            <c:set var="randomColor" value="${colorArray[randomColorIndex]}" />
		            
		            <!-- 이름 첫 글자 가져오기 -->
		            <c:set var="initial" value="${fn:substring(assignee.empName, 0, 1)}" />
		            
		            <div class="detail-avatar ${randomColor}" title="${assignee.empName}" data-emp-name="${assignee.empName}" data-emp-no="${assignee.assigneeEmpNo}" data-phone="${assignee.phone}" data-email="${assignee.email}" data-dept="${assignee.deptName}" data-position="${assignee.positionName}">
		              ${initial}
		              
		              <!-- 사원 정보 카드 (기본적으로 숨겨짐) -->
		              <div class="employee-card">
		                <div class="card-avatar">${initial}</div>
		                <div class="card-info">
		                  <div class="emp-name">${assignee.empName}</div>
		                  <div class="emp-position">${assignee.deptName} (${assignee.positionName})</div>
		                  <div class="emp-phone">${assignee.phone}</div>
		                  <div class="emp-email">${assignee.email}</div>
		                </div>
		              </div>
		            </div>
		          </c:if>
		        </c:forEach>
		        
		        <!-- 3명 초과인 경우 +N 표시 -->
		        <c:if test="${fn:length(assignees) > 3}">
		          <div class="detail-avatar empty" title="추가 담당자">
		            +${fn:length(assignees) - 3}
		          </div>
		        </c:if>
		      </c:when>
		      <c:otherwise>
		        <!-- 담당자가 없는 경우 -->
		        <div class="detail-avatar empty">없음</div>
		      </c:otherwise>
		    </c:choose>
		  </div>
		</div>
    </div>
  </div>


</div>
