package com.kh.flokrGroupware.facility.model.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.facility.model.vo.Facility;

@Repository
public class FacilityDao {
    
    @Autowired
    private SqlSessionTemplate sqlSession;
    
    // * 시설 관련
    
    // 모든 시설 목록 조회
    public List<Facility> selectFacilityList() {
        return sqlSession.selectList("facilityMapper.selectFacilityList");
    }
    
    // 특정 시설 정보 조회
    public Facility selectFacility(int facilityNo) {
        return sqlSession.selectOne("facilityMapper.selectFacility", facilityNo);
    }
    
    // 시설 검색
    public List<Facility> searchFacilities(String keyword) {
        return sqlSession.selectList("facilityMapper.searchFacilities", keyword);
    }
    
    // 시설 추가
    public int insertFacility(Facility facility) {
        return sqlSession.insert("facilityMapper.insertFacility", facility);
    }
    
    // 시설 정보 수정
    public int updateFacility(Facility facility) {
        return sqlSession.update("facilityMapper.updateFacility", facility);
    }
    
    // 시설 삭제
    public int deleteFacility(int facilityNo) {
        return sqlSession.delete("facilityMapper.deleteFacility", facilityNo);
    }
    
    // 활성화된 시설 목록 조회
    public List<Facility> selectActiveFacilityList() {
        return sqlSession.selectList("facilityMapper.selectActiveFacilityList");
    }
    
    // 조건에 따른 사용 가능한 시설 목록 조회
    public List<Facility> selectAvailableFacilities(Map<String, Object> params) {
        return sqlSession.selectList("facilityMapper.selectAvailableFacilities", params);
    }
    
    // * 예약 관련
    
    // 최근 예약 목록 조회
    public List<Map<String, Object>> selectRecentReservations(int days) {
        return sqlSession.selectList("facilityMapper.selectRecentReservations", days);
    }
    
    // 조건별 예약 목록 조회
    public List<Map<String, Object>> selectReservationsByCondition(Map<String, Object> params) {
        return sqlSession.selectList("facilityMapper.selectReservationsByCondition", params);
    }
    
    // 예약 상세 정보 조회
    public Map<String, Object> selectReservationDetail(int reservationNo) {
        return sqlSession.selectOne("facilityMapper.selectReservationDetail", reservationNo);
    }
    
    // 시설별 예약 수 조회
    public int getReservationCountByFacility(int facilityNo) {
        return sqlSession.selectOne("facilityMapper.getReservationCountByFacility", facilityNo);
    }
    
    // 예약 상태 변경
    public int updateReservationStatus(Map<String, Object> params) {
        return sqlSession.update("facilityMapper.updateReservationStatus", params);
    }
    
    // 예약 중복 체크
    public int checkReservationOverlap(Map<String, Object> params) {
        return sqlSession.selectOne("facilityMapper.checkReservationOverlap", params);
    }
    
    // 예약 생성
    public int insertReservation(Map<String, Object> params) {
        return sqlSession.insert("facilityMapper.insertReservation", params);
    }
    
    // 사용자별 예약 목록 조회
    public List<Map<String, Object>> selectMyReservations(Map<String, Object> params) {
        return sqlSession.selectList("facilityMapper.selectMyReservations", params);
    }
    
    // 예약 수정
    public int updateReservation(Map<String, Object> params) {
        return sqlSession.update("facilityMapper.updateReservation", params);
    }
    
	// 승인 대기 상태로 재설정
    public int resetReservationApprovalStatus(int reservationNo) {
        return sqlSession.update("facilityMapper.resetReservationApprovalStatus", reservationNo);
    }

    // 자신의 예약 제외 중복 체크
    public int checkReservationOverlapExcludeSelf(Map<String, Object> params) {
        return sqlSession.selectOne("facilityMapper.checkReservationOverlapExcludeSelf", params);
    }
    
}