package com.kh.flokrGroupware.approval.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class ApprovalLine implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// 기본필드
	private int lineNo;				// 결재선번호
	private int docNo;				// 문서번호
	private int approverEmpNo;		// 결재자 사원 번호
	private int approvalOrder;		// 결재순서
	private String approvalComment;	// 결재의견  
	private Date processedDate;		// 처리일
	private String lineStatus;		// 결재상태(WAITING, APPROVED, REJECTED)
	
	// 조인 결과 담을 필드
	private String approverName;	// 결재자 이름
	private String deptName;		// 결재자 부서명
	private String positionName;	// 결재자 직급명

}
