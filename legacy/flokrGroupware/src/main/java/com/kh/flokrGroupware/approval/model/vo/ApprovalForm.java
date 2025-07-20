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
public class ApprovalForm implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// 기본필드
	private int formNo;			// 결재양식번호
	private String formName;	// 양식명
	private String description;	// 양식설명
	private int isActive;		// 활성여부
	private int createEmpNo;	// 생성자 번호
	private Date createDate;	// 생성일
	private String status;		// 활성상태
	
	// 조인결과 담을 필드
	private String createName; // 생성자 이름
	
}
