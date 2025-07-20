package com.kh.flokrGroupware.chat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.kh.flokrGroupware.chat.model.service.ChatServiceImpl;
import com.kh.flokrGroupware.chat.model.vo.ChatMessage;
import com.kh.flokrGroupware.chat.model.vo.ChatRoom;
import com.kh.flokrGroupware.employee.model.vo.Employee;

import lombok.RequiredArgsConstructor;

@Controller
public class ChatController {
	
	@Autowired
	private ChatServiceImpl cService;
	
	/** 
	 * 내 채팅 목록 페이지 로드 및 목록 조회
	 * @param session 현재 사용자 정보를 얻기 위한 세션 객체
	 * @param mv      데이터와 뷰 정보를 담을 ModelAndView 객체
	 * @return ModelAndView 객체 
	 */
	@RequestMapping("chatList.ch")
	public ModelAndView findMyChatRooms(HttpSession session, ModelAndView mv) {
		System.out.println("findMyChatRooms 메소드 실행됨");
		// 1. 세션에서 로그인한 사용자 정보 가져오기
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        // 2. 로그인 상태 확인 (로그인 안 했으면 로그인 페이지로 리다이렉트 등 처리)
        if (loginUser == null) {
        	
        	System.out.println("로그인 사용자 정보 없음!");
        	
        	// 예시: 로그인 페이지로 리다이렉트 (세션에 alertMsg 추가 가능)
            session.setAttribute("alertMsg", "로그인이 필요한 서비스입니다.");
            mv.setViewName("redirect:/"); // 또는 로그인 페이지 경로
            return mv;
        }
        
        // --- 이 부분에 로그 추가 ---
        System.out.println("--- ChatController에서 loginUser 디버깅 정보 ---");
        System.out.println("loginUser 객체 자체: " + loginUser); // 객체 전체 정보 출력 (롬복 @ToString 사용 시)
        if (loginUser != null) {
            System.out.println("  사번 (EmpNo): " + loginUser.getEmpNo());
            System.out.println("  사원 ID (EmpId): " + loginUser.getEmpId());
            System.out.println("  이름 (EmpName): " + loginUser.getEmpName());
            System.out.println("  관리자 여부 (IsAdmin): " + loginUser.getIsAdmin());
            // Employee 객체에 다른 중요한 필드가 있다면 여기에 추가하여 확인
        }
        System.out.println("---------------------------------------------");
        // 
        
        // 3. 로그인한 사용자의 empNo 가져오기
        int empNo = loginUser.getEmpNo();

        // 4. ChatService 호출하여 해당 사용자의 채팅방 목록 조회
        //    (반환 타입은 List<ChatRoom> 또는 필요시 ArrayList<ChatRoom>)
        ArrayList<ChatRoom> chatRoomList = cService.findMyChatRooms(empNo);
        System.out.println(chatRoomList);
        

        // 5. 조회된 목록을 ModelAndView 에 담기
        mv.addObject("chatRoomList", chatRoomList);
        mv.addObject("loginUser", loginUser); // loginUser 객체 전체를 넘김

        // 6. 보여줄 View(JSP) 경로 설정
        mv.setViewName("chat/chatList"); // 예시 경로: /WEB-INF/views/chat/chatListView.jsp

        // 7. ModelAndView 반환
        return mv;
		
	}
	
	/**
	 * 특정 채팅방의 대화 내역 및 정보를 조회하여 JSON으로 반환
	 * @param roomNo 요청 파라미터로 전달된 채팅방 번호
	 * @param session 
	 * @return 채팅방 정보와 메시지 목록을 담은 Map (JSON으로 변환됨)
	 */
	@ResponseBody
	@RequestMapping(value="chatMessage.ch/{roomNo}", produces="application/json; charset=utf-8")
	public ResponseEntity<Map<String, Object>> getChatHistory(@PathVariable("roomNo") int roomNo, HttpSession session) {
		
		Map<String, Object> response = new HashMap<>();
		
		// (보안 강화) 현재 사용자가 해당 roomNo에 접근 권한이 있는지 확인하는 로직 추가 권장
		
		Employee loginUser = (Employee) session.getAttribute("loginUser");
		
		if(loginUser == null || !cService.isUserInRoom(roomNo, loginUser.getEmpNo())) {
			System.out.println("[ERROR] ChatController: 로그인 사용자 없음");
			response.put("error", "접근 권한이 없습니다.");
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}
		
		int empNo = loginUser.getEmpNo();
	    System.out.println("ChatController: 현재 사용자 empNo = " + empNo); // 추가
	    
	    // (보안 확인 로직)
	    if (!cService.isUserInRoom(roomNo, empNo)) {
	         System.out.println("[ERROR] ChatController: 사용자가 방 멤버 아님 (roomNo: " + roomNo + ", empNo: " + empNo + ")"); // 추가
	         // ...
	         return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
	    }

	    int loginUserEmpNo = loginUser.getEmpNo(); // 로그인한 사용자의 empNo 가져오기
		
		try {
            // 1. 채팅방 정보 조회 (헤더 업데이트용)
            //    findChatRoomById는 방 정보 외에 상대방 정보 등 필요한 값을 같이 반환하도록 수정될 수 있음
			System.out.println("ChatController: chatService.findChatRoomById 호출 전");
			ChatRoom roomInfo = cService.findChatRoomById(roomNo, loginUserEmpNo);
			System.out.println("ChatController: chatService.findChatRoomById 호출 후, roomInfo: " + roomInfo); // 추가 (roomInfo가 null인지 확인)
			
            // 2. 메시지 목록 조회
			System.out.println("ChatController: chatService.getChatHistory 호출 전");
            ArrayList<ChatMessage> messages = cService.getChatHistory(roomNo);
            System.out.println("ChatController: chatService.getChatHistory 호출 후, messages size: " + (messages == null ? "null" : messages.size())); // 추가 (null 또는 size 확인)

            response.put("room", roomInfo);   // 채팅방 정보 추가
            response.put("messages", messages); // 메시지 목록 추가
            System.out.println("ChatController: 응답 생성 완료, response: " + response); // 추가
            return ResponseEntity.ok(response); // 200 OK 와 함께 데이터 반환

        } catch (Exception e) {
            // 예외 처리
        	System.err.println("[ERROR] ChatController: getChatHistory 처리 중 예외 발생!"); 
        	e.printStackTrace();
        	response.put("error", "데이터 조회 중 오류 발생");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
		
	}
	
	// ================================================================
	//          WebSocket 메시지 처리 메소드 추가
	// ================================================================
	
	@MessageMapping("/chat/message")
	public void handleChatMessage(@Payload ChatMessage chatMessage) {
		System.out.println("WebSocket Message received: " + chatMessage);
		
		// TODO: 여기에 필요한 로직 추가 가능
		// 예: 메시지 수신 시간 설정 (서버 시간 기준)
		// chatMessage.setSendDate(new java.util.Date()); // Date 타입으로 변경 필요 시
		// 예: 보낸 사람의 정보가 누락되었다면 세션 등에서 가져와 설정 (보안 강화)
		// if (chatMessage.getSenderEmpNo() == 0) { /* 세션에서 가져오기 등 */ }

		// Service 계층으로 메시지 전달하여 DB 저장 및 브로드캐스팅 처리 요청
		try {
			cService.processAndSendMessage(chatMessage); //
		} catch (Exception e) {
			System.err.println("[ERROR] ChatController: handleChatMessage 처리 중 예외 발생!");
			e.printStackTrace();
			// 클라이언트에게 직접 오류를 알리기는 어려움 (별도 에러 채널 구독 필요)
			// 로깅을 철저히 하는 것이 중요
		}
		
	}
	
    /**
     * 새로운 채팅방 생성 요청 처리
     * (AJAX 요청으로 처리 - RequestParam 방식)
     * @param chatType 채팅 유형 ('oneToOne' 또는 'group')
     * @param roomName 생성할 채팅방 이름 (단체 채팅 시 필수, 1:1 채팅 시 null)
     * @param participants 참여자 직원 번호 목록 (쉼표로 구분된 문자열 형태)
     * @param session 현재 사용자 정보를 얻기 위한 세션 객체
     * @return 생성 결과 (JSON 응답)
     */
    @RequestMapping(value = "createChatRoom.ch", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createChatRoom(
            @RequestParam("chatType") String chatType,
            @RequestParam(value="roomName", required=false) String roomName, // 1:1 채팅 시 필수가 아님
            @RequestParam("participants") String participantsStr, // 쉼표로 구분된 문자열로 받음
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        Employee loginUser = (Employee) session.getAttribute("loginUser");

        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요한 서비스입니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }

        int creatorEmpNo = loginUser.getEmpNo();

        // 쉼표로 구분된 참여자 문자열을 ArrayList<Integer>로 변환
        ArrayList<Integer> participants = new ArrayList<>();
        if (participantsStr != null && !participantsStr.trim().isEmpty()) {
            try {
                String[] empNoArray = participantsStr.split(",");
                for (String empNoStr : empNoArray) {
                    participants.add(Integer.parseInt(empNoStr.trim()));
                }
            } catch (NumberFormatException e) {
                //console.error("[ERROR] ChatController: Invalid participant number format", e);
                response.put("success", false);
                response.put("message", "잘못된 참여자 정보 형식입니다.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 Bad Request
            }
        }

        // 참여자 목록에 자신 추가 (프론트에서 자신을 제외하고 보냈을 경우)
        if (!participants.contains(creatorEmpNo)) {
             participants.add(creatorEmpNo);
        }


        // 참여자 목록이 비어있는지 다시 한번 체크 (변환 후)
        if (participants.isEmpty()) {
             response.put("success", false);
             response.put("message", "대화 상대를 선택해주세요.");
             return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 Bad Request
        }


        try {
            ChatRoom createdRoom = null;
            if ("oneToOne".equals(chatType)) {
                // 1:1 채팅 생성 로직
                if (participants.size() != 2) { // 자신 포함 2명이어야 함
                     response.put("success", false);
                     response.put("message", "1:1 채팅은 한 명의 대화 상대만 선택해야 합니다."); // 자신 제외 1명 선택
                     return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                // 자신을 제외한 상대방 empNo 찾기
                int participantEmpNo = (participants.get(0) == creatorEmpNo) ? participants.get(1) : participants.get(0);

                // TODO: 이미 두 사용자 간의 1:1 채팅방이 존재하는지 확인하는 로직 추가 필요
                 ChatRoom existingRoom = cService.findExistingPrivateChatRoom(creatorEmpNo, participantEmpNo);
                 if (existingRoom != null) {
                     response.put("success", false);
                     response.put("message", "이미 두 사용자 간의 채팅방이 존재합니다.");
                     response.put("room", existingRoom); // 기존 방 정보 반환 (선택 사항)
                     return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 Conflict 또는 200 OK와 메시지
                 }

                createdRoom = cService.createPrivateChatRoom(participants, creatorEmpNo); // 1:1 채팅 생성 서비스 호출

            } else if ("group".equals(chatType)) {
                // 단체 채팅 생성 로직
                if (roomName == null || roomName.trim().isEmpty()) {
                     response.put("success", false);
                     response.put("message", "채팅방 이름을 입력해주세요.");
                     return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 Bad Request
                }
                 // 단체 채팅은 자신 포함 최소 2명
                 if (participants.size() < 2) {
                      response.put("success", false);
                      response.put("message", "단체 채팅방은 두 명 이상의 대화 상대가 필요합니다.");
                      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                 }

                createdRoom = cService.createGroupChatRoom(roomName, participants, creatorEmpNo); // 단체 채팅 생성 서비스 호출

            } else {
                 response.put("success", false);
                 response.put("message", "잘못된 채팅 유형입니다.");
                 return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }


            if (createdRoom != null) {
                response.put("success", true);
                response.put("message", "채팅방이 성공적으로 생성되었습니다.");
                response.put("room", createdRoom); // 생성된 방 정보 반환
                return ResponseEntity.ok(response); // 200 OK
            } else {
                response.put("success", false);
                response.put("message", "채팅방 생성에 실패했습니다.");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            }
        } catch (IllegalArgumentException e) {
             // 유효하지 않은 인자 예외 처리 (예: 1:1 채팅인데 참여자가 2명이 아님 등)
             //console.error("[ERROR] ChatController: Invalid arguments for chat creation", e);
             response.put("success", false);
             response.put("message", e.getMessage()); // Service에서 던진 메시지 사용
             return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 Bad Request
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "채팅방 생성 중 오류가 발생했습니다: " + e.getMessage()); // 오류 메시지 포함
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }
	
    /**
     * 클라이언트로부터 특정 채팅방의 메시지들을 읽음 처리하라는 요청을 받습니다.
     * 요청 URL: /chat/markAsRead
     * HTTP 메소드: POST
     * 요청 파라미터: roomNo, userId
     * @param roomNo 클라이언트에서 전송한 채팅방 번호
     * @param userId 클라이언트에서 전송한 사용자(직원) 번호 (주의: 보안상 서버에서 세션 정보로 가져오는 것이 안전)
     * @return 처리 결과 (성공/실패)를 담은 ResponseEntity
     */
    @PostMapping("/chat/markAsRead")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(@RequestParam("roomNo") int roomNo, HttpSession session) {
        // @RequestParam("roomNo")는 요청 파라미터 중 이름이 "roomNo"인 값을 int roomNo 변수에 바인딩합니다.
        // @RequestParam("userId")는 요청 파라미터 중 이름이 "userId"인 값을 int userId 변수에 바인딩합니다.

        // === 중요: 보안상 강화 필요 ===
        // 클라이언트에서 userId 값을 그대로 받는 것은 보안에 취약합니다.
        // 요청을 보낸 사용자가 누구인지 서버에서 직접 확인해야 합니다.
        // 예를 들어, 세션에서 로그인 사용자 정보를 가져오거나, Spring Security Context 등에서 인증된 사용자 ID를 가져와야 합니다.
        // --- 메소드 내부 ---
        // 세션에서 로그인 사용자 정보를 가져와 empNo를 얻는 로직이 필요합니다.
        Employee loginUser = (Employee) session.getAttribute("loginUser");

        if (loginUser == null) {
            // 로그인 정보가 없으면 처리 거부
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인이 필요한 서비스입니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 또는 400 Bad Request
        }

        int loggedInUserEmpNo = loginUser.getEmpNo(); // 세션에서 안전하게 가져온 사용자 번호

        
        
        try {
            // 서비스 호출: 메시지 읽음 처리
            cService.markMessagesAsRead(roomNo, loggedInUserEmpNo);

            // 갱신된 총 안 읽은 수 조회 (헤더 배지 업데이트용)
            // 이 부분은 이전 논의에서 추가하기로 했던 로직입니다.
            int newTotalUnreadCount = cService.getTotalUnreadChatCountForUser(loggedInUserEmpNo); // ChatService/DAO/Mapper 구현 필요

            // 성공 응답 (JSON 형태로 클라이언트에 반환)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "메시지가 읽음 처리되었습니다.");
            response.put("totalUnreadCount", newTotalUnreadCount); // 갱신된 총 개수 포함

            return ResponseEntity.ok(response); // 200 OK 상태와 함께 response Map을 JSON으로 반환


        } catch (Exception e) {
            // 오류 발생 시 500 응답 (JSON 형태로 클라이언트에 반환)
            e.printStackTrace(); // 서버 로그에 예외 출력
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "메시지 읽음 처리 중 오류 발생: " + e.getMessage());
            // HTTP 상태 코드 500 (Internal Server Error)과 함께 오류 메시지 응답
        	return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	

    /**
     * 특정 사용자의 총 안 읽은 채팅 수를 조회하는 REST API
     * 페이지 로드 시 또는 다른 페이지에서 채팅 아이콘 배지 업데이트에 사용
     * @param session
     * @return JSON 응답 ({ "totalUnreadCount": 5 })
     */
    @RequestMapping(value = "/chat/unreadCount", method = RequestMethod.GET)
    @ResponseBody // JSON/Plain Text 응답을 위해 필요
    public ResponseEntity<Map<String, Object>> getUnreadChatCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Employee loginUser = (Employee) session.getAttribute("loginUser");

        if (loginUser == null) {
            System.out.println("[ERROR] ChatController: 로그인 사용자 정보 없음! 안 읽은 채팅 수 조회 거부.");
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            // response.put("totalUnreadCount", 0); // 로그인 안 했으면 0
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }

        int empNo = loginUser.getEmpNo();
        try {
            // 서비스 메소드 호출하여 총 안 읽은 수 조회
            int totalUnreadCount = cService.getTotalUnreadChatCountForUser(empNo);

            response.put("success", true);
            response.put("totalUnreadCount", totalUnreadCount);
            System.out.println("ChatController: 안 읽은 채팅 수 조회 성공, 사용자: " + empNo + ", 개수: " + totalUnreadCount);
            return ResponseEntity.ok(response); // 200 OK
        } catch (Exception e) {
            System.err.println("[ERROR] ChatController: 안 읽은 채팅 수 조회 중 오류 발생!");
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "안 읽은 채팅 수 조회 중 오류 발생: " + e.getMessage());
            response.put("totalUnreadCount", 0); // 오류 발생 시 0
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }
    
    /**
     * 채팅방 나가기 요청 처리
     * @param roomNo 나갈 채팅방 번호
     * @param session 현재 사용자 정보를 얻기 위한 세션 객체
     * @return 처리 결과 (JSON 응답)
     */
    @PostMapping("/chat/leaveRoom") // AJAX 요청을 받을 경로
    @ResponseBody // JSON 응답을 위해 사용
    public ResponseEntity<Map<String, Object>> leaveChatRoom(@RequestParam("roomNo") int roomNo, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Employee loginUser = (Employee) session.getAttribute("loginUser");

        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요한 서비스입니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int empNo = loginUser.getEmpNo();

        try {
            boolean success = cService.leaveChatRoom(roomNo, empNo); // 서비스 호출
            if (success) {
                response.put("success", true);
                response.put("message", "채팅방에서 나갔습니다.");
                // TODO: 채팅방 나간 후 필요한 추가 알림 (예: 다른 멤버에게 알림)이 있다면 여기서 처리
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "채팅방 나가기에 실패했습니다. 해당 채팅방의 멤버가 아니거나 오류가 발생했습니다.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "채팅방 나가기 처리 중 오류가 발생했습니다: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

}
