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
public class ScheduleAttendee {
	
	private int attendeeNo;  			// 참석자 매핑 번호
	private int scheduleNo;				// 일정 번호
	private int empNo;					// 참석하는 직원 번호
	private String responseStatus; 		// 응답 상태
	private int notificationSent; 		// 알림 발송 여부 (1=발송, 0=미발송)
	private String status;				// 활성 상태
	
	// 조회용 추가 필드 (JOIN 시 사용)
    private String empName;        // 참석자 이름
    private String deptName;       // 부서명
    private String positionName;   // 직급명	
    private String profilePath;    // 프로필 이미지 경로
    

}
