package com.kh.flokrGroupware.task.model.service;

import java.util.ArrayList;
import java.util.List;

import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.task.model.vo.Task;
import com.kh.flokrGroupware.task.model.vo.TaskAssignee;

public interface TaskService {
    ArrayList<Task> taskList(int empNo);
    
    int taskInsert(Task task, Attachment atmt);
    
    // 담당자 목록도 처리하는 새 메소드
    int taskInsert(Task task, Attachment atmt, List<Integer> assigneeList);
    
    Task taskDetail(int taskNo);
    
    Attachment getAttachment(int taskNo);
    
    int taskAtmtUpdate(Task task, Attachment atmt);
    
    int attachmentDelete(Attachment atmt);
    
    int taskUpdate(Task task);
    
    List<Employee> getAllEmployees(int empNo);
    
    // 업무 담당자 목록을 가져오는 새 메소드
    List<TaskAssignee> getTaskAssignees(int taskNo);
}