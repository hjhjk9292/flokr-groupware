package com.kh.flokrGroupware.chat.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatRoom {
	
	private int roomNo;
	private String roomName;
	private String roomType;
	private int createdByEmpNo;
	private String createDate;
	private String status;
	
	// 마지막 메시지 정보
	private String lastMessageContent;
	private Date lastMessageTime;
	
	// 1:1 채팅 상대방 프로필
	private String chatUserImgPath;
	
    // 참여 인원 수
    private int memberCount;
    
    // --- 안 읽은 메시지 수 필드 추가 ---
    private int unreadCount;



}
