package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 활성 상태 채팅방 조회
    List<ChatRoom> findByStatusOrderByCreateDateDesc(String status);

    // 타입별 채팅방 조회
    List<ChatRoom> findByRoomTypeAndStatusOrderByCreateDateDesc(ChatRoom.RoomType roomType, String status);

    // 특정 사용자가 참여한 채팅방 조회
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN cr.members m " +
            "WHERE m.employee.empNo = :empNo AND m.status = 'Y' AND cr.status = 'Y' " +
            "ORDER BY cr.createDate DESC")
    List<ChatRoom> findChatRoomsByMember(@Param("empNo") Long empNo);

    // 부서별 채팅방 조회 (부서 타입 채팅방)
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.roomType = 'DEPARTMENT' AND cr.status = 'Y' " +
            "AND cr.createdBy.department.deptNo = :deptNo " +
            "ORDER BY cr.createDate DESC")
    List<ChatRoom> findDepartmentChatRooms(@Param("deptNo") Long deptNo);

    // 채팅방 참여자 수 조회
    @Query("SELECT cr.roomNo, COUNT(m) FROM ChatRoom cr " +
            "LEFT JOIN cr.members m " +
            "WHERE cr.status = 'Y' AND m.status = 'Y' " +
            "GROUP BY cr.roomNo")
    List<Object[]> findChatRoomMemberCounts();
}