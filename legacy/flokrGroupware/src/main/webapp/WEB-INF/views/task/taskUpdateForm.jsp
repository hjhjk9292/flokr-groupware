<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form id="update-task-form" action="${pageContext.request.contextPath}/task/update" method="post" enctype="multipart/form-data">
<input type="hidden" name="taskWriter" value="${ loginUser.empNo }" />
<input type="hidden" name="taskNo" value="${ task.taskNo }" />
  <div class="insert-wrapper">
    <!-- 상단 헤더 -->
    <div class="insert-header">
      <div class="insert-title-left">
        <button type="button" class="emoji-btn">
        <c:choose>
	        <c:when test="${ task.emoji eq null }">
	        	<span class="material-icons">
					add_circle_outline
				</span>
			</c:when>
			<c:otherwise>
				<span>
					${ task.emoji }
				</span>
			</c:otherwise>
		</c:choose>
        </button>
        <input type="hidden" name="emoji" id="selectedEmoji" value="${ task.emoji }" />
        <input type="text" name="taskTitle" class="insert-title-input" value="${ task.taskTitle }" required/>
      </div>
      <div class="insert-header-actions">
        <button type="button" class="close-btn" onclick="backToList()">✕</button>
      </div>
    </div>

    <!-- 본문 -->
    <div class="insert-body">
      <!-- 왼쪽 업무 내용 -->
      <div class="insert-left-pane">
        <div class="update-rowLeft">
          <label>업무 내용</label>
          <textarea name="taskContent" class="insert-content-box" required>${ task.taskContent }</textarea>
        </div>

        <div class="update-rowLeft">
        <label>첨부파일</label>
	        <div class="custom-file-wrapper">
			  <label class="fake-file-label">
			    파일 선택
			    <input type="file" name="reUploadfile" class="real-file-input" onchange="updateFileName(this)" />
			  </label>
			  <input type="hidden" name="originalFilename" value="${atmt.originalFilename}">
			  <input type="hidden" name="storedFilepath" value="${atmt.storedFilepath}">
			  <input type="hidden" name="attachmentNo" value="${atmt.attachmentNo}">

			  <span class="file-name-text" id="fileName">
			    <c:choose>
			      <c:when test="${not empty atmt.originalFilename}">
			        ${atmt.originalFilename}
			      </c:when>
			      <c:otherwise>
			        선택된 파일 없음
			      </c:otherwise>
			    </c:choose>
			  </span>
			</div>
        </div>
      </div>

      <!-- 오른쪽 정보 영역 -->
      <div class="insert-right-pane">
        <div class="update-rowRight">
          <label>카테고리</label>
			<select name="category" class="insert-dropdown">
			<option value="" ${ empty task.category ? 'selected' : ''}>선택하세요</option>
			<option value="디자인" ${task.category eq '디자인' ? 'selected' : ''}>디자인</option>
			<option value="데이터" ${task.category eq '데이터' ? 'selected' : ''}>데이터</option>
			<option value="개발" ${task.category eq '개발' ? 'selected' : ''}>개발</option>
			<option value="기획" ${task.category eq '기획' ? 'selected' : ''}>기획</option>
			</select>
        </div>
        
	    <div class="update-rowRight">
		    <label>상태</label>
		    <div class="status-tags">
				<span class="tag 요청 ${statusKor eq '요청' ? 'active' : ''}" onclick="selectStatus('REQUEST', this)">요청</span>
				<span class="tag 진행중 ${statusKor eq '진행중' ? 'active' : ''}" onclick="selectStatus('IN_PROGRESS', this)">진행 중</span>
				<span class="tag 피드백 ${statusKor eq '피드백' ? 'active' : ''}" onclick="selectStatus('FEEDBACK', this)">피드백</span>
				<span class="tag 보류 ${statusKor eq '보류' ? 'active' : ''}" onclick="selectStatus('HOLD', this)">보류</span>
				<span class="tag 완료 ${statusKor eq '완료' ? 'active' : ''}" onclick="selectStatus('DONE', this)">완료</span>
			</div>
			<input type="hidden" name="taskStatus" id="taskStatus" value="${ task.taskStatus }" />
	    </div>

        <div class="update-rowRight">
          <label>마감일</label>
          <input type="date" name="dueDate" class="insert-date-input" value="${ task.dueDate }"/>
        </div>

        <div class="insert-btn-group">
          <button type="button" onclick="submitUpdate()" class="red-btn">등록</button>
        </div>
      </div>

    </div>
  </div>

</form>
