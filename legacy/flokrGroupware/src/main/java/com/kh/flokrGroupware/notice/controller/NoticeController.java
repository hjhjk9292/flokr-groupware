package com.kh.flokrGroupware.notice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.common.template.Pagination;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.notice.model.service.NoticeService;
import com.kh.flokrGroupware.notice.model.vo.Notice;
import com.kh.flokrGroupware.notification.model.service.NotificationSenderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class NoticeController {

    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class);
    
    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private NotificationSenderService notificationSenderService;
    
    // 공지사항 목록 조회
    @GetMapping("noticeList")
    public String noticeList(@RequestParam(value="page", defaultValue="1") int currentPage,
                            @RequestParam(value="keyword", required=false) String keyword,
                            @RequestParam(value="category", required=false) String category,
                            Model model, HttpSession session) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null) {
            session.setAttribute("alertMsg", "로그인이 필요한 서비스입니다.");
            return "redirect:/";
        }
        
        // 한 페이지에 보여줄 게시글 수
        int limit = 10;
        
        // 총 게시글 수 조회
        int listCount = noticeService.getNoticeCount(keyword, category);
        
        // 페이지 정보 설정
        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 10, limit);
        
        // 공지사항 목록 조회
        List<Notice> noticeList = noticeService.selectNoticeList(pi, keyword, category);
        
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("pi", pi);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        
        // 현재 메뉴 표시 (헤더 메뉴 활성화용)
        model.addAttribute("currentMenu", "notice");
        
        return "notice/noticeList";
    }
    
    // 공지사항 상세 조회
    @GetMapping("noticeDetail/{noticeNo}")
    public String noticeDetail(@PathVariable("noticeNo") int noticeNo, Model model, HttpSession session) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null) {
            session.setAttribute("alertMsg", "로그인이 필요한 서비스입니다.");
            return "redirect:/";
        }
        
        // 조회수 증가
        noticeService.increaseCount(noticeNo);
        
        // 공지사항 상세 조회
        Notice notice = noticeService.selectNotice(noticeNo);
        
        if(notice != null) {
            model.addAttribute("notice", notice);
            
            // 현재 메뉴 표시 (헤더 메뉴 활성화용)
            model.addAttribute("currentMenu", "notice");
            
            return "notice/noticeDetail";
        } else {
            model.addAttribute("errorMsg", "공지사항을 찾을 수 없습니다.");
            return "common/errorPage";
        }
    }
    
    // 공지사항 등록 폼 - 관리자만 접근 가능
    @GetMapping("noticeCreate")
    public String noticeCreateForm(Model model, HttpSession session) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            return "redirect:/";
        }
        
        // 현재 메뉴 표시 (헤더 메뉴 활성화용)
        model.addAttribute("currentMenu", "notice");
        
        return "notice/noticeForm";
    }
    
    // 공지사항 등록 처리 - 관리자만 가능
    @PostMapping("noticeInsert")
    public String insertNotice(Notice notice, 
                             @RequestParam(value="sendNotification", defaultValue="false") boolean sendNotification,
                             HttpSession session, 
                             Model model) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            return "redirect:/";
        }
        
        try {
            // 작성자 정보 설정
            notice.setEmpNo(loginUser.getEmpNo());
            
            // 공지사항 등록 서비스 호출
            int noticeNo = noticeService.insertNotice(notice);
            
            if(noticeNo > 0) {
                // 알림 발송 옵션이 선택된 경우 전체 사용자에게 알림 발송
                if(sendNotification) {
                    // STOMP 기반 알림 발송
                    notificationSenderService.sendNotificationToAll(
                        "NOTICE", 
                        "[공지] " + notice.getNoticeTitle(), 
                        notice.getNoticeContent(), 
                        "notice", 
                        String.valueOf(noticeNo)
                    );
                }
                
                session.setAttribute("alertMsg", "공지사항이 성공적으로 등록되었습니다.");
                return "redirect:/noticeDetail/" + noticeNo;
            } else {
                model.addAttribute("errorMsg", "공지사항 등록에 실패했습니다.");
                return "common/errorPage";
            }
        } catch (Exception e) {
            logger.error("공지사항 등록 중 오류", e);
            model.addAttribute("errorMsg", "공지사항 등록 중 오류 발생: " + e.getMessage());
            return "common/errorPage";
        }
    }
    
    // 공지사항 수정 폼 - 관리자만 접근 가능
    @GetMapping("noticeUpdate/{noticeNo}")
    public String noticeUpdateForm(@PathVariable("noticeNo") int noticeNo, Model model, HttpSession session) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            return "redirect:/";
        }
        
        // 공지사항 상세 조회
        Notice notice = noticeService.selectNotice(noticeNo);
        
        if(notice != null) {
            model.addAttribute("notice", notice);
            
            // 현재 메뉴 표시 (헤더 메뉴 활성화용)
            model.addAttribute("currentMenu", "notice");
            
            return "notice/noticeUpdateForm";
        } else {
            model.addAttribute("errorMsg", "공지사항을 찾을 수 없습니다.");
            return "common/errorPage";
        }
    }
    
    // 공지사항 수정 처리 - 관리자만 가능
    @PostMapping("noticeUpdate")
    public String updateNotice(Notice notice, 
                             @RequestParam(value="sendNotification", defaultValue="false") boolean sendNotification,
                             HttpSession session, 
                             Model model) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            return "redirect:/";
        }
        
        try {
            // 작성자 정보 설정
            notice.setEmpNo(loginUser.getEmpNo());
            
            // 공지사항 수정 서비스 호출
            int result = noticeService.updateNotice(notice);
            
            if(result > 0) {
                // 알림 발송 옵션이 선택된 경우 전체 사용자에게 알림 발송
                if(sendNotification) {
                    // STOMP 기반 알림 발송
                    notificationSenderService.sendNotificationToAll(
                        "NOTICE", 
                        "[공지 수정] " + notice.getNoticeTitle(), 
                        notice.getNoticeContent(), 
                        "notice", 
                        String.valueOf(notice.getNoticeNo())
                    );
                }
                
                session.setAttribute("alertMsg", "공지사항이 성공적으로 수정되었습니다.");
                return "redirect:/noticeDetail/" + notice.getNoticeNo();
            } else {
                model.addAttribute("errorMsg", "공지사항 수정에 실패했습니다.");
                return "common/errorPage";
            }
        } catch (Exception e) {
            logger.error("공지사항 수정 중 오류", e);
            model.addAttribute("errorMsg", "공지사항 수정 중 오류 발생: " + e.getMessage());
            return "common/errorPage";
        }
    }
    
    // 공지사항 삭제 처리 - 관리자만 가능
    @PostMapping("noticeDelete")
    public String deleteNotice(@RequestParam("noticeNo") int noticeNo, HttpSession session, Model model) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            return "redirect:/";
        }
        
        try {
            int result = noticeService.deleteNotice(noticeNo);
            
            if(result > 0) {
                session.setAttribute("alertMsg", "공지사항이 성공적으로 삭제되었습니다.");
                return "redirect:/noticeList";
            } else {
                model.addAttribute("errorMsg", "공지사항 삭제에 실패했습니다.");
                return "common/errorPage";
            }
        } catch (Exception e) {
            logger.error("공지사항 삭제 중 오류", e);
            model.addAttribute("errorMsg", "공지사항 삭제 중 오류 발생: " + e.getMessage());
            return "common/errorPage";
        }
    }
    
    // AJAX 공지사항 삭제
    @PostMapping("noticeDeleteAjax")
    @ResponseBody
    public Map<String, Object> deleteNoticeAjax(@RequestParam("noticeNo") int noticeNo, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            return response;
        }
        
        try {
            int result = noticeService.deleteNotice(noticeNo);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "공지사항이 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "공지사항 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            logger.error("공지사항 삭제 중 오류", e);
            response.put("success", false);
            response.put("message", "공지사항 삭제 중 오류 발생: " + e.getMessage());
        }
        
        return response;
    }
    
    // 메인용 최근 공지사항 조회 (AJAX)
    @GetMapping("recentNotices")
    @ResponseBody
    public List<Notice> getRecentNotices(@RequestParam(value="limit", defaultValue="5") int limit) {
        return noticeService.selectRecentNotices(limit);
    }
}