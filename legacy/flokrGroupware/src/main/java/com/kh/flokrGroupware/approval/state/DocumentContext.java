package com.kh.flokrGroupware.approval.state;

import com.kh.flokrGroupware.approval.model.service.ApprovalService;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;

import lombok.extern.slf4j.Slf4j;

/**
 * 문서의 현재 상태를 관리하는 컨텍스트 클래스
 * 상태의 패턴에서 상태 객체에게 행동을 위임함
 */
@Slf4j
public class DocumentContext {
	
	private DocumentState state;	 	// 현재 상태
	private ApprovalDoc document; 		// 문서 객체
	private ApprovalService aService;	// 결재 서비스 인터페이스
	
	/**
	 * 생성자: 문서의 상태에 따라 적절한 상태 객체 설정
	 * @param document 현재문서
	 * @param aService 결재 서비스(인터페이스)
	 */
	public DocumentContext(ApprovalDoc document, ApprovalService aService) {
		this.document = document;
		this.aService = aService;
		updateStateFromDocument();
	}
	
		// 문서 상태에 따라 적절한 상태 객체 설정
		private void updateStateFromDocument() {
			switch(document.getDocStatus()) {
			case "DRAFT": // 임시저장
				this.state = new DraftState(aService);
				break;
			case "REQUESTED": // 결재요청
				this.state = new RequestedState(aService);
				break;
			case "APPROVED": // 승인
				this.state = new ApprovedState(aService);
				break;
			case "REJECTED": // 반려
				this.state = new RejectedState(aService);
				break;
			default:
				this.state = new DraftState(aService);
				break;
			}
			
		}
		
	/**
	 * 상태 변경 메소드
	 * @param state 새 상태
	 */
	public void setState(DocumentState state) {
		this.state = state;
	}
	
	/**
	 * 저장 동작을 현재 상태에 위임
	 */
	public void save() {
		state.save(document);
		updateStateFromDocument();
	}
	
	/**
	 * 결재 요청 동작을 현재 상태에 위임
	 */
	public void submit() {
		state.submit(document);
		updateStateFromDocument();
	}
	
	/**
	 * 승인 동작을 현재 상태에 위임
	 * @param currentLine 현재 결재선
	 */
	public void approve(ApprovalLine currentLine) {
		state.approve(document, currentLine);
		document = aService.selectDocumentByNo(document.getDocNo());
		updateStateFromDocument();
	}
	
	/**
	 * 반려 동작을 현재 상태에 위임
	 * @param currentLine 현재 결재선
	 */
	public void reject(ApprovalLine currentLine) {
		state.reject(document, currentLine);
		updateStateFromDocument();
	}
	
	/**
	 * 현재 상태 이름 반환
	 * @return 상태명
	 */
	public String getStateName() {
		return state.getStateName();
	}

}
