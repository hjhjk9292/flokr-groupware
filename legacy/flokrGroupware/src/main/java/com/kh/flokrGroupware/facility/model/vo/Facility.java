package com.kh.flokrGroupware.facility.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Facility {
    private int facilityNo;            // 시설 번호
    private String facilityName;       // 시설명
    private String facilityLocation;   // 시설 위치
    private int capacity;              // 수용 인원
    private String description;        // 설명
    private String imagePath;          // 이미지 경로
    private String facilityStatus;     // 'APPROVED', 'PENDING', 'CANCELED' 상태값
    private String facilityType;       // 가상 필드: 'MEETING_ROOM', 'EQUIPMENT', 'VEHICLE' 유형값 
    private Date createDate;           // 생성일
    private Date updateDate;           // 수정일
}