package com.kh.flokrGroupware.schedule.model.service;

import java.util.ArrayList;
import java.util.Map;

import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.schedule.model.vo.Schedule;
import com.kh.flokrGroupware.schedule.model.vo.ScheduleAttendee;

public interface ScheduleService {
	
    // 일정 상세 조회
    Schedule selectSchedule(int scheduleNo);
    
    // 일정 등록
    int insertSchedule(Schedule schedule, int[] attendeeArray);
    
    // 직원 목록 조회
    ArrayList<Employee> selectEmployeeList(int loginEmpNo);
    
    // 일정 수정
    int updateSchedule(Schedule schedule, int[] attendeeArray);
    
    // 일정 삭제
    int deleteSchedule(int scheduleNo, int empNo);
    
    // 일정 참석자 조회
    ArrayList<ScheduleAttendee> selectAttendees(int scheduleNo);
    
    // 캘린더용 일정 변환
    ArrayList<Map<String, Object>> convertToCalendarEvents(ArrayList<Schedule> scheduleList);
    
    // FullCalendar용 일정 데이터 조회 및 변환
    ArrayList<Map<String, Object>> getScheduleEvents(int empNo, int deptNo, String start, String end);

    // 특정 날짜의 일정 목록 조회 (하이브리드 접근 방식용)
    ArrayList<Schedule> selectDaySchedules(String date, int empNo, int deptNo);
}
