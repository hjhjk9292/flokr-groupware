package com.kh.flokrGroupware.attendance.model.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.attendance.model.vo.Attendance;

@Repository
public class AttendanceDao {
    
    public Attendance getTodayAttendance(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("attendanceMapper.getTodayAttendance", empNo);
    }
    
    public int insertTodayAttendance(SqlSessionTemplate sqlSession, int empNo, Date today) {
        Attendance a = new Attendance();
        a.setEmpNo(empNo);
        a.setAttendanceDate(today);
        return sqlSession.insert("attendanceMapper.insertTodayAttendance", a);
    }
    
    public int updateClockIn(SqlSessionTemplate sqlSession, int empNo, Timestamp clockInTime, String attStatus) {
        Attendance a = new Attendance();
        a.setEmpNo(empNo);
        a.setClockInTime(clockInTime);
        a.setAttStatus(attStatus);
        return sqlSession.update("attendanceMapper.updateClockIn", a);
    }
    
    public int updateClockOut(SqlSessionTemplate sqlSession, int empNo, Timestamp clockOutTime) {
        Attendance a = new Attendance();
        a.setEmpNo(empNo);
        a.setClockOutTime(clockOutTime);
        return sqlSession.update("attendanceMapper.updateClockOut", a);
    }
    
    // 업무 형태 업데이트 메서드 추가
    public int updateWorkType(SqlSessionTemplate sqlSession, int empNo, String type) {
        Attendance a = new Attendance();
        a.setEmpNo(empNo);
        a.setAttStatus(type);
        return sqlSession.update("attendanceMapper.updateWorkType", a);
    }
    
    // 기간 별 출근 기록 조회 (수정 버전)
    public List<Attendance> getAttendancesBetween(SqlSessionTemplate sqlSession, int empNo, LocalDate start, LocalDate end) {
        Attendance a = new Attendance();
        a.setEmpNo(empNo);
        a.setStartDate(java.sql.Date.valueOf(start));
        a.setEndDate(java.sql.Date.valueOf(end));
        return sqlSession.selectList("attendanceMapper.getAttendancesBetween", a);
    }
}