package com.kh.flokrGroupware.approval.state;

import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;

/**
 * 전자결재 문서의 상태를 나타내는 인터페이스
 * 모든 상태가 구현해야 할 메소드들을 정의함
 */
public interface DocumentState {
	
	// 저장 동작 - 문서를 저장/수정함
	void save(ApprovalDoc document);
	
	// 결재 요청 동작 - 문서를 결재 요청 상태로 변경
	void submit(ApprovalDoc document);
	
	// 승인 동작 - 문서를 승인
	void approve(ApprovalDoc document, ApprovalLine currentLine);
	
	// 반려 동작 - 문서를 반려
	void reject(ApprovalDoc document, ApprovalLine currentLine);
	
	// 상태 이름 반환
	String getStateName();

}
