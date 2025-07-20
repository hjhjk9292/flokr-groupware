package com.kh.flokrGroupware.approval.state;

import java.util.Date;

import com.kh.flokrGroupware.approval.model.service.ApprovalService;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;

import lombok.extern.slf4j.Slf4j;

/**
 * 임시저장 상태를 나타내는 클래스
 */
@Slf4j
public class DraftState implements DocumentState{
	
	private ApprovalService aService;
	
	public DraftState(ApprovalService aService) {
		this.aService = aService;
	}
	
	@Override
	public void save(ApprovalDoc document) {
		// 임시저장 상태에서는 언제든지 저장 가능 - 문서 업데이트
		document.setUpdateDate(new Date());
		aService.updateDocument(document);
	}

	@Override
	public void submit(ApprovalDoc document) {
		// 임시저장 -> 결재요청 상태로 변경
		document.setDocStatus("REQUESTED");
		document.setRequestedDate(new Date());
		document.setUpdateDate(new Date());
		aService.updateDocument(document);
		
		// 첫 번째 결재자의 상태를 WAITING으로 변경(활성화 시킴)
		aService.updateFirstApproverStatus(document.getDocNo());
	}

	@Override
	public void approve(ApprovalDoc document, ApprovalLine currentLine) {
		// 임시저장 상태에서는 승인 불가능
		throw new IllegalStateException("임시저장 상태에서는 승인할 수 없습니다.");
	}

	@Override
	public void reject(ApprovalDoc document, ApprovalLine currentLine) {
		// 임시저장 상태에서는 반려 불가능
		throw new IllegalStateException("임시저장 상태에서는 반려할 수 없습니다.");
	}

	@Override
	public String getStateName() {
		return "임시저장";
	}

}
