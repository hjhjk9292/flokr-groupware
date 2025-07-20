package com.kh.flokrGroupware.chat.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class ChatMessage {
	
	private int messageNo;
	private int roomNo;
	private int senderEmpNo;
	private String chatContent;
	private String messageType;
	private String sendDate;
	private String status;
	
	// 추가 
	
	private String senderName;
	private String senderProfileImgPath;
	
	public int getMessageNo() {
		return messageNo;
	}
	public void setMessageNo(int messageNo) {
		this.messageNo = messageNo;
	}
	public int getRoomNo() {
		return roomNo;
	}
	public void setRoomNo(int roomNo) {
		this.roomNo = roomNo;
	}
	public int getSenderEmpNo() {
		return senderEmpNo;
	}
	public void setSenderEmpNo(int senderEmpNo) {
		this.senderEmpNo = senderEmpNo;
	}
	public String getChatContent() {
		return chatContent;
	}
	public void setChatContent(String chatContent) {
		this.chatContent = chatContent;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getSendDate() {
		return sendDate;
	}
	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSenderProfileImgPath() {
		return senderProfileImgPath;
	}
	public void setSenderProfileImgPath(String senderProfileImgPath) {
		this.senderProfileImgPath = senderProfileImgPath;
	}
	
	

}
