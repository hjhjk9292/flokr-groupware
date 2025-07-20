package com.kh.flokrGroupware.schedule.model.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.schedule.model.vo.Schedule;
import com.kh.flokrGroupware.schedule.model.vo.ScheduleAttendee;

@Repository
public class ScheduleDao {
	
	/**
	 * 일정 등록
	 * @param sqlSession SQL 세션
	 * @param schedule 등록할 일정 정보
	 * @return 처리 결과 (성공 1, 실패 0)
	 */
	public int insertSchedule(SqlSessionTemplate sqlSession, Schedule schedule) {
		return sqlSession.insert("scheduleMapper.insertSchedule", schedule);
	}
	
	/**
	 * 일정 참석자 등록
	 * @param sqlSession SQL 세션
	 * @param attendee 참석자 정보
	 * @return 처리 결과 (성공 1, 실패 0)
	 */
	public int insertScheduleAttendee(SqlSessionTemplate sqlSession, ScheduleAttendee attendee) {
		return sqlSession.insert("scheduleMapper.insertScheduleAttendee", attendee);
	}
	
	/**
	 * 직원 목록 조회
	 * @param sqlSession SQL 세션
	 * @return 전체 직원 목록
	 */
	public ArrayList<Employee> selectEmployeeList(SqlSessionTemplate sqlSession, int loginEmpNo){
		return (ArrayList)sqlSession.selectList("scheduleMapper.selectEmployeeList", loginEmpNo);
	}
	
	/**
	 * 모든 유형의 일정 조회 (개인/팀/회사)
	 * @param sqlSession SQL 세션
	 * @param params 조회 파라미터 (empNo, deptNo, start, end)
	 * @return 해당 조건에 맞는 일정 목록
	 */
	public ArrayList<Schedule> selectAllTypeSchedules(SqlSessionTemplate sqlSession, Map<String, Object> params){
		return (ArrayList)sqlSession.selectList("scheduleMapper.selectAllTypeSchedules", params);
	}
	
	/**
	 * 일정 상세 조회
	 * @param sqlSession SQL 세션
	 * @param scheduleNo 조회할 일정 번호
	 * @return 일정 상세 정보
	 */
	public Schedule selectSchedule(SqlSessionTemplate sqlSession, int scheduleNo) {
		return sqlSession.selectOne("scheduleMapper.selectSchedule", scheduleNo);
	}
	
	/**
	 * 일정 참석자 목록 조회
	 * @param sqlSession SQL 세션
	 * @param scheduleNo 일정 번호
	 * @return 일정 참석자 목록
	 */
	public ArrayList<ScheduleAttendee> selectAttendees(SqlSessionTemplate sqlSession, int scheduleNo){
		return (ArrayList)sqlSession.selectList("scheduleMapper.selectAttendees", scheduleNo);
	}
	
	/**
	 * 특정 날짜에 해당하는 일정 목록을 조회
	 * @param sqlSession SQL 세션
	 * @param date 조회할 날짜 (형식: YYYY-MM-DD)
	 * @param empNo 로그인한 사용자의 사번
	 * @return 해당 날짜에 포함된 모든 일정 목록     
	 */
	public ArrayList<Schedule> selectDaySchedules(SqlSessionTemplate sqlSession, Map<String, Object> params){
		return (ArrayList)sqlSession.selectList("scheduleMapper.selectDaySchedules", params);
	}
	
	/**
	 * 일정 정보 업데이트
	 * @param sqlSession SQL 세션
	 * @param schedule 업데이트할 일정 정보
	 * @return 업데이트 결과
	 */
	public int updateSchedule(SqlSessionTemplate sqlSession, Schedule schedule) {
		return sqlSession.update("scheduleMapper.updateSchedule", schedule);
	}
	
	/**
	 * 일정의 모든 참석자 삭제 (활성상태 Y => N)
	 * @param sqlSession SQL 세션
	 * @param scheduleNo 참석자를 삭제할 일정 번호
	 * @return 삭제 결과
	 */
	public int deleteScheduleAttendees(SqlSessionTemplate sqlSession, int scheduleNo) {
		return sqlSession.update("scheduleMapper.deleteScheduleAttendees", scheduleNo);
	}
	
	/**
	 * 일정-참석자 조합이 이미 존재하는지 확인
	 * @param sqlSession SQL 세션
	 * @param attendee 확인할 참석자 정보(scheduleNo와 empNo를 포함)
	 * @return 존재하는 경우 해당 참석자 정보, 존재하지 않는 경우 null
	 */
	public ScheduleAttendee checkExistingAttendee(SqlSessionTemplate sqlSession, ScheduleAttendee attendee) {
		return sqlSession.selectOne("scheduleMapper.checkExistingAttendee", attendee);
	}
	
	/**
	 * 비활성화된 참석자 정보 다시 활성화
	 * @param sqlSession SQL 세션
	 * @param attendee 비활성화된 참석자
	 * @return 활성화 결과
	 */
	public int reactivateScheduleAttendee(SqlSessionTemplate sqlSession, ScheduleAttendee attendee) {
		return sqlSession.update("scheduleMapper.reactivateScheduleAttendee", attendee);
	}
	
	
	/**
	 * 일정 삭제 (활성상태 Y => N)
	 * @param sqlSession SQL 세션 객체
	 * @param scheduleNo 삭제할 일정의 번호
	 * @return 삭제 결과
	 */
	public int deleteSchedule(SqlSessionTemplate sqlSession, int scheduleNo) {
		return sqlSession.update("scheduleMapper.deleteSchedule", scheduleNo);
	}
	


}
