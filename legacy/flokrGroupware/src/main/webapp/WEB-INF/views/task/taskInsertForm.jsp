<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form id="insert-task-form" action="${pageContext.request.contextPath}/task/insert" method="post" enctype="multipart/form-data">
<input type="hidden" name="taskWriter" value="${ loginUser.empNo }" />
  <div class="insert-wrapper">
    <!-- 상단 헤더 -->
    <div class="insert-header">
      <div class="insert-title-left">
        <button type="button" class="emoji-btn">
        	<span class="material-icons">
				add_circle_outline
			</span>
        </button>
        <input type="hidden" name="emoji" id="selectedEmoji" value=""/>
        <input type="text" name="taskTitle" class="insert-title-input" placeholder="제목을 입력하세요" required/>
      </div>
      <div class="insert-header-actions">
        <button type="button" class="close-btn" onclick="backToList()">✕</button>
      </div>
    </div>

    <!-- 본문 -->
    <div class="insert-body">
      <!-- 왼쪽 업무 내용 -->
      <div class="insert-left-pane">
        <div class="insert-rowLeft">
          <label>업무 내용</label>
          <textarea name="taskContent" class="insert-content-box" placeholder="업무 내용을 입력하세요" required></textarea>
        </div>

        <div class="insert-rowLeft">
          <label>첨부파일</label>
          <div class="insert-attachment-box">
            <input type="file" name="uploadFile"/>
          </div>
        </div>
      </div>

      <!-- 오른쪽 정보 영역 -->
      <div class="insert-right-pane">
        <div class="insert-rowRight">
          <label>카테고리</label>
          <select name="category" class="insert-dropdown">
            <option value="">선택하세요</option>
            <option value="디자인">디자인</option>
            <option value="데이터">데이터</option>
            <option value="개발">개발</option>
            <option value="기획">기획</option>
          </select>
        </div>

        <div class="insert-rowRight">
          <label>마감일</label>
          <input type="date" name="dueDate" class="insert-date-input" required/>
        </div>

        <div class="insert-rowRight">
		  <label>담당자</label>
		  <div style="display: flex; align-items: center; gap: 8px; margin-top: 6px;">
		    <div class="assignees" id="selected-assignees" style="margin: 0;">
		      <!-- 선택된 담당자 아바타가 여기에 동적으로 생성됨 -->
		    </div>
		    <button type="button" class="round-icon-btn" onclick="openAssigneeModal()" style="margin-top: 0;">
		      <span class="material-icons">person_add</span>
		    </button>
		  </div>
		</div>
        
        <!-- 선택된 empNo들을 서버로 보낼 hidden input -->
		<input type="hidden" name="assignees" id="assignees-hidden" />

        <div class="insert-btn-group">
          <button type="button" class="gray-btn" onclick="resetInsertForm()">초기화</button>
          <button type="submit" class="red-btn">등록</button>
        </div>
      </div>

    </div>
  </div>

</form>

<div id="modal-backdrop"></div>

  <div id="assignee-modal" class="modal" style="display:none;">
	  <div class="modal-content">
	    <div class="modal-header">
	      <h3>담당자 선택</h3>
	      <button onclick="closeAssigneeModal()">×</button>
	    </div>
	    <div class="modal-body">
	      <div id="employee-list">
	        <!-- 부서별 직원이 여기에 들어감 -->
	      </div>
	    </div>
	    <div class="modal-footer">
	      <button onclick="confirmAssigneeSelection()">확인</button>
	    </div>
	  </div>
	</div>
