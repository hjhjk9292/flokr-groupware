package com.kh.flokrGroupware.attendance.model.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.attendance.model.dao.AttendanceDao;
import com.kh.flokrGroupware.attendance.model.vo.Attendance;
import com.kh.flokrGroupware.attendance.model.vo.WeeklySummary;

@Service
public class AttendanceServiceImpl implements AttendanceService {

	@Autowired
	private AttendanceDao attDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Override
	public Attendance getTodayAttendance(int empNo) {
		return attDao.getTodayAttendance(sqlSession, empNo);
	}

	@Override
	public int insertTodayAttendance(int empNo, Date today) {
		return attDao.insertTodayAttendance(sqlSession, empNo, today);
	}

	@Override
	public int updateClockIn(int empNo, Timestamp now, String status) {
	    return attDao.updateClockIn(sqlSession, empNo, now, status); // now는 Timestamp 타입으로 전달
	}

	@Override
	public int updateClockOut(int empNo, Timestamp now) {
		return attDao.updateClockOut(sqlSession, empNo, now);
	}
	
	@Override
	public int updateWorkType(int empNo, String type) {
	    // 업무 형태 업데이트 메서드 추가
	    return attDao.updateWorkType(sqlSession, empNo, type);
	}

	@Override
	public Duration calculateWeekWorkDuration(int empNo) {
	    LocalDate today = LocalDate.now();
	    
	    // 이번 주의 시작(월요일) 날짜와 끝(일요일)을 구함
	    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
	    LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY).plusDays(1);  // 일요일 자정 포함

	    // DB에서 해당 날짜 범위 내 근태 기록을 가져옴
	    List<Attendance> weekRecords = attDao.getAttendancesBetween(sqlSession, empNo, startOfWeek, endOfWeek);

	    Duration total = Duration.ZERO;

	    for (Attendance att : weekRecords) {
	        if (att.getClockInTime() != null) {
	            // 출근 시간과 퇴근 시간 계산
	            LocalDateTime clockIn = att.getClockInTime().toLocalDateTime();
	            LocalDateTime clockOut = (att.getClockOutTime() != null) 
	                ? att.getClockOutTime().toLocalDateTime() 
	                : LocalDateTime.now(); // 퇴근 안 했으면 현재 시간까지 계산

	            total = total.plus(Duration.between(clockIn, clockOut)); // 근무 시간 누적
	        }
	    }

	    return total;  // 최종 누적 근무 시간
	}
	
	@Override
	public List<WeeklySummary> calculateWeeklySummaries(int empNo) {
	    LocalDate today = LocalDate.now();
	    LocalDate firstDayOfMonth = today.withDayOfMonth(1);
	    LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

	    List<Attendance> records = attDao.getAttendancesBetween(sqlSession, empNo, firstDayOfMonth, lastDayOfMonth);

	    Map<Integer, WeeklySummary> summaryMap = new LinkedHashMap<>();

	    for (LocalDate date = firstDayOfMonth; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
	        int weekNumber = getWeekOfMonth(date);
	        
	        // 복사본 사용
	        LocalDate dateCopy = date;

	        summaryMap.computeIfAbsent(weekNumber, k -> {
	            LocalDate start = dateCopy;
	            while (start.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == weekNumber && !start.isBefore(firstDayOfMonth)) {
	                start = start.minusDays(1);
	            }
	            start = start.plusDays(1);

	            LocalDate end = dateCopy;
	            while (end.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == weekNumber && !end.isAfter(lastDayOfMonth)) {
	                end = end.plusDays(1);
	            }
	            end = end.minusDays(1);

	            return new WeeklySummary(weekNumber, start, end);
	        });
	    }

	    for (Attendance att : records) {
	        if (att.getClockInTime() != null) {
	            LocalDate d = att.getAttendanceDate().toLocalDate();
	            int weekNum = getWeekOfMonth(d);
	            Duration workDuration;
	            if (att.getClockOutTime() != null) {
	                workDuration = Duration.between(att.getClockInTime().toLocalDateTime(), att.getClockOutTime().toLocalDateTime());
	            } else {
	                workDuration = Duration.between(att.getClockInTime().toLocalDateTime(), LocalDateTime.now());
	            }
	            summaryMap.get(weekNum).addDuration(workDuration);
	        }
	    }

	    return new ArrayList<>(summaryMap.values());
	}


	private int getWeekOfMonth(LocalDate date) {
	    return date.get(ChronoField.ALIGNED_WEEK_OF_MONTH);
	}
	
	@Override
	public Map<String, Duration> calculateMonthSummary(int empNo) {
	    LocalDate first = LocalDate.now().withDayOfMonth(1);
	    LocalDate last = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
	    
	    return calculateMonthSummaryForDates(empNo, first, last);
	}
	
	@Override
	public Map<String, Duration> calculateMonthSummaryForYearMonth(int empNo, int year, int month) {
	    // 특정 연도, 월에 대한 월간 근무 시간 계산
	    YearMonth yearMonth = YearMonth.of(year, month);
	    LocalDate first = yearMonth.atDay(1);
	    LocalDate last = yearMonth.atEndOfMonth();
	    
	    return calculateMonthSummaryForDates(empNo, first, last);
	}
	
	private Map<String, Duration> calculateMonthSummaryForDates(int empNo, LocalDate first, LocalDate last) {
	    List<Attendance> monthRecords = attDao.getAttendancesBetween(sqlSession, empNo, first, last);

	    Duration total = Duration.ZERO;
	    Duration overtime = Duration.ZERO;
	    Map<Integer, Duration> weeklyMap = new HashMap<>();

	    for (Attendance att : monthRecords) {
	        if (att.getClockInTime() != null) {
	            Duration d;
	            if (att.getClockOutTime() != null) {
	                d = Duration.between(att.getClockInTime().toLocalDateTime(), att.getClockOutTime().toLocalDateTime());
	            } else {
	                // 현재 출근 중이고 아직 퇴근 안한 경우
	                LocalDate attDate = att.getAttendanceDate().toLocalDate();
	                LocalDate today = LocalDate.now();
	                // 오늘 날짜의 출근 중인 데이터만 실시간 계산
	                if (attDate.isEqual(today)) {
	                    d = Duration.between(att.getClockInTime().toLocalDateTime(), LocalDateTime.now());
	                } else {
	                    // 과거 날짜이지만 퇴근 기록이 없는 경우 (0으로 처리)
	                    d = Duration.ZERO;
	                }
	            }
	            int week = getWeekOfMonth(att.getAttendanceDate().toLocalDate());
	            weeklyMap.put(week, weeklyMap.getOrDefault(week, Duration.ZERO).plus(d));
	        }
	    }

	    for (Duration week : weeklyMap.values()) {
	        total = total.plus(week);
	        if (week.compareTo(Duration.ofHours(35)) > 0) {
	            overtime = overtime.plus(week.minusHours(35));
	        }
	    }

	    return Map.of("monthlyTotal", total, "monthlyOvertime", overtime);
	}

	@Override
	public Map<Integer, List<Attendance>> getWeeklyAttendanceMap(int empNo) {
	    LocalDate today = LocalDate.now();
	    LocalDate firstDay = today.withDayOfMonth(1);
	    LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

	    List<Attendance> monthRecords = attDao.getAttendancesBetween(sqlSession, empNo, firstDay, lastDay);

	    Map<Integer, List<Attendance>> map = new LinkedHashMap<>();
	    for (Attendance att : monthRecords) {
	        int week = att.getAttendanceDate().toLocalDate().get(ChronoField.ALIGNED_WEEK_OF_MONTH);
	        map.computeIfAbsent(week, k -> new ArrayList<>()).add(att);
	    }
	    return map;
	}

	@Override
	public List<WeeklySummary> getWeeklySummariesForMonth(int empNo, int year, int month) {
	    LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
	    LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

	    List<Attendance> records = attDao.getAttendancesBetween(sqlSession, empNo, firstDayOfMonth, lastDayOfMonth);
	    Map<Integer, WeeklySummary> summaryMap = new LinkedHashMap<>();

	    for (LocalDate date = firstDayOfMonth; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
	        int weekNumber = getWeekOfMonth(date);
	        LocalDate dateCopy = date;

	        summaryMap.computeIfAbsent(weekNumber, k -> {
	            LocalDate start = dateCopy;
	            while (start.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == weekNumber && !start.isBefore(firstDayOfMonth)) {
	                start = start.minusDays(1);
	            }
	            start = start.plusDays(1);

	            LocalDate end = dateCopy;
	            while (end.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == weekNumber && !end.isAfter(lastDayOfMonth)) {
	                end = end.plusDays(1);
	            }
	            end = end.minusDays(1);

	            return new WeeklySummary(weekNumber, start, end);
	        });
	    }

	    for (Attendance att : records) {
	        if (att.getClockInTime() != null) {
	            LocalDate d = att.getAttendanceDate().toLocalDate();
	            int weekNum = getWeekOfMonth(d);
	            Duration workDuration;
	            if (att.getClockOutTime() != null) {
	                workDuration = Duration.between(att.getClockInTime().toLocalDateTime(), att.getClockOutTime().toLocalDateTime());
	            } else {
	                // 오늘인 경우만 실시간 계산, 그 외에는 0으로 처리
	                if (d.isEqual(LocalDate.now())) {
	                    workDuration = Duration.between(att.getClockInTime().toLocalDateTime(), LocalDateTime.now());
	                } else {
	                    workDuration = Duration.ZERO;
	                }
	            }
	            
	            // 주차 정보가 있는 경우에만 추가
	            if (summaryMap.containsKey(weekNum)) {
	                summaryMap.get(weekNum).addDuration(workDuration);
	            }
	        }
	    }

	    return new ArrayList<>(summaryMap.values());
	}

	@Override
	public Map<Integer, List<Attendance>> getWeeklyAttendanceMapForMonth(int empNo, int year, int month) {
	    LocalDate firstDay = LocalDate.of(year, month, 1);
	    LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

	    List<Attendance> monthRecords = attDao.getAttendancesBetween(sqlSession, empNo, firstDay, lastDay);

	    Map<Integer, List<Attendance>> map = new LinkedHashMap<>();
	    for (Attendance att : monthRecords) {
	        int week = att.getAttendanceDate().toLocalDate().get(ChronoField.ALIGNED_WEEK_OF_MONTH);
	        map.computeIfAbsent(week, k -> new ArrayList<>()).add(att);
	    }
	    return map;
	}
}