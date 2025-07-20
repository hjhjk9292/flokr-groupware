package com.kh.flokrGroupware.approval.state;

import java.util.Date;

import com.kh.flokrGroupware.approval.model.service.ApprovalService;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;

import lombok.extern.slf4j.Slf4j;

/**
 * 반려 상태를 나타내는 클래스
 */
@Slf4j
public class RejectedState implements DocumentState {
	
	private ApprovalService aService;
	
	public RejectedState(ApprovalService aService) {
		this.aService = aService;
	}
	
	@Override
	public void save(ApprovalDoc document) {
		// 반려된 문서는 수정 가능
		document.setUpdateDate(new Date());
		aService.updateDocument(document);
	}

	@Override
	public void submit(ApprovalDoc document) {
		// 반려된 문서 재제출 로직
		document.setDocStatus("REQUESTED");
		document.setRequestedDate(new Date());
		document.setCompletedDate(null);
		document.setUpdateDate(new Date());
		document.setVersion(document.getVersion() + 1); // 버전 증가
		aService.updateDocument(document);
		
		// 결재선 초기화 및 첫번째 결재자 WAITING으로 설정
		aService.resetApprovalLines(document.getDocNo());
	}

	@Override
	public void approve(ApprovalDoc document, ApprovalLine currentLine) {
		// 반려 상태에서는 승인 불가능
		throw new IllegalStateException("반려 상태에서는 승인할 수 없습니다.");
	}

	@Override
	public void reject(ApprovalDoc document, ApprovalLine currentLine) {
		// 이미 반려 상태
		throw new IllegalStateException("이미 반려 상태입니다.");
	}

	@Override
	public String getStateName() {
		return "반려";
	}
}
