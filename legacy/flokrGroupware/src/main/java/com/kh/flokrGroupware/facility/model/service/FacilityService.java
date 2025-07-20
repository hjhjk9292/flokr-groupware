package com.kh.flokrGroupware.facility.model.service;

import java.util.List;
import java.util.Map;

import com.kh.flokrGroupware.facility.model.vo.Facility;

/**
 * 시설 및 예약 관리 서비스 인터페이스
 */
public interface FacilityService {
    
    // * 시설 관련 기능
    
    // 모든 시설 목록 조회
    List<Facility> selectFacilityList();
    
    // 특정 시설 정보 조회
    Facility selectFacility(int facilityNo);
    
    // 시설 검색
    List<Facility> searchFacilities(String keyword);
    
    // 시설 추가
    int insertFacility(Facility facility);
    
    // 시설 정보 수정
    int updateFacility(Facility facility);
    
    // 시설 삭제
    int deleteFacility(int facilityNo);
    
    // 사용 가능한 시설 목록 조회
    List<Facility> selectActiveFacilityList();
    
    // 조건에 따른 사용 가능한 시설 목록 조회
    List<Facility> selectAvailableFacilities(Map<String, Object> params);
    
    // * 예약 관련 기능
    
    // 최근 예약 목록 조회
    List<Map<String, Object>> selectRecentReservations(int days);
    
    // 조건별 예약 목록 조회
    List<Map<String, Object>> selectReservationsByCondition(Map<String, Object> params);
    
    // 예약 상세 정보 조회
    Map<String, Object> selectReservationDetail(int reservationNo);
    
    // 시설별 예약 수 조회
    int getReservationCountByFacility(int facilityNo);
    
    // 예약 상태 변경
    int updateReservationStatus(Map<String, Object> params);
    
    // 예약 가능 여부 확인 (시간 중복 체크)
    boolean checkReservationAvailability(Map<String, Object> params);
    
    // 예약 생성
    int insertReservation(Map<String, Object> params);
    
    // 특정 사용자의 예약 목록 조회
    List<Map<String, Object>> selectMyReservations(int empNo);
    
    // 조건별 사용자 예약 목록 조회
    List<Map<String, Object>> selectMyReservations(Map<String, Object> params);
    
	// 예약 수정
    int updateReservation(Map<String, Object> params);
    
	// 승인 대기 상태로 재설정
    int resetReservationApprovalStatus(int reservationNo);
    
    // 자신의 예약 제외 예약 가능 여부 확인
    boolean checkReservationAvailabilityExcludeSelf(Map<String, Object> params);
}