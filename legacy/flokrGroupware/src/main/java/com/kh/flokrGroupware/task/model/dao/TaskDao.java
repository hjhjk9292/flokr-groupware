package com.kh.flokrGroupware.task.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.task.model.vo.Task;
import com.kh.flokrGroupware.task.model.vo.TaskAssignee;

@Repository
public class TaskDao {
	
	public ArrayList<Task> taskList(SqlSessionTemplate sqlSession, int empNo) {
		return (ArrayList)sqlSession.selectList("taskMapper.taskList", empNo);
	}
	
	public int taskInsert(SqlSessionTemplate sqlSession, Task task) {
		return sqlSession.insert("taskMapper.taskInsert", task);
	}
	
	public int insertAttachment(SqlSessionTemplate sqlSession, Attachment atmt) {
		return sqlSession.insert("taskMapper.insertAttachment", atmt);
	}
	
	public Task taskDetail(SqlSessionTemplate sqlSession, int taskNo) {
		return sqlSession.selectOne("taskMapper.taskDetail", taskNo);
	}
	
	public Attachment getAttachment(SqlSessionTemplate sqlSession, int taskNo) {
		return sqlSession.selectOne("taskMapper.getAttachment", taskNo);
	}
	
	public int attachmentDelete(SqlSessionTemplate sqlSession, Attachment atmt) {
		return sqlSession.update("taskMapper.attachmentDelete", atmt);
	}
	
	public int taskUpdate(SqlSessionTemplate sqlSession, Task task) {
		return sqlSession.update("taskMapper.taskUpdate", task);
	}
	
	public int newAttachment(SqlSessionTemplate sqlSession, Attachment atmt) {
		return sqlSession.insert("taskMapper.newAttachment", atmt);
	}
	
	public Task selectRecentTask(SqlSessionTemplate sqlSession, Task task) {
	    return sqlSession.selectOne("taskMapper.selectRecentTask", task);
	}
	
	public List<Employee> getAllEmployees(SqlSessionTemplate sqlSession, int empNo) {
		return sqlSession.selectList("taskMapper.getAllEmployees", empNo);
	}
	
	public int insertTaskAssignee(SqlSessionTemplate sqlSession, TaskAssignee assignee) {
		return sqlSession.insert("taskMapper.insertTaskAssignee", assignee);
	}
	
	// 새로 추가한 메소드: 특정 업무의 담당자 목록 조회
	public List<TaskAssignee> getTaskAssignees(SqlSessionTemplate sqlSession, int taskNo) {
		return sqlSession.selectList("taskMapper.getTaskAssignees", taskNo);
	}
}