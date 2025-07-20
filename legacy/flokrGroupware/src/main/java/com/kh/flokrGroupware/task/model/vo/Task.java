package com.kh.flokrGroupware.task.model.vo;

import java.util.List;

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
public class Task {
	
	private int taskNo;
	private String taskTitle;
	private String taskContent;
	private String category;
	private String taskWriter;
	private String dueDate;
	private String createDate;
	private String updateDate;
	private String taskStatus;
	private String emoji;
	private List<TaskAssignee> assignees;

}
