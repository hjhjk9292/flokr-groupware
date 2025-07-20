package com.kh.flokrGroupware.attendance.model.vo;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

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
public class Attendance {
	
	private int attendanceNo;
	private int empNo;
	private Date attendanceDate;
	private Timestamp clockInTime;
	private Timestamp clockOutTime;
	private String attStatus;
	
	// 기간 검색을 위한 필드 추가
	private Date startDate;
	private Date endDate;
	
	// startOfWeek 필드 추가
	private Date startOfWeek;
	
	// getter/setter 명시적 추가
	public int getAttNo() {
		return attendanceNo;
	}
	
	public void setAttNo(int attNo) {
		this.attendanceNo = attNo;
	}
}