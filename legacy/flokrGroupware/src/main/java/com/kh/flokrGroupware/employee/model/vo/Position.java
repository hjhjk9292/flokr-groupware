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
public class Position {
    private int positionNo;
    private String positionName;
    private Date createDate;
    private String status;
    
    // 수동 getter/setter 추가
    public int getPositionNo() {
        return this.positionNo;
    }
    
    public void setPositionNo(int positionNo) {
        this.positionNo = positionNo;
    }
    
    public String getPositionName() {
        return this.positionName;
    }
    
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }
}