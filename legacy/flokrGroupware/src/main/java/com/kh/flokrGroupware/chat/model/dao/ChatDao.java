package com.kh.flokrGroupware.chat.model.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.chat.model.vo.ChatMessage;
import com.kh.flokrGroupware.chat.model.vo.ChatRoom;
import com.kh.flokrGroupware.chat.model.vo.ChatRoomMember;

@Repository
public class ChatDao {
	
	public ArrayList<ChatRoom> findMyChatRooms(SqlSessionTemplate sqlSession, Map<String, Object> params) {
		
		return (ArrayList)sqlSession.selectList("chatMapper.findMyChatRooms", params);

	}
	
	public int checkUserInRoom(SqlSessionTemplate sqlSession, int roomNo, int empNo) {
		
        // 1. 파라미터를 담을 Map 생성
        Map<String, Object> params = new HashMap<>();
        // 2. Map에 파라미터 추가 (key는 XML의 #{...} 이름과 일치)
        params.put("roomNo", roomNo);
        params.put("empNo", empNo);

        // 3. selectOne 메소드에 Map 객체를 파라미터로 전달
        // COUNT(*) 결과는 보통 Integer로 반환되므로 Integer로 받거나 int로 캐스팅
        Integer count = sqlSession.selectOne("chatMapper.checkUserInRoom", params);

        // null 체크 후 int 반환 (count(*)는 보통 null을 반환하지 않지만 안전하게 처리)
        return (count != null) ? count.intValue() : 0;
	}
	
    public ChatRoom findRoomById(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        // selectOne 메소드는 단일 객체를 반환할 때 사용합니다.
        // 파라미터로 Map 객체를 전달합니다.
        return sqlSession.selectOne("chatMapper.findRoomById", params);
    }
	
	public ArrayList<ChatMessage> findMessagesByRoomId(SqlSessionTemplate sqlSession, int roomNo) {
		
		return (ArrayList) sqlSession.selectList("chatMapper.findMessagesByRoomId", roomNo);
		
	}
	
	public int insertChatMessage(SqlSessionTemplate sqlSession, ChatMessage message) {
		
		return sqlSession.insert("chatMapper.insertChatMessage", message);
	}
	
    public int insertChatRoom(SqlSessionTemplate sqlSession, ChatRoom chatRoom) {
        return sqlSession.insert("chatMapper.insertChatRoom", chatRoom);
    }

     public int insertChatRoomMember(SqlSessionTemplate sqlSession, ChatRoomMember chatRoomMember) {
        return sqlSession.insert("chatMapper.insertChatRoomMember", chatRoomMember);
    }
     
     public ChatRoom findExistingPrivateChatRoom(SqlSessionTemplate sqlSession, int emp1, int emp2) {
         Map<String, Object> params = new HashMap<>();
         params.put("emp1", emp1);
         params.put("emp2", emp2);
         // chatMapper.findExistingPrivateChatRoom 쿼리 실행
         return sqlSession.selectOne("chatMapper.findExistingPrivateChatRoom", params);
     }
	

     public int updateLastReadMessageNo(SqlSessionTemplate sqlSession, Map<String, Object> params) {

         return sqlSession.update("chatMapper.updateLastReadMessageNo", params);
     }
     
     /**
      * 특정 사용자의 총 안 읽은 채팅 수를 조회
      * @param sqlSession
      * @param empNo 사용자 직원 번호
      * @return 총 안 읽은 메시지 수
      */
     public int getTotalUnreadChatCountForUser(SqlSessionTemplate sqlSession, int empNo) {
         return sqlSession.selectOne("chatMapper.getTotalUnreadChatCountForUser", empNo);
     }
     
     /**
      * 특정 채팅방의 모든 활성 멤버의 직원 번호(empNo) 목록을 조회합니다.
      * @param roomNo 채팅방 번호
      * @return 멤버 empNo 목록
      */
     public ArrayList<Integer> getChatRoomMemberEmpNos(SqlSessionTemplate sqlSession, int roomNo) {
         // chatMapper.getChatRoomMemberEmpNos 쿼리를 실행합니다.
         // 결과 타입이 단일 Integer 목록이므로 resultType="_int"를 사용하고,
         // selectList는 해당 타입의 List를 반환합니다.
         return (ArrayList)sqlSession.selectList("chatMapper.getChatRoomMemberEmpNos", roomNo);
     }
     
     /**
      * 특정 채팅방의 활성 멤버 수를 조회합니다.
      * @param sqlSession SqlSessionTemplate 객체
      * @param roomNo 조회할 채팅방 번호
      * @return 활성 멤버 수
      */
     public int getActiveChatRoomMemberCount(SqlSessionTemplate sqlSession, int roomNo) {
         Integer count = sqlSession.selectOne("chatMapper.getActiveChatRoomMemberCount", roomNo);
         return (count != null) ? count.intValue() : 0;
     }

     /**
      * 특정 채팅방 멤버의 상태를 업데이트합니다.
      * @param sqlSession SqlSessionTemplate 객체
      * @param roomNo 채팅방 번호
      * @param empNo 직원 번호
      * @param status 변경할 상태 ('Y' 또는 'N')
      * @return 업데이트된 행의 수
      */
     public int updateChatRoomMemberStatus(SqlSessionTemplate sqlSession, int roomNo, int empNo, String status) {
         Map<String, Object> params = new HashMap<>();
         params.put("roomNo", roomNo);
         params.put("empNo", empNo);
         params.put("status", status);
         return sqlSession.update("chatMapper.updateChatRoomMemberStatus", params);
     }

     /**
      * 특정 채팅방의 상태를 업데이트합니다.
      * @param sqlSession SqlSessionTemplate 객체
      * @param roomNo 채팅방 번호
      * @param status 변경할 상태 ('Y' 또는 'N')
      * @return 업데이트된 행의 수
      */
     public int updateChatRoomStatus(SqlSessionTemplate sqlSession, int roomNo, String status) {
         Map<String, Object> params = new HashMap<>();
         params.put("roomNo", roomNo);
         params.put("status", status);
         return sqlSession.update("chatMapper.updateChatRoomStatus", params);
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     

}
