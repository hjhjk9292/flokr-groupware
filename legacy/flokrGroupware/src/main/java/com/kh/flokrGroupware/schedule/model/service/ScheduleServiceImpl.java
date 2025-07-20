package com.kh.flokrGroupware.schedule.model.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.schedule.model.dao.ScheduleDao;
import com.kh.flokrGroupware.schedule.model.vo.Schedule;
import com.kh.flokrGroupware.schedule.model.vo.ScheduleAttendee;

@Service
public class ScheduleServiceImpl implements ScheduleService {
	
	@Autowired
	private ScheduleDao sDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;

	/**
	 * 일정 상세 정보 조회
	 */
	@Override
	public Schedule selectSchedule(int scheduleNo) {
		return sDao.selectSchedule(sqlSession, scheduleNo);
	}

	@Override
	public int insertSchedule(Schedule schedule, int[] attendeeArray) {
		// 일정 기본 정보 등록
		int result = sDao.insertSchedule(sqlSession, schedule);
		
		// 등록 성공 & 참석자 있으면 참석자 정보 등록
		if(result > 0 && attendeeArray != null && attendeeArray.length > 0) {
			for(int empNo : attendeeArray) {
				ScheduleAttendee attendee = new ScheduleAttendee();
				attendee.setScheduleNo(schedule.getScheduleNo());
				attendee.setEmpNo(empNo);
				attendee.setResponseStatus("PENDING");
				attendee.setNotificationSent(0); // 알림 발송 여부 0
				
				int attendeeResult = sDao.insertScheduleAttendee(sqlSession, attendee);
				if(attendeeResult == 0) {
					// 로그 기록 등 실패 처리
					System.out.println("참석자 등록 실패: " + empNo);
				}
			}
		}

		return result;
	
	}

	/**
	 * 일정 수정 메소드
	 */
	@Override
	public int updateSchedule(Schedule schedule, int[] attendeeArray) {
		// 1. 일정 정보 업데이트
		int result1 = sDao.updateSchedule(sqlSession, schedule);
		
		if(result1 > 0) {
			// 2. 기존 참석자 정보 삭제 (참석자 테이블 활성상태 Y -> N)
			sDao.deleteScheduleAttendees(sqlSession, schedule.getScheduleNo());
			
			// 3. 새 참석자 정보 추가
			int result2 = 1; // 참석자가 없는 경우 성공으로 처리
			if(attendeeArray != null && attendeeArray.length > 0) {
				for(int empNo : attendeeArray) {
					// 해당 일정- 참석자 조합이 이미 존재하는지 확인
					ScheduleAttendee existingAttendee = new ScheduleAttendee();
					existingAttendee.setScheduleNo(schedule.getScheduleNo());
					existingAttendee.setEmpNo(empNo);
					
					// 이미 존재하는 참석자인지 확인 (status에 상관없이)
					ScheduleAttendee foundAttendee = sDao.checkExistingAttendee(sqlSession, existingAttendee);
					
					int insertResult = 0;
					if(foundAttendee != null) {
						// 존재하는 경우 status만 'Y'로 업데이트
						foundAttendee.setResponseStatus("PENDING"); // 응답 상태 초기화
						foundAttendee.setStatus("Y"); // 활성화
						insertResult = sDao.reactivateScheduleAttendee(sqlSession, foundAttendee);
					}else {
						// 존재하지 않는 경우 새로 추가
						ScheduleAttendee attendee = new ScheduleAttendee();
						attendee.setScheduleNo(schedule.getScheduleNo());
						attendee.setEmpNo(empNo);
						attendee.setResponseStatus("PENDING"); // 기본 응답 상태
						attendee.setNotificationSent(0); // 알림 미발송 상태
						
						insertResult = sDao.insertScheduleAttendee(sqlSession, attendee);
					}
					
					if(insertResult == 0) {
						result2 = 0; // 하나라도 실패하면 0으로 설정
						break;
					}
				}
			}
			
			return (result2 > 0) ? 1 : 0; // 참석자 처리까지 성공해야 성공으로 반환
			
		}
		
		return 0; // 일정 업데이트 실패
	
	}

	/**
	 * 일정 삭제 메소드
	 */
	@Override
	public int deleteSchedule(int scheduleNo, int empNo) {
		// 1. 권한 확인 (작성자만 삭제 가능)
		Schedule schedule = sDao.selectSchedule(sqlSession, scheduleNo);
		if(schedule == null || schedule.getCreateEmpNo() != empNo) {
			return 0; // 권한 없음
		}
		
		// 참석자 테이블 삭제 처리(status = 'Y' => 'N')
		sDao.deleteScheduleAttendees(sqlSession, scheduleNo);
		
		// 3. 일정 테이블 삭제 처리(status = 'Y' => 'N')
		return sDao.deleteSchedule(sqlSession, scheduleNo);
	}

	/**
	 * 일정 참석자 목록 조회
	 */
	@Override
	public ArrayList<ScheduleAttendee> selectAttendees(int scheduleNo) {
		return sDao.selectAttendees(sqlSession, scheduleNo);
	}

	@Override
	public ArrayList<Map<String, Object>> getScheduleEvents(int empNo, int deptNo, String start, String end) {
		
		// 개인 일정, 팀 일정, 회사 일정 모두 한번에 조회
		Map<String, Object> params = new HashMap<>();
		params.put("empNo", empNo);
		params.put("deptNo", deptNo);
		params.put("start", start);
		params.put("end", end);
		
		// 2. DAO 호출: 사용자가 볼 수 있는 모든 관련 일정 조회
		ArrayList<Schedule> allSchedules = sDao.selectAllTypeSchedules(sqlSession, params);
		
		// 3. 조회된 데이터를 FullCalendar 형식으로 변환하는 메소드 호출
        return convertToCalendarEvents(allSchedules);
	
	}
	
	// 데이터 변환 메소드 구현
	@Override
	public ArrayList<Map<String, Object>> convertToCalendarEvents(ArrayList<Schedule> scheduleList) {
		ArrayList<Map<String, Object>> events = new ArrayList<>();
		
		if (scheduleList == null || scheduleList.isEmpty()) {
	        return events;
	    }
		
		// 날짜 포맷 설정
	    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		for(Schedule schedule : scheduleList) {
			Map<String, Object> event = new HashMap<>();
			
			// 기본 이벤트 데이터
	        event.put("id", schedule.getScheduleNo());
	        event.put("title", schedule.getScheduleTitle());
	        
	        // 종일 일정 여부 확인
	        boolean isAllDay = false;
	        
	     // 1. allDay 필드 확인 (입력 폼에서 설정된 경우)
	        if ("Y".equals(schedule.getAllDay())) {
	            isAllDay = true;
	        } 
	        // 2. 시간 분석 (allDay 필드가 없거나 null인 경우)
	        else if (schedule.getAllDay() == null || schedule.getAllDay().isEmpty()) {
	            if (schedule.getStartDate() != null && schedule.getEndDate() != null) {
	                Calendar startCal = Calendar.getInstance();
	                startCal.setTime(schedule.getStartDate());
	                
	                Calendar endCal = Calendar.getInstance();
	                endCal.setTime(schedule.getEndDate());
	                
	                // 시작 시간이 00:00:00이고, 종료 시간이 00:00:00 이거나 23:59:59인 경우 종일 일정으로 판단
	                boolean startTimeIsMidnight = startCal.get(Calendar.HOUR_OF_DAY) == 0 &&
	                                              startCal.get(Calendar.MINUTE) == 0 &&
	                                              startCal.get(Calendar.SECOND) == 0;
	                                              
	                boolean endTimeIsMidnightOrLastSecond =
	                        (endCal.get(Calendar.HOUR_OF_DAY) == 0 &&
	                         endCal.get(Calendar.MINUTE) == 0 &&
	                         endCal.get(Calendar.SECOND) == 0) ||
	                        (endCal.get(Calendar.HOUR_OF_DAY) == 23 &&
	                         endCal.get(Calendar.MINUTE) == 59 &&
	                         endCal.get(Calendar.SECOND) == 59);
	                
	                isAllDay = startTimeIsMidnight && endTimeIsMidnightOrLastSecond;
	            }
	        }
	        
	        // 날짜 포맷 적용
	        if (schedule.getStartDate() != null) {
	            event.put("start", isAllDay ? 
	                      dateFormat.format(schedule.getStartDate()) : 
	                      isoFormat.format(schedule.getStartDate()));
	        }
	        
	        if (schedule.getEndDate() != null) {
	            // 종일 일정인 경우 종료일 처리
	            if (isAllDay) {
	                Calendar  endCal = Calendar.getInstance();
	                endCal.setTime(schedule.getEndDate());
	                
	                // 종료 시간이 00:00:00이 아닌 경우(23:59:59)에는 다음 날로 설정하지 않음
	                // FullCalendar는 종일 일정의 end date를 exclusive로 처리하기 때문
	                if (endCal.get(Calendar.HOUR_OF_DAY) == 0 && 
	                    endCal.get(Calendar.MINUTE) == 0 && 
	                    endCal.get(Calendar.SECOND) == 0) {
	                    endCal.add(Calendar.DATE, 1);
	                }
	                
	                event.put("end", dateFormat.format(endCal.getTime()));
	            } else {
	                event.put("end", isoFormat.format(schedule.getEndDate()));
	            }
	        }
	        
	        // FullCalendar에 종일 일정 여부 전달
	        event.put("allDay", isAllDay);
			
			// 일정 유형에 따른 클래스 지정
			String className = "";
			
			if("PERSONAL".equals(schedule.getScheduleType())) {
				className = "calendar-personal-event";
			}else if("TEAM".equals(schedule.getScheduleType())) {
				className = "calendar-team-event";
			}else if("COMPANY".equals(schedule.getScheduleType())) {
				className = "calendar-company-event";
			}else if("OTHER".equals(schedule.getScheduleType())) {
			    className = "calendar-other-event";
			}else { // 기본값
				className = "calendar-personal-event";
			}
			
			event.put("className", className);
			
			// 추가속성
			Map<String, Object> extendedProps = new HashMap<>();
	        extendedProps.put("description", schedule.getDescription());
	        extendedProps.put("location", schedule.getLocation());
	        extendedProps.put("important", schedule.getImportant());
	        extendedProps.put("scheduleType", schedule.getScheduleType());
	        
	        event.put("extendedProps", extendedProps);
	        
	        events.add(event);
			
		}
		
		return events;
		
	}

	/**
	 * 직원 목록 조회 (등록폼)
	 */
	@Override
	public ArrayList<Employee> selectEmployeeList(int loginEmpNo) {
		return sDao.selectEmployeeList(sqlSession, loginEmpNo);
	}

	/**
	 * 특정 날짜의 일정 목록을 조회하는 메소드
	 */
	@Override
	public ArrayList<Schedule> selectDaySchedules(String date, int empNo, int deptNo) {
		Map<String, Object> params = new HashMap<>();
		params.put("date", date);
		params.put("empNo", empNo);
		params.put("deptNo", deptNo);
		
		return sDao.selectDaySchedules(sqlSession, params);
	}

}
