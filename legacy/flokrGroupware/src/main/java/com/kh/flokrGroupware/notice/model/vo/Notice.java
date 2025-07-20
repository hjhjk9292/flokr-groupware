package com.kh.flokrGroupware.notice.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    private int noticeNo;          // 공지사항 번호
    private int empNo;             // 작성자 번호
    private String noticeTitle;    // 공지사항 글제목
    private String noticeContent;  // 내용
    private int isMandatory;       // 필독 여부 (1=필독, 0=일반)
    private String category;       // 공지 분류
    private int viewCount;         // 조회수
    private Date createDate;       // 생성일
    private Date updateDate;       // 수정일
    private String status;         // 활성 상태 (Y/N)
    
    // 추가 필드 (조인용)
    private String noticeWriter;   // 작성자 이름
    private String deptName;       // 작성자 부서명
}