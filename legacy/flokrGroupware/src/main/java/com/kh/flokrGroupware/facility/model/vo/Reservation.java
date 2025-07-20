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
public class Reservation {
    private int reservationNo;     // 예약 번호
    private int facilityNo;        // 시설 번호
    private int reserverEmpNo;     // 예약자 사번
    private Date startTime;        // 예약 시작 시간
    private Date endTime;          // 예약 종료 시간
    private String purpose;        // 예약 목적
    private Date createDate;       // 생성일
    private Date updateDate;       // 수정일
    private String resStatus;      // 예약 상태: 'APPROVED', 'PENDING', 'CANCELED'
}