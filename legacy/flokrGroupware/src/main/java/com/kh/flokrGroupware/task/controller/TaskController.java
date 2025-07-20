package com.kh.flokrGroupware.task.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.task.model.service.TaskServiceImpl;
import com.kh.flokrGroupware.task.model.vo.Task;
import com.kh.flokrGroupware.task.model.vo.TaskAssignee;

@Controller
@RequestMapping("/task")
public class TaskController {
	
	@Autowired
	private TaskServiceImpl tService;
	// 나중에 impl말고 그냥 service로 바꿔주기!!!!!!!!
	
	@RequestMapping("/list")
	public ModelAndView taskList(ModelAndView mv, HttpSession session) {
	    
		Employee loginUser = (Employee) session.getAttribute("loginUser");
		int empNo = loginUser.getEmpNo();
		
		Map<String, String> statusNameMap = new HashMap<>();
	    statusNameMap.put("REQUEST", "요청");
	    statusNameMap.put("IN_PROGRESS", "진행 중");
	    statusNameMap.put("FEEDBACK", "피드백");
	    statusNameMap.put("HOLD", "보류");
	    statusNameMap.put("DONE", "완료");
	    
	    Map<String, String> statusColorMap = new HashMap<>();
	    statusColorMap.put("REQUEST", "gray");
	    statusColorMap.put("IN_PROGRESS", "blue");
	    statusColorMap.put("FEEDBACK", "pink");
	    statusColorMap.put("HOLD", "yellow");
	    statusColorMap.put("DONE", "green");

		ArrayList<Task> list = tService.taskList(empNo);
		
		for(Task task : list) {
	        List<TaskAssignee> assignees = tService.getTaskAssignees(task.getTaskNo());
	        task.setAssignees(assignees); // Task 클래스에 담당자 목록을 저장할 속성 필요
	    }
		
		mv.addObject("list", list)
		  .addObject("statusNameMap", statusNameMap)
		  .addObject("statusColorMap", statusColorMap)
		  .setViewName("task/taskListView");
		
	    return mv;
	}
	
	@RequestMapping("/detail")
	public String taskDetail(@RequestParam("taskId") int taskId, Model model) {
	    Task task = tService.taskDetail(taskId);
	    Attachment atmt = tService.getAttachment(taskId);
	    if (atmt != null) {
	        model.addAttribute("atmt", atmt);
	    }
	    
	    List<TaskAssignee> assignees = tService.getTaskAssignees(taskId);
	    
	    Map<String, String> statusMap = new HashMap<>();
	    statusMap.put("REQUEST", "요청");
	    statusMap.put("IN_PROGRESS", "진행중");
	    statusMap.put("FEEDBACK", "피드백");
	    statusMap.put("HOLD", "보류");
	    statusMap.put("DONE", "완료");

	    String statusKor = statusMap.get(task.getTaskStatus());
	    
	    model.addAttribute("task", task);
	    model.addAttribute("atmt", atmt);
	    model.addAttribute("statusKor", statusKor);
	    model.addAttribute("assignees", assignees);

	    return "task/taskDetailView";
	}

	
	@RequestMapping("/insertForm")
    public String taskInsertForm() {
		return "task/taskInsertForm";
    }

	@RequestMapping("/insert")
	public String taskInsert(
			@RequestParam("taskTitle") String title,
		    @RequestParam("taskContent") String content,
		    @RequestParam("category") String category,
		    @RequestParam("dueDate") String dueDate,
		    @RequestParam("emoji") String emoji,
		    @RequestParam(value = "uploadFile", required = false) MultipartFile upfile,
		    @RequestParam(value = "assignees", required = false) String assignees,
		    HttpSession session) {

		Employee loginUser = (Employee) session.getAttribute("loginUser");
		int empNo = loginUser.getEmpNo();
		
		Task task = new Task();
	    task.setTaskTitle(title);
	    task.setTaskContent(content);
	    task.setCategory(category);
	    task.setDueDate(dueDate);
	    task.setEmoji(emoji);
	    task.setTaskWriter(String.valueOf(((Employee) session.getAttribute("loginUser")).getEmpNo()));
	    
	    Attachment atmt = null;
		
		// 파일 업로드 처리
		if (upfile != null && !upfile.isEmpty() && upfile.getOriginalFilename() != null && !upfile.getOriginalFilename().trim().equals("")) {
		    atmt = new Attachment();
		    String changeName = saveFile(upfile, session);

		    if (changeName != null) {
		        atmt.setOriginalFilename(upfile.getOriginalFilename());
		        atmt.setStoredFilepath("resources/uploadFiles/" + changeName);
		        atmt.setFileExtension(changeName.substring(changeName.lastIndexOf(".")));
		        atmt.setUploaderEmpNo(empNo);
		    }
		    
		} else {
		    atmt = null;
		}

		// 담당자 목록 처리
		List<Integer> assigneeList = null;
		if (assignees != null && !assignees.isEmpty()) {
		    assigneeList = Arrays.stream(assignees.split(","))
		                      .map(String::trim)
		                      .filter(s -> !s.isEmpty())
		                      .map(Integer::parseInt)
		                      .collect(Collectors.toList());
		    
		}
		
		// 업데이트된 서비스 메소드 호출
		int result = tService.taskInsert(task, atmt, assigneeList);
		
		if(result > 0) {
			session.setAttribute("alertMsg", "업무가 성공적으로 추가되었습니다.");
			return "redirect:/task/list";
		}else {
			session.setAttribute("insertFormFail", true);
			return "redirect:/task/list";
		}
		
	}
	
	public String saveFile(MultipartFile upfile, HttpSession session) {
		
		String originName = upfile.getOriginalFilename();
		
		String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); // "20250417093922"
		int ranNum = (int)(Math.random() * 90000 + 10000); // 74158 (5자리 랜덤값)
		String ext = originName.substring(originName.lastIndexOf(".")); // ".png"
		
		String changeName = currentTime + ranNum + ext;
		
		String savePath = session.getServletContext().getRealPath("/resources/uploadFiles/");
		try {
			upfile.transferTo(new File(savePath + changeName));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		return changeName;
	}
	
	@ResponseBody
	@RequestMapping("/checkFailFlag")
	public boolean checkFailFlag(HttpSession session) {
	    Boolean failFlag = (Boolean) session.getAttribute("insertFormFail");
	    if (failFlag != null && failFlag) {
	        session.removeAttribute("insertFormFail"); // 확인했으면 제거
	        return true;
	    }
	    return false;
	}
	
	@RequestMapping("/updateForm")
    public String taskUpdateForm(@RequestParam("taskId") int taskId, Model model) {
		Task task = tService.taskDetail(taskId);
	    Attachment atmt = tService.getAttachment(taskId);
	    
	    Map<String, String> statusMap = new HashMap<>();
	    statusMap.put("REQUEST", "요청");
	    statusMap.put("IN_PROGRESS", "진행중");
	    statusMap.put("FEEDBACK", "피드백");
	    statusMap.put("HOLD", "보류");
	    statusMap.put("DONE", "완료");

	    String statusKor = statusMap.get(task.getTaskStatus());
	    
	    model.addAttribute("task", task);
	    model.addAttribute("atmt", atmt);
	    model.addAttribute("statusKor", statusKor);
		return "task/taskUpdateForm";
    }
	
	@ResponseBody
	@RequestMapping("/update")
	public Map<String, Object> taskUpdate(Task task, Attachment atmt, MultipartFile reUploadfile, HttpSession session, Model model) {
		
		Map<String, Object> response = new HashMap<>();
		
		Employee loginUser = (Employee) session.getAttribute("loginUser");
		int empNo = loginUser.getEmpNo();
		
		int result = 0;
		
		System.out.println("if문 전");
		
		// 새로 넘어온 첨부파일이 있을 경우
		if(atmt != null && atmt.getOriginalFilename() != null && !atmt.getOriginalFilename().isEmpty()) {
			
			// 기존에 첨부파일이 있었을 경우 => 기존의 첨부파일 지우기
			if(atmt.getOriginalFilename() != null) {
				new File(session.getServletContext().getRealPath(atmt.getStoredFilepath())).delete();
				result = tService.attachmentDelete(atmt);
				System.out.println("기존 첨부파일 삭제");
			}
			
			// 새로 넘어온 첨부파일 서버 업로드 시키기
			String changeName = saveFile(reUploadfile, session);
			
			// atmt에 새로 넘어온 첨부파일에 대한 원본명, 저장경로 담기
			atmt.setOriginalFilename(reUploadfile.getOriginalFilename());
			atmt.setStoredFilepath("resources/uploadFiles/" + changeName);
			atmt.setFileExtension(changeName.substring(changeName.lastIndexOf(".")));
	        atmt.setUploaderEmpNo(empNo);
	        atmt.setRefNo(task.getTaskNo());
	        
	        result = tService.taskAtmtUpdate(task, atmt);
	        
		} else {
			
			result = tService.taskUpdate(task);
			
		}
		
		if(result > 0) { // 수정 성공 => 상세페이지
			session.setAttribute("alertMsg", "업무가 성공적으로 수정되었습니다.");
			response.put("success", true);
		}else { // 수정 실패 => 에러페이지
			session.setAttribute("alertMsg", "업무 수정에 실패하였습니다.");
			response.put("fail", false);
		}
		
		return response;
	}
	
	@GetMapping("/employeeList")
	@ResponseBody
	public List<Employee> getEmployeeList(HttpSession session) {
		Employee loginUser = (Employee) session.getAttribute("loginUser");
		int empNo = loginUser.getEmpNo();
	    return tService.getAllEmployees(empNo); // 사번, 이름, 부서명 포함
	}
	
}