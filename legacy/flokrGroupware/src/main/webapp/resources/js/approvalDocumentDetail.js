/**
 * 결재 문서 상세보기 페이지 스크립트
 * 
 * 이 파일은 JSP의 EL 표현식과의 충돌을 방지하기 위해 
 * 별도 JS 파일로 분리된 코드입니다.
 */

// 즉시 실행 함수로 감싸서 전역 네임스페이스 오염 방지
(function() {
    
    /**
     * HTML 문자열 이스케이프 처리
     * @param {string} unsafe - 이스케이프 처리할 문자열
     * @returns {string} 이스케이프 처리된 문자열
     */
    function escapeHtml(unsafe) {
        if (typeof unsafe !== 'string') return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    /**
     * 숫자 형식화 함수
     * @param {number|string} num - 형식화할 숫자 또는 문자열
     * @returns {string} 형식화된 숫자 문자열
     */
    function formatNumber(num) {
        if (num === null || num === undefined || isNaN(parseFloat(num))) return '0';
        return parseFloat(num).toLocaleString();
    }
    
    /**
     * 문서 내용 표시
     * JSON 형식의 문서 내용을 파싱하고 적절한 HTML로 렌더링
     */
    function displayDocumentContent() {
        try {
            const contentElement = document.getElementById('document-content');
            if (!contentElement) {
                console.error('document-content 요소를 찾을 수 없습니다.');
                return;
            }
    
            // JSP에서 설정한 전역 변수 사용
            const originalContent = rawDocContent;
    
            if (originalContent === null || originalContent.trim() === "") {
                contentElement.innerHTML = "<p>표시할 문서 내용이 없습니다.</p>";
                return;
            }
    
            // EL 표현식 이스케이프 해제 및 정리
            let cleanContent = originalContent.replace(/\\\$\{/g, "$" + "{");
    
            // 모든 종류의 EL 함수 호출 패턴 처리
            cleanContent = cleanContent.replace(/\$\{[^}]*?:[^}]*?\}/g, "0");
            cleanContent = cleanContent.replace(/\$\{[^}]*?\}/g, "0");
            
            // 콘솔에 디버깅 정보 출력
            console.log('정리된 내용:', cleanContent);
    
            try {
                // JSON 파싱 시도
                const jsonData = JSON.parse(cleanContent);
      
                // JSON 데이터를 사용자 친화적인 HTML로 변환
                let htmlContent = '';
                switch(jsonData.formType) {
                    case 'vacation':
                        htmlContent = renderVacationContent(jsonData.data);
                        break;
                    case 'expense':
                        htmlContent = renderExpenseContent(jsonData.data);
                        break;
                    case 'remoteWork':
                        htmlContent = renderRemoteWorkContent(jsonData.data);
                        break;
                    case 'businessTrip':
                        htmlContent = renderBusinessTripContent(jsonData.data);
                        break;
                    default:
                        htmlContent = '<p>지원되지 않는 문서 유형입니다.</p>';
                }
      
                // 변환된 HTML을 표시
                contentElement.innerHTML = htmlContent;
      
            } catch (error) {
                // JSON 파싱 실패 시
                console.error('JSON 파싱 오류:', error);
                console.log('파싱 시도한 콘텐츠:', cleanContent);
      
                // 원본 내용을 이스케이프하여 표시
                contentElement.innerHTML = '<pre>' + escapeHtml(cleanContent) + '</pre>';
                console.log('JSON 형식이 아니므로 원본 내용을 표시합니다.');
            }
        } catch (error) {
            console.error('displayDocumentContent 실행 중 오류 발생:', error);
            const contentElement = document.getElementById('document-content');
            if (contentElement) {
                contentElement.innerHTML = '<p>문서 내용을 표시하는 중 오류가 발생했습니다.</p>';
            }
        }
    }

    /**
     * 휴가 신청서 내용 렌더링
     * @param {Object} data - 휴가 신청서 데이터
     * @returns {string} 렌더링된 HTML
     */
    function renderVacationContent(data) {
        return `
            <div class="doc-section vacation-document">
                <h3 class="doc-section-title">휴가 신청서</h3>
                
                <div class="doc-info-table">
                    <div class="doc-info-row">
                        <div class="doc-info-label">기안자</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.empName : '-'}</div>
                        <div class="doc-info-label">소속부서</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.deptName : '-'}</div>
                        <div class="doc-info-label">직위</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.positionName : '-'}</div>
                    </div>
                </div>
                
                <div class="doc-section-content">
                    <div class="doc-field-group">
                        <div class="doc-field-label">휴가종류</div>
                        <div class="doc-field-value">${data.vacationType || '-'}</div>
                    </div>
                
                    <div class="doc-field-group">
                        <div class="doc-field-label">휴가기간</div>
                        <div class="doc-field-value">${data.startDate || '-'} ~ ${data.endDate || '-'} (${data.days || '0'}일)</div>
                    </div>
                
                    <div class="doc-field-group">
                        <div class="doc-field-label">연차현황</div>
                        <div class="doc-field-value">총 ${data.totalDays || '0'}일 중 사용 ${data.usedDays || '0'}일, 잔여 ${data.remainingDays || '0'}일</div>
                    </div>
                
                    <div class="doc-field-group">
                        <div class="doc-field-label">휴가사유</div>
                        <div class="doc-field-value">${data.reason || '-'}</div>
                    </div>
                
                    <div class="doc-field-group">
                        <div class="doc-field-label">비상연락처</div>
                        <div class="doc-field-value">${data.contact || '-'}</div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * 지출 결의서 내용 렌더링
     * @param {Object} data - 지출 결의서 데이터
     * @returns {string} 렌더링된 HTML
     */
    function renderExpenseContent(data) {
        // 지출 항목 HTML 생성
        let itemsHtml = '';
        if (data.items && Array.isArray(data.items)) {
            itemsHtml = `
                <table class="doc-item-table">
                    <thead>
                        <tr>
                            <th>No.</th>
                            <th>지출일자</th>
                            <th>지출내역</th>
                            <th>지출유형</th>
                            <th>금액(원)</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            
            data.items.forEach((item, index) => {
                const formattedAmount = typeof item.amount === 'number' ? item.amount.toLocaleString() : '0';
                itemsHtml += `
                    <tr>
                        <td>${index + 1}</td>
                        <td>${item.date || '-'}</td>
                        <td>${item.detail || '-'}</td>
                        <td>${item.category || '-'}</td>
                        <td style="text-align: right">${formattedAmount}</td>
                    </tr>
                `;
            });
            
            // 합계 행 추가
            const totalAmount = data.totalAmount ? parseFloat(data.totalAmount).toLocaleString() : '0';
            itemsHtml += `
                    <tr class="doc-item-total">
                        <td colspan="4" style="text-align: center">합계</td>
                        <td style="text-align: right">${totalAmount}</td>
                    </tr>
                </tbody>
                </table>
            `;
        }
        
        return `
            <div class="doc-section expense-document">
                <h3 class="doc-section-title">지출 결의서</h3>
                
                <div class="doc-info-table">
                    <div class="doc-info-row">
                        <div class="doc-info-label">기안자</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.empName : '-'}</div>
                        <div class="doc-info-label">소속부서</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.deptName : '-'}</div>
                        <div class="doc-info-label">직위</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.positionName : '-'}</div>
                    </div>
                </div>
                
                <div class="doc-section-content">
                    <div class="doc-field-group">
                        <div class="doc-field-label">지출유형</div>
                        <div class="doc-field-value">${data.expenseType || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">계좌정보</div>
                        <div class="doc-field-value">${data.accountInfo || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">지출내역</div>
                        <div class="doc-field-value">${itemsHtml}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">지출내용</div>
                        <div class="doc-field-value">${data.description || '-'}</div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * 재택근무 신청서 내용 렌더링
     * @param {Object} data - 재택근무 신청서 데이터
     * @returns {string} 렌더링된 HTML
     */
    function renderRemoteWorkContent(data) {
        return `
            <div class="doc-section remote-work-document">
                <h3 class="doc-section-title">재택근무 신청서</h3>
                
                <div class="doc-info-table">
                    <div class="doc-info-row">
                        <div class="doc-info-label">기안자</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.empName : '-'}</div>
                        <div class="doc-info-label">소속부서</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.deptName : '-'}</div>
                        <div class="doc-info-label">직위</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.positionName : '-'}</div>
                    </div>
                </div>
                
                <div class="doc-section-content">
                    <div class="doc-field-group">
                        <div class="doc-field-label">근무유형</div>
                        <div class="doc-field-value">${data.workType || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">근무기간</div>
                        <div class="doc-field-value">${data.startDate || '-'} ~ ${data.endDate || '-'} (${data.days || '0'}일)</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">업무계획</div>
                        <div class="doc-field-value">${data.workPlan || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">신청사유</div>
                        <div class="doc-field-value">${data.reason || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">근무지주소</div>
                        <div class="doc-field-value">${data.workLocation || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">비상연락처</div>
                        <div class="doc-field-value">${data.contact || '-'}</div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * 출장 신청서 내용 렌더링
     * @param {Object} data - 출장 신청서 데이터
     * @returns {string} 렌더링된 HTML
     */
    function renderBusinessTripContent(data) {
        // 예상 경비 정보 생성
        let expensesHtml = '';
        if (data.expenses) {
            // 숫자 형식 지정 함수 - 내부에서만 사용
            function formatAmount(num) {
                if (num === null || num === undefined || isNaN(parseFloat(num))) return '0';
                return parseFloat(num).toLocaleString();
            }
            
            expensesHtml = `
                <table class="doc-expense-table">
                    <tr>
                        <th>구분</th>
                        <th>금액(원)</th>
                    </tr>
                    <tr>
                        <td>교통비</td>
                        <td style="text-align: right">${formatAmount(data.expenses.transport)}</td>
                    </tr>
                    <tr>
                        <td>숙박비</td>
                        <td style="text-align: right">${formatAmount(data.expenses.accommodation)}</td>
                    </tr>
                    <tr>
                        <td>식비</td>
                        <td style="text-align: right">${formatAmount(data.expenses.meal)}</td>
                    </tr>
                    <tr>
                        <td>기타</td>
                        <td style="text-align: right">${formatAmount(data.expenses.other)}</td>
                    </tr>
                    <tr class="doc-expense-total">
                        <td>합계</td>
                        <td style="text-align: right">${formatAmount(data.expenses.total)}</td>
                    </tr>
                </table>
            `;
        }
        
        return `
            <div class="doc-section business-trip-document">
                <h3 class="doc-section-title">출장 신청서</h3>
                
                <div class="doc-info-table">
                    <div class="doc-info-row">
                        <div class="doc-info-label">기안자</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.empName : '-'}</div>
                        <div class="doc-info-label">소속부서</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.deptName : '-'}</div>
                        <div class="doc-info-label">직위</div>
                        <div class="doc-info-value">${data.drafterInfo ? data.drafterInfo.positionName : '-'}</div>
                    </div>
                </div>
                
                <div class="doc-section-content">
                    <div class="doc-field-group">
                        <div class="doc-field-label">출장유형</div>
                        <div class="doc-field-value">${data.tripType || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">출장지</div>
                        <div class="doc-field-value">${data.location || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">출장기간</div>
                        <div class="doc-field-value">${data.startDate || '-'} ~ ${data.endDate || '-'} (${data.days || '0'}일)</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">출장목적</div>
                        <div class="doc-field-value">${data.purpose || '-'}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">예상경비</div>
                        <div class="doc-field-value">${expensesHtml}</div>
                    </div>
                    
                    <div class="doc-field-group">
                        <div class="doc-field-label">비상연락처</div>
                        <div class="doc-field-value">${data.contact || '-'}</div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * 문서 내용에서 특정 레이블에 해당하는 값 추출
     * @param {string} content - 문서 내용
     * @param {string} label - 추출할 레이블
     * @returns {string} 추출된 값
     */
    function extractValueFromContent(content, label) {
        const regex = new RegExp(`<strong>${label}\\s*:</strong>\\s*([^<]+)`, 'i');
        const match = content.match(regex);
        return match ? match[1].trim() : '';
    }

    /**
     * 승인 모달 열기
     */
    function openApprovalModal() {
        document.getElementById('apdetail-comment-title').textContent = '결재 승인';
        document.getElementById('apdetail-comment-text').placeholder = '의견을 입력하세요 (선택사항)';
        document.getElementById('apdetail-comment-text').required = false;
        document.getElementById('apdetail-submit-btn').textContent = '승인';
        document.getElementById('apdetail-submit-btn').className = 'apdetail-btn apdetail-btn-approve';
        document.getElementById('apdetail-comment-form').action = 'approve.ap';
        document.getElementById('apdetail-comment-form').method = 'POST';
        
        document.getElementById('apdetail-comment-modal').classList.add('active');
    }
    
    /**
     * 반려 모달 열기
     */
    function openRejectModal() {
        document.getElementById('apdetail-comment-title').textContent = '결재 반려';
        document.getElementById('apdetail-comment-text').placeholder = '반려 사유를 입력하세요 (필수)';
        document.getElementById('apdetail-comment-text').required = true;
        document.getElementById('apdetail-submit-btn').textContent = '반려';
        document.getElementById('apdetail-submit-btn').className = 'apdetail-btn apdetail-btn-reject';
        document.getElementById('apdetail-comment-form').action = 'reject.ap';
        
        document.getElementById('apdetail-comment-modal').classList.add('active');
    }
    
    /**
     * 모달 닫기
     */
    function closeCommentModal() {
        document.getElementById('apdetail-comment-modal').classList.remove('active');
        document.getElementById('apdetail-comment-text').value = '';
    }
    
    /**
     * 문서 수정 페이지로 이동
     */
    function editDocument() {
        window.location.href = 'updateDocument.ap?docNo=' + docNo;
    }
    
    /**
     * 문서 삭제
     */
    function deleteDocument() {
        if (confirm('정말 삭제하시겠습니까?')) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = 'deleteDocument.ap';
            
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'docNo';
            input.value = docNo;
            
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        }
    }
    
    /**
     * 이전 페이지로 이동
     */
    function goBack() {
        history.back();
    }
    
    // 페이지 로드 시 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // 문서 내용 표시
        displayDocumentContent();
        
        // 모달 외부 클릭 시 닫기
        document.getElementById('apdetail-comment-modal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeCommentModal();
            }
        });
    });
    
    // 전역으로 접근 가능한 함수 노출
    window.openApprovalModal = openApprovalModal;
    window.openRejectModal = openRejectModal;
    window.closeCommentModal = closeCommentModal;
    window.editDocument = editDocument;
    window.deleteDocument = deleteDocument;
    window.goBack = goBack;
    
})();