package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_NO")
    private Long messageNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_chatmessage_room"))
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_EMP_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_chatmessage_sender"))
    private Employee sender;

    @Column(name = "CHAT_CONTENT", nullable = false, columnDefinition = "TEXT")
    private String chatContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE", length = 50)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @CreationTimestamp
    @Column(name = "SEND_DATE")
    private LocalDateTime sendDate;

    @Column(name = "STATUS", length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String status = "Y";

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }
}