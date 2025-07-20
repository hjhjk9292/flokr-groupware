package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 채팅방별 메시지 조회 (페이징, 최신순)
    Page<ChatMessage> findByChatRoom_RoomIdOrderBySentAtDesc(Long roomId, Pageable pageable);
    
    // 특정 시간 이후 메시지 조회 (실시간 업데이트용)
    List<ChatMessage> findByChatRoom_RoomIdAndSentAtAfterOrderBySentAtAsc(Long roomId, LocalDateTime after);
    
    // 발신자별 메시지 조회
    List<ChatMessage> findBySender_EmpIdOrderBySentAtDesc(String senderId);
    
    // 메시지 타입별 조회
    List<ChatMessage> findByChatRoom_RoomIdAndMessageTypeOrderBySentAtDesc(Long roomId, ChatMessage.MessageType messageType);
    
    // 특정 채팅방의 최근 메시지 1개
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.roomId = :roomId " +
           "ORDER BY cm.sentAt DESC LIMIT 1")
    ChatMessage findLatestMessageByRoomId(@Param("roomId") Long roomId);
    
    // 검색어로 메시지 검색
    List<ChatMessage> findByChatRoom_RoomIdAndContentContainingOrderBySentAtDesc(Long roomId, String keyword);
    
    // 특정 기간의 메시지 조회
    List<ChatMessage> findByChatRoom_RoomIdAndSentAtBetweenOrderBySentAtAsc(
        Long roomId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 파일 메시지만 조회
    List<ChatMessage> findByChatRoom_RoomIdAndMessageTypeAndFileUrlIsNotNullOrderBySentAtDesc(
        Long roomId, ChatMessage.MessageType messageType);
    
    // 특정 사용자의 읽지 않은 메시지 수 (특정 시간 이후)
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "WHERE cm.chatRoom.roomId = :roomId " +
           "AND cm.sentAt > :lastReadAt " +
           "AND cm.sender.empId != :empId")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("lastReadAt") LocalDateTime lastReadAt, @Param("empId") String empId);
}
