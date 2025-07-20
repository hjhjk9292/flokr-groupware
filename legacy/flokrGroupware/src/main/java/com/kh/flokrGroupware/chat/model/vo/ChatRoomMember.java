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
public class ChatRoomMember {
	
	private int memberNo;
	private int roomNo;
	private int empNo;
	private Date joinDate;
	private int lastReadMessageNo;
	private String isAdmin;
	private int notificationEnabled;
	private String status;
	
	
	
	
	public int getMemberNo() {
		return memberNo;
	}
	public void setMemberNo(int memberNo) {
		this.memberNo = memberNo;
	}
	public int getRoomNo() {
		return roomNo;
	}
	public void setRoomNo(int roomNo) {
		this.roomNo = roomNo;
	}
	public int getEmpNo() {
		return empNo;
	}
	public void setEmpNo(int empNo) {
		this.empNo = empNo;
	}
	public int getLastReadMessageNo() {
		return lastReadMessageNo;
	}
	public void setLastReadMessageNo(int lastReadMessageNo) {
		this.lastReadMessageNo = lastReadMessageNo;
	}
	public String getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(String isAdmin) {
		this.isAdmin = isAdmin;
	}
	public int getNotificationEnabled() {
		return notificationEnabled;
	}
	public void setNotificationEnabled(int notificationEnabled) {
		this.notificationEnabled = notificationEnabled;
	}
	
	

}
