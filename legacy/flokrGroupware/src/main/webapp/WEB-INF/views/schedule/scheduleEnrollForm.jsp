<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Flokr - 일정 등록</title>
    <link href="${pageContext.request.contextPath}/resources/css/tabler.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/scheduleEnrollForm.css">

</head>
<body>
    <jsp:include page="../common/header.jsp"/>
	
    <div class="page-body">
        <div class="sc-enroll-container">
            <div class="sc-enroll-card">
                <div class="sc-enroll-header">
                    <h3 class="sc-enroll-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                        새 일정 등록
                    </h3>
                </div>
                <div class="sc-enroll-body">
                    <div class="sc-enroll-notice">
                        <div class="sc-enroll-notice-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="16" x2="12" y2="12"></line>
                                <line x1="12" y1="8" x2="12.01" y2="8"></line>
                            </svg>
                        </div>
                        <div class="sc-enroll-notice-text">
                            새로운 일정을 등록합니다. 필수 항목(*)을 모두 입력해주세요.
                        </div>
                    </div>

                    <form action="insert.sc" method="post" id="scheduleForm">
                        <div class="sc-enroll-group">
                            <label class="sc-enroll-label required">일정 제목</label>
                            <input type="text" class="sc-enroll-input" name="scheduleTitle" placeholder="일정 제목을 입력하세요" required>
                        </div>

                        <div class="sc-enroll-fields-group">
                            <div class="sc-enroll-field-half">
                                <label class="sc-enroll-label required">시작일</label>
                                <div class="sc-enroll-date-wrapper">
                                    <input type="date" class="sc-enroll-input" name="startDate" required>
                                    <div class="sc-enroll-date-input-overlay" onclick="this.previousElementSibling.showPicker()"></div>
                                </div>
                            </div>
                            <div class="sc-enroll-field-half">
                                <label class="sc-enroll-label required">종료일</label>
                                <div class="sc-enroll-date-wrapper">
                                    <input type="date" class="sc-enroll-input" name="endDate" required>
                                    <div class="sc-enroll-date-input-overlay" onclick="this.previousElementSibling.showPicker()"></div>
                                </div>
                            </div>
                        </div>

                        <div class="sc-enroll-checkbox-wrapper">
                            <input type="checkbox" id="allDay" name="allDay" class="sc-enroll-checkbox">
                            <label for="allDay" class="sc-enroll-checkbox-label">종일 일정</label>
                        </div>

                        <div class="sc-enroll-fields-group sc-enroll-time-fields">
                            <div class="sc-enroll-field-half">
                                <label class="sc-enroll-label">시작 시간</label>
                                <div class="sc-enroll-time-wrapper">
                                    <input type="time" class="sc-enroll-input" name="startTime" value="09:00">
                                    <div class="sc-enroll-time-input-overlay" onclick="this.previousElementSibling.showPicker()"></div>
                                </div>
                            </div>
                            <div class="sc-enroll-field-half">
                                <label class="sc-enroll-label">종료 시간</label>
                                <div class="sc-enroll-time-wrapper">
                                    <input type="time" class="sc-enroll-input" name="endTime" value="18:00">
                                    <div class="sc-enroll-time-input-overlay" onclick="this.previousElementSibling.showPicker()"></div>
                                </div>
                            </div>
                        </div>

                        <div class="sc-enroll-group">
                            <label class="sc-enroll-label">장소</label>
                            <input type="text" class="sc-enroll-input" name="location" placeholder="일정 장소를 입력하세요">
                        </div>

                        <div class="sc-enroll-group">
                            <label class="sc-enroll-label">중요도</label>
                            <div class="sc-enroll-radio-group">
                                <label class="sc-enroll-radio-item">
                                    <input type="radio" name="important" value="LOW" class="sc-enroll-radio-input">
                                    <span class="sc-enroll-radio-label">낮음</span>
                                </label>
                                <label class="sc-enroll-radio-item">
                                    <input type="radio" name="important" value="NORMAL" class="sc-enroll-radio-input" checked>
                                    <span class="sc-enroll-radio-label">보통</span>
                                </label>
                                <label class="sc-enroll-radio-item">
                                    <input type="radio" name="important" value="HIGH" class="sc-enroll-radio-input">
                                    <span class="sc-enroll-radio-label">높음</span>
                                </label>
                            </div>
                        </div>

                        <div class="sc-enroll-group">
                            <label class="sc-enroll-label">일정 유형</label>
                            <div class="sc-enroll-color-group">
                                <label class="sc-enroll-color-item">
                                    <input type="radio" name="scheduleType" value="PERSONAL" class="sc-enroll-color-input" checked>
                                    <span class="sc-enroll-color-label sc-enroll-color-blue">개인</span>
                                </label>
                                <label class="sc-enroll-color-item">
                                    <input type="radio" name="scheduleType" value="TEAM" class="sc-enroll-color-input">
                                    <span class="sc-enroll-color-label sc-enroll-color-green">팀</span>
                                </label>
                                <label class="sc-enroll-color-item">
                                    <input type="radio" name="scheduleType" value="COMPANY" class="sc-enroll-color-input">
                                    <span class="sc-enroll-color-label sc-enroll-color-orange">회사</span>
                                </label>
                                <label class="sc-enroll-color-item">
                                    <input type="radio" name="scheduleType" value="OTHER" class="sc-enroll-color-input">
                                    <span class="sc-enroll-color-label sc-enroll-color-purple">기타</span>
                                </label>
                            </div>
                        </div>

                        <div class="sc-enroll-group">
                            <label class="sc-enroll-label">참석자</label>
                            <div id="selectedAttendeesDisplay" class="selected-attendees-display empty">
                                참석자를 선택해주세요.
                            </div>
                            <input type="hidden" name="attendee" id="attendeeIds">
                            <button type="button" class="btn-select-attendee" id="openAttendeeModalBtn">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="17" y1="11" x2="23" y2="11"></line></svg>
                                참석자 선택
                            </button>
                        </div>

                        <div class="sc-enroll-group">
                            <label class="sc-enroll-label">내용</label>
                            <textarea class="sc-enroll-textarea" name="description" placeholder="일정에 대한 세부 내용을 입력하세요" rows="4"></textarea>
                        </div>
                    </form>
                </div>
                <div class="sc-enroll-footer">
                    <div class="sc-enroll-buttons">
                        <a href="list.sc" class="sc-enroll-btn-cancel">취소</a>
                        <button type="submit" form="scheduleForm" class="sc-enroll-btn-submit">등록</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="attendeeModalOverlay" class="attendee-modal-overlay">
        <div class="attendee-modal-content">
            <div class="attendee-modal-header">
                <h4 class="attendee-modal-title">참석자 선택</h4>
                <button type="button" class="attendee-modal-close-btn" id="closeAttendeeModalBtn">&times;</button>
            </div>
            <input type="text" id="attendeeSearchInput" class="attendee-search-input" placeholder="이름 또는 부서로 검색...">
            <div id="attendeeModalList" class="attendee-modal-list">
                <c:if test="${empty eList}">
                    <p style="color: #64748b; font-size: 14px; text-align: center; margin: 20px 0;">선택 가능한 직원이 없습니다.</p>
                </c:if>
                <c:forEach var="emp" items="${eList}">
                    <div class="attendee-modal-item" data-emp-name="${emp.empName}" data-dept-name="${emp.deptName}">
                        <input type="checkbox" value="${emp.empNo}" id="modal_attendee_${emp.empNo}" class="attendee-modal-checkbox">
                        <label for="modal_attendee_${emp.empNo}" class="attendee-modal-label">
                            <span class="name">${emp.empName}</span>
                            <span class="details" style="display: inline-block; color: #64748b;">${emp.deptName} / ${emp.positionName}</span>
                        </label>
                    </div>
                </c:forEach>
                 </div>
            <div class="attendee-modal-footer">
                <button type="button" class="attendee-modal-btn attendee-modal-btn-cancel" id="cancelAttendeeSelectionBtn">취소</button>
                <button type="button" class="attendee-modal-btn attendee-modal-btn-confirm" id="confirmAttendeeSelectionBtn">확인</button>
            </div>
        </div>
    </div>


    <script src="${pageContext.request.contextPath}/resources/js/tabler.min.js"></script>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const allDayCheckbox = document.getElementById('allDay');
        const timeFieldsContainer = document.querySelector('.sc-enroll-time-fields');
        const startTimeInput = document.querySelector('input[name="startTime"]');
        const endTimeInput = document.querySelector('input[name="endTime"]');
        const startDateInput = document.querySelector('input[name="startDate"]');
        const endDateInput = document.querySelector('input[name="endDate"]');

        function toggleTimeFields() {
            if (allDayCheckbox.checked) {
                timeFieldsContainer.classList.add('hidden');
                startTimeInput.value = '00:00';
                endTimeInput.value = '23:59';
                allDayCheckbox.value = 'Y'; // 체크됐을 때 "Y" 값 설정
            } else {
                timeFieldsContainer.classList.remove('hidden');
                if (!startTimeInput.value || startTimeInput.value === '00:00') startTimeInput.value = '09:00';
                if (!endTimeInput.value || endTimeInput.value === '23:59') endTimeInput.value = '18:00';
                allDayCheckbox.value = 'N'; // 체크 해제됐을 때 "N" 값 설정
            }
        }

        toggleTimeFields();
        allDayCheckbox.addEventListener('change', toggleTimeFields);

        startDateInput.addEventListener('change', function() {
            const selectedDate = new Date(this.value);
            selectedDate.setHours(0, 0, 0, 0);
            
            if (selectedDate < today) {
                // 사용자에게 알림만 제공하고 계속 진행 허용
                alert('선택하신 날짜는 과거 날짜입니다.');
            }
            
            // 기존 로직 유지 (종료일이 시작일보다 빠르면 종료일 업데이트)
            if (!endDateInput.value || new Date(endDateInput.value) < selectedDate) {
                endDateInput.value = this.value;
            }
            endDateInput.min = this.value;
        });;

         endDateInput.addEventListener('change', function() {
             if (startDateInput.value && new Date(this.value) < new Date(startDateInput.value)) {
                 this.value = startDateInput.value;
             }
         });

      // URL에서 날짜 파라미터 가져오기 (script 태그 내의 기존 DOMContentLoaded 함수 안에 추가)

      // URL에서 전달된 날짜 파라미터 확인
      function getUrlParameter(name) {
          name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
          var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
          var results = regex.exec(location.search);
          return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
      }

      // 오늘 날짜 생성
      const today = new Date();
      today.setHours(0, 0, 0, 0); // 시간 부분을 0으로 설정하여 날짜만 비교하도록 함

      // 날짜 문자열 포맷팅 함수
      function formatDate(date) {
          const year = date.getFullYear();
          let month = date.getMonth() + 1;
          let day = date.getDate();
          month = month < 10 ? '0' + month : month;
          day = day < 10 ? '0' + day : day;
          return `${year}-${month}-${day}`;
      }

      // 오늘 날짜 문자열
      const todayString = formatDate(today);

      // 날짜 파라미터가 있다면 해당 날짜로 시작일과 종료일 설정
      const dateParam = getUrlParameter('date');
      if (dateParam) {
          try {
              // 파라미터가 유효한 날짜 형식인지 확인
              const paramDate = new Date(dateParam);
              paramDate.setHours(0, 0, 0, 0); // 시간 부분을 0으로 설정하여 날짜만 비교하도록 함
              
              if (!isNaN(paramDate.getTime())) {
                  const formattedDate = formatDate(paramDate);
                  
                  // 시작일과 종료일 모두 선택된 날짜로 설정
                  startDateInput.value = formattedDate;
                  endDateInput.value = formattedDate;
                  endDateInput.min = formattedDate; // 종료일의 최소값도 설정
                  
                  // 선택된 날짜가 오늘보다 이전인지 확인
                  if (paramDate < today) {
                      // 사용자에게 과거 날짜임을 알림
                      setTimeout(() => {
                          const confirmPastDate = confirm('선택하신 날짜는 과거 날짜입니다. 계속 진행하시겠습니까?');
                          if (!confirmPastDate) {
                              // 사용자가 취소를 선택한 경우 오늘 날짜로 변경
                              startDateInput.value = todayString;
                              endDateInput.value = todayString;
                              endDateInput.min = todayString;
                          }
                      }, 100); // 약간의 지연 후 경고 표시
                  }
              }
          } catch (e) {
              console.error('날짜 파라미터 처리 중 오류:', e);
          }
      } else {
          // 날짜 파라미터가 없을 경우 기존 코드대로 오늘 날짜로 설정
          if (!startDateInput.value) startDateInput.value = todayString;
          if (!endDateInput.value) endDateInput.value = todayString;
          if (startDateInput.value) endDateInput.min = startDateInput.value;
      }

      // 시작일 변경 시에도 과거 날짜 체크
      startDateInput.addEventListener('change', function() {
          const selectedDate = new Date(this.value);
          selectedDate.setHours(0, 0, 0, 0);
          
          if (selectedDate < today) {
              // 사용자에게 알림만 제공하고 계속 진행 허용
              alert('선택하신 날짜는 과거 날짜입니다.');
          }
          
          // 기존 로직 유지 (종료일이 시작일보다 빠르면 종료일 업데이트)
          if (!endDateInput.value || new Date(endDateInput.value) < selectedDate) {
              endDateInput.value = this.value;
          }
          endDateInput.min = this.value;
      });

        document.querySelectorAll('.sc-enroll-date-input-overlay, .sc-enroll-time-input-overlay').forEach(overlay => {
            overlay.addEventListener('click', function() {
                try { this.previousElementSibling.showPicker(); }
                catch (e) { this.previousElementSibling.focus(); }
            });
        });

        // --- 참석자 선택 모달 관련 로직 ---
        const openModalBtn = document.getElementById('openAttendeeModalBtn');
        const modalOverlay = document.getElementById('attendeeModalOverlay');
        const closeModalBtn = document.getElementById('closeAttendeeModalBtn');
        const cancelBtn = document.getElementById('cancelAttendeeSelectionBtn');
        const confirmBtn = document.getElementById('confirmAttendeeSelectionBtn');
        const attendeeSearchInput = document.getElementById('attendeeSearchInput');
        const attendeeModalList = document.getElementById('attendeeModalList');
        const selectedAttendeesDisplay = document.getElementById('selectedAttendeesDisplay');
        const attendeeIdsInput = document.getElementById('attendeeIds');

        // 모달 열기
        openModalBtn.addEventListener('click', function() {
            modalOverlay.classList.add('active');
            // 모달 열 때, 현재 숨겨진 필드에 저장된 ID를 기반으로 체크박스 상태 복원
            const currentIds = attendeeIdsInput.value.split(',').filter(id => id); // 빈 값 제거
            const checkboxes = attendeeModalList.querySelectorAll('.attendee-modal-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = currentIds.includes(checkbox.value);
            });
            // 검색창 초기화 및 필터링 초기화
            attendeeSearchInput.value = '';
            filterAttendees();
        });

        // 모달 닫기 (X 버튼 또는 취소 버튼)
        function closeModal() {
            modalOverlay.classList.remove('active');
        }
        closeModalBtn.addEventListener('click', closeModal);
        cancelBtn.addEventListener('click', closeModal);

        // 모달 외부 클릭 시 닫기 (선택적)
        modalOverlay.addEventListener('click', function(event) {
            if (event.target === modalOverlay) {
                closeModal();
            }
        });

        // 참석자 검색 (실시간 필터링)
        attendeeSearchInput.addEventListener('input', filterAttendees);

        function filterAttendees() {
            const searchTerm = attendeeSearchInput.value.toLowerCase();
            const items = attendeeModalList.querySelectorAll('.attendee-modal-item');
            items.forEach(item => {
                const name = item.dataset.empName?.toLowerCase() || '';
                const dept = item.dataset.deptName?.toLowerCase() || '';
                const isVisible = name.includes(searchTerm) || dept.includes(searchTerm);
                item.style.display = isVisible ? 'flex' : 'none';
            });
        }

        // 확인 버튼 클릭 시 선택된 참석자 처리
        confirmBtn.addEventListener('click', function() {
            const selectedNames = [];
            const selectedIds = [];
            const checkedCheckboxes = attendeeModalList.querySelectorAll('.attendee-modal-checkbox:checked');

            checkedCheckboxes.forEach(checkbox => {
                selectedIds.push(checkbox.value);
                // 체크박스에 연결된 라벨에서 이름 찾기
                const label = checkbox.closest('.attendee-modal-item').querySelector('.attendee-modal-label .name');
                if (label) {
                    selectedNames.push(label.textContent.trim());
                }
            });

            // 선택된 이름 표시 업데이트
            selectedAttendeesDisplay.innerHTML = ''; // 기존 내용 지우기
            if (selectedNames.length > 0) {
                selectedAttendeesDisplay.classList.remove('empty');
                selectedNames.forEach(name => {
                    const tag = document.createElement('span');
                    tag.className = 'attendee-tag';
                    tag.textContent = name;
                    selectedAttendeesDisplay.appendChild(tag);
                });
            } else {
                selectedAttendeesDisplay.classList.add('empty');
                selectedAttendeesDisplay.textContent = '참석자를 선택해주세요.';
            }

            // 숨겨진 필드에 ID 목록 업데이트 (쉼표로 구분)
            attendeeIdsInput.value = selectedIds.join(',');

            closeModal(); // 모달 닫기
        });
        // --- 참석자 선택 모달 관련 로직 끝 ---
        
        // 초기 값 설정
		allDayCheckbox.value = allDayCheckbox.checked ? 'Y' : 'N';
     	// 폼 제출 시 처리 로직 추가 
        const scheduleForm = document.getElementById('scheduleForm');

        scheduleForm.addEventListener('submit', function(e) {
            e.preventDefault(); // 기본 제출 동작 중지
            
            // 종일 일정이 아닐 경우, 시작일과 종료일을 시간과 결합
            if (!allDayCheckbox.checked) {
                // 날짜와 시간 결합을 서버에서 처리할 수도 있지만, 프론트에서도 유효성 검증을 위해 확인
                const startDateValue = startDateInput.value;
                const endDateValue = endDateInput.value;
                const startTimeValue = startTimeInput.value;
                const endTimeValue = endTimeInput.value;
                
                // 같은 날짜에 종료 시간이 시작 시간보다 빠른지 확인
                if (startDateValue === endDateValue && startTimeValue > endTimeValue) {
                    alert('종료 시간은 시작 시간보다 늦어야 합니다.');
                    return false;
                }
            } else {
                // 종일 일정일 경우 allDay 값이 Y로 설정되도록
                allDayCheckbox.value = 'Y';
            }
            
            // 추가 검증 통과 시 폼 제출
            this.submit();
        });

    });
    </script>
</body>
</html>