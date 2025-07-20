package com.kh.flokrGroupware.attendance.model.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.kh.flokrGroupware.attendance.model.vo.Attendance;
import com.kh.flokrGroupware.attendance.model.vo.WeeklySummary;

public interface AttendanceService {
	
	Attendance getTodayAttendance(int empNo);
	
	int insertTodayAttendance(int empNo, Date today);
	
	int updateClockIn(int empNo, Timestamp now, String status);
	
	int updateClockOut(int empNo, Timestamp now);
	
	// 업무 형태 업데이트 메서드 추가
	int updateWorkType(int empNo, String type);
	
	Duration calculateWeekWorkDuration(int empNo);
	
	List<WeeklySummary> calculateWeeklySummaries(int empNo);
	
	Map<String, Duration> calculateMonthSummary(int empNo);
	
	// 특정 연도, 월에 대한 월간 근무 시간 계산 메서드 추가
	Map<String, Duration> calculateMonthSummaryForYearMonth(int empNo, int year, int month);
	
	Map<Integer, List<Attendance>> getWeeklyAttendanceMap(int empNo);
	
	List<WeeklySummary> getWeeklySummariesForMonth(int empNo, int year, int month);
	
	Map<Integer, List<Attendance>> getWeeklyAttendanceMapForMonth(int empNo, int year, int month);
}