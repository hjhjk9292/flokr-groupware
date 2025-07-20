package com.kh.flokrGroupware.chat.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.flokrGroupware.chat.model.dao.ChatDao;
import com.kh.flokrGroupware.chat.model.vo.ChatMessage;
import com.kh.flokrGroupware.chat.model.vo.ChatRoom;
import com.kh.flokrGroupware.chat.model.vo.ChatRoomMember;
import com.kh.flokrGroupware.employee.model.dao.EmployeeDao;
import com.kh.flokrGroupware.employee.model.vo.Employee;

@Service
public class ChatServiceImpl implements ChatService{
	
	@Autowired
	private ChatDao cDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용 객체 주입

    @Autowired
    private EmployeeDao empDao; // EmployeeDao 주입
	
	@Override
	@Transactional
	public ChatRoom createChatRoom(String roomName, int creatorEmpNo) {
        // 1. ChatRoom 객체 생성 및 필요한 정보 설정
        ChatRoom newRoom = new ChatRoom();
        newRoom.setRoomName(roomName);
        newRoom.setRoomType("G"); // 예: 그룹 채팅방으로 설정
        newRoom.setCreatedByEmpNo(creatorEmpNo);
        // createDate와 status는 DB에서 자동 설정 또는 기본값 사용

        // 2. DAO를 통해 CHAT_ROOM 테이블에 삽입
        int result = cDao.insertChatRoom(sqlSession, newRoom);

        if (result > 0) {
            // 3. (선택 사항) 채팅방 생성자(creatorEmpNo)를 CHAT_ROOM_MEMBER 테이블에 추가
            ChatRoomMember creatorMember = new ChatRoomMember();
            creatorMember.setRoomNo(newRoom.getRoomNo()); // selectKey로 받아온 roomNo 사용
            creatorMember.setEmpNo(creatorEmpNo);
            creatorMember.setIsAdmin("Y"); // 방장으로 설정
            // joinDate, lastReadMessageNo, notificationEnabled, status는 기본값 사용

            int memberResult = cDao.insertChatRoomMember(sqlSession, creatorMember);

            if (memberResult > 0) {
                // 4. 성공 시 생성된 ChatRoom 객체 반환
                return newRoom;
            } else {
                // 멤버 추가 실패 시 롤백 처리 (Transactional 어노테이션으로 자동 처리)
                throw new RuntimeException("채팅방 멤버 추가에 실패했습니다.");
            }
        } else {
            // 채팅방 생성 실패
            return null;
        }
	}

	@Override
	public ArrayList<ChatRoom> findMyChatRooms(int empNo) {
        // --- DAO로 넘길 파라미터 맵 생성 ---
        Map<String, Object> params = new HashMap<>();
        params.put("empNo", empNo); // "empNo"라는 키로 empNo 값을 Map에 담습니다.

        // --- ChatDAO의 findMyChatRooms 메소드 호출 ---
        // SqlSessionTemplate와 파라미터 맵을 DAO 메소드에 전달합니다.
        ArrayList<ChatRoom> chatRoomList = cDao.findMyChatRooms(sqlSession, params); // <-- 여기 수정

        // 반환 타입에 맞게 형변환하여 반환
        return chatRoomList;
	}

	@Override
	public ChatRoom findChatRoomById(int roomNo, int loginUserEmpNo) { // loginUserEmpNo 인자 추가
	    Map<String, Object> params = new HashMap<>();
	    params.put("roomNo", roomNo);
	    params.put("loginUserEmpNo", loginUserEmpNo);
	    return cDao.findRoomById(sqlSession, params); // 수정된 DAO 메소드 호출
	}

	@Override
	public int updateChatRoomName(int roomNo, String newRoomName, int empNo) {
		return 0;
	}

	@Override
	public int deleteChatRoom(int roomNo, int empNo) {
		return 0;
	}

	@Override
	@Transactional
	public void processAndSendMessage(ChatMessage message) {
		
        System.out.println("Service: Processing message for room " + message.getRoomNo());

        // 1. DB에 메시지 저장
        int result = cDao.insertChatMessage(sqlSession, message); // DAO 호출

        if (result > 0) {
            System.out.println("Service: Message inserted to DB, result: " + result + ", messageNo: " + message.getMessageNo());

            // 2. 해당 채팅방 구독자들에게 메시지 브로드캐스팅 (방으로 메시지 내용 자체를 보냄)
            String roomDestination = "/topic/chat/room/" + message.getRoomNo();
            System.out.println("Service: Broadcasting message content to: " + roomDestination);
            messagingTemplate.convertAndSend(roomDestination, message); // message 객체를 JSON으로 변환하여 전송

            // ★ 3. 해당 채팅방 멤버들에게 '총 안 읽은 채팅 수' 업데이트 알림 ★
            // 메시지 수신자(본인 제외 모든 멤버)에게 새로운 총 안 읽은 수를 알려야 합니다.

            // 3-1. 해당 채팅방 멤버들의 empNo 목록 조회 (자신 제외)
            // TODO: ChatDao에 특정 방 멤버 empNo 목록 조회 쿼리 및 메소드 추가 필요 (자신 제외)
            // 임시로 모든 멤버 조회 쿼리를 사용하거나, 메시지 VO에 수신자 목록을 포함시키는 방식 고려
            // 가장 정확한 방법은 RoomMember 테이블에서 해당 방 멤버를 조회하는 것입니다.
             ArrayList<Integer> memberEmpNos = cDao.getChatRoomMemberEmpNos(sqlSession, message.getRoomNo());

            if (memberEmpNos != null) {
                System.out.println("Service: 방 멤버 empNos 조회됨: " + memberEmpNos);
                for (Integer memberEmpNo : memberEmpNos) {
                    // 메시지를 보낸 본인에게는 안 읽은 수 알림을 보내지 않음
                    if (memberEmpNo != message.getSenderEmpNo()) {
                        try {
                            // 3-2. 각 멤버의 갱신된 '총 안 읽은 채팅 수' 계산
                            int newTotalUnreadCount = getTotalUnreadChatCountForUser(memberEmpNo); // 위에서 추가한 서비스 메소드 호출

                            // 3-3. 멤버의 empId 조회 (WebSocket user destination 사용 위함)
                            // TODO: EmployeeDao에 empNo로 empId 조회하는 쿼리/메소드 추가 필요
                            Employee member = empDao.selectEmployee(memberEmpNo); // 기존 selectEmployee 활용 가능
                            String memberEmpId = (member != null) ? member.getEmpId() : null;

                            if (memberEmpId != null) {
                                // 3-4. 해당 멤버의 개인 큐로 '총 안 읽은 채팅 수' 메시지 전송
                                String userDestination = "/user/" + memberEmpId + "/queue/chat.unread.total";
                                System.out.println("Service: '총 안 읽은 수' 알림 전송: 사용자 " + memberEmpId + ", 개수: " + newTotalUnreadCount + ", 토픽: " + userDestination);
                                messagingTemplate.convertAndSendToUser(
                                        memberEmpId,
                                        "/queue/chat.unread.total", // User destination은 /user/{empId}를 제외한 나머지 경로
                                        String.valueOf(newTotalUnreadCount) // 숫자를 문자열로 변환하여 전송
                                );
                            } else {
                                System.err.println("Service: 멤버 empId를 찾을 수 없어 총 안 읽은 수 알림 전송 실패: empNo=" + memberEmpNo);
                            }

                        } catch (Exception e) {
                            System.err.println("Service: 멤버 " + memberEmpNo + " 에게 총 안 읽은 수 알림 전송 중 오류 발생");
                            e.printStackTrace();
                            // 특정 멤버 전송 실패가 전체 트랜잭션에 영향을 주지 않도록 여기서 예외를 잡습니다.
                        }
                    }
                }
            } else {
                System.err.println("Service: 메시지를 보낸 방의 멤버 목록을 가져오지 못했습니다. 안 읽은 수 알림 전송 불가.");
            }


        } else {
            System.err.println("Service: Failed to insert message into DB.");
            // DB 저장 실패 시 브로드캐스팅하지 않음
        }
		
		
	}

	@Override
	public ArrayList<ChatMessage> getChatHistory(int roomNo) {
		return cDao.findMessagesByRoomId(sqlSession, roomNo);
	}

	@Override
	public int inviteUserToRoom(int roomNo, int inviterEmpNo, int inviteeEmpNo) {
		return 0;
	}

	@Override
    @Transactional // 여러 DB 작업을 하므로 트랜잭션 처리
    public boolean leaveChatRoom(int roomNo, int empNo) {
        // 1. 사용자가 해당 채팅방 멤버인지 확인 (선택사항, DAO에서 처리 가능)
        //    이미 isUserInRoom 메소드가 있다면 활용 가능
        if (!isUserInRoom(roomNo, empNo)) {
            System.out.println("Service: 사용자가 방 멤버가 아님 (roomNo: " + roomNo + ", empNo: " + empNo + ")");
            return false; // 멤버가 아니면 실패 처리
        }

        // 2. 현재 채팅방의 활성 멤버 수 확인
        int activeMemberCount = cDao.getActiveChatRoomMemberCount(sqlSession, roomNo);
        System.out.println("Service: 채팅방 (roomNo: " + roomNo + ") 현재 활성 멤버 수: " + activeMemberCount);


        // 3. CHAT_ROOM_MEMBER 테이블에서 해당 사용자의 STATUS를 'N'으로 변경
        int memberUpdateResult = cDao.updateChatRoomMemberStatus(sqlSession, roomNo, empNo, "N");

        if (memberUpdateResult > 0) {
            // 4. 만약 나가는 사용자가 마지막 1명이었다면 (즉, 나가기 전 멤버 수가 1명이었다면)
            if (activeMemberCount == 1) {
                // CHAT_ROOM 테이블의 해당 채팅방 STATUS를 'N'으로 변경
                int roomUpdateResult = cDao.updateChatRoomStatus(sqlSession, roomNo, "N");
                if (roomUpdateResult > 0) {
                    System.out.println("Service: 채팅방 (roomNo: " + roomNo + ") 상태를 'N'으로 변경 (마지막 멤버 나감)");
                    return true;
                } else {
                    System.err.println("Service: 채팅방 상태 변경 실패 (roomNo: " + roomNo + ")");
                    // 트랜잭션 롤백을 위해 예외 발생 또는 false 반환 후 Controller에서 처리
                    throw new RuntimeException("채팅방 상태 변경에 실패했습니다."); 
                }
            }
            System.out.println("Service: 채팅방 멤버 (empNo: " + empNo + ") 상태를 'N'으로 변경 (roomNo: " + roomNo + ")");
            return true; // 멤버 상태만 변경 성공
        } else {
            System.err.println("Service: 채팅방 멤버 상태 변경 실패 (roomNo: " + roomNo + ", empNo: " + empNo + ")");
            return false; // 멤버 상태 변경 실패
        }
    }

	@Override
	public ArrayList<Employee> findChatRoomMembers(int roomNo) {
		return null;
	}

	@Override
	public boolean isUserInRoom(int roomNo, int empNo) {
        // Mapper의 checkUserInRoom 메소드 호출하여 결과(count) 받기
        int count = cDao.checkUserInRoom(sqlSession, roomNo, empNo);
        // count가 0보다 크면 (즉, 1 이상이면) 멤버이므로 true, 아니면 false 반환
        return count > 0;
	}

	@Override
	@Transactional
	public ChatRoom createPrivateChatRoom(ArrayList<Integer> participants, int creatorEmpNo) {
        // 1:1 채팅방 생성 로직
        if (participants == null || participants.size() != 2) {
            throw new IllegalArgumentException("1:1 채팅방은 정확히 2명의 참여자가 필요합니다.");
        }

        // 1:1 채팅 상대방 empNo 찾기 (자신을 제외한 나머지 한 명)
        int participantEmpNo = (participants.get(0) == creatorEmpNo) ? participants.get(1) : participants.get(0);

        // TODO: 이미 존재하는 1:1 채팅방인지 확인하는 로직 활성화
        // SqlSessionTemplate 객체를 DAO 메소드에 파라미터로 전달하는 방식 대신,
        // DAO에서 SqlSessionTemplate을 직접 사용하도록 변경했으므로 호출 방식 수정
        ChatRoom existingRoom = cDao.findExistingPrivateChatRoom(sqlSession, creatorEmpNo, participantEmpNo);
        if (existingRoom != null) {
            // 이미 존재하면 기존 방 정보 반환
            // Controller에서 이 반환 값을 확인하여 적절한 응답을 보낼 수 있습니다.
            // 예: 이미 존재하는 방이라고 알리거나, 해당 방으로 리다이렉트
             System.out.println("Service: Existing private chat room found: " + existingRoom.getRoomNo());
            return existingRoom;
        }


        // 1. ChatRoom 객체 생성 (1:1 채팅)
        ChatRoom newRoom = new ChatRoom();
        newRoom.setRoomType("P"); // Private (1:1) 채팅
        newRoom.setCreatedByEmpNo(creatorEmpNo);
        // 1:1 채팅방 이름은 참여자 이름 조합 또는 상대방 이름으로 설정 (프론트/백엔드 협의)
        // 여기서는 일단 null로 두고, 목록 조회 시 이름을 조합해서 보여주도록 함
        newRoom.setRoomName(null); // 1:1 채팅방 이름은 DB에 저장하지 않거나 null로 설정

        // 2. CHAT_ROOM 테이블에 삽입
        int roomResult = cDao.insertChatRoom(sqlSession, newRoom);

        if (roomResult <= 0 || newRoom.getRoomNo() == 0) {
             throw new RuntimeException("1:1 채팅방 생성에 실패했습니다.");
        }

        // 3. CHAT_ROOM_MEMBER 테이블에 참여자들 추가
        int memberResultSum = 0;
        for (Integer empNo : participants) {
            ChatRoomMember member = new ChatRoomMember();
            member.setRoomNo(newRoom.getRoomNo()); // selectKey로 받아온 roomNo 사용
            member.setEmpNo(empNo);
            member.setIsAdmin(empNo == creatorEmpNo ? "Y" : "N"); // 생성자만 방장
            // 다른 필드는 기본값 사용

            memberResultSum += cDao.insertChatRoomMember(sqlSession, member);
        }

        // 참여자 수만큼 멤버가 제대로 추가되었는지 확인
        if (memberResultSum != participants.size()) {
             throw new RuntimeException("1:1 채팅방 멤버 추가에 실패했습니다.");
        }

        // 4. 생성된 ChatRoom 객체 반환 (필요하다면 DB에서 다시 조회하여 완전한 정보 로드)
        // return cDao.findRoomById(newRoom.getRoomNo()); // 필요시 주석 해제하여 상세 정보 조회
         return newRoom; // 기본 정보만 반환
	}

	@Override
	@Transactional
	public ChatRoom createGroupChatRoom(String roomName, ArrayList<Integer> participants, int creatorEmpNo) {
        // 단체 채팅방 생성 로직
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("단체 채팅방 이름은 필수입니다.");
        }
        if (participants == null || participants.size() < 2) { // 자신 포함 최소 2명
             throw new IllegalArgumentException("단체 채팅방은 두 명 이상의 참여자가 필요합니다.");
        }

        // 1. ChatRoom 객체 생성 (단체 채팅)
        ChatRoom newRoom = new ChatRoom();
        newRoom.setRoomName(roomName.trim());
        newRoom.setRoomType("G"); // Group (단체) 채팅
        newRoom.setCreatedByEmpNo(creatorEmpNo);
        // createDate, status는 DB에서 자동 설정

        // 2. CHAT_ROOM 테이블에 삽입
        int roomResult = cDao.insertChatRoom(sqlSession, newRoom);

        if (roomResult <= 0 || newRoom.getRoomNo() == 0) {
             throw new RuntimeException("단체 채팅방 생성에 실패했습니다.");
        }

        // 3. CHAT_ROOM_MEMBER 테이블에 참여자들 추가
        int memberResultSum = 0;
        for (Integer empNo : participants) {
            ChatRoomMember member = new ChatRoomMember();
            member.setRoomNo(newRoom.getRoomNo()); // selectKey로 받아온 roomNo 사용
            member.setEmpNo(empNo);
            member.setIsAdmin(empNo == creatorEmpNo ? "Y" : "N"); // 생성자만 방장
             // 다른 필드는 기본값 사용

            memberResultSum += cDao.insertChatRoomMember(sqlSession, member);
        }

        // 참여자 수만큼 멤버가 제대로 추가되었는지 확인
        if (memberResultSum != participants.size()) {
             throw new RuntimeException("단체 채팅방 멤버 추가에 실패했습니다.");
        }

        // 4. 생성된 ChatRoom 객체 반환 (필요하다면 DB에서 다시 조회하여 완전한 정보 로드)
        // return cDao.findRoomById(sqlSession, newRoom.getRoomNo()); // 필요시 주석 해제하여 상세 정보 조회
         return newRoom; // 기본 정보만 반환
	}

	@Override
	public ChatRoom findExistingPrivateChatRoom(int emp1, int emp2) {
		return cDao.findExistingPrivateChatRoom(sqlSession, emp1, emp2);
	}

	@Override
	@Transactional
	public void markMessagesAsRead(int roomNo, int userEmpNo) {
        // --- DAO로 넘길 파라미터 맵 생성 ---
        Map<String, Object> params = new HashMap<>();
        params.put("roomNo", roomNo); // "roomNo"라는 키로 roomNo 값을 Map에 담습니다.
        params.put("userEmpNo", userEmpNo); // "userEmpNo"라는 키로 userEmpNo 값을 Map에 담습니다.

        // --- ChatDAO의 updateLastReadMessageNo 메소드 호출 ---
        // SqlSessionTemplate와 파라미터 맵을 DAO 메소드에 전달합니다.
        cDao.updateLastReadMessageNo(sqlSession, params); // <-- 여기 수정
	}

	@Override
	public int getTotalUnreadChatCountForUser(int empNo) {
		return cDao.getTotalUnreadChatCountForUser(sqlSession, empNo);
	}

	
	


}
