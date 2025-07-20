package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // 활성 채팅방만 조회
    List<ChatRoom> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // 채팅방 타입별 조회
    List<ChatRoom> findByRoomTypeAndIsActiveTrueOrderByCreatedAtDesc(ChatRoom.RoomType roomType);
    
    // 채팅방 이름으로 검색
    List<ChatRoom> findByRoomNameContainingAndIsActiveTrueOrderByCreatedAtDesc(String roomName);
    
    // 생성자별 채팅방 조회
    List<ChatRoom> findByCreatedBy_EmpIdAndIsActiveTrueOrderByCreatedAtDesc(String creatorId);
    
    // 특정 사용자가 참여한 채팅방 조회
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "JOIN cr.members m " +
           "WHERE m.employee.empId = :empId AND m.isActive = true AND cr.isActive = true " +
           "ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findChatRoomsByMember(@Param("empId") String empId);
    
    // 부서별 채팅방 조회 (부서 타입 채팅방)
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.roomType = 'DEPARTMENT' AND cr.isActive = true " +
           "AND cr.createdBy.department.deptId = :deptId " +
           "ORDER BY cr.createdAt DESC")
    List<ChatRoom> findDepartmentChatRooms(@Param("deptId") Long deptId);
    
    // 채팅방 참여자 수 조회
    @Query("SELECT cr.roomId, COUNT(m) FROM ChatRoom cr " +
           "LEFT JOIN cr.members m " +
           "WHERE cr.isActive = true AND m.isActive = true " +
           "GROUP BY cr.roomId")
    List<Object[]> findChatRoomMemberCounts();
}
