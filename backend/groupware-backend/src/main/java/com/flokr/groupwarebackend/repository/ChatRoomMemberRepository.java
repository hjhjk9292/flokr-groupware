package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    
    // 채팅방별 활성 멤버 조회
    List<ChatRoomMember> findByChatRoom_RoomIdAndIsActiveTrueOrderByJoinedAtAsc(Long roomId);
    
    // 사용자별 참여 채팅방 조회
    List<ChatRoomMember> findByEmployee_EmpIdAndIsActiveTrueOrderByJoinedAtDesc(String empId);
    
    // 특정 사용자의 특정 채팅방 멤버십 조회
    Optional<ChatRoomMember> findByChatRoom_RoomIdAndEmployee_EmpId(Long roomId, String empId);
    
    // 채팅방 관리자 조회
    List<ChatRoomMember> findByChatRoom_RoomIdAndRoleAndIsActiveTrue(Long roomId, ChatRoomMember.MemberRole role);
    
    // 채팅방 멤버 수 조회
    long countByChatRoom_RoomIdAndIsActiveTrue(Long roomId);
    
    // 사용자가 참여한 채팅방 수
    long countByEmployee_EmpIdAndIsActiveTrue(String empId);
    
    // 멤버 읽음 시간 업데이트
    @Modifying
    @Query("UPDATE ChatRoomMember crm SET crm.lastReadAt = :readAt " +
           "WHERE crm.chatRoom.roomId = :roomId AND crm.employee.empId = :empId")
    void updateLastReadTime(@Param("roomId") Long roomId, @Param("empId") String empId, @Param("readAt") LocalDateTime readAt);
    
    // 채팅방 나가기 (비활성화)
    @Modifying
    @Query("UPDATE ChatRoomMember crm SET crm.isActive = false, crm.leftAt = :leftAt " +
           "WHERE crm.chatRoom.roomId = :roomId AND crm.employee.empId = :empId")
    void leaveChatRoom(@Param("roomId") Long roomId, @Param("empId") String empId, @Param("leftAt") LocalDateTime leftAt);
    
    // 특정 부서 직원들 조회 (부서 채팅방 생성용)
    @Query("SELECT crm FROM ChatRoomMember crm " +
           "WHERE crm.employee.department.deptId = :deptId AND crm.isActive = true")
    List<ChatRoomMember> findByDepartment(@Param("deptId") Long deptId);
}
