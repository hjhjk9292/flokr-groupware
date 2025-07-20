package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방별 메시지 조회 (최신순)
    List<ChatMessage> findByChatRoom_RoomNoOrderBySendDateDesc(Long roomNo);

    // 채팅방별 메시지 조회 (페이징)
    List<ChatMessage> findByChatRoom_RoomNoOrderBySendDateDesc(Long roomNo, Pageable pageable);

    // 채팅방의 최신 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.roomNo = :roomNo " +
            "ORDER BY cm.sendDate DESC LIMIT 1")
    ChatMessage findLatestMessageByRoomNo(@Param("roomNo") Long roomNo);

    // 검색어로 메시지 검색
    List<ChatMessage> findByChatRoom_RoomNoAndChatContentContainingOrderBySendDateDesc(Long roomNo, String keyword);

    // 특정 기간의 메시지 조회
    List<ChatMessage> findByChatRoom_RoomNoAndSendDateBetweenOrderBySendDateAsc(
            Long roomNo, LocalDateTime startDate, LocalDateTime endDate);

    // 파일 메시지만 조회
    List<ChatMessage> findByChatRoom_RoomNoAndMessageTypeOrderBySendDateDesc(
            Long roomNo, String messageType);

    // 특정 사용자의 읽지 않은 메시지 수 (특정 시간 이후)
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
            "WHERE cm.chatRoom.roomNo = :roomNo " +
            "AND cm.sendDate > :lastReadDate " +
            "AND cm.sender.empNo != :empNo")
    long countUnreadMessages(@Param("roomNo") Long roomNo, @Param("lastReadDate") LocalDateTime lastReadDate, @Param("empNo") Long empNo);
}