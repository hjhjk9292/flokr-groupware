<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script
	src="https://cdn.jsdelivr.net/npm/@joeattardi/emoji-button@3.0.3/dist/index.min.js"></script>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/taskListView.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/taskDetailView.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/taskInsertForm.css">
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"
	rel="stylesheet">
<style>
  .hidden {
    display: none;
  }
</style>
</head>
<body>

	<jsp:include page="../common/header.jsp" />

	<br>

	<div class="outer">
		<div class="content-container">
			<div class="left-section">
				<!-- 왼쪽 내용 들어가는 자리 -->
				<div class="sidebar-title">업무 관리</div>
				<div class="sidebar-subtitle">업무를 한눈에 확인하고 관리하세요.</div>

				<a id="add-task-btn" class="add-btn">+ 새 업무 추가</a> <a
					href="${pageContext.request.contextPath}/task/list"
					class="nav-item"> <svg xmlns="http://www.w3.org/2000/svg"
						fill="none" stroke="currentColor" stroke-width="2"
						viewBox="0 0 24 24">
					    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
					  </svg> 홈
				</a>


				<div class="left-scrollable">
					<div class="section-divider"></div>
					<div class="section-title"
						onclick="toggleSection('in-progress', this)">
						진행 중인 업무 목록
						<svg class="arrow rotate" xmlns="http://www.w3.org/2000/svg"
							fill="none" stroke="currentColor" stroke-width="2"
							viewBox="0 0 24 24">
						        <path d="M6 9l6 6 6-6" />
						    </svg>
					</div>

					<div class="task-group" id="in-progress">
						<c:forEach var="list" items="${ list }">
							<div class="task-subitem" data-task-id="${list.taskNo}">${ list.emoji }
								${ list.taskTitle }</div>
						</c:forEach>
					</div>

					<div class="section-divider"></div>

					<div class="section-title"
						onclick="toggleSection('completed', this)">
						완료된 업무 목록
						<svg class="arrow" xmlns="http://www.w3.org/2000/svg" fill="none"
							stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
						        <path d="M6 9l6 6 6-6" />
						    </svg>
					</div>

					<div class="task-group closed" id="completed">
						<div class="task-subitem">📌 프로젝트 기획 및 요구사항 분석</div>
					</div>
				</div>
			</div>

			<div class="right-section">
				<!-- 오른쪽 내용 들어가는 자리 -->
				<div id="task-list-view">
					<!-- 기존 검색창 + 카드 목록 들어가는 영역 -->
					<!-- 검색창 -->
					<div class="search-header">
						<div class="search-input-group">
							<svg class="search-icon" xmlns="http://www.w3.org/2000/svg"
								width="16" height="16" fill="none" stroke="currentColor"
								stroke-width="2" viewBox="0 0 24 24">
					            <circle cx="11" cy="11" r="8" />
					            <line x1="21" y1="21" x2="16.65" y2="16.65" />
					        </svg>
							<input type="text" id="autocomplete-input"
								class="search-input-modern" placeholder="검색어를 입력하세요"
								value="${keyword}">
							<ul id="autocomplete-results"></ul>

							<button class="search-btn" title="검색">
								<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"
									fill="none" stroke="white" stroke-width="2" viewBox="0 0 24 24">
							        <circle cx="11" cy="11" r="8" />
							        <line x1="21" y1="21" x2="16.65" y2="16.65" />
							    </svg>
							</button>
						</div>
					</div>


					<!-- 검색 정보 -->
					<div class="search-info hidden">
					</div>

					<div class="search-result-count hidden">
					</div>

					<!-- 업무 카드 목록 -->
					<div class="task-cards">
						<!-- 카드 1 -->
						<c:forEach var="list" items="${ list }">
							<div class="task-card" data-task-id="${list.taskNo}">
								<div class="task-header">
									<span class="task-title">${ list.emoji } ${ list.taskTitle }</span>
								</div>
								<div class="task-meta">
									<div>
										<strong>업무 담당자:</strong> 
										<c:choose>
								            <c:when test="${empty list.assignees}">
								                담당자 없음
								            </c:when>
								            <c:otherwise>
								                <c:set var="count" value="0" />
								                <c:forEach var="assignee" items="${list.assignees}" varStatus="status">
								                    <c:if test="${count < 3}">
								                        ${assignee.empName}<c:if test="${!status.last && count < 2 && fn:length(list.assignees) > count+1}">, </c:if>
								                        <c:set var="count" value="${count + 1}" />
								                    </c:if>
								                </c:forEach>
								                <c:if test="${fn:length(list.assignees) > 3}">
								                    외 ${fn:length(list.assignees) - 3}명
								                </c:if>
								            </c:otherwise>
								        </c:choose>
									</div>
									<div>
										<strong>카테고리:</strong> ${ list.category }
									</div>
									<div>
										<strong>마감일:</strong> ${ list.dueDate }
									</div>

									<div>
										<strong>상태:</strong> <span
											class="badge ${statusColorMap[list.taskStatus]}">
											${statusNameMap[list.taskStatus]} </span>
									</div>
								</div>
							</div>
						</c:forEach>
					</div>
				</div>

				<div id="task-detail-view" style="display: none;"></div>

				<div id="task-insert-view" style="display: none;"></div>

				<div id="task-update-view" style="display: none;"></div>

			</div>
		</div>
	</div>
	
	<script>
	function toggleSection(id, triggerElement) {
	    const section = document.getElementById(id);
	    const arrow = triggerElement.querySelector('.arrow');
	    const isClosed = section.classList.contains('closed');

	    section.classList.toggle('closed', !isClosed);
	    section.style.maxHeight = isClosed ? section.scrollHeight + 'px' : '0px';
	    if (arrow) arrow.classList.toggle('rotate', isClosed);
	  }

	  function loadTaskDetail(taskId, clickedItem) {
		  document.getElementById("task-list-view").style.display = "none";
		  document.getElementById("task-detail-view").style.display = "block";
		  document.getElementById("task-insert-view").style.display = "none";
		  document.getElementById("task-update-view").style.display = "none";

		  // 수정: 템플릿 리터럴을 사용하지 않고 문자열 연결 사용
		  const detailUrl = "${pageContext.request.contextPath}/task/detail?taskId=" + taskId;
		  fetch(detailUrl)
		    .then(res => res.text())
		    .then(html => {
		      document.getElementById("task-detail-view").innerHTML = html;
		      initEmployeeCards();
		    });

		  document.querySelectorAll('.task-subitem').forEach(item => {
		    item.classList.remove('selected');
		  });

		  const matchingListItem = document.querySelector('.task-subitem[data-task-id="' + taskId + '"]');
		  if (matchingListItem) {
		    matchingListItem.classList.add('selected');
		  }
		}
	  
	  function initEmployeeCards() {
		  console.log('담당자 카드 기능 초기화');
		  const avatars = document.querySelectorAll('.detail-avatar');
		  
		  // 스타일 추가
		  const style = document.createElement('style');
		  style.textContent = `
		    .employee-card {
		      opacity: 0;
		      transform: translateX(-50%) translateY(10px);
		      transition: opacity 0.3s ease, transform 0.3s ease;
		    }
		    
		    .employee-card.visible {
		      opacity: 1;
		      transform: translateX(-50%) translateY(0);
		    }
		  `;
		  document.head.appendChild(style);
		  
		  avatars.forEach(avatar => {
		    // 마우스 진입 시 카드 표시 및 위치 조정
		    avatar.addEventListener('mouseenter', function() {
		      const card = this.querySelector('.employee-card');
		      if (card) {
		        // 현재 아바타의 위치 계산
		        const avatarRect = this.getBoundingClientRect();
		        
		        // 카드 위치 설정 - 아바타 위에 표시
		        card.style.top = (avatarRect.top - 225) + 'px'; // 카드 높이 + 여백 고려
		        card.style.left = (avatarRect.left + (avatarRect.width / 2)) + 'px';
		        
		        // 카드 표시 - 먼저 display 속성 변경
		        card.style.display = 'block';
		        
		        // 약간의 지연 후 가시성 클래스 추가 (트랜지션 효과를 위해)
		        setTimeout(() => {
		          card.classList.add('visible');
		        }, 10);
		      }
		    });
		    
		    // 마우스 이탈 시 카드 숨김
		    avatar.addEventListener('mouseleave', function() {
		      const card = this.querySelector('.employee-card');
		      if (card) {
		        // 먼저 가시성 클래스 제거 (트랜지션 효과를 위해)
		        card.classList.remove('visible');
		        
		        // 트랜지션이 완료된 후 display 속성 변경
		        setTimeout(() => {
		          card.style.display = 'none';
		        }, 300); // 트랜지션과 동일한 시간(0.3초)
		      }
		    });
		  });
		}


	  function loadTaskInsert() {
	    document.getElementById("task-list-view").style.display = "none";
	    document.getElementById("task-detail-view").style.display = "none";
	    document.getElementById("task-insert-view").style.display = "block";
	    document.getElementById("task-update-view").style.display = "none";

	    fetch("${pageContext.request.contextPath}/task/insertForm")
	      .then(res => res.text())
	      .then(html => {
	        document.getElementById("task-insert-view").innerHTML = html;
	        
	        const form = document.getElementById("insert-task-form");
	        if (form) {
	          form.addEventListener("submit", function (e) {
	            e.preventDefault();
	            console.log("✅ insert-task-form 제출 시도됨!");

	            const formData = new FormData(form);
	            for (let [key, value] of formData.entries()) {
	              console.log(`${key} →`, value instanceof File ? value.name : value);
	            }

	            // 진짜 서버 전송
	            fetch(form.action, {
	              method: "POST",
	              body: formData
	            })
	              .then(res => {
	                if (!res.ok) throw new Error("서버 오류");
	                return res.text();
	              })
	              .then(resp => {
	                console.log("업무 등록 완료", resp);
	                location.href = `${pageContext.request.contextPath}/task/list`;
	              })
	              .catch(err => {
	                console.error("등록 실패", err);
	              });
	          });
	        } else {
	          console.warn("❌ insert-task-form을 찾지 못함");
	        }
	        
	        // 폼이 로드된 직후에 직원 목록 로드 - DOMContentLoaded 이벤트를 기다리지 않음
	        loadEmployeeList();
	        
	        // 이모지 피커 초기화
	        initEmojiPicker();
	      });

	    // 삽입 모드일 땐 좌측 리스트 선택 해제
	    document.querySelectorAll('.task-subitem').forEach(item => {
	      item.classList.remove('selected');
	    });
	  }

	  // 직원 목록을 로드하는 함수
	  function loadEmployeeList() {
	    fetch("${pageContext.request.contextPath}/task/employeeList")
	      .then(res => {
	        if (!res.ok) {
	          throw new Error('네트워크 응답이 정상적이지 않습니다');
	        }
	        return res.json();
	      })
	      .then(data => {
	        console.log("직원 데이터 로드됨:", data);
	        allEmployees = data;
	        renderEmployeeModal(data);
	      })
	      .catch(error => {
	        console.error("직원 로드 중 오류 발생:", error);
	        alert("직원 목록을 불러오는데 실패했습니다.");
	      });
	  }

	  // 이모지 피커 초기화 함수
	  function initEmojiPicker() {
	    const emojiButton = document.querySelector('.emoji-btn');
	    const selectedEmojiInput = document.getElementById('selectedEmoji');
	    
	    if (emojiButton && selectedEmojiInput) {
	      const picker = new EmojiButton({
	        position: 'bottom-start',
	        theme: 'light'
	      });

	      picker.on('emoji', selection => {
	        emojiButton.innerHTML = selection;
	        selectedEmojiInput.value = selection;
	      });

	      emojiButton.addEventListener('click', () => {
	        picker.togglePicker(emojiButton);
	      });
	    }
	  }

	  function backToList() {
	    document.getElementById("task-detail-view").style.display = "none";
	    document.getElementById("task-insert-view").style.display = "none";
	    document.getElementById("task-list-view").style.display = "block";
	    document.getElementById("task-update-view").style.display = "none";

	    document.querySelectorAll('.task-subitem').forEach(item => {
	      item.classList.remove('selected');
	    });
	  }

	  function resetInsertForm() {
	    document.querySelector('.insert-title-input').value = '';
	    document.querySelector('.insert-content-box').value = '';
	    document.querySelector('.insert-dropdown').selectedIndex = 0;
	    document.querySelector('.insert-date-input').value = '';
	    document.querySelector('#selectedEmoji').value = '';
	    document.querySelector('.emoji-btn').innerHTML = '<span class="material-icons">add_circle_outline</span>';
	    document.querySelector('.insert-attachment-box input[type="file"]').value = '';
	    
	    // 담당자 초기화
	    selectedEmployees = [];
	    renderSelectedAvatars();
	    updateAssigneeHiddenInput();
	  }

	  // 새 업무 추가 버튼
	  document.getElementById("add-task-btn").addEventListener("click", function(e) {
	    e.preventDefault();
	    loadTaskInsert();
	  });

	  window.addEventListener('DOMContentLoaded', function () {
		  // 검색 정보와 결과 카운트 영역 숨기기
		  document.querySelector(".search-info").classList.add("hidden");
		  document.querySelector(".search-result-count").classList.add("hidden");
		  
		  // 모달 및 UI에 필요한 스타일 추가
		  addRequiredStyles();
		  
		  // ✅ 등록 실패 시 처리
		  fetch("${pageContext.request.contextPath}/task/checkFailFlag")
		    .then(res => res.json())
		    .then(fail => {
		      if (fail) {
		        alertify.alert("업무 등록에 실패했습니다. 다시 시도해주세요.", function () {
		          loadTaskInsert();
		        });
		      }
		    });

		  // ✅ 리스트 클릭 바인딩
		  document.querySelectorAll('.task-subitem').forEach(item => {
		    item.addEventListener('click', function () {
		      const taskId = this.dataset.taskId;
		      loadTaskDetail(taskId, this);
		    });
		  });

		  // ✅ 카드 클릭 바인딩
		  document.querySelectorAll('.task-card').forEach(item => {
		    item.addEventListener('click', function () {
		      const taskId = this.dataset.taskId;
		      loadTaskDetail(taskId, null);
		    });
		  });
		});
	  
	  function toggleOptions(e) {
	    e.stopPropagation();
	    const menu = document.getElementById("actionMenu");
	    if (menu) {
	      menu.style.display = menu.style.display === "block" ? "none" : "block";
	    }
	  }

	  // 바깥 클릭 시 닫기
	  document.addEventListener("click", function() {
	    const menu = document.getElementById("actionMenu");
	    if (menu) {
	      menu.style.display = "none";
	    }
	  });
		
	  function editTask(taskId) {
	    // 뷰 상태 변경
	    document.getElementById("task-list-view").style.display = "none";
	    document.getElementById("task-detail-view").style.display = "none";
	    document.getElementById("task-insert-view").style.display = "none";
	    document.getElementById("task-update-view").style.display = "block";

	    // AJAX로 폼 로딩 - 수정: 템플릿 리터럴을 사용하지 않고 문자열 연결 사용
	    const updateUrl = "${pageContext.request.contextPath}/task/updateForm?taskId=" + taskId;
	    fetch(updateUrl)
	      .then(res => res.text())
	      .then(html => {
	        document.getElementById("task-update-view").innerHTML = html;
	        
	        const emojiButton = document.querySelector('.emoji-btn');
	        const selectedEmojiInput = document.getElementById('selectedEmoji');
	        if (emojiButton && selectedEmojiInput) {
	          const picker = new EmojiButton({
	            position: 'bottom-start',
	            theme: 'light'
	          });

	          picker.on('emoji', selection => {
	            emojiButton.innerText = selection;
	            selectedEmojiInput.value = selection;
	          });

	          emojiButton.addEventListener('click', () => {
	            picker.togglePicker(emojiButton);
	          });
	        }
	      });
	  }

	  function deleteTask(taskId) {
	    if (confirm("정말 삭제하시겠습니까?")) {
	      // 수정: 템플릿 리터럴을 사용하지 않고 문자열 연결 사용
	      location.href = "${pageContext.request.contextPath}/task/delete?taskId=" + taskId;
	    }
	  }
		
	  function updateFileName(input) {
	    const fileNameSpan = document.getElementById("fileName");
	    const fileName = input.files.length > 0 ? input.files[0].name : "선택된 파일 없음";
	    fileNameSpan.textContent = fileName;
	  }

	  function selectStatus(status, clickedSpan) {
	    document.getElementById("taskStatus").value = status;
	    document.querySelectorAll('.status-tags .tag').forEach(tag => tag.classList.remove('active'));
	    clickedSpan.classList.add('active');
	  }
		
	  function submitUpdate() {
	    const form = document.getElementById("update-task-form");
	    const formData = new FormData(form);

	    // 수정: 템플릿 리터럴을 사용하지 않고 문자열 연결 사용
	    const updateUrl = "${pageContext.request.contextPath}/task/update";
	    fetch(updateUrl, {
	      method: "POST",
	      body: formData
	    })
	    .then(async res => {
	      const contentType = res.headers.get("content-type");
	      if (res.ok && contentType && contentType.includes("application/json")) {
	        return res.json();
	      } else {
	        const text = await res.text();  // 만약 HTML 에러 페이지 같은 게 응답으로 오면 여기서 잡힘
	        throw new Error("Unexpected response:\n\n" + text);
	      }
	    })
	    .then(data => {
	      if (data.success) {
	        console.log("업무 수정 성공~");
	        location.href = "${pageContext.request.contextPath}/task/list";
	      } else {
	        console.log("업무 수정 실패");
	        location.href = "${pageContext.request.contextPath}/task/list";
	      }
	    })
	    .catch(err => {
	      console.error("업데이트 중 오류 발생", err);
	      alert("서버 오류로 수정에 실패했습니다.");
	    });
	  }
		
	  document.addEventListener("DOMContentLoaded", function () {
	    const input = document.getElementById("autocomplete-input");
	    const resultsContainer = document.getElementById("autocomplete-results");

	    input.addEventListener("input", function () {
	      const keyword = input.value.trim();
	      if (keyword.length < 1) {
	        resultsContainer.style.display = "none";
	        return;
	      }

	      // 수정된 코드: encodeURIComponent를 JavaScript 내에서만 사용
	      const autocompleteUrl = "${pageContext.request.contextPath}/autocomplete?prefix=" + encodeURIComponent(keyword);
	      fetch(autocompleteUrl)
	        .then(res => res.json())
	        .then(data => {
	          resultsContainer.innerHTML = "";
	          if (data.length === 0) {
	            resultsContainer.style.display = "none";
	            return;
	          }

	          data.forEach(suggestion => {
	            const li = document.createElement("li");
	            li.textContent = suggestion;
	            li.style.padding = "10px 16px";
	            li.style.cursor = "pointer";
	            li.style.borderBottom = "1px solid #eee";
	            li.style.fontSize = "14px";
	            li.style.transition = "background 0.2s";
	            li.addEventListener("mouseover", () => {
	              li.style.background = "#f1f1f1";
	            });
	            li.addEventListener("mouseout", () => {
	              li.style.background = "white";
	            });
	            li.addEventListener("click", function () {
	              input.value = suggestion;
	              resultsContainer.style.display = "none";
	              document.querySelector(".search-btn").click();
	            });
	            resultsContainer.appendChild(li);
	          });

	          resultsContainer.style.display = "block";
	        });
	    });

	    // 클릭 외부 시 닫기
	    document.addEventListener("click", function (e) {
	      if (!resultsContainer.contains(e.target) && e.target !== input) {
	        resultsContainer.style.display = "none";
	      }
	    });
	  });
		
	  document.querySelector(".search-btn").addEventListener("click", function(e) {
	    e.preventDefault();

	    const keyword = document.getElementById("autocomplete-input").value.trim();
	    if (!keyword) return;
	    
	    const forceOriginal = this.hasAttribute("data-force-original");
	    if (forceOriginal) {
	      this.removeAttribute("data-force-original"); // 플래그 제거
	    }
	    
	    const searchUrl = "${pageContext.request.contextPath}/searchTasks?keyword=" + encodeURIComponent(keyword) + 
	                     (forceOriginal ? "&forceOriginal=true" : "");
	    
	    fetch(searchUrl)
	      .then(res => res.json())
	      .then(data => {
	        console.log("데이터 받음:", data);
	        
	        const container = document.querySelector(".task-cards");
	        const searchInfoEl = document.querySelector(".search-info");
	        const resultCountEl = document.querySelector(".search-result-count");
	        
	        container.innerHTML = ""; // 기존 결과 지움
	        
	        searchInfoEl.classList.add("hidden");
	        resultCountEl.classList.remove("hidden");
	        
	        let correctedKeyword = null;
	        let actualResults = [...data]; // 배열 복사
	        
	     // 첫 번째 항목에 _corrected 필드가 있는지 확인하고, 원본 검색이 아닐 때만 처리
	        if (!forceOriginal && data.length > 0 && data[0]._corrected && data[0]._corrected !== keyword) {
	          correctedKeyword = data[0]._corrected;
	          // 교정 정보는 결과에서 제외
	          actualResults = data.filter(item => !item._corrected);
	          
	          // 오타 수정 정보 표시
	          searchInfoEl.classList.remove("hidden");
	          searchInfoEl.innerHTML = 
	            "✅ 입력한 검색어를 자동으로 수정했습니다: <strong>" + correctedKeyword + "</strong><br>" +
	            "&nbsp;&nbsp;&nbsp; <a href='#' onclick='searchAgain(\"!" + keyword + "\"); return false;'>" +
	            "'" + keyword + "' 검색 결과 보기</a>";
	          
	          // 검색창의 값을 교정된 키워드로 변경하지 않음
	          // document.getElementById("autocomplete-input").value = correctedKeyword;
	        } else {
	          // 오타 수정이 없으면 검색 정보 영역 숨김
	          searchInfoEl.classList.add("hidden");
	        }
	        
	        // 결과 카운트 표시
	        if (actualResults.length === 0) {
	          resultCountEl.innerHTML = "<h3>검색 결과가 없습니다.</h3>";
	        } else {
	          const displayKeyword = forceOriginal ? searchKeyword : (correctedKeyword || keyword);
	          resultCountEl.innerHTML = "<h3>'" + displayKeyword + "' 검색 결과 - " + actualResults.length + "건</h3>";
	        }
	        
	        // 단계별로 데이터 추가하기
	        for (let i = 0; i < actualResults.length; i++) {
	          const task = actualResults[i];
	          console.log("작업 데이터:", task);
	          
	          // 카드 생성
	          const card = document.createElement("div");
	          card.className = "task-card";
	          card.setAttribute("data-task-id", task.taskNo || "");
	          
	          // 카드 내용 작성
	          const headerDiv = document.createElement("div");
	          headerDiv.className = "task-header";
	          
	          const titleSpan = document.createElement("span");
	          titleSpan.className = "task-title";
	          
	          // 이모지와 제목 추가
	          if (task.emoji) {
	            titleSpan.textContent = task.emoji + " ";
	            console.log(task.emoji)
	          }
	          if (task.taskTitle) {
	            titleSpan.textContent += task.taskTitle;
	          }
	          
	          headerDiv.appendChild(titleSpan);
	          card.appendChild(headerDiv);
	          
	          // 메타 정보 영역 생성
	          const metaDiv = document.createElement("div");
	          metaDiv.className = "task-meta";
	          
	          // 담당자 정보
	          const assigneeDiv = document.createElement("div");
	          assigneeDiv.innerHTML = "<strong>업무 담당자:</strong> 최민준, 김지혜 외";
	          metaDiv.appendChild(assigneeDiv);
	          
	          // 카테고리
	          const categoryDiv = document.createElement("div");
	          categoryDiv.innerHTML = "<strong>카테고리:</strong> " + (task.category || "");
	          metaDiv.appendChild(categoryDiv);
	          
	          // 마감일
	          const dueDateDiv = document.createElement("div");
	          dueDateDiv.innerHTML = "<strong>마감일:</strong> " + (task.dueDate || "");
	          metaDiv.appendChild(dueDateDiv);
	          
	          // 상태
	          const statusDiv = document.createElement("div");
	          statusDiv.innerHTML = "<strong>상태:</strong> ";
	          
	          // 상태 매핑
	          const statusColorMap = {
	            TODO: "gray",
	            IN_PROGRESS: "blue",
	            FEEDBACK: "pink",
	            ON_HOLD: "yellow",
	            DONE: "green",
	            REQUEST: "purple"
	          };
	          
	          const statusNameMap = {
	            TODO: "요청",
	            IN_PROGRESS: "진행 중",
	            FEEDBACK: "피드백",
	            ON_HOLD: "보류",
	            DONE: "완료",
	            REQUEST: "요청"
	          };
	          
	          const statusColor = statusColorMap[task.taskStatus] || "gray";
	          const statusName = statusNameMap[task.taskStatus] || "상태 미정";
	          
	          const statusBadge = document.createElement("span");
	          statusBadge.className = "badge " + statusColor;
	          statusBadge.textContent = statusName;
	          
	          statusDiv.appendChild(statusBadge);
	          metaDiv.appendChild(statusDiv);
	          
	          card.appendChild(metaDiv);
	          
	          // 클릭 이벤트 추가
	          card.addEventListener("click", function() {
	            loadTaskDetail(task.taskNo);
	          });
	          
	          // 카드를 컨테이너에 추가
	          container.appendChild(card);
	        }
	      })
	      .catch(error => {
	        console.error("오류 발생:", error);
	      });
	  });
		
	  function searchAgain(originalKeyword) {
	    // 느낌표는 실제로 보이지 않도록 처리
	    const input = document.getElementById("autocomplete-input");
	    input.value = originalKeyword;
	    
	    // 원본 검색 강제 플래그 설정
	    const searchBtn = document.querySelector(".search-btn");
	    
	    // 데이터 속성으로 원본 검색 플래그 추가
	    searchBtn.setAttribute("data-force-original", "true");
	    searchBtn.click();
	  }
		
	  // 전역 변수 선언
	  let allEmployees = []; // 전체 사원 리스트 (초기 로딩 시 받아오기)
	  let selectedEmployees = []; // 선택된 사원들 (empNo, empName 등 저장)

	  // 직원 목록 렌더링 함수 - 사진과 같은 형태로 개선
	  function renderEmployeeModal(data) {
	    console.log("직원 데이터:", data);
	    
	    const container = document.getElementById("employee-list");
	    if (!container) {
	      console.error("employee-list 요소를 찾을 수 없습니다");
	      return;
	    }
	    
	    // 컨테이너 초기화
	    container.innerHTML = "";
	    
	    // 데이터가 없는 경우
	    if (!Array.isArray(data) || data.length === 0) {
	      container.innerHTML = "<p>불러올 직원 정보가 없습니다.</p>";
	      return;
	    }
	    
	    // 부서별로 그룹화
	    const grouped = {};
	    data.forEach(emp => {
	      const deptName = emp.deptName || "기타";
	      if (!grouped[deptName]) {
	        grouped[deptName] = [];
	      }
	      grouped[deptName].push(emp);
	    });
	    
	    // 각 부서별 UI 생성 - 아코디언 스타일로
	    for (const deptName in grouped) {
	      // 부서 섹션 (아코디언 아이템)
	      const section = document.createElement("div");
	      section.className = "dept-accordion-item";
	      section.style.marginBottom = "10px";
	      section.style.borderRadius = "8px";
	      section.style.overflow = "hidden";
	      section.style.border = "1px solid #eee";
	      section.style.backgroundColor = "#fff";
	      
	      // 부서 헤더 (아코디언 헤더)
	      const header = document.createElement("div");
	      header.className = "dept-accordion-header";
	      header.style.display = "flex";
	      header.style.justifyContent = "space-between";
	      header.style.alignItems = "center";
	      header.style.padding = "12px 16px";
	      header.style.backgroundColor = "#f8f9fa";
	      header.style.cursor = "pointer";
	      header.style.borderBottom = "1px solid #eee";
	      
	      // 헤더 내용
	      const titleSpan = document.createElement("span");
	      titleSpan.style.fontWeight = "500";
	      titleSpan.textContent = deptName + " (" + grouped[deptName].length + ")";
	      
	      // 화살표 아이콘
	      const arrowSpan = document.createElement("span");
	      arrowSpan.innerHTML = "&#9660;"; // 아래 화살표
	      arrowSpan.style.fontSize = "12px";
	      arrowSpan.style.transition = "transform 0.3s";
	      
	      header.appendChild(titleSpan);
	      header.appendChild(arrowSpan);
	      
	      // 부서 내용 (아코디언 내용)
	      const content = document.createElement("div");
	      content.className = "dept-accordion-content";
	      content.style.padding = "0";
	      content.style.maxHeight = "500px"; // 초기에는 열려 있음
	      content.style.overflow = "hidden";
	      content.style.transition = "max-height 0.3s ease";
	      
	      // 직원 목록 만들기
	      grouped[deptName].forEach(emp => {
	        const employeeItem = document.createElement("div");
	        employeeItem.style.display = "flex";
	        employeeItem.style.alignItems = "center";
	        employeeItem.style.padding = "12px 16px";
	        employeeItem.style.borderBottom = "1px solid #f5f5f5";
	        
	        // 체크박스
	        const checkbox = document.createElement("input");
	        checkbox.type = "checkbox";
	        checkbox.id = "emp-" + emp.empNo;
	        checkbox.value = emp.empNo;
	        checkbox.dataset.name = emp.empName;
	        checkbox.style.marginRight = "10px";
	        
	        // 이미 선택된 직원이면 체크박스 체크
	        if (selectedEmployees.some(selected => selected.empNo === emp.empNo)) {
	          checkbox.checked = true;
	        }
	        
	        // 프로필 아이콘 또는 이미지
	        const profileDiv = document.createElement("div");
	        profileDiv.style.width = "24px";
	        profileDiv.style.height = "24px";
	        profileDiv.style.borderRadius = "50%";
	        profileDiv.style.backgroundColor = "#e0e0e0";
	        profileDiv.style.display = "flex";
	        profileDiv.style.alignItems = "center";
	        profileDiv.style.justifyContent = "center";
	        profileDiv.style.marginRight = "10px";
	        profileDiv.style.color = "#666";
	        profileDiv.style.fontSize = "12px";
	        profileDiv.style.fontWeight = "bold";
	        
	        // 프로필 이미지가 있다면 사용, 없으면 이니셜
	        if (emp.profileImgPath) {
	          profileDiv.style.backgroundImage = `url('${emp.profileImgPath}')`;
	          profileDiv.style.backgroundSize = "cover";
	          profileDiv.style.backgroundPosition = "center";
	        } else {
	          profileDiv.textContent = emp.empName.charAt(0);
	        }
	        
	        // 직원 이름과 직급
	        const nameSpan = document.createElement("span");
	        nameSpan.textContent = emp.empName + " (" + emp.positionName + ")";
	        
	        // 체크박스 변경 이벤트
	        checkbox.addEventListener("change", function() {
	          if (this.checked) {
	            // 선택된 직원 추가
	            selectedEmployees.push({ 
	              empNo: parseInt(this.value), 
	              empName: this.dataset.name,
	              profileImgPath: emp.profileImgPath
	            });
	          } else {
	            // 선택된 직원 제거
	            selectedEmployees = selectedEmployees.filter(e => e.empNo !== parseInt(this.value));
	          }
	          
	          // UI 업데이트
	          renderSelectedAvatars();
	          
	          // hidden input 업데이트
	          updateAssigneeHiddenInput();
	        });
	        
	        // 요소들 조립
	        employeeItem.appendChild(checkbox);
	        employeeItem.appendChild(profileDiv);
	        employeeItem.appendChild(nameSpan);
	        content.appendChild(employeeItem);
	      });
	      
	      // 아코디언 토글 이벤트
	      header.addEventListener("click", function() {
	        // 아코디언 열고 닫기
	        const isOpen = content.style.maxHeight !== "0px";
	        
	        if (isOpen) {
	          content.style.maxHeight = "0px";
	          arrowSpan.style.transform = "rotate(-90deg)";
	        } else {
	          content.style.maxHeight = content.scrollHeight + "px";
	          arrowSpan.style.transform = "rotate(0)";
	        }
	      });
	      
	      // 섹션에 헤더와 내용 추가
	      section.appendChild(header);
	      section.appendChild(content);
	      
	      // 컨테이너에 섹션 추가
	      container.appendChild(section);
	    }
	  }

	  // 모달과 백드롭에 필요한 CSS 추가 - 사진과 유사하게 스타일 적용
	  function addRequiredStyles() {
	    // 이미 스타일이 추가되어 있는지 확인
	    if (document.getElementById('assignee-modal-styles')) {
	      return;
	    }
	    
	    const styleElement = document.createElement('style');
	    styleElement.id = 'assignee-modal-styles';
	    styleElement.textContent = `
	      #modal-backdrop {
	        position: fixed;
	        top: 0;
	        left: 0;
	        width: 100%;
	        height: 100%;
	        background-color: rgba(0, 0, 0, 0.5);
	        z-index: 1000;
	        display: none;
	      }
	      
	      #assignee-modal {
	        position: fixed;
	        top: 50%;
	        left: 50%;
	        transform: translate(-50%, -50%);
	        background-color: white;
	        padding: 0;
	        border-radius: 8px;
	        width: 500px;
	        max-width: 90%;
	        max-height: 80vh;
	        overflow: hidden;
	        z-index: 1001;
	        display: none;
	        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	      }
	      
	      .modal-header {
	        display: flex;
	        justify-content: space-between;
	        align-items: center;
	        padding: 16px;
	        border-bottom: 1px solid #eee;
	      }
	      
	      .modal-header h3 {
	        margin: 0;
	        font-size: 18px;
	        font-weight: 500;
	      }
	      
	      .modal-header button {
	        background: none;
	        border: none;
	        font-size: 20px;
	        cursor: pointer;
	        color: #666;
	      }
	      
	      .modal-body {
	        padding: 0;
	        max-height: calc(80vh - 120px);
	        overflow-y: auto;
	      }
	      
	      .modal-footer {
	        padding: 12px 16px;
	        text-align: right;
	        border-top: 1px solid #eee;
	        background-color: #f8f9fa;
	      }
	      
	      .modal-footer button {
	        background-color: #4a6bfd;
	        color: white;
	        border: none;
	        padding: 8px 16px;
	        border-radius: 4px;
	        cursor: pointer;
	        font-weight: 500;
	      }
	      
	      .department-section {
	        margin-bottom: 0;
	      }
	      
	      .department-header {
	        font-weight: 500;
	        padding: 12px 16px;
	        background-color: #f8f9fa;
	        border-bottom: 1px solid #eee;
	      }
	      
	      .department-members {
	        padding: 0;
	      }
	      
	      .employee-item {
	        display: flex;
	        align-items: center;
	        padding: 12px 16px;
	        border-bottom: 1px solid #f5f5f5;
	      }
	      
	      .employee-item input {
	        margin-right: 10px;
	      }
	      
	      .assignees {
	        display: flex;
	        flex-wrap: wrap;
	        gap: 5px;
	        align-items: center;
	      }
	      
	      .avatar {
	        width: 32px;
	        height: 32px;
	        border-radius: 50%;
	        background-color: #e0e0e0;
	        display: flex;
	        align-items: center;
	        justify-content: center;
	        font-weight: bold;
	        color: #555;
	      }
	      
	      .avatar.empty {
	        background-color: #f0f0f0;
	        border: 1px dashed #ccc;
	        color: #777;
	      }
	      
	      .round-icon-btn {
	        width: 32px;
	        height: 32px;
	        border-radius: 50%;
	        border: 1px dashed #ccc;
	        background: none;
	        cursor: pointer;
	        display: flex;
	        align-items: center;
	        justify-content: center;
	      }
	      
	      .dept-accordion-item {
	        transition: all 0.3s ease;
	      }
	      
	      .dept-accordion-item:hover {
	        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
	      }
	    `;
	    document.head.appendChild(styleElement);
	  }

	  // 담당자 모달 생성 함수 - UI 개선
	  function createAssigneeModal() {
	    // 기존 모달이 있다면 제거
	    const existingModal = document.getElementById("assignee-modal");
	    const existingBackdrop = document.getElementById("modal-backdrop");
	    
	    if (existingModal) existingModal.remove();
	    if (existingBackdrop) existingBackdrop.remove();
	    
	    // 백드롭 생성
	    const backdrop = document.createElement("div");
	    backdrop.id = "modal-backdrop";
	    
	    // 모달 생성
	    const modal = document.createElement("div");
	    modal.id = "assignee-modal";
	    
	    // 모달 헤더
	    const header = document.createElement("div");
	    header.className = "modal-header";
	    
	    const title = document.createElement("h3");
	    title.textContent = "담당자 선택";
	    
	    const closeBtn = document.createElement("button");
	    closeBtn.innerHTML = "&times;";
	    closeBtn.onclick = closeAssigneeModal;
	    
	    header.appendChild(title);
	    header.appendChild(closeBtn);
	    
	    // 모달 본문
	    const body = document.createElement("div");
	    body.className = "modal-body";
	    
	    const employeeList = document.createElement("div");
	    employeeList.id = "employee-list";
	    employeeList.innerHTML = "<div style='padding: 16px; text-align: center;'>직원 목록을 불러오는 중...</div>";
	    
	    body.appendChild(employeeList);
	    
	    // 모달 푸터
	    const footer = document.createElement("div");
	    footer.className = "modal-footer";
	    
	    const confirmBtn = document.createElement("button");
	    confirmBtn.textContent = "확인";
	    confirmBtn.onclick = confirmAssigneeSelection;
	    
	    footer.appendChild(confirmBtn);
	    
	    // 모달 조립
	    modal.appendChild(header);
	    modal.appendChild(body);
	    modal.appendChild(footer);
	    
	    // DOM에 추가
	    document.body.appendChild(backdrop);
	    document.body.appendChild(modal);
	    
	    // CSS 스타일 추가
	    addRequiredStyles();
	  }

	  // 담당자 모달 열기 함수
	  function openAssigneeModal() {
	    console.log("담당자 모달 열기");
	    
	    // 모달이 없으면 생성
	    if (!document.getElementById("assignee-modal")) {
	      createAssigneeModal();
	    }
	    
	    // 모달 표시
	    document.getElementById("assignee-modal").style.display = "block";
	    document.getElementById("modal-backdrop").style.display = "block";
	    
	    // 직원 목록 로드
	    loadEmployeeList();
	  }

	  // 담당자 모달 닫기
	  function closeAssigneeModal() {
	    document.getElementById("assignee-modal").style.display = "none";
	    document.getElementById("modal-backdrop").style.display = "none";
	  }

	  // 담당자 선택 확인 함수 - 개선
	  function confirmAssigneeSelection() {
	    closeAssigneeModal();
	    renderSelectedAvatars();
	    updateAssigneeHiddenInput();
	  }

	  // 선택된 담당자 아바타 렌더링
	  function renderSelectedAvatars() {
	    const container = document.querySelector(".assignees");
	    if (!container) {
	      console.error("assignees 컨테이너를 찾을 수 없습니다");
	      return;
	    }
	    
	    container.innerHTML = "";

	    const visibleLimit = 3;

	    selectedEmployees.slice(0, visibleLimit).forEach(emp => {
	      const avatar = document.createElement("div");
	      avatar.className = "avatar";
	      
	      // 프로필 이미지가 있다면 썸네일 표시, 없으면 이름 첫 글자
	      if (emp.profileImgPath) {
	        avatar.style.backgroundImage = `url('${emp.profileImgPath}')`;
	        avatar.style.backgroundSize = 'cover';
	        avatar.style.backgroundPosition = 'center';
	      } else {
	        avatar.textContent = emp.empName.charAt(0);
	      }

	      container.appendChild(avatar);
	    });

	    // 초과된 인원 수 표시
	    if (selectedEmployees.length > visibleLimit) {
	      const restCount = selectedEmployees.length - visibleLimit;
	      const extra = document.createElement("div");
	      extra.className = "avatar empty";
	      extra.textContent = `+${restCount}`;
	      container.appendChild(extra);
	    }
	  }
	  

	  // 폼 제출을 위한 hidden input 업데이트 함수
	  function updateAssigneeHiddenInput() {
	    const hiddenInput = document.getElementById('assignees-hidden');
	    if (hiddenInput) {
	      // 직원 ID를 쉼표로 구분하여 저장
	      hiddenInput.value = selectedEmployees.map(emp => emp.empNo).join(',');
	    }
	  }
	  
	</script>

</body>
</html>