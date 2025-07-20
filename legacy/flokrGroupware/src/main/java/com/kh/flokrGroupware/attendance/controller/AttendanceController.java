package com.kh.flokrGroupware.attendance.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.flokrGroupware.attendance.model.service.AttendanceServiceImpl;
import com.kh.flokrGroupware.attendance.model.vo.Attendance;
import com.kh.flokrGroupware.attendance.model.vo.WeeklySummary;
import com.kh.flokrGroupware.employee.model.vo.Employee;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
	
	@Autowired
	private AttendanceServiceImpl attService;
	// 나중에 impl말고 그냥 service로 바꿔주기!!!!!!!!
	
	private Map<String, String> formatDuration(Duration d) {
	    long totalSeconds = d.getSeconds();
	    long h = totalSeconds / 3600;
	    long m = (totalSeconds % 3600) / 60;
	    long s = totalSeconds % 60;
	    return Map.of(
	        "h", String.format("%02d", h),
	        "m", String.format("%02d", m),
	        "s", String.format("%02d", s),
	        "seconds", String.valueOf(totalSeconds) // 총 초 값 추가
	    );
	}
	
	@RequestMapping("/main")
	public String attendanceMainpage(HttpSession session, Model model) {
	    Employee loginUser = (Employee) session.getAttribute("loginUser");
	    int empNo = loginUser.getEmpNo();

	    Attendance todayAttendance = attService.getTodayAttendance(empNo);
	    if (todayAttendance == null) {
	        Date today = new Date(System.currentTimeMillis());
	        attService.insertTodayAttendance(empNo, today);
	        todayAttendance = attService.getTodayAttendance(empNo); // 다시 조회
	    }

	    // 주차 계산 추가
	    int currentWeek = LocalDate.now().get(java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH);
	    model.addAttribute("currentWeek", currentWeek);

	    // 주간 누적/초과/잔여 계산
	    Duration weekDur = attService.calculateWeekWorkDuration(empNo);
	    Duration weeklyOver = weekDur.minus(Duration.ofHours(35)).isNegative() ? Duration.ZERO : weekDur.minus(Duration.ofHours(35));
	    Duration weeklyRemain = weekDur.minus(Duration.ofHours(35)).isNegative() ? Duration.ofHours(35).minus(weekDur) : Duration.ZERO;

	    Map<String, String> weekFormatted = formatDuration(weekDur);
	    Map<String, String> weekOverFormatted = formatDuration(weeklyOver);
	    Map<String, String> weekRemainFormatted = formatDuration(weeklyRemain);
	    
	    model.addAttribute("week", weekFormatted);
	    model.addAttribute("weekOver", weekOverFormatted);
	    model.addAttribute("weekRemain", weekRemainFormatted);
	    
	    // 전체 초 단위 값도 모델에 추가
	    model.addAttribute("weekDurationSeconds", weekDur.getSeconds());
	    model.addAttribute("weekOverSeconds", weeklyOver.getSeconds());
	    model.addAttribute("weekRemainSeconds", weeklyRemain.getSeconds());

	    // 월간 누적/연장 계산
	    Map<String, Duration> monthMap = attService.calculateMonthSummary(empNo);
	    Map<String, String> monthFormatted = formatDuration(monthMap.get("monthlyTotal"));
	    Map<String, String> monthOverFormatted = formatDuration(monthMap.get("monthlyOvertime"));
	    
	    model.addAttribute("month", monthFormatted);
	    model.addAttribute("monthOver", monthOverFormatted);
	    model.addAttribute("monthDurationSeconds", monthMap.get("monthlyTotal").getSeconds());
	    model.addAttribute("monthOverDurationSeconds", monthMap.get("monthlyOvertime").getSeconds());
	    
	    // 기존 Duration 객체도 유지
	    model.addAttribute("monthDuration", monthMap.get("monthlyTotal"));
	    model.addAttribute("monthOverDuration", monthMap.get("monthlyOvertime"));
	    
	    model.addAttribute("attendance", todayAttendance);
	    
	    // 출근 시간 밀리초 값 명시적 추가 (JavaScript에서 사용)
	    if (todayAttendance != null && todayAttendance.getClockInTime() != null) {
	        model.addAttribute("clockInTimeMillis", todayAttendance.getClockInTime().getTime());
	    }
	    
	    model.addAttribute("weekDuration", weekDur);
	    model.addAttribute("weeklySummaries", attService.calculateWeeklySummaries(empNo));
	    
	    LocalDate now = LocalDate.now();
	    model.addAttribute("currentYear", now.getYear());
	    model.addAttribute("currentMonth", now.getMonthValue());

	    Map<Integer, List<Attendance>> weeklyAttendanceMap = attService.getWeeklyAttendanceMap(empNo);
	    model.addAttribute("weeklyAttendanceMap", weeklyAttendanceMap);

	    return "attendance/attendanceMain";
	}
	
	@PostMapping("/clockIn")
	@ResponseBody
	public Map<String, Object> clockIn(@RequestBody Map<String, String> req, HttpSession session) {
	    Employee loginUser = (Employee) session.getAttribute("loginUser");
	    int empNo = loginUser.getEmpNo();
	    String type = req.get("type"); // NORMAL 또는 REMOTE

	    Timestamp now = new Timestamp(System.currentTimeMillis()); // 현재 시간
	    System.out.println(now);

	    int result = attService.updateClockIn(empNo, now, type); // 출근 처리

	    // JSON 형식으로 응답
	    if (result > 0) {
	        return Map.of("success", true); // 성공 시
	    } else {
	        return Map.of("success", false, "message", "출근 처리 실패"); // 실패 시
	    }
	}


	
	@PostMapping("/clockOut")
	@ResponseBody
	public Map<String, Object> clockOut(HttpSession session) {
		Employee loginUser = (Employee) session.getAttribute("loginUser");
		int empNo = loginUser.getEmpNo();
		Timestamp now = new Timestamp(System.currentTimeMillis());

	    int result = attService.updateClockOut(empNo, now);
	    return Map.of("success", result > 0);
	}
	
	// 업무 형태 업데이트 API 추가
	@PostMapping("/updateType")
	@ResponseBody
	public Map<String, Object> updateWorkType(@RequestBody Map<String, String> req, HttpSession session) {
	    Employee loginUser = (Employee) session.getAttribute("loginUser");
	    int empNo = loginUser.getEmpNo();
	    String type = req.get("type"); // NORMAL 또는 REMOTE
	    
	    int result = attService.updateWorkType(empNo, type);
	    return Map.of("success", result > 0);
	}
	
	// 월별 데이터 조회 API 추가
	@GetMapping("/getMonthData")
	@ResponseBody
	public Map<String, Object> getMonthData(
	        @RequestParam("year") int year, 
	        @RequestParam("month") int month,
	        HttpSession session) {
	    
	    Employee loginUser = (Employee) session.getAttribute("loginUser");
	    int empNo = loginUser.getEmpNo();
	    
	    // 해당 월의 주차별 요약 정보 조회
	    List<WeeklySummary> weeklySummaries = attService.getWeeklySummariesForMonth(empNo, year, month);
	    
	    // 해당 월의 주차별 출퇴근 기록 조회
	    Map<Integer, List<Attendance>> weeklyAttendanceMap = attService.getWeeklyAttendanceMapForMonth(empNo, year, month);
	    
	    // 해당 월의 근무 시간 통계 계산
	    Map<String, Duration> monthSummary = attService.calculateMonthSummaryForYearMonth(empNo, year, month);
	    
	    // 반환할 데이터 구성
	    Map<String, Object> response = new HashMap<>();
	    response.put("weeklySummaries", weeklySummaries);
	    response.put("weeklyAttendanceMap", weeklyAttendanceMap);
	    
	    // 월간 누적 시간 및 초과 시간 정보
	    Map<String, String> monthFormatted = formatDuration(monthSummary.get("monthlyTotal"));
	    Map<String, String> monthOverFormatted = formatDuration(monthSummary.get("monthlyOvertime"));
	    
	    response.put("month", monthFormatted);
	    response.put("monthOver", monthOverFormatted);
	    response.put("monthDurationSeconds", monthSummary.get("monthlyTotal").getSeconds());
	    response.put("monthOverDurationSeconds", monthSummary.get("monthlyOvertime").getSeconds());
	    
	    return response;
	}
	
	@RequestMapping("/getClockInTime")
	@ResponseBody
	public Map<String, Object> getClockInTime(HttpSession session) {
	    Employee loginUser = (Employee) session.getAttribute("loginUser");
	    int empNo = loginUser.getEmpNo();
	    
	    Attendance todayAttendance = attService.getTodayAttendance(empNo);
	    
	    Map<String, Object> response = new HashMap<>();
	    if (todayAttendance != null && todayAttendance.getClockInTime() != null) {
	        response.put("success", true);
	        response.put("clockInTime", todayAttendance.getClockInTime());  // 출근 시간 전달
	    } else {
	        response.put("success", false);
	    }
	    
	    return response;
	}

}