package com.kh.flokrGroupware.facility.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.facility.model.dao.FacilityDao;
import com.kh.flokrGroupware.facility.model.vo.Facility;

/**
 * 시설 및 예약 관리 서비스 구현 클래스
 */
@Service
public class FacilityServiceImpl implements FacilityService {
    
    @Autowired
    private FacilityDao facilityDao;
    
    // 시설 관련 기능 구현
    
    @Override
    public List<Facility> selectFacilityList() {
        return facilityDao.selectFacilityList();
    }
    
    @Override
    public Facility selectFacility(int facilityNo) {
        return facilityDao.selectFacility(facilityNo);
    }
    
    @Override
    public List<Facility> searchFacilities(String keyword) {
        return facilityDao.searchFacilities(keyword);
    }
    
    @Override
    public int insertFacility(Facility facility) {
        return facilityDao.insertFacility(facility);
    }
    
    @Override
    public int updateFacility(Facility facility) {
        return facilityDao.updateFacility(facility);
    }
    
    @Override
    public int deleteFacility(int facilityNo) {
        return facilityDao.deleteFacility(facilityNo);
    }
    
    @Override
    public List<Facility> selectActiveFacilityList() {
        return facilityDao.selectActiveFacilityList();
    }
    
    @Override
    public List<Facility> selectAvailableFacilities(Map<String, Object> params) {
        return facilityDao.selectAvailableFacilities(params);
    }
    
    // 예약 관련 기능 구현
    
    @Override
    public List<Map<String, Object>> selectRecentReservations(int days) {
        return facilityDao.selectRecentReservations(days);
    }
    
    @Override
    public List<Map<String, Object>> selectReservationsByCondition(Map<String, Object> params) {
        return facilityDao.selectReservationsByCondition(params);
    }
    
    @Override
    public Map<String, Object> selectReservationDetail(int reservationNo) {
        return facilityDao.selectReservationDetail(reservationNo);
    }
    
    @Override
    public int getReservationCountByFacility(int facilityNo) {
        return facilityDao.getReservationCountByFacility(facilityNo);
    }
    
    @Override
    public int updateReservationStatus(Map<String, Object> params) {
        return facilityDao.updateReservationStatus(params);
    }
    
    @Override
    public boolean checkReservationAvailability(Map<String, Object> params) {
        int count = facilityDao.checkReservationOverlap(params);
        return count == 0; // 중복된 예약이 없으면 true 반환
    }
    
    @Override
    public int insertReservation(Map<String, Object> params) {
        return facilityDao.insertReservation(params);
    }
    
    @Override
    public List<Map<String, Object>> selectMyReservations(int empNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        return facilityDao.selectMyReservations(params);
    }
    
    @Override
    public List<Map<String, Object>> selectMyReservations(Map<String, Object> params) {
        return facilityDao.selectMyReservations(params);
    }
    
    @Override
    public int updateReservation(Map<String, Object> params) {
        int result = facilityDao.updateReservation(params);
        
        // 승인 대기 상태로 재설정이 필요한 경우
        if (result > 0 && Boolean.TRUE.equals(params.get("resetApprovalStatus"))) {
            int reservationNo = Integer.parseInt(params.get("reservationNo").toString());
            facilityDao.resetReservationApprovalStatus(reservationNo);
        }
        
        return result;
    }

    @Override
    public int resetReservationApprovalStatus(int reservationNo) {
        return facilityDao.resetReservationApprovalStatus(reservationNo);
    }

    @Override
    public boolean checkReservationAvailabilityExcludeSelf(Map<String, Object> params) {
        if (params.containsKey("excludeReservationNo")) {
            int count = facilityDao.checkReservationOverlapExcludeSelf(params);
            return count == 0; // 중복된 예약이 없으면 true 반환
        } else {
            // excludeReservationNo가 없으면 일반 중복 체크
            return checkReservationAvailability(params);
        }
    }
}