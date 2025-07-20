/**
 * 결재선 선택 모달 스크립트
 */

// 부서 선택 처리
function selectDept(deptNo, element) {
    // 부서 선택 상태 업데이트
    document.querySelectorAll('.apline-dept-item').forEach(item => {
        item.classList.remove('active');
    });
    element.classList.add('active');
    
    // 해당 부서 직원 목록 로드
    loadEmployeesByDept(deptNo);
}

// 부서별 직원 목록 로드
function loadEmployeesByDept(deptNo) {
    // TODO: Ajax로 부서별 직원 목록 조회
    // 현재는 전체 직원 표시
}

// 사원 검색
function searchEmployees(event) {
    event.preventDefault();
    const keyword = document.getElementById('apline-search-keyword').value;
    
    // 검색어 디버깅 로그
    console.log('검색 키워드:', keyword);
    
    $.ajax({
        url: 'searchEmployees.ap',
        method: 'GET',
        data: { keyword: keyword },
        dataType: 'json', // 명시적으로 JSON 응답 기대
        success: function(data) {
            console.log('검색 결과:', data);
            updateEmployeeGrid(data);
        },
        error: function(xhr, status, error) {
            console.error('검색 중 오류:', xhr, status, error);
            alert('검색 중 오류가 발생했습니다.');
        }
    });
}

// HTML 이스케이프 함수
function escapeHtml(str) {
    if (!str) return '';
    var result = str;
    result = result.split('&').join('&amp;');
    result = result.split('<').join('&lt;');
    result = result.split('>').join('&gt;');
    result = result.split('"').join('&quot;');
    result = result.split("'").join('&#039;');
    return result;
}

// 따옴표 이스케이프 함수
function escapeQuotes(str) {
    return str.split("'").join("\\'");
}

// 검색 결과로 직원 그리드 업데이트
function updateEmployeeGrid(employees) {
    const grid = document.getElementById('apline-employee-grid');
    
    // 데이터가 이미 JSON 객체인지 문자열인지 확인
    let empData = employees;
    if (typeof employees === 'string') {
        try {
            empData = JSON.parse(employees);
        } catch (e) {
            console.error('JSON 파싱 오류:', e);
            return;
        }
    }
    
    // 데이터가 없거나 배열이 아닌 경우 처리
    if (!empData || !Array.isArray(empData)) {
        console.error('유효한 직원 데이터가 아닙니다:', empData);
        grid.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
        return;
    }
    
    // 안전한 방식으로 HTML 문자열 생성 (템플릿 리터럴 사용 안함)
    let html = '';
    
    empData.forEach(emp => {
        const empName = escapeHtml(emp.empName || '이름 없음');
        const deptName = escapeHtml(emp.deptName || '부서 없음');
        const positionName = escapeHtml(emp.positionName || '직급 없음');
        const firstLetter = empName.charAt(0) || '?';
        
        html += '<div class="apline-emp-card" data-emp-no="' + emp.empNo + '" ';
        html += 'onclick="selectEmployee(' + emp.empNo + ', \'' + escapeQuotes(empName) + '\', \'' 
                + escapeQuotes(deptName) + '\', \'' + escapeQuotes(positionName) + '\')">';
        html += '<div class="apline-emp-info">';
        html += '<div class="apline-emp-avatar">' + firstLetter + '</div>';
        html += '<div class="apline-emp-details">';
        html += '<div class="apline-emp-name">' + empName + '</div>';
        html += '<div class="apline-emp-position">' + deptName + ' · ' + positionName + '</div>';
        html += '</div></div></div>';
    });
    
    grid.innerHTML = html;
}

// 직원 선택 처리
function selectEmployee(empNo, empName, deptName, positionName) {
    // 디버깅 로그
    console.log("선택한 사원 정보:", { empNo, empName, deptName, positionName });
    
    // 특수문자 및 따옴표 처리를 위한 인코딩
    const employee = {
        empNo: empNo,
        empName: empName,
        deptName: deptName,
        positionName: positionName
    };
    
    // 부모 창의 addApprovalLine 함수 호출
    if (window.opener && window.opener.addApprovalLine) {
        try {
            window.opener.addApprovalLine(employee);
            
            // 선택 성공 표시를 UI에 반영
            const card = document.querySelector('.apline-emp-card[data-emp-no="' + empNo + '"]');
            if (card) {
                card.style.borderColor = '#28a745';
                card.style.backgroundColor = '#f8fffe';
                setTimeout(() => {
                    card.style.borderColor = '';
                    card.style.backgroundColor = '';
                }, 500);
            }
            
            // 선택 알림 메시지
            alert(empName + ' (' + deptName + ', ' + positionName + ')이(가) 결재선에 추가되었습니다.');
        } catch (e) {
            console.error("결재선 추가 중 오류 발생:", e);
            alert("결재선 추가 중 오류가 발생했습니다: " + e.message);
        }
    } else {
        alert("부모 창을 찾을 수 없거나 addApprovalLine 함수가 정의되지 않았습니다.");
    }
}

// 최근 결재선 로드
function loadRecentApprovalLine() {
    // TODO: 최근 결재선 불러오기 구현
    alert('최근 결재선 기능은 준비 중입니다.');
}

// 동적으로 최근 결재선 불러오기
function loadRecentApprovalLines() {
    $.ajax({
        url: 'recentApprovalLines.ap',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            const recentList = document.querySelector('.apline-recent-list');
            if (!data || data.length === 0) {
                recentList.innerHTML = '<div class="apline-no-recent">최근 결재선이 없습니다.</div>';
                return;
            }
            
            let html = '';
            data.forEach(lineGroup => {
                const title = lineGroup.title || '결재선';
                let names = '결재선';
                
                if (lineGroup.approvers && Array.isArray(lineGroup.approvers)) {
                    names = lineGroup.approvers.map(a => a.empName || '').join('-');
                }
                
                html += '<div class="apline-recent-item" data-line-id="' + lineGroup.id + '" onclick="loadRecentApprovalLine(' + lineGroup.id + ')">';
                html += names;
                html += '</div>';
            });
            
            recentList.innerHTML = html;
        },
        error: function(xhr, status, error) {
            console.error('최근 결재선 로드 중 오류:', xhr, status, error);
            // 오류 시 조용히 실패 (UI에 영향 없음)
        }
    });
}

// 엔터키 검색
document.getElementById('apline-search-keyword').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        searchEmployees(e);
    }
});

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('결재선 모달 초기화');
    
    // 최근 결재선 데이터 불러오기
    loadRecentApprovalLines();
    
    // 그 외 초기화 코드
});