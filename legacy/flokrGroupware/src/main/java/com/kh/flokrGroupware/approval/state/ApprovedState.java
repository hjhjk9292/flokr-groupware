package com.kh.flokrGroupware.approval.state;

import com.kh.flokrGroupware.approval.model.service.ApprovalService;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;

import lombok.extern.slf4j.Slf4j;

/**
 * 승인완료 상태를 나타내는 클래스
 */
@Slf4j
public class ApprovedState implements DocumentState {
	
	private ApprovalService aService;
	
	public ApprovedState(ApprovalService aService) {
		this.aService = aService;
	}
	
	@Override
	public void save(ApprovalDoc document) {
		// 승인완료 상태에서는 저장 불가능
		throw new IllegalStateException("승인완료 상태에서는 저장할 수 없습니다.");
	}

	@Override
	public void submit(ApprovalDoc document) {
		// 승인완료 상태에서는 결재요청 불가능
		throw new IllegalStateException("승인완료 상태에서는 결재요청할 수 없습니다.");
	}

	@Override
	public void approve(ApprovalDoc document, ApprovalLine currentLine) {
		// 이미 승인완료 상태
		throw new IllegalStateException("이미 승인완료 상태입니다.");
	}

	@Override
	public void reject(ApprovalDoc document, ApprovalLine currentLine) {
		// 승인완료 상태에서는 반려 불가능
		throw new IllegalStateException("승인완료 상태에서는 반려할 수 없습니다.");
	}

	@Override
	public String getStateName() {
		return "승인완료";
	}

}
