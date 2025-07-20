package com.kh.flokrGroupware.approval.state;

import java.util.ArrayList;
import java.util.Date;

import com.kh.flokrGroupware.approval.model.service.ApprovalService;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;

import lombok.extern.slf4j.Slf4j;

/**
 * 결제요청 상태를 나타내는 클래스
 */
@Slf4j
public class RequestedState implements DocumentState {
	
	private ApprovalService aService;
	
	public RequestedState(ApprovalService aService) {
		this.aService = aService;
	}

	@Override
	public void save(ApprovalDoc document) {
		// 결재요청 상태에서는 저장 불가능
		throw new IllegalStateException("결재요청 상태에서는 저장할 수 없습니다.");
	}

	@Override
	public void submit(ApprovalDoc document) {
		// 이미 결재요청 상태이므로 재요청 불가능
		throw new IllegalStateException("이미 결재요청 상태입니다.");
	}

	@Override
	public void approve(ApprovalDoc document, ApprovalLine currentLine) {
		// 현재 결재자의 승인 처리
		currentLine.setLineStatus("APPROVED");
		currentLine.setProcessedDate(new Date());
		aService.updateApprovalLine(currentLine);
		
		// 다음 결재자가 있는지 확인
		ArrayList<ApprovalLine> lineList = aService.selectApprovalLineByDocNo(document.getDocNo());
		
		boolean nextApprover = false;
		for(ApprovalLine line : lineList) {
			if(line.getLineStatus().equals("PENDING")) {
				// 다음 결재자가 있으면 상태를 WAITING으로 변경
				line.setLineStatus("WAITING");
				aService.updateApprovalLine(line);
				nextApprover = true;
				break;					
			}		
		}
		
		if(!nextApprover) {
			// 다음 결재자가 없으면 문서를 승인완료 상태로 변경
			document.setDocStatus("APPROVED");
			document.setCompletedDate(new Date());
			document.setUpdateDate(new Date());
			aService.updateDocument(document);
		}
		
	}

	@Override
	public void reject(ApprovalDoc document, ApprovalLine currentLine) {
		// 현재 결재자의 반려 처리
		currentLine.setLineStatus("REJECTED");
		currentLine.setProcessedDate(new Date());
		aService.updateApprovalLine(currentLine);
		
		// 문서 상태를 반려로 변경
		document.setDocStatus("REJECTED");
		document.setCompletedDate(new Date());
		document.setUpdateDate(new Date());
		aService.updateDocument(document);
	}

	@Override
	public String getStateName() {
		return "결재요청";
	}
	
}
