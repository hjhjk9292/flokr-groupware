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
public class ApprovalDoc implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// 필드
	private int docNo; 				// 문서 번호
	private int formNo; 			// 사용된 양식 번호
	private int drafterEmpNo; 		// 기안자 번호
	private String title; 			// 문서제목
	private String docContent; 		// 문서내용
	private Date requestedDate; 		// 결제 요청일
	private Date completedDate;		// 결제 완료일
	private int version;			// 문서버전
	private Date createDate; 		// 생성일
	private Date updateDate; 		// 수정일
	private String docStatus; 		// 문서상태(DRAFT, REQUESTED, APPROVED, REJECTED)
	private String status; 			// 활성상태(Y/N)
	
	// 조인 결과를 담을 필드
	private String formName;			// 양식이름
	private String drafterName;			// 기안자 이름
	private String drafterDeptName;		// 기안자 부서명
	private String drafterPositionName;	// 기안자 직급명
	
}
