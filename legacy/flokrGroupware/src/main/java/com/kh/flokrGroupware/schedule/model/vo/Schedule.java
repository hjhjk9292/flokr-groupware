package com.kh.flokrGroupware.schedule.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Schedule {
	
	private int scheduleNo;				// 일정 번호
	private int createEmpNo; 			// 작성자 사번
	private String scheduleTitle;		// 일정 제목
	private String description;      	// 일정 설명
	private Date startDate;				// 시작일
	private Date endDate;				// 종료일
	private String location;			// 장소
	private String important;			// 중요도
	private String scheduleType;		// 일정유형
	private Date createDate;			// 생성일
	private Date updateDate;			// 수정일
	private String status;				// 활성상태
	
	// FullCalendar 표시용 추가 필드
    private String empName;       		// 작성자 이름
    private String allDay;				// 종일일정
    
 



}
