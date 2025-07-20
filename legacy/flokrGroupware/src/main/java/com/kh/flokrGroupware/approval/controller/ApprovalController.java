package com.kh.flokrGroupware.approval.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage.Body;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kh.flokrGroupware.approval.model.service.ApprovalServiceImpl;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalForm;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;
import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.employee.model.vo.Employee;


/**
 * 전자결재 컨트롤러
 * 모든 결재 관련 요청을 처리
 */
@Controller
public class ApprovalController {
	
	@Autowired
	private ApprovalServiceImpl aService;
	
	// ====================== 메인 대시보드 ====================== 
	   
	   /**
	    * 전자결재 메인 대시보드
	    * @param session HTTP 세션
	    * @param model 뷰로 전달할 데이터
	    * @return 메인 대시보드 페이지
	    */
	   @RequestMapping("main.ap")
	   public String main(HttpSession session, Model model) {
		   try {
		        // 세션 체크
		        Employee loginUser = (Employee)session.getAttribute("loginUser");
		        if (loginUser == null) {
		            return "redirect:/login";
		        }
		        
		        // 통계 데이터 조회 - 기본값 설정으로 안전하게
		        HashMap<String, Object> stats = new HashMap<>();
		        
		        try {
		            // 결재 대기 건수 조회 - 결재자 관점 (내가 결재해야 할 문서)
		            stats.put("waitingCount", aService.selectWaitingCount(loginUser.getEmpNo()));
		        } catch (Exception e) {
		            stats.put("waitingCount", 0);
		            e.printStackTrace();
		        }

		        try {
		            // 진행 중인 결재 건수 조회 - 기안자 관점 (내가 올린 문서 중 진행 중)
		            stats.put("processingCount", aService.selectProcessingCount(loginUser.getEmpNo()));
		        } catch (Exception e) {
		            stats.put("processingCount", 0);
		            e.printStackTrace();
		        }

		        try {
		            // 결재 완료 건수 조회 - 결재자 관점으로 변경 (내가 승인한 문서)
		            // 기존: stats.put("approvedCount", aService.selectApprovedCount(loginUser.getEmpNo()));
		            stats.put("approvedCount", aService.selectApprovedCountByApprover(loginUser.getEmpNo()));
		        } catch (Exception e) {
		            stats.put("approvedCount", 0);
		            e.printStackTrace();
		        }

		        try {
		            // 결재 반려 건수 조회 - 결재자 관점으로 변경 (내가 반려한 문서)
		            // 기존: stats.put("rejectedCount", aService.selectRejectedCount(loginUser.getEmpNo()));
		            stats.put("rejectedCount", aService.selectRejectedCountByApprover(loginUser.getEmpNo()));
		        } catch (Exception e) {
		            stats.put("rejectedCount", 0);
		            e.printStackTrace();
		        }
		        
		        // 각 문서함 조회 - 실패해도 빈 리스트로 안전하게 처리
		        ArrayList<ApprovalDoc> waitingDocuments = new ArrayList<>();
		        ArrayList<ApprovalDoc> myDocuments = new ArrayList<>();
		        ArrayList<ApprovalDoc> draftDocuments = new ArrayList<>(); 
		        ArrayList<ApprovalDoc> completedDocuments = new ArrayList<>();
		        
		        try {
		            waitingDocuments = aService.selectRecentWaitingDocuments(loginUser.getEmpNo(), 5);
		            if (waitingDocuments == null) waitingDocuments = new ArrayList<>();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        try {
		            myDocuments = aService.selectRecentMyDocuments(loginUser.getEmpNo(), 5);
		            if (myDocuments == null) myDocuments = new ArrayList<>();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        try {
		            draftDocuments = aService.selectRecentDraftDocuments(loginUser.getEmpNo(), 5);
		            if (draftDocuments == null) draftDocuments = new ArrayList<>();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        try {
		            completedDocuments = aService.selectRecentCompletedDocuments(loginUser.getEmpNo(), 5);
		            if (completedDocuments == null) completedDocuments = new ArrayList<>();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        // 모델에 데이터 추가
		        model.addAttribute("loginUser", loginUser);
		        model.addAttribute("stats", stats);
		        model.addAttribute("waitingDocuments", waitingDocuments);
		        model.addAttribute("myDocuments", myDocuments);
		        model.addAttribute("draftDocuments", draftDocuments);
		        model.addAttribute("completedDocuments", completedDocuments);
		        
		        return "approval/approvalMain";
		    } catch (Exception e) {
		        e.printStackTrace();
		        model.addAttribute("errorMsg", "전자결재 메인 페이지 로드 중 오류가 발생했습니다.");
		        return "common/errorPage";
		    }
		}
	// ====================== 양식 관련 ====================== 
	   
   /**
    * 결재 양식 목록 조회 (AJAX)
    * @return 양식 목록 JSON
    */
    @RequestMapping(value="formList.ap", produces="application/json; charset=utf-8")
    @ResponseBody
    public String getFormList() {
        ArrayList<ApprovalForm> formList = aService.selectAllActiveForms();
        String jsonResult = new Gson().toJson(formList);
        // System.out.println("JSON 응답: " + jsonResult); // 디버깅용 로그
        return jsonResult;
    }   
	
	   
	
    /**
     * 결재 양식 선택 화면
     * @param model 뷰로 전달할 데이터
     * @return 양식 선택 페이지
     */
    @RequestMapping("selectForm.ap")
    public String selectForm(Model model) {
        ArrayList<ApprovalForm> formList = aService.selectAllActiveForms();
        model.addAttribute("formList", formList);
        return "approval/approvalFormList";
    }
    
    /**
     * 특정 양식 정보 조회 (AJAX)
     * @param formNo 조회할 양식 번호
     * @return 양식 정보 JSON
     */
    @RequestMapping("formDetail.ap")
    @ResponseBody
    public String getFormDetail(@RequestParam("formNo") int formNo) {
        ApprovalForm form = aService.selectFormByNo(formNo);
        return new Gson().toJson(form);
    }
    
    // ====================== 문서 작성/수정 ====================== 
    /**
     * 문서 작성 화면
     * @param formNo 사용할 양식 번호
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 문서 작성 페이지
     */
    @RequestMapping("writeDocument.ap")
    public String writeDocument(@RequestParam("formNo") int formNo, HttpSession session, Model model) {
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        ApprovalForm form = aService.selectFormByNo(formNo);
        
        // 현재 날짜 정보
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("form", form);
        model.addAttribute("today", today);
        
     // 양식 번호에 따라 다른 템플릿 반환
        switch(formNo) {
            case 1: // 휴가신청서
                return "approval/vacationForm";
            case 2: // 지출결의서
                return "approval/expenseForm";
            case 3: // 재택근무신청서
                return "approval/remoteWorkForm";
            case 4: // 출장신청서
                return "approval/businessTripForm";
            default:
                // 기본 양식 작성 페이지로 이동
            	return "approval/approvalDocumentWrite";
        }
    }
	
    /**
     * 문서 작성 처리 (임시저장 또는 결재요청)
     * @param document 문서 정보
     * @param lines 결재선 정보 (JSON 형태)
     * @param action 동작 타입 (save: 임시저장, submit: 결재요청)
     * @param attachFile 첨부파일 (단일)
     * @param session HTTP 세션
     * @return 처리 결과 페이지
     */
    @PostMapping("insertDocument.ap")
    public ModelAndView insertDocument(@ModelAttribute ApprovalDoc document,
                                     @RequestParam("lines") String lines,
                                     @RequestParam("action") String action,
                                     @RequestParam(value = "attachFile", required = false) MultipartFile attachFile,
                                     HttpSession session, HttpServletRequest request) {
        
        ModelAndView mv = new ModelAndView();
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        try {
            // 기안자 정보 설정
            document.setDrafterEmpNo(loginUser.getEmpNo());
            
            // 문서 상태 설정
            if ("save".equals(action)) {
                document.setDocStatus("DRAFT");
            } else {
                document.setDocStatus("REQUESTED"); // 일단 DRAFT로 설정
            }
            
            // 기본 값 설정 (없는 경우에만)
            if (document.getVersion() == 0) {
                document.setVersion(1);
            }
            
            if (document.getStatus() == null) {
                document.setStatus("Y");
            }
            
            if ("submit".equals(action) && document.getRequestedDate() == null) {
                document.setRequestedDate(new Date());
            }
            
            // 문서 저장
            int result = aService.insertDocument(document);
            
            if (result > 0) {
                // 결재선 정보 파싱 및 저장
                ArrayList<ApprovalLine> approvalLines = parseApprovalLines(lines, document.getDocNo());
                if (!approvalLines.isEmpty()) {
                    aService.insertApprovalLines(approvalLines);
                }
                
                // 첨부파일 처리 (단일)
                if (attachFile != null && !attachFile.isEmpty()) {
                	// 파일명 변경 및 서버에 업로드
                    String changeName = saveFile(attachFile, session);
                    
                    // 첨부파일 정보를 Attachment 객체에 설정
                    Attachment fileInfo = new Attachment();
                    fileInfo.setRefType("APPROVAL_DOC");
                    fileInfo.setRefNo(document.getDocNo());
                    fileInfo.setUploaderEmpNo(loginUser.getEmpNo());
                    fileInfo.setOriginalFilename(attachFile.getOriginalFilename());
                    fileInfo.setStoredFilepath(session.getServletContext().getRealPath("/resources/uploadFiles/") + changeName);
                    fileInfo.setFileExtension(changeName.substring(changeName.lastIndexOf(".") + 1).toLowerCase());
                    fileInfo.setStatus("Y");
                    
                    // 첨부파일 DB에 저장
                    aService.insertAttachment(fileInfo);
                }
                
                // 결재 요청 시 첫 번째 결재자 상태 변경만 수행
                if ("submit".equals(action)) {
                    aService.updateFirstApproverStatus(document.getDocNo());
                }
                
                if ("save".equals(action)) {
                    mv.setViewName("redirect:/draftList.ap");
                } else {
                    mv.setViewName("redirect:/requestedList.ap");
                }
                mv.addObject("alertMsg", "문서가 성공적으로 저장되었습니다.");
            } else {
                mv.setViewName("common/errorPage");
                mv.addObject("errorMsg", "문서 저장에 실패했습니다.");
            }
            
        } catch (Exception e) {
            mv.setViewName("common/errorPage");
            mv.addObject("errorMsg", e.getMessage());
        }
        
        // 문서 내용 및 상태를 로그로 출력
        System.out.println("문서 상태: " + document.getDocStatus());
        System.out.println("문서 버전: " + document.getVersion());
        System.out.println("요청일: " + document.getRequestedDate());
        System.out.println("액션 타입: " + action);
        
        return mv;
    }
    
    /**
     * 문서 수정 화면
     * @param docNo 수정할 문서 번호
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 문서 수정 페이지
     */
    @RequestMapping("updateDocument.ap")
    public String updateDocumentForm(@RequestParam("docNo") int docNo, HttpSession session, Model model) {
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if (loginUser == null) {
            model.addAttribute("errorMsg", "로그인이 필요합니다.");
            return "common/errorPage";
        }
        
        ApprovalDoc document = aService.selectDocumentByNo(docNo);
        if (document == null) {
            model.addAttribute("errorMsg", "존재하지 않는 문서입니다.");
            return "common/errorPage";
        }
        
        // 권한 확인: 기안자가 임시저장 또는 반려 상태인 문서만 수정 가능
        if (document.getDrafterEmpNo() != loginUser.getEmpNo() || 
            (!"DRAFT".equals(document.getDocStatus()) && !"REJECTED".equals(document.getDocStatus()))) {
            model.addAttribute("errorMsg", "문서를 수정할 권한이 없습니다.");
            return "common/errorPage";
        }
        
        ArrayList<ApprovalLine> lines = aService.selectApprovalLineByDocNo(docNo);
        Attachment attachment = aService.selectAttachmentByDocNo(docNo);
        
        // null인 경우 빈 리스트로 초기화
        if (lines == null) {
            lines = new ArrayList<>();
        }
        
        // JSON 파싱 처리 추가
        Map<String, Object> documentData = new HashMap<>();
        if (document.getDocContent() != null && !document.getDocContent().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                documentData = mapper.readValue(document.getDocContent(), Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                documentData = new HashMap<>();
            }
        }
        
        model.addAttribute("document", document);
        model.addAttribute("lines", lines);
        model.addAttribute("attachment", attachment);
        model.addAttribute("documentData", documentData);
        
        return "approval/approvalDocumentUpdate";
    }
    
    /**
     * 문서 수정 처리
     * @param document 수정된 문서 정보
     * @param lines 수정된 결재선 정보
     * @param action 동작 타입
     * @param attachFile 새로 업로드할 첨부파일
     * @param deleteFileNo 삭제할 첨부파일 번호
     * @param session HTTP 세션
     * @return 처리 결과 페이지
     */
    @PostMapping("updateDocument.ap")
    public ModelAndView updateDocument(@ModelAttribute ApprovalDoc document,
                                     @RequestParam(value = "lines", required = false) String lines,
                                     @RequestParam("action") String action,
                                     @RequestParam(value = "attachFile", required = false) MultipartFile attachFile,
                                     @RequestParam(value = "deleteFileNo", required = false) Integer deleteFileNo,
                                     HttpSession session) {
        
        ModelAndView mv = new ModelAndView();
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        try {
        	// 문서 상태 설정 - 수정된 부분
            if ("save".equals(action)) {
                document.setDocStatus("DRAFT");
            } else if ("submit".equals(action)) {
                document.setDocStatus("REQUESTED"); // 바로 REQUESTED로 설정
            }
            
            // 기본 값 설정 (없는 경우에만)
            if (document.getVersion() == 0) {
                document.setVersion(1);
            }
            
            if (document.getStatus() == null) {
                document.setStatus("Y");
            }
            
            if ("submit".equals(action) && document.getRequestedDate() == null) {
                document.setRequestedDate(new Date());
            }
        	
        	
            // 기존 첨부파일 삭제 처리
            if (deleteFileNo != null) {
            	// 기존 파일의 물리적 경로 확인 (선택적)
                Attachment oldAttachment = aService.selectAttachmentByNo(deleteFileNo);
                if(oldAttachment != null && oldAttachment.getStoredFilepath() != null) {
                	// 물리적 파일 삭제(선택적)
                	File oldFile = new File(oldAttachment.getStoredFilepath());
                	if(oldFile.exists()) {
                		oldFile.delete();
                	}
                }
                // DB에서 첨부파일 정보 삭제/비활성화
                aService.deleteAttachmentByDocNo(document.getDocNo());
            }
            
            // 새 첨부파일 업로드
            if (attachFile != null && !attachFile.isEmpty()) {
            	// 기존 파일이 있다면 삭제
                aService.deleteAttachmentByDocNo(document.getDocNo());
                
                // 새 파일명 생성 및 업로드
                String changeName = saveFile(attachFile, session);
                
                // 첨부파일 정보를 Attachment 객체에 설정
                Attachment fileInfo = new Attachment();
                fileInfo.setRefType("APPROVAL_DOC");  // 샘플 데이터에서 사용하는 참조 타입
                fileInfo.setRefNo(document.getDocNo());
                fileInfo.setUploaderEmpNo(loginUser.getEmpNo());
                fileInfo.setOriginalFilename(attachFile.getOriginalFilename());
                fileInfo.setStoredFilepath(session.getServletContext().getRealPath("/resources/uploadFiles/") + changeName);
                fileInfo.setFileExtension(changeName.substring(changeName.lastIndexOf(".") + 1).toLowerCase());
                fileInfo.setStatus("Y");
                
                // 첨부파일 DB에 저장
                aService.insertAttachment(fileInfo);
            }
            
            // 문서 업데이트
            int result = aService.updateDocument(document);
            
            if (result > 0) {
            	// 결재선 업데이트 - 임시저장/반려 상태에서만 수정 가능
                if (("DRAFT".equals(document.getDocStatus()) || "REJECTED".equals(document.getDocStatus()))
                    && lines != null && !lines.isEmpty()) {
                    
                    // 기존 결재선 완전 삭제
                    aService.deleteApprovalLinesByDocNo(document.getDocNo());
                    
                    // 새 결재선 등록
                    ArrayList<ApprovalLine> approvalLines = parseApprovalLines(lines, document.getDocNo());
                    aService.insertApprovalLines(approvalLines);
                }
                
             // 결재 요청 시 첫번째 결재자 상태 변경만 수행
                if ("submit".equals(action)) {
                    aService.updateFirstApproverStatus(document.getDocNo());
                    mv.setViewName("redirect:/requestedList.ap");
                } else {
                    mv.setViewName("redirect:/draftList.ap");
                }
                
                mv.addObject("alertMsg", "문서가 성공적으로 수정되었습니다.");
            } else {
                mv.setViewName("common/errorPage");
                mv.addObject("errorMsg", "문서 수정에 실패했습니다.");
            }
            
        } catch (Exception e) {
            mv.setViewName("common/errorPage");
            mv.addObject("errorMsg", "문서 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return mv;
    }
    
    // ====================== 결재 처리 ====================== 
    /**
     * 문서 상세 보기
     * @param docNo 조회할 문서번호
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 문서 상세 페이지
     */
    @RequestMapping("documentDetail.ap")
    public String documentDetail(@RequestParam("docNo") int docNo, HttpSession session, Model model) {
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	
    	// 문서정보 조회
    	ApprovalDoc document = aService.selectDocumentByNo(docNo);
    	ArrayList<ApprovalLine> lines = aService.selectApprovalLineByDocNo(docNo);
    	Attachment attachment = aService.selectAttachmentByDocNo(docNo);
    	
    	// docContent에서 formatNumber 함수 호출 제거
    	if (document != null && document.getDocContent() != null) {
    	    String originalContent = document.getDocContent();
    	    System.out.println("원본 내용: " + originalContent);
    	    
    	    String content = originalContent.replaceAll("\\$\\{[^}]*?:formatNumber[^}]*?\\}", "0");
    	    System.out.println("수정된 내용: " + content);
    	    
    	    document.setDocContent(content);
    	}
    	
    	// 현재 결재 차례인지 확인
    	boolean isCurrentApprover = false;
    	ApprovalLine currentLine = null;
    	
    	for(ApprovalLine line : lines) {
    		if(line.getApproverEmpNo() == loginUser.getEmpNo() &&
    			"WAITING".equals(line.getLineStatus())) {
    			isCurrentApprover = true;
    			currentLine = line;
    			break;
    		}
    	}
    	
    	model.addAttribute("document", document);
    	model.addAttribute("lines", lines);
        model.addAttribute("attachment", attachment);
        model.addAttribute("isCurrentApprover", isCurrentApprover);
        model.addAttribute("currentLine", currentLine);
        model.addAttribute("loginUser", loginUser);
        
        return "approval/approvalDocumentDetail";
    }
    
    /**
     * 문서 승인 처리
     * @param lineNo 결재선 번호
     * @param comment 결재 의견
     * @param session HTTP 세션
     * @return 처리 결과 페이지
     */
    @PostMapping("approve.ap")
    public ModelAndView approveDocument(@RequestParam("lineNo") int lineNo,
    									@RequestParam(value = "comment", required = false) String comment, HttpSession session) {
    	
    	ModelAndView mv = new ModelAndView();
    	
    	try {
    		// 세션에서 로그인 유저 정보 추출
            Employee loginUser = (Employee)session.getAttribute("loginUser");
    		
            ApprovalLine currentLine = aService.selectApprovalLineByNo(lineNo);
            currentLine.setApprovalComment(comment);
            
            // 승인 처리 로직
            aService.approveDocument(currentLine);
            
            // 캐시 명시적 무효화 추가 - 수신문서함 목록과 충돌 방지를 위해 직접 호출
            aService.refreshWaitingDocumentsForUser(loginUser.getEmpNo());
            
            mv.setViewName("redirect:/waitingList.ap?refresh=true");
            mv.addObject("alertMsg", "문서가 성공적으로 승인되었습니다.");
            
        } catch (Exception e) {
            mv.setViewName("common/errorPage");
            mv.addObject("errorMsg", "승인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return mv;
    }
    
    /**
     * GET 방식의 승인 요청 처리 (오류 방지용)
     * @return 수신 문서함으로 리다이렉트
     */
    @GetMapping({"approve.ap", "reject.ap"})
    public String approveDocumentRedirect() {
        return "redirect:/waitingList.ap"; // 수신 문서함으로 리다이렉트
    }
    
    /**
     * 문서 반려 처리
     * @param lineNo 결재선 번호
     * @param comment 반려 사유(필수)
     * @param session HTTP 세션
     * @return 처리 결과 페이지
     */
    @PostMapping("reject.ap")
    public ModelAndView rejectDocument(@RequestParam("lineNo") int lineNo,
                                     @RequestParam("comment") String comment,
                                     HttpSession session) {
    	
    	ModelAndView mv = new ModelAndView();
    	
    	if(comment == null || comment.trim().isEmpty()) {
    		mv.setViewName("common/errorPage");
    		mv.addObject("errorMsg", "반려 사유는 필수 입력 항목입니다.");
    		return mv;
    	}
    	
    	try {
    		// 세션에서 로그인 유저 정보 추출
            Employee loginUser = (Employee)session.getAttribute("loginUser");
    		
            ApprovalLine currentLine = aService.selectApprovalLineByNo(lineNo);
            currentLine.setApprovalComment(comment);
            
            // 반려 처리 로직
            aService.rejectDocument(currentLine);
            
            // 캐시 명시적 무효화 추가
            aService.refreshWaitingDocumentsForUser(loginUser.getEmpNo());
            
            // 리다이렉트 시 refresh 파라미터 추가
            mv.setViewName("redirect:/waitingList.ap?refresh=true");
            mv.addObject("alertMsg", "문서가 반려되었습니다.");
            
        } catch (Exception e) {
            mv.setViewName("common/errorPage");
            mv.addObject("errorMsg", "반려 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    	
    	return mv;
    }
    
    /**
     * 문서 직접 승인 처리 API (수신 문서함에서 바로 승인)
     * @param docNo 문서 번호
     * @param session HTTP 세션
     * @return 처리 결과
     */
    @PostMapping("directApprove.ap")
    @ResponseBody
    public HashMap<String, Object> directApproveDocument(@RequestParam("docNo") int docNo, 
    													 @RequestParam(value = "comment", required = false) String comment,
    													 HttpSession session) {
        HashMap<String, Object> result = new HashMap<>();
        
        try {
            Employee loginUser = (Employee)session.getAttribute("loginUser");
            if (loginUser == null) {
                result.put("success", false);
                result.put("message", "로그인 정보가 없습니다.");
                return result;
            }
            
            // 현재 결재자 확인
            ArrayList<ApprovalLine> lines = aService.selectApprovalLineByDocNo(docNo);
            ApprovalLine currentLine = null;
            
            for (ApprovalLine line : lines) {
                if (line.getApproverEmpNo() == loginUser.getEmpNo() && 
                    "WAITING".equals(line.getLineStatus())) {
                    currentLine = line;
                    break;
                }
            }
            
            if (currentLine == null) {
                result.put("success", false);
                result.put("message", "승인 권한이 없습니다.");
                return result;
            }
            
            // 찾은 결재선에 의견 설정
            currentLine.setApprovalComment(comment);
            
            // 승인 처리
            int processResult = aService.approveDocument(currentLine);
            
            if (processResult > 0) {
            	// 캐시 명시적 무효화 추가
                aService.refreshWaitingDocumentsForUser(loginUser.getEmpNo());
                
                result.put("success", true);
                result.put("message", "문서가 성공적으로 승인되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "승인 처리 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "승인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 문서 직접 반려 처리 API (수신 문서함에서 바로 반려)
     * @param docNo 문서 번호
     * @param comment 반려 사유
     * @param session HTTP 세션
     * @return 처리 결과
     */
    @PostMapping("directReject.ap")
    @ResponseBody
    public HashMap<String, Object> directRejectDocument(@RequestParam("docNo") int docNo, 
                                                       @RequestParam("comment") String comment,
                                                       HttpSession session) {
        HashMap<String, Object> result = new HashMap<>();
        
        try {
            if (comment == null || comment.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "반려 사유는 필수 입력 항목입니다.");
                return result;
            }
            
            Employee loginUser = (Employee)session.getAttribute("loginUser");
            if (loginUser == null) {
                result.put("success", false);
                result.put("message", "로그인 정보가 없습니다.");
                return result;
            }
            
            // 현재 결재자 확인
            ArrayList<ApprovalLine> lines = aService.selectApprovalLineByDocNo(docNo);
            ApprovalLine currentLine = null;
            
            for (ApprovalLine line : lines) {
                if (line.getApproverEmpNo() == loginUser.getEmpNo() && 
                    "WAITING".equals(line.getLineStatus())) {
                    currentLine = line;
                    break;
                }
            }
            
            if (currentLine == null) {
                result.put("success", false);
                result.put("message", "반려 권한이 없습니다.");
                return result;
            }
            
            // 반려 사유 설정
            currentLine.setApprovalComment(comment);
            
            // 반려 처리
            int processResult = aService.rejectDocument(currentLine);
            
            if (processResult > 0) {
            	
            	// 캐시 명시적 무효화 추가
                aService.refreshWaitingDocumentsForUser(loginUser.getEmpNo());
            	
                result.put("success", true);
                result.put("message", "문서가 성공적으로 반려되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "반려 처리 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "반려 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    // ====================== 결재선 설정 ======================
    /**
     * 결재선 설정 모달 창
     * @param model 뷰로 전달할 데이터
     * @return 결재선 설정 모달
     */
    @RequestMapping("approvalLineModal.ap")
    public String approvalLineModal(HttpSession session, Model model) {
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        ArrayList<Employee> employeeList = aService.selectEmployeesForApprovalLine();
        
        // 로그인한 사용자 제외
        if (loginUser != null) {
            employeeList.removeIf(emp -> emp.getEmpNo() == loginUser.getEmpNo());
        }
        
        // 부서 목록을 중복 없이 가져오기
        ArrayList<HashMap<String, Object>> departments = aService.selectAllDepartments();
        
        model.addAttribute("employeeList", employeeList);
        model.addAttribute("departments", departments);
        model.addAttribute("loginUser", loginUser); // 로그인 사용자 정보도 전달
        return "approval/approvalLineListModal";
    }
    
    /**
     * 사원 검색 (AJAX)
     * @param keyword 검색 키워드
     * @return 검색된 사원 목록 JSON
     */
    @RequestMapping("searchEmployees.ap")
    @ResponseBody
    public String searchEmployees(@RequestParam("keyword") String keyword, HttpSession session) {
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
        ArrayList<Employee> employees = aService.searchEmployeesForApprovalLine(keyword);
        
        // 로그인한 사용자 제외
        if (loginUser != null) {
            employees.removeIf(emp -> emp.getEmpNo() == loginUser.getEmpNo());
        }
        
        return new Gson().toJson(employees);
    }
    
    /**
     * 최근 결재선 목록 조회 (AJAX)
     * @param session HTTP 세션
     * @return 최근 결재선 목록 JSON
     */
    @RequestMapping(value="recentApprovalLines.ap", produces="application/json; charset=utf-8")
    @ResponseBody
    public String getRecentApprovalLines(HttpSession session) {
        // 최근 결재선을 저장할 맵
        ArrayList<HashMap<String, Object>> recentLinesList = new ArrayList<>();
        
        // 최근 결재선은 클라이언트 측에서 localStorage를 통해 관리하므로
        // 서버에서는 빈 배열을 반환
        return new Gson().toJson(recentLinesList);
    }
    
    /**
     * 모든 부서 목록 조회 (AJAX)
     * @return 부서 목록 JSON
     */
    @RequestMapping(value="selectAllDepartments.ap", produces="application/json; charset=utf-8")
    @ResponseBody
    public String selectAllDepartments() {
        ArrayList<HashMap<String, Object>> departments = aService.selectAllDepartments();
        return new Gson().toJson(departments);
    }

    /**
     * 부서별 직원 목록 조회 (AJAX)
     * @param deptNo 부서 번호
     * @return 부서별 직원 목록 JSON
     */
    @RequestMapping(value="selectEmployeesByDept.ap", produces="application/json; charset=utf-8")
    @ResponseBody
    public String selectEmployeesByDept(@RequestParam(value = "deptNo", defaultValue = "ALL") String deptNo, HttpSession session) {
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
        ArrayList<Employee> employees;
        
        // 전체 조회 또는 특정 부서 직원 조회
        if ("ALL".equals(deptNo)) {
            employees = aService.selectEmployeesForApprovalLine();
        } else {
            try {
                int deptNoInt = Integer.parseInt(deptNo);
                employees = aService.selectEmployeesByDept(deptNoInt);
            } catch (NumberFormatException e) {
                employees = new ArrayList<>(); // 오류 시 빈 목록 반환
            }
        }
        
        // 로그인한 사용자 제외
        if (loginUser != null) {
            employees.removeIf(emp -> emp.getEmpNo() == loginUser.getEmpNo());
        }
        
        return new Gson().toJson(employees);
    }
    
    /**
     * 문서의 결재선 존재 여부 확인 (AJAX)
     * 임시저장함에서 결재요청 시 결재선 설정 여부를 확인
     * 
     * @param docNo 문서 번호
     * @return 결재선 존재 여부 JSON
     */
    @GetMapping("checkApprovalLine.ap")
    @ResponseBody
    public Map<String, Object> checkApprovalLine(@RequestParam("docNo") int docNo) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ArrayList<ApprovalLine> lines = aService.selectApprovalLineByDocNo(docNo);
            result.put("hasApprovalLine", lines != null && !lines.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("hasApprovalLine", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 임시저장 문서 결재요청 처리
     * 임시저장 상태의 문서를 결재요청 상태로 변경
     * 
     * @param docNo 문서 번호
     * @param session HTTP 세션
     * @return 처리 결과 페이지
     */
    @PostMapping("submitDraft.ap")
    public ModelAndView submitDraft(@RequestParam("docNo") int docNo, HttpSession session) {
        ModelAndView mv = new ModelAndView();
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        try {
            // 문서 정보 조회
            ApprovalDoc document = aService.selectDocumentByNo(docNo);
            
            // 권한 확인
            if (document.getDrafterEmpNo() != loginUser.getEmpNo()) {
                mv.setViewName("common/errorPage");
                mv.addObject("errorMsg", "문서를 결재 요청할 권한이 없습니다.");
                return mv;
            }
            
            // 상태 확인 - 임시저장 또는 반려 상태만 결재요청 가능
            if (!"DRAFT".equals(document.getDocStatus()) && !"REJECTED".equals(document.getDocStatus())) {
                mv.setViewName("common/errorPage");
                mv.addObject("errorMsg", "임시저장 또는 반려 상태의 문서만 결재 요청할 수 있습니다.");
                return mv;
            }
            
            // 결재선 확인
            ArrayList<ApprovalLine> lines = aService.selectApprovalLineByDocNo(docNo);
            if (lines == null || lines.isEmpty()) {
                mv.setViewName("redirect:/updateDocument.ap");
                mv.addObject("docNo", docNo);
                mv.addObject("alertMsg", "결재선이 설정되지 않았습니다. 결재선을 추가해주세요.");
                return mv;
            }
            
            // 결재요청 상태로 변경
            document.setDocStatus("REQUESTED");
            document.setRequestedDate(new Date());
            document.setUpdateDate(new Date());
            
            int result = aService.updateDocument(document);
            
            if (result > 0) {
                // 첫 번째 결재자의 상태를 WAITING으로 변경
                aService.updateFirstApproverStatus(docNo);
                
                mv.setViewName("redirect:/requestedList.ap");
                mv.addObject("alertMsg", "결재 요청이 완료되었습니다.");
            } else {
                mv.setViewName("common/errorPage");
                mv.addObject("errorMsg", "결재 요청 처리 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("common/errorPage");
            mv.addObject("errorMsg", "결재 요청 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return mv;
    }
    
    // ====================== 문서함 관련 ====================== 
    
    /**
     * 임시저장함 목록 조회 (페이징)
     * @param currentPage 현재 페이지
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 임시저장함 페이지
     */
    @RequestMapping("draftList.ap")
    public String draftList(@RequestParam(value = "page", defaultValue = "1") int currentPage,
    						HttpSession session, Model model) {
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
    	if (loginUser == null) {
            // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
    	
    	try {
            HashMap<String, Object> result = aService.selectDraftDocumentsPaging(loginUser.getEmpNo(), currentPage);
            
            // 결과가 null인 경우 처리
            if (result == null) {
                model.addAttribute("pageInfo", null);
                model.addAttribute("documentList", new ArrayList<>());
            } else {
                model.addAttribute("pageInfo", result.get("pageInfo"));
                model.addAttribute("documentList", result.get("list"));
            }
            
            model.addAttribute("boxType", "draft");
            
            return "approval/approvalDraftList";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMsg", "임시저장함 조회 중 오류가 발생했습니다.");
            return "common/errorPage";
        }
    	
    }
    
    /**
     * 상신 문서함 목록 조회 (페이징)
     * @param currentPage 현재 페이지
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 상신 문서함 페이지
     */
    @RequestMapping("requestedList.ap")
    public String requestedList(@RequestParam(value = "page", defaultValue = "1") int currentPage,
    							@RequestParam(value = "statusFilter", required = false) String statusFilter,
    							@RequestParam(value = "fromDashboard", required = false) String fromDashboard,
    							HttpSession session, Model model) {
        
    	// 대시보드에서 왔고 필터가 없으면 자동으로 진행중 필터 설정
        if("pending".equals(fromDashboard) && (statusFilter == null || statusFilter.isEmpty())) {
            statusFilter = "REQUESTED"; // 또는 진행중에 해당하는 상태값
        }
    	
    	Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        HashMap<String, Object> result = aService.searchDocuments("requested", loginUser.getEmpNo(), 
                currentPage, null, null, null, null, statusFilter);
        
        model.addAttribute("pageInfo", result.get("pageInfo"));
        model.addAttribute("documentList", result.get("list"));
        model.addAttribute("boxType", "requested");
        model.addAttribute("statusFilter", statusFilter);
        
        return "approval/approvalRequestedList";
    }
    
    /**
     * 수신 문서함 목록 조회 (페이징)
     * @param currentPage 현재 페이지
     * @param refresh 캐시 강제 갱신 여부
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 수신 문서함 페이지
     */
    @RequestMapping("waitingList.ap")
    public String waitingList(@RequestParam(value = "page", defaultValue = "1") int currentPage,
                             @RequestParam(value = "refresh", required = false) String refresh,
                             HttpSession session, Model model) {
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        // "refresh" 파라미터가 있거나 session에 "waitingListLoaded"가 없으면 캐시 무효화
        if ("true".equals(refresh) || session.getAttribute("waitingListLoaded") == null) {
            // 캐시 무효화
            aService.refreshWaitingDocumentsForUser(loginUser.getEmpNo());
            
            // 첫 로드 표시 설정
            session.setAttribute("waitingListLoaded", true);
        }
        
        // searchDocuments 메소드 사용 (부서 정보가 포함된 결과를 얻기 위해)
        HashMap<String, Object> result = aService.searchDocuments("waiting", loginUser.getEmpNo(), 
                currentPage, null, null, null, null, null);
        
        // 문서 목록 가져오기
        ArrayList<ApprovalDoc> docList = (ArrayList<ApprovalDoc>)result.get("list");
        
        // 긴급 문서 개수 계산
        int urgentCount = 0;
        if(docList != null) {
            Date now = new Date();
            for(ApprovalDoc doc : docList) {
                if(doc.getRequestedDate() != null) {
                    long diffInMillies = now.getTime() - doc.getRequestedDate().getTime();
                    long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
                    if(diffInDays > 3) {
                        urgentCount++;
                    }
                }
            }
        }
        
        model.addAttribute("urgentCount", urgentCount);
        model.addAttribute("pageInfo", result.get("pageInfo"));
        model.addAttribute("documentList", docList);
        model.addAttribute("boxType", "waiting");
        
        return "approval/approvalWaitingList";
    }
    
    /**
     * 완료문서함 목록 조회 (페이징)
     * @param currentPage 현재 페이지
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 완료 문서함 페이지
     */
    @RequestMapping("completedList.ap")
    public String completedList(@RequestParam(value = "page", defaultValue = "1") int currentPage,
                               @RequestParam(value = "statusFilter", required = false) String statusFilter,
                               @RequestParam(value = "periodFilter", required = false) String periodFilter,
                               @RequestParam(value = "dateFrom", required = false) String dateFrom,
                               @RequestParam(value = "dateTo", required = false) String dateTo,
                               HttpSession session, Model model) {
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        // 기간 필터가 선택된 경우 날짜 계산
        if (periodFilter != null && !periodFilter.isEmpty() && (dateFrom == null || dateTo == null)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            Date fromDate = new Date();
            
            switch(periodFilter) {
                case "today":
                    // 오늘 날짜의 00시 00분 00초로 설정
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    fromDate = cal.getTime();
                    break;
                case "week":
                    // 일주일 전
                    Calendar calWeek = Calendar.getInstance();
                    calWeek.setTime(today);
                    calWeek.add(Calendar.DATE, -7);
                    fromDate = calWeek.getTime();
                    break;
                case "month":
                    // 한달 전
                    Calendar calMonth = Calendar.getInstance();
                    calMonth.setTime(today);
                    calMonth.add(Calendar.MONTH, -1);
                    fromDate = calMonth.getTime();
                    break;
                case "3month":
                    // 3개월 전
                    Calendar cal3Month = Calendar.getInstance();
                    cal3Month.setTime(today);
                    cal3Month.add(Calendar.MONTH, -3);
                    fromDate = cal3Month.getTime();
                    break;
            }
            
            dateFrom = sdf.format(fromDate);
            dateTo = sdf.format(today);
        }
        
        // 항상 searchDocuments 메소드 사용하여 데이터 로드 (부서 정보 포함)
        HashMap<String, Object> result = aService.searchDocuments("completed", loginUser.getEmpNo(), 
                currentPage, null, null, dateFrom, dateTo, statusFilter);
        
        // 기존 통계 데이터 계산
        ArrayList<ApprovalDoc> docList = (ArrayList<ApprovalDoc>)result.get("list");
        int totalCount = ((PageInfo)result.get("pageInfo")).getListCount();
        int approvedCount = 0;
        int rejectedCount = 0;
        
        if(docList != null) {
            for(ApprovalDoc doc : docList) {
                if("APPROVED".equals(doc.getDocStatus())) {
                    approvedCount++;
                } else if("REJECTED".equals(doc.getDocStatus())) {
                    rejectedCount++;
                }
            }
        }
        
        // 통계 데이터 세팅
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("approvedCount", approvedCount);
        stats.put("rejectedCount", rejectedCount);
        
        // 처리 효율성 지표 추가
        HashMap<String, Object> efficiency = aService.getProcessingEfficiency(
                loginUser.getEmpNo(), statusFilter, dateFrom, dateTo);
        stats.put("processingEfficiency", efficiency);
        
        result.put("stats", stats);
        
        // 모델에 데이터 추가
        model.addAttribute("pageInfo", result.get("pageInfo"));
        model.addAttribute("documentList", result.get("list"));
        model.addAttribute("stats", result.get("stats"));
        model.addAttribute("boxType", "completed");
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("periodFilter", periodFilter);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        
        return "approval/approvalCompletedList";
    }
    
    // ====================== 첨부파일 관련 ====================== 
    
    
    /**
     * 첨부파일 다운로드
     * @param attachmentNo 다운로드할 파일 번호
     * @return 파일 데이터
     */
    @RequestMapping("downloadAttachment.ap")
    public ResponseEntity<Resource> downloadAttachment(@RequestParam("attachmentNo") int attachmentNo){
    	try {
    		Attachment attachment = aService.selectAttachmentByNo(attachmentNo);
    		
    		if(attachment == null || "N".equals(attachment.getStatus())) {
    			return ResponseEntity.notFound().build();
    		}
    		
    		Path filePath = Paths.get(attachment.getStoredFilepath());
    		Resource resource = new UrlResource(filePath.toUri());
    		
    		if(!resource.exists()) {
    			return ResponseEntity.notFound().build();
    		}
    		
    		// 파일명 인코딩 (한글 파일명 처리)
    		String encodedFilename = URLEncoder.encode(attachment.getOriginalFilename(), "UTF-8")
    				.replaceAll("\\+", "%20");
    		
    		return ResponseEntity.ok()
    				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
    				.body(resource);			
                    
        } catch (Exception e) {
        	
        	return ResponseEntity.notFound().build();
            
        }
    }
    
    /**
     * 문서 삭제 처리
     * @param docNo 삭제할 문서 번호
     * @param session HTTP 세션
     * @return 처리 결과 페이지
     */
    @PostMapping("deleteDocument.ap")
    public ModelAndView deleteDocument(@RequestParam("docNo") int docNo, HttpSession session) {
    	ModelAndView mv = new ModelAndView();
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        
        try {
            ApprovalDoc document = aService.selectDocumentByNo(docNo);
            
            // 권한 확인 : 기안자만 임시저장/반려 상태의 문서를 삭제할수 있음
            if(document.getDrafterEmpNo() != loginUser.getEmpNo()) {
            	mv.setViewName("common/errorPage");
            	mv.addObject("errorMsg", "문서를 삭제할 권한이 없습니다.");
            	return mv;
            }
            
            // 상태 확인 : 임시저장 또는 반려 상태만 삭제 가능
            if(!"DRAFT".equals(document.getDocStatus()) && !"REJECTED".equals(document.getDocStatus())) {
            	mv.setViewName("common/errorPage");
                mv.addObject("errorMsg", "임시저장 또는 반려 상태의 문서만 삭제할 수 있습니다.");
                return mv;
            }
            
            // 문서 삭제 (논리적 삭제 STATUS 필드를 'N'으로 변경)
            document.setStatus("N");
            int result = aService.updateDocument(document);
            
            if(result > 0) {
            	mv.setViewName("redirect:/draftList.ap");
            	mv.addObject("alertMsg", "문서가 성공적으로 삭제되었습니다.");
            }
            
        } catch (Exception e) {
            mv.setViewName("common/errorPage");
            mv.addObject("errorMsg", "문서 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return mv;
    }
    
    
    // ====================== 유틸리티 메서드 ====================== 
    /**
     * JSON 문자열을 ApprovalLine 목록으로 변환
     * @param lines JSON 형태의 결재선 정보
     * @param docNo 문서 번호
     * @return 결재선 목록
     */
    private ArrayList<ApprovalLine> parseApprovalLines(String lines, int docNo) {
        ArrayList<ApprovalLine> approvalLines = new ArrayList<>();
        
        if (lines != null && !lines.isEmpty()) {
            // Gson으로 JSON 파싱
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType();
            ArrayList<HashMap<String, Object>> lineArray = gson.fromJson(lines, listType);
            
            for (int i = 0; i < lineArray.size(); i++) {
                HashMap<String, Object> lineData = lineArray.get(i);
                ApprovalLine line = new ApprovalLine();
                line.setDocNo(docNo);
                line.setApproverEmpNo(((Double)lineData.get("empNo")).intValue());
                line.setApprovalOrder(i + 1);
                line.setLineStatus("PENDING"); // 초기 상태는 PENDING
                
                approvalLines.add(line);
            }
        }
        
        return approvalLines;
    }

    /**
     * 현재 넘어온 첨부파일을 서버 폴더에 저장하는 메서드
     * @param uploadFile 업로드할 파일
     * @param session HTTP 세션
     * @return 변경된 파일명
     */
    public String saveFile(MultipartFile uploadFile, HttpSession session) {
    	String originName = uploadFile.getOriginalFilename(); // 원본 파일명
    	
    	// 파일명 수정 (시간+랜덤숫자 형식)
    	String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	int ranNum = (int)(Math.random() * 90000 + 10000); // 5자리 랜덤값
    	String ext = originName.substring(originName.lastIndexOf(".")); // 확장자
    	
    	String changeName = currentTime + ranNum + ext;
    	
    	// 업로드 폴더의 물리적 경로 알아내기
    	String savePath = session.getServletContext().getRealPath("/resources/uploadFiles/");
    	
    	// 폴더가 없으면 생성
    	File directory = new File(savePath);
    	if(!directory.exists()) {
    		directory.mkdirs();
    	}
    	
    	try {
            uploadFile.transferTo(new File(savePath + changeName));
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    	
    	return changeName;
    }
    
    /**
     * 공통 문서함 검색 처리
     * @param boxType 문서함 타입 (draft, requested, waiting, completed)
     * @param currentPage 현재 페이지
     * @param searchType 검색 타입 (title, form, drafter 등)
     * @param keyword 검색어
     * @param dateFrom 시작일
     * @param dateTo 종료일
     * @param statusFilter 상태 필터
     * @param session HTTP 세션
     * @param model 뷰로 전달할 데이터
     * @return 검색 결과 페이지
     */
    @RequestMapping("searchDocuments.ap")
    public String searchDocuments(@RequestParam("boxType") String boxType,
                                  @RequestParam(value = "page", defaultValue = "1") int currentPage,
                                  @RequestParam(value = "searchType", required = false) String searchType,
                                  @RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                  @RequestParam(value = "dateTo", required = false) String dateTo,
                                  @RequestParam(value = "statusFilter", required = false) String statusFilter,
                                  HttpSession session, Model model) {
        
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        
        try {
            // 검색 처리
            HashMap<String, Object> result = aService.searchDocuments(boxType, loginUser.getEmpNo(), 
                    currentPage, searchType, keyword, dateFrom, dateTo, statusFilter);
            
            model.addAttribute("pageInfo", result.get("pageInfo"));
            model.addAttribute("documentList", result.get("list"));
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("dateFrom", dateFrom);
            model.addAttribute("dateTo", dateTo);
            model.addAttribute("statusFilter", statusFilter);
            model.addAttribute("boxType", boxType);
            
            // 완료 문서함에 통계 추가
            if ("completed".equals(boxType)) {
                // 기존 통계 정보 가져오기
                HashMap<String, Object> stats;
                if (result.containsKey("stats")) {
                    stats = (HashMap<String, Object>) result.get("stats");
                } else {
                    stats = new HashMap<>();
                }
                
                // ❤️ 처리 효율성 지표 항상 계산하여 추가 (수정된 부분)
                HashMap<String, Object> efficiency = aService.getProcessingEfficiency(
                        loginUser.getEmpNo(), statusFilter, dateFrom, dateTo);
                stats.put("processingEfficiency", efficiency);
                
                model.addAttribute("stats", stats);
            }
            
            // 수신 문서함에 긴급문서 개수 계산 추가
            if ("waiting".equals(boxType)) {
                int urgentCount = 0;
                ArrayList<ApprovalDoc> docList = (ArrayList<ApprovalDoc>)result.get("list");
                if (docList != null) {
                    Date now = new Date();
                    for (ApprovalDoc doc : docList) {
                        if (doc.getRequestedDate() != null) {
                            long diffInMillies = now.getTime() - doc.getRequestedDate().getTime();
                            long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
                            if (diffInDays > 3) {
                                urgentCount++;
                            }
                        }
                    }
                }
                model.addAttribute("urgentCount", urgentCount);
            }
            
            // 각 문서함별 페이지 반환
            switch(boxType) {
                case "draft":
                    return "approval/approvalDraftList";
                case "requested":
                    return "approval/approvalRequestedList";
                case "waiting":
                    return "approval/approvalWaitingList";
                case "completed":
                    return "approval/approvalCompletedList";
                default:
                    return "approval/approvalMain";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMsg", "문서 검색 중 오류가 발생했습니다: " + e.getMessage());
            return "common/errorPage";
        }
    }
}
