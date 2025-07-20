package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 가장 기본적인 메서드들만 우선 정의
    // 실제 존재하는 필드만 사용

    // 채팅방별 멤버 조회
    List<ChatRoomMember> findByChatRoom_RoomNo(Long roomNo);

    // 사용자별 멤버 조회
    List<ChatRoomMember> findByEmployee_EmpNo(Long empNo);

    // 특정 채팅방의 특정 사용자 조회
    Optional<ChatRoomMember> findByChatRoom_RoomNoAndEmployee_EmpNo(Long roomNo, Long empNo);

    // 채팅방의 멤버 수 조회
    long countByChatRoom_RoomNo(Long roomNo);
}