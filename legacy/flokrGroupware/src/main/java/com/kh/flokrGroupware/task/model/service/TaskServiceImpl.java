package com.kh.flokrGroupware.task.model.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.task.model.dao.TaskDao;
import com.kh.flokrGroupware.task.model.vo.Task;
import com.kh.flokrGroupware.task.model.vo.TaskAssignee;

@Service
public class TaskServiceImpl implements TaskService {
	
	@Autowired
	private TaskDao tDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Autowired
	private RestHighLevelClient elasticsearchClient;

	@Override
	public ArrayList<Task> taskList(int empNo) {
		return tDao.taskList(sqlSession, empNo);
	}

	@Override
	@Transactional
	public int taskInsert(Task task, Attachment atmt) {
	    // 담당자 목록이 null인 새 메소드 호출
	    return taskInsert(task, atmt, null);
	}
	
	@Override
	@Transactional
	public int taskInsert(Task task, Attachment atmt, List<Integer> assigneeList) {
	    // 업무 먼저 등록
	    int result1 = tDao.taskInsert(sqlSession, task);
	    int result2 = 1;
	    int result3 = 1;

	    // 첨부파일이 있으면 등록
	    if (atmt != null) {
	        result2 = tDao.insertAttachment(sqlSession, atmt);
	    }
	    
	    // 담당자가 있으면 등록
	    if (assigneeList != null && !assigneeList.isEmpty() && result1 > 0) {
	        Task insertedTask = tDao.selectRecentTask(sqlSession, task);
	        if (insertedTask != null) {
	            int taskNo = insertedTask.getTaskNo();
	            for (Integer assigneeEmpNo : assigneeList) {
	                TaskAssignee assignee = new TaskAssignee();
	                assignee.setTaskNo(taskNo);
	                assignee.setAssigneeEmpNo(assigneeEmpNo);
	                
	                int assignResult = tDao.insertTaskAssignee(sqlSession, assignee);
	                if (assignResult <= 0) {
	                    result3 = 0;
	                    break;
	                }
	            }
	        } else {
	            result3 = 0;
	        }
	    }
	    
	    // Elasticsearch 인덱싱 (기존 코드)
	    if (result1 * result2 * result3 > 0) {
	    	try {
	    		Task insertedTask = tDao.selectRecentTask(sqlSession, task);
	            
	            if (insertedTask != null) {
	                Map<String, Object> esDoc = new HashMap<>();
	                esDoc.put("taskNo", insertedTask.getTaskNo());
	                esDoc.put("taskTitle", insertedTask.getTaskTitle());
	                esDoc.put("taskContent", insertedTask.getTaskContent());
	                esDoc.put("category", insertedTask.getCategory());
	                esDoc.put("taskStatus", insertedTask.getTaskStatus());
	                esDoc.put("emoji", task.getEmoji());

	                
	                String dueDate = insertedTask.getDueDate();
	                if (dueDate != null && !dueDate.isEmpty()) {
	                    // "2025-05-13 00:00:00.0" 형식에서 날짜 부분만 추출
	                    if (dueDate.contains(" ")) {
	                        dueDate = dueDate.split(" ")[0]; // "2025-05-13" 부분만 사용
	                    }
	                    esDoc.put("dueDate", dueDate);
	                } else {
	                    // 날짜가 null이면 필드 자체를 제외
	                    esDoc.put("dueDate", null);
	                }
	                
	                // 자동완성용 suggest 입력어 분리
	                List<String> inputs = new ArrayList<>();
	                inputs.add(insertedTask.getTaskTitle());
	                inputs.addAll(Arrays.asList(insertedTask.getTaskTitle().split("\\s+")));
	                esDoc.put("suggest", Map.of("input", inputs));
	                
	                // Elasticsearch에 인덱싱
	                IndexRequest request = new IndexRequest("flokr_task_index").source(esDoc);
	                elasticsearchClient.index(request, RequestOptions.DEFAULT);
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	            // 실패 시에도 DB 저장은 유지
	        }
	    }

	    return result1 * result2 * result3;
	}

	@Override
	public Task taskDetail(int taskNo) {
		return tDao.taskDetail(sqlSession, taskNo);
	}

	@Override
	public Attachment getAttachment(int taskNo) {
		return tDao.getAttachment(sqlSession, taskNo);
	}
	
	@Override
	public int taskAtmtUpdate(Task task, Attachment atmt) {
		int result1 = tDao.taskUpdate(sqlSession, task);
	    int result2 = 1;

	    if (atmt != null) {
	        result2 = tDao.newAttachment(sqlSession, atmt);
	    }

	    return result1 * result2;
	}

	@Override
	public int attachmentDelete(Attachment atmt) {
		return tDao.attachmentDelete(sqlSession, atmt);
	}

	@Override
	public int taskUpdate(Task task) {
		return tDao.taskUpdate(sqlSession, task);
	}

	@Override
	public List<Employee> getAllEmployees(int empNo) {
		return tDao.getAllEmployees(sqlSession, empNo);
	}
	
	@Override
	public List<TaskAssignee> getTaskAssignees(int taskNo) {
		return tDao.getTaskAssignees(sqlSession, taskNo);
	}

}