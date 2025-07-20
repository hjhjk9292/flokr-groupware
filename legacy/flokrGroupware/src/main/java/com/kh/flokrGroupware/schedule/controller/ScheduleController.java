package com.kh.flokrGroupware.schedule.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.schedule.model.service.ScheduleService;
import com.kh.flokrGroupware.schedule.model.vo.Schedule;
import com.kh.flokrGroupware.schedule.model.vo.ScheduleAttendee;


@Controller
public class ScheduleController {
	
	@Autowired
    private ScheduleService scheduleService;
    
    /**
     * 일정 캘린더 화면으로 이동 메소드
     * @param model
     * @return
     */
    @RequestMapping("calendar.sc")
    public String scheduleCalendar(Model model) {
        model.addAttribute("currentPage", "schedule");
        return "schedule/scheduleCalendar";
    }
    
    /**
     * 일정 데이터 조회 (AJAX 요청 처리) 메소드
     * @param start
     * @param end
     * @param personal
     * @param dept
     * @param company
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("getSchedules.sc")    
    public ResponseEntity<Object> getSchedules(String start, String end, String personal, String dept, String company, HttpSession session) {
        
    	// 로그인한 사용자 정보 가져오기
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인되지 않은 경우 401 Unauthorized 상태 코드 반환
            return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);
        }
        int empNo = loginUser.getEmpNo();
        int deptNo = loginUser.getDeptNo();
        
        // 서비스를 통해 일정 데이터 조회 및 변환
        ArrayList<Map<String, Object>> events = scheduleService.getScheduleEvents(empNo, deptNo, start, end);
        return new ResponseEntity<Object>(events, HttpStatus.OK);
    }
    
    /**
     * 일정 상세 조회 (모달용) 메소드
     * @param scheduleNo 조회할 일정 번호
     * @param model 뷰로 데이터를 전달하기 위한 Model 객체
     * @return 일정 상세 모달 뷰
     */
    @RequestMapping("detailModal.sc")
    public String scheduleDetail(int scheduleNo, Model model, HttpSession session) {
    	// 일정 정보 조회
    	Schedule schedule = scheduleService.selectSchedule(scheduleNo);
    	
    	// 일정이 없는 경우 처리
        if(schedule == null) {
            model.addAttribute("errorMsg", "존재하지 않는 일정입니다.");
            return "common/errorPage";
        }
    	
    	// 일정 참석자 목록 조회
    	ArrayList<ScheduleAttendee> attendees = scheduleService.selectAttendees(scheduleNo);
    	
    	// 로그인한 사용자 정보 가져오기
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	
    	model.addAttribute("schedule", schedule);
    	model.addAttribute("attendees", attendees);
    	model.addAttribute("loginUser", loginUser);
    	
    	// 모달용 부분 뷰 리턴
    	return "schedule/scheduleDetailModal";
    	
    }
    
    /**
     * 일정 등록 폼으로 이동하는 메소드
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @param session 로그인 정보를 가져오기 위한 HttpSession 객체
     * @return 일정 등록 폼 페이지의 뷰 이름
     */
    @RequestMapping("enrollForm.sc")
    public String scheduleEnrollForm(Model model, HttpSession session) {
    	// 로그인한 사용자 정보 가져오기
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	
    	// 참석자 선택을 위한 직원 목록 조회
    	ArrayList<Employee> eList = scheduleService.selectEmployeeList(loginUser.getEmpNo());
    	
    	// 헤더에서 일정 메뉴를 활성화하기 위해 추가
    	model.addAttribute("currentPage", "schedule");
    	model.addAttribute("eList", eList);
    	//System.out.println(eList);
    	
    	return "schedule/scheduleEnrollForm";    	
    	
    }
    
    /**
     * 일정 등록 메소드
     * @param schedule 등록할 일정 정보
     * @param attendee 참석자 ID 배열 (쉼표로 구분된 문자열)
     * @param session 로그인 정보 확인용 세션
     * @return 일정 목록 페이지로 리다이렉트
     */
    @RequestMapping("insert.sc")
    public String insertSchedule(Schedule schedule, String startDate, String startTime,
            String endDate, String endTime, String attendee, HttpSession session, Model model) {
    	
    	System.out.println("일정 등록 요청 받음");
        System.out.println("제목: " + schedule.getScheduleTitle());
        System.out.println("참석자: " + attendee);
    	
    	// 날짜와 시간 결합
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date startDateTime = dateFormat.parse(startDate + " " + startTime);
            java.util.Date endDateTime = dateFormat.parse(endDate + " " + endTime);
            
            // 종일 일정인 경우 시간 처리 (allDay값이 "Y"면 종일 일정)
            if ("Y".equals(schedule.getAllDay())) {
                // 종일 일정은 시작일은 00:00, 종료일은 23:59로 설정
                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
                startDateTime = dateFormat.parse(dateOnlyFormat.format(startDateTime) + " 00:00");
                endDateTime = dateFormat.parse(dateOnlyFormat.format(endDateTime) + " 23:59");
            }
            
            // java.util.Date => java.sql.Date로 변환
            schedule.setStartDate(new java.sql.Date(startDateTime.getTime()));
            schedule.setEndDate(new java.sql.Date(endDateTime.getTime()));
        } catch (ParseException e) {
            model.addAttribute("errorMsg", "날짜/시간 처리 중 오류가 발생했습니다.");
            return "common/errorPage";
        }
    	
    	// 로그인한 사용자 정보 가져오기
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	if (loginUser == null) {
    	    model.addAttribute("errorMsg", "로그인이 필요한 서비스입니다.");
    	    return "common/errorPage";
    	}
    	
    	int createEmpNo = loginUser.getEmpNo();
    	
    	// 일정 작성자 설정
    	schedule.setCreateEmpNo(createEmpNo);
    	
    	// 참석자 ID 배열 처리
        int[] attendeeArray = null;
        if(attendee != null && !attendee.isEmpty()) {
            try {
                String[] attendeeStrings = attendee.split(",");
                attendeeArray = new int[attendeeStrings.length];
                
                for(int i=0; i<attendeeStrings.length; i++) {
                    attendeeArray[i] = Integer.parseInt(attendeeStrings[i]);
                }
            } catch (Exception e) {
                model.addAttribute("errorMsg", "참석자 정보 처리 중 오류가 발생했습니다.");
                return "common/errorPage";
            }
        }
    	
    	// 서비스 호출하여 일정 등록
    	int result = scheduleService.insertSchedule(schedule, attendeeArray);
    	
    	if(result > 0) { 
    		// 일정 등록 성공
    		session.setAttribute("alertMsg", "일정이 성공적으로 등록되었습니다.");
    		return "redirect:calendar.sc";
    		
    	}else {
    		// 일정 등록 실패
    		model.addAttribute("errorMsg", "일정 등록에 실패했습니다.");
            return "common/errorPage";
    	}
    	
    }
    
    /**
     * 특정 날짜에 해당하는 일정 목록을 조회하는 메소드
     * @param date 조회할 날짜 (형식: YYYY-MM-DD)
     * @param session 로그인한 사용자 정보를 담고 있는 HttpSession 객체
     * @return ArrayList<Schedule> 해당 날짜의 일정 목록
     */
    @ResponseBody
    @RequestMapping("getDaySchedules.sc")
    public ArrayList<Schedule> getDaySchedules(String date, HttpSession session){
    	// 로그인한 사용자의 empNo 가져오기
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	int empNo = loginUser.getEmpNo();
    	int deptNo = loginUser.getDeptNo();
    	
    	// 특정 날짜의 일정목록 조회
    	ArrayList<Schedule> daySchedules = scheduleService.selectDaySchedules(date, empNo, deptNo);
    	
    	return daySchedules;
    }
    
    /**
     * 일정 수정폼으로 이동하는 메소드
     * @param scheduleNo 수정할 일정의 고유 번호
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @param session 로그인한 사용자 정보를 담고 있는 HttpSession 객체
     * @return 수정폼 JSP 페이지
     */
    @RequestMapping("updateForm.sc")
    public String updateForm(int scheduleNo, Model model, HttpSession session) {
    	// 로그인한 사용자 정보 가져오기
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	
    	// 1. 일정 정보 조회 - 입력받은 scheduleNo로 해당 일정의 상세 정보를 조회
    	Schedule schedule = scheduleService.selectSchedule(scheduleNo);
    	
    	// 2. 참석자 정보 조회 - 해당 일정에 참석하는 직원 목록 조회
    	ArrayList<ScheduleAttendee> attendees = scheduleService.selectAttendees(scheduleNo);
   
    	// 3. 전체 직원 목록 조회 - 참석자 선택 모달에서 사용할 전체 직원 목록
    	ArrayList<Employee> eList = scheduleService.selectEmployeeList(loginUser.getEmpNo());
    
    	// 4. 참석자 ID 목록 생성 - 콤마로 구분된 문자열 형태(hidden input에 사용)
    	String attendeeEmpNo = "";
    	
    	// 간단한 문자열 연결 방식으로 참석자 ID 문자열 생성
    	if(attendees != null && !attendees.isEmpty()) {
    		for(int i=0; i<attendees.size(); i++) {
    			attendeeEmpNo += attendees.get(i).getEmpNo();
    			if(i<attendees.size() - 1) {
    				attendeeEmpNo += ",";
    			}
    		}    		
    	}
    	
    	// 5. 참석자 ID 배열 생성 - 모달에서 체크박스 상태 설정에 사용
    	ArrayList<String> atList = new ArrayList<>();
    	if(!attendeeEmpNo.isEmpty()) {
    		String[] atArray = attendeeEmpNo.split(",");
    		for(String at : atArray) {
    			if(!at.trim().isEmpty()) {
    				atList.add(at.trim());
    			}
    		}
    	}
    	
    	// 6. 모델에 데이터 추가 - JSP에서 사용할 수 있도록
    	model.addAttribute("schedule", schedule);			// 수정할 일정 정보
    	model.addAttribute("attendees", attendees);			// 현재 참석자 목록
    	model.addAttribute("eList", eList);					// 전체 직원 목록(참석자 선택용)
    	model.addAttribute("attendeeEmpNo", attendeeEmpNo); // 콤마로 구분된 참석자 사번
    	model.addAttribute("atList", atList);				// 참석자 사번 배열(체크박스 상태 설정용)
    
    	// 지정된 수정폼jsp로 포워딩
    	return "schedule/scheduleUpdateForm";
    
    }
    
    /**
     * 일정 수정 메소드
     * @param schedule 수정할 일정 정보
     * @param startDate 시작날짜
     * @param startTime 시작시간
     * @param endDate 종료날짜
     * @param endTime 종료시간
     * @param attendee 참석자 목록 (쉼표로 구분된 문자열)
     * @param session 세션 정보
     * @param model 모델
     * @return 리다이렉트 경로
     */
    @RequestMapping("update.sc")
    public String updateSchedule(Schedule schedule, String startDate, String startTime, String endDate, String endTime, String attendee, HttpSession session, Model model) {
    	
    	// 날짜와 시간 결합
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date startDateTime = dateFormat.parse(startDate + " " + startTime);
            java.util.Date endDateTime = dateFormat.parse(endDate + " " + endTime);
            
            // 종일 일정인 경우 시간 처리 (allDay값이 "Y"면 종일 일정)
            if ("Y".equals(schedule.getAllDay())) {
                // 종일 일정은 시작일은 00:00, 종료일은 23:59로 설정
                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
                startDateTime = dateFormat.parse(dateOnlyFormat.format(startDateTime) + " 00:00");
                endDateTime = dateFormat.parse(dateOnlyFormat.format(endDateTime) + " 23:59");
            }
            
            // java.util.Date => java.sql.Date로 변환
            schedule.setStartDate(new java.sql.Date(startDateTime.getTime()));
            schedule.setEndDate(new java.sql.Date(endDateTime.getTime()));
        } catch (ParseException e) {
            model.addAttribute("errorMsg", "날짜/시간 처리 중 오류가 발생했습니다.");
            return "common/errorPage";
        }
        
        // 로그인한 사용자 정보 확인
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
        	model.addAttribute("errorPage", "로그인이 필요한 서비스입니다.");
        	return "common/errorPage";
        }
        
        // 참석자 사번 배열 처리
        int[] attendeeArray = null;
        if(attendee != null && !attendee.isEmpty()) {
        	try {
	        	String[] attendeeStrings = attendee.split(",");
	        	attendeeArray = new int[attendeeStrings.length];
	        	
	        	for(int i=0; i<attendeeStrings.length; i++) {
	        		attendeeArray[i] = Integer.parseInt(attendeeStrings[i]);
	        	}
	        } catch(Exception e) {
	        	model.addAttribute("errorMsg", "참석자 정보 처리 중 오류가 발생했습니다.");
	        	return "common/errorPage";
	        }
        }
        
        // 서비스 호출하여 일정 수정
        int result = scheduleService.updateSchedule(schedule, attendeeArray);
        
        if(result > 0) {
        	// 일정 수정 성공
        	session.setAttribute("alertMsg", "일정이 성공적으로 수정되었습니다.");
        	return "redirect:calendar.sc";
        	
        }else {
        	// 일정 수정 실패
        	model.addAttribute("errorMsg", "일정 수정에 실패했습니다.");
        	return "common/errorPage";
        }
    	
    }
    
    /**
     * 일정 삭제 메소드
     * @param scheduleNo 삭제할 일정 번호
     * @param session HTTP 세션 객체
     * @param model 모델 객체
     * @return 리다이렉트 경로
     */
    @RequestMapping("delete.sc")
    public String deleteSchedule(int scheduleNo, HttpSession session, Model model) {
    	// 로그인 사용자 확인
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	if(loginUser == null) {
    		model.addAttribute("errorMsg", "로그인이 필요한 서비스입니다.");
    		return "common/errorPage";
    	}
    	
    	// 일정 삭제 요청
    	int result = scheduleService.deleteSchedule(scheduleNo, loginUser.getEmpNo());
    	
    	if(result > 0) {
    		// 일정 삭제 성공
    		session.setAttribute("alertMsg", "일정이 성공적으로 삭제되었습니다.");
    		return "redirect:calendar.sc"; // 캘린더 페이지로 리다이렉트
    	}else {
    		// 일정 삭제 실패
    		model.addAttribute("errorMsg", "일정 삭제에 실패했습니다.");
    		return "common/errorPage";
    	}

    	
    }
    
    


}
