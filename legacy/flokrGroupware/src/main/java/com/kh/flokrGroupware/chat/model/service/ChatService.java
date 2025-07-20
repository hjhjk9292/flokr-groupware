package com.kh.flokrGroupware.chat.model.service;

import java.util.ArrayList;

import com.kh.flokrGroupware.chat.model.vo.ChatMessage;
import com.kh.flokrGroupware.chat.model.vo.ChatRoom;
import com.kh.flokrGroupware.employee.model.vo.Employee;

public interface ChatService {
	
    // --- 채팅방 관리 ---

    /**
     * 새로운 채팅방 생성 (RequestParam 방식에 맞게 시그니처 조정)
     * @param chatType 채팅 유형 ('oneToOne' 또는 'group')
     * @param roomName 생성할 채팅방 이름 (단체 채팅 시 사용, 1:1 시 null)
     * @param participants 참여자 직원 번호 목록
     * @param creatorEmpNo 생성자 직원 번호
     * @return 생성된 채팅방 정보 (ChatRoom 객체)
     */
    ChatRoom createChatRoom(String roomName, int creatorEmpNo); // 또는 int createChatRoom(...)
    
    /**
     * 1:1 채팅방 생성 (내부 사용 또는 필요시 유지)
     * @param participants 참여자 직원 번호 목록 (2명)
     * @param creatorEmpNo 생성자 직원 번호
     * @return 생성된 채팅방 정보
     */
    ChatRoom createPrivateChatRoom(ArrayList<Integer> participants, int creatorEmpNo);

    /**
     * 단체 채팅방 생성 (내부 사용 또는 필요시 유지)
     * @param roomName 채팅방 이름
     * @param participants 참여자 직원 번호 목록 (생성자 포함)
     * @param creatorEmpNo 생성자 직원 번호
     * @return 생성된 채팅방 정보
     */
    ChatRoom createGroupChatRoom(String roomName, ArrayList<Integer> participants, int creatorEmpNo);
    
    /**
     * 사용자가 참여하고 있는 채팅방 목록 조회
     * @param empNo 직원 번호
     * @return 해당 직원이 참여중인 채팅방 목록 (ArrayList)
     */
    ArrayList<ChatRoom> findMyChatRooms(int empNo);

    /**
     * 특정 채팅방 정보 조회
     * @param roomNo 채팅방 번호
     * @return 채팅방 상세 정보
     */
    ChatRoom findChatRoomById(int roomNo, int loginUserEmpNo);

    /**
     * 채팅방 이름 변경 (선택 사항)
     * @param roomNo 변경할 채팅방 번호
     * @param newRoomName 새로운 채팅방 이름
     * @param empNo 요청한 직원 번호 (권한 확인용)
     * @return 성공 여부 (int)
     */
    int updateChatRoomName(int roomNo, String newRoomName, int empNo);

    /**
     * 채팅방 삭제 (선택 사항 - 실제 삭제 또는 상태 변경)
     * @param roomNo 삭제할 채팅방 번호
     * @param empNo 요청한 직원 번호 (권한 확인용)
     * @return 성공 여부 (int)
     */
    int deleteChatRoom(int roomNo, int empNo);


    // --- 메시지 처리 ---

    /**
     * 채팅 메시지 처리 (DB 저장 및 WebSocket 전송)
     * ChatController 에서 호출되어 메시지를 받아 처리합니다.
     * 내부적으로 DB 저장 후, SimpMessagingTemplate 를 이용해 구독자에게 메시지를 보냅니다.
     * @param message 수신된 채팅 메시지 객체 (ChatMessage VO의 senderEmpNo 필드 사용)
     */
    void processAndSendMessage(ChatMessage message);

    /**
     * 특정 채팅방의 이전 대화 내역 조회
     * @param roomNo 대화 내역을 조회할 채팅방 번호
     * @return 해당 채팅방의 메시지 목록 (ArrayList)
     */
    ArrayList<ChatMessage> getChatHistory(int roomNo);


    // --- 채팅방 참여자 관리 ---

    /**
     * 특정 채팅방에 사용자 초대 (또는 추가)
     * @param roomNo 대상 채팅방 번호
     * @param inviterEmpNo 초대한 직원 번호
     * @param inviteeEmpNo 초대받은 직원 번호 (또는 번호 목록 ArrayList<Integer>)
     * @return 성공 여부 (int)
     */
    int inviteUserToRoom(int roomNo, int inviterEmpNo, int inviteeEmpNo);
    // 또는 int inviteUsersToRoom(int roomNo, int inviterEmpNo, ArrayList<Integer> inviteeEmpNos);

    /**
     * 사용자가 채팅방 나가기
     * @param roomNo 나갈 채팅방 번호
     * @param empNo 나가는 직원 번호
     * @return 성공 여부 (boolean)
     */
    boolean leaveChatRoom(int roomNo, int empNo);

    /**
     * 특정 채팅방의 참여자 목록 조회
     * @param roomNo 조회할 채팅방 번호
     * @return 해당 채팅방에 참여중인 직원 목록 (Employee 객체 ArrayList 또는 empNo ArrayList)
     */
    ArrayList<Employee> findChatRoomMembers(int roomNo); // 또는 ArrayList<Integer> findChatRoomMemberNos(int roomNo);

    /**
     * 사용자가 특정 채팅방의 멤버인지 확인
     * @param roomNo 확인할 채팅방 번호
     * @param empNo 확인할 직원 번호
     * @return 멤버 여부 (boolean 또는 int count)
     */
    boolean isUserInRoom(int roomNo, int empNo); // 또는 int checkUserInRoom(...) 
    
    /**
     * 채팅방이 이미 존재하는지 확인하는 메소드
     * @param emp1
     * @param emp2
     * @return
     */
    ChatRoom findExistingPrivateChatRoom(int emp1, int emp2);
    
    /**
     * 특정 사용자의 특정 채팅방 마지막 읽은 메시지 번호를 최신 메시지 번호로 업데이트
     * chat-mapper.xml의 updateLastReadMessageNo 쿼리 호출
     * @param roomNo 채팅방 번호
     * @param userEmpNo 사용자(직원) 번호
     * @return 업데이트된 레코드 수
     */
    public void markMessagesAsRead(int roomNo, int userEmpNo);
    
    /**
     * 특정 사용자의 총 안 읽은 채팅 수를 조회
     * @param empNo 사용자 직원 번호
     * @return 총 안 읽은 메시지 수
     */
    int getTotalUnreadChatCountForUser(int empNo);
    
    
}
