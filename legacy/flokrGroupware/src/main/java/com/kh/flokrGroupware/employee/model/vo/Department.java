package com.kh.flokrGroupware.employee.model.vo;

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
public class Department {
    private int deptNo;
    private String deptName;
    private Date createDate;
    private String status;
    
    // 수동 getter/setter 추가
    public int getDeptNo() {
        return this.deptNo;
    }
    
    public void setDeptNo(int deptNo) {
        this.deptNo = deptNo;
    }
    
    public String getDeptName() {
        return this.deptName;
    }
    
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}