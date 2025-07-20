package com.kh.flokrGroupware.approval.model.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalForm;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;
import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.employee.model.vo.Employee;

/**
 * 전자결재 서비스 인터페이스
 * 결재 관련 비즈니스 로직을 정의
 */
public interface ApprovalService {
	
	// *양식관련
	// 활성화된 모든 결재 양식 목록 조회
	ArrayList<ApprovalForm> selectAllActiveForms();
	
	// 양식 번호로 양식 정보 조회
	ApprovalForm selectFormByNo(int formNo);
	
	// *문서관련
	// 새문서 생성
	int insertDocument(ApprovalDoc document);
	
	// 문서 번호로 문서 상세 조회
	ApprovalDoc selectDocumentByNo(int docNo);
	
	// 문서 수정
	int updateDocument(ApprovalDoc document);
	
	// *결재선 관련
	// 결재선 생성
	int insertApprovalLines(ArrayList<ApprovalLine> lines);
	
	// 문서 번호로 결재선 목록 조회
	ArrayList<ApprovalLine> selectApprovalLineByDocNo(int docNo);
	
	// 결재선 항목 수정
	int updateApprovalLine(ApprovalLine line);
	
	// 첫번째 결재자 상태 변경
	int updateFirstApproverStatus(int docNo);
	
	// 결재선 초기화(반려 후 재요청시)
	int resetApprovalLines(int docNo);
	
	// 결재선 번호로 결재서 항목 조회
	ApprovalLine selectApprovalLineByNo(int lineNo);
	
	// *문서함 관련
	// 임시저장함 조회
	ArrayList<ApprovalDoc> selectDraftDocuments(int empNo);
	
	// 상신 문서함 조회
	ArrayList<ApprovalDoc> selectRequestedDocuments(int empNo);
	
	// 수신 문서함 조회
	ArrayList<ApprovalDoc> selectWaitingDocuments(int empNo);
	
	// 완료 문서함 조회
	ArrayList<ApprovalDoc> selectCompletedDocuments(int empNo);
	
	// ====================== 첨부파일 관련 ======================
	
	// 첨부파일 등록
	int insertAttachment(Attachment file);
	
	// 문서번호로 첨부파일 조회
	Attachment selectAttachmentByDocNo(int docNo);
	
	// 첨부파일 번호로 조회
	Attachment selectAttachmentByNo(int attachmentNo);
	
	// 기존 첨부파일 삭제
	int deleteAttachmentByDocNo(int docNo);
	
	// ====================== 페이징 처리된 문서함 관련 ======================
	
	// 페이징된 임시저장함 조회
	HashMap<String, Object> selectDraftDocumentsPaging(int empNo, int currentPage);
	
	// 페이징된 상신문서함 조회
	HashMap<String, Object> selectRequestedDocumentsPaging(int empNo, int currentPage);
	
	// 페이징된 수신문서함 조회
	HashMap<String, Object> selectWaitingDocumentsPaging(int empNo, int currentPage);
	
	// 페이징된 완료문서함 조회
	HashMap<String, Object> selectCompletedDocumentsPaging(int empNo, int currentPage);
	
	// ====================== 검색 기능 ======================
	
	/**
	 * 검색 조건을 포함한 페이징된 임시저장함 조회
	 * @param empNo 사원번호
	 * @param currentPage 현재 페이지
	 * @param searchType 검색 타입
	 * @param keyword 검색어
	 * @param dateFrom 시작일
	 * @param dateTo 종료일
	 * @return Map<String, Object> (문서 목록과 페이징 정보)
	 */
	HashMap<String, Object> selectDraftDocumentsSearch(int empNo, int currentPage, String searchType, String keyword, String dateFrom, String dateTo);
	
	// ====================== 사원 조회 관련 (메소드명 수정) ======================
	
	// 결재선 설정을 위한 사원 목록 조회
	ArrayList<Employee> selectEmployeesForApprovalLine();
	
	// 검색 조건으로 사원 조회
	ArrayList<Employee> searchEmployeesForApprovalLine(String searchKeyword);
	
	// ====================== 통계 조회 관련 ======================
	/**
     * 결재 대기 건수 조회
     * @param empNo 사원번호
     * @return 대기 건수
     */
    int selectWaitingCount(int empNo);
    
    /**
     * 진행 중 결재 건수 조회
     * @param empNo 사원번호
     * @return 진행 중 건수 
     */
    int selectProcessingCount(int empNo);
    
    /**
     * 결재 완료 건수 조회
     * @param empNo 사원번호
     * @return 완료 건수
     */
    int selectApprovedCount(int empNo);
    
    /**
     * 결재 반려 건수 조회
     * @param empNo 사원번호
     * @return 반려 건수
     */
    int selectRejectedCount(int empNo);
    
    // ====================== 최근 문서 조회 관련 ======================
    
    /**
     * 수신 문서함 최근 문서 조회
     * @param empNo 사원번호
     * @param limit 조회 건수
     * @return 최근 수신 문서 목록
     */
    ArrayList<ApprovalDoc> selectRecentWaitingDocuments(int empNo, int limit);
    
    /**
     * 내가 올린 문서 최근 조회
     * @param empNo 사원번호
     * @param limit 조회 건수
     * @return 최근 내 문서 목록
     */
    ArrayList<ApprovalDoc> selectRecentMyDocuments(int empNo, int limit);
    
    /**
     * 임시저장함 최근 문서 조회
     * @param empNo 사원번호
     * @param limit 조회 건수
     * @return 최근 임시저장 문서 목록
     */
    ArrayList<ApprovalDoc> selectRecentDraftDocuments(int empNo, int limit);
	
    /**
     * 완료 문서함 최근 문서 조회
     * @param empNo 사원번호
     * @param limit 조회 개수
     * @return 완료 문서 목록
     */
    ArrayList<ApprovalDoc> selectRecentCompletedDocuments(int empNo, int limit);
    
    /**
     * 문서 승인 처리
     * @param currentLine 승인할 결재선 정보
     * @return 처리 결과
     */
    int approveDocument(ApprovalLine currentLine);
    
    /**
     * 문서 반려 처리
     * @param currentLine 반려할 결재선 정보
     * @return 처리 결과
     */
    int rejectDocument(ApprovalLine currentLine);
    
    /**
     * 부서별 직원 목록 조회
     * @param deptNo 부서 번호
     * @return 해당 부서 직원 목록
     */
    ArrayList<Employee> selectEmployeesByDept(int deptNo);
    
    /**
     * 모든 부서 목록 조회
     * @return 부서 목록 (부서번호, 부서명)
     */
    ArrayList<HashMap<String, Object>> selectAllDepartments();
    
    /**
     * 결재선 완전 삭제 (문서 수정 시 사용)
     * @param docNo 문서번호
     * @return 처리 결과
     */
    int deleteApprovalLinesByDocNo(int docNo);
    
    /**
     * 공통 검색 기능: 각 문서함 검색
     * @param boxType 문서함 타입 (draft, requested, waiting, completed)
     * @param empNo 사원번호
     * @param currentPage 현재 페이지
     * @param searchType 검색 타입 (title, form, drafter 등)
     * @param keyword 검색어
     * @param dateFrom 시작일
     * @param dateTo 종료일
     * @param statusFilter 상태 필터 (DRAFT, REQUESTED, APPROVED, REJECTED 등)
     * @return Map<String, Object> (문서 목록과 페이징 정보)
     */
    HashMap<String, Object> searchDocuments(String boxType, int empNo, int currentPage, 
            String searchType, String keyword, String dateFrom, String dateTo, String statusFilter);
    
    /**
     * 결재자 관점의 승인 문서 건수 조회
     * 현재 로그인한 사용자가 결재자로서 승인한 문서의 개수를 반환
     * 
     * @param empNo 조회할 사원 번호 (결재자)
     * @return 해당 사원이 결재자로서 승인한 문서의 개수
     */
    int selectApprovedCountByApprover(int empNo);

    /**
     * 결재자 관점의 반려 문서 건수 조회
     * 현재 로그인한 사용자가 결재자로서 반려한 문서의 개수를 반환
     * 
     * @param empNo 조회할 사원 번호 (결재자)
     * @return 해당 사원이 결재자로서 반려한 문서의 개수
     */
    int selectRejectedCountByApprover(int empNo);
    
    /**
     * 특정 사용자의 수신 문서함 캐시를 명시적으로 갱신
     * Redis 캐시를 효과적으로 관리하기 위한 유틸리티 메서드
     * 
     * @param empNo 사원 번호
     */
    void refreshWaitingDocumentsForUser(int empNo);
    
    /**
     * 처리 효율성 지표 계산
     * @param empNo 사원번호
     * @param statusFilter 상태 필터
     * @param dateFrom 시작일
     * @param dateTo 종료일
     * @return 처리 효율성 정보 (상태, 비율)
     */
    HashMap<String, Object> getProcessingEfficiency(int empNo, String statusFilter, 
                                                  String dateFrom, String dateTo);

    /**
     * 처리 효율성 지표 캐시 무효화
     */
    void refreshProcessingEfficiencyCache();
    
}



	


