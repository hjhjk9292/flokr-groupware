package com.kh.flokrGroupware.task.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                   // getter, setter, toString, equals, hashCode 등을 자동 생성
@NoArgsConstructor      // 기본 생성자 자동 생성
@AllArgsConstructor     // 모든 필드를 매개변수로 받는 생성자 자동 생성
public class TaskAssignee {
    private int taskAssigneeNo;
    private int taskNo;
    private int assigneeEmpNo;
    private String assignDate;
    private String empName;
    private String deptName;
    private String positionName;
    private String phone;
    private String email;
}