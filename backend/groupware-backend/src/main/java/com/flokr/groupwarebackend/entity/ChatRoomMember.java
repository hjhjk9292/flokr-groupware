package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ROOM_MEMBER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_NO")
    private Long memberNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_chatroommember_room"))
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_chatroommember_employee"))
    private Employee employee;

    @CreationTimestamp
    @Column(name = "JOIN_DATE")
    private LocalDateTime joinDate;

    @Column(name = "LAST_READ_MESSAGE_NO")
    private Long lastReadMessageNo;

    @Column(name = "IS_ADMIN", length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String isAdmin = "N";

    @Column(name = "NOTIFICATION_ENABLED")
    @Builder.Default
    private Boolean notificationEnabled = true;

    @Column(name = "STATUS", length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String status = "Y";

    public enum MemberRole {
        ADMIN, MODERATOR, MEMBER
    }
}