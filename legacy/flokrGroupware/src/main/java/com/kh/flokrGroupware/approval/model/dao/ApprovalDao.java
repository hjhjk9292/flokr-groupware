package com.kh.flokrGroupware.approval.model.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalForm;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;
import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.employee.model.vo.Employee;

/**
 * 전자결재 DAO 클래스
 * MyBatis를 통해 DB와 통신
 */
@Repository
public class ApprovalDao {
	
	// ====================== 양식 관련 메소드 ======================
	
	/**
	 * 활성화된 모든 결재 양식 조회
	 * @param sqlSession 객체
	 * @return 활성화된 양식 목록
	 */
	public ArrayList<ApprovalForm> selectAllActiveForms(SqlSessionTemplate sqlSession){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectAllActiveForms");
	}
	
	/**
	 * 양식 번호로 특정 양식 조회
	 * @param sqlSession 객체
	 * @param formNo 조회할 양식 번호
	 * @return 양식 정보 or null
	 */
	public ApprovalForm selectFormByNo(SqlSessionTemplate sqlSession, int formNo) {
		return sqlSession.selectOne("approvalMapper.selectFormByNo", formNo);
	}
	
	// ====================== 문서 관련 메소드 ======================
	
	/**
	 * 새로운 문서 등록
	 * @param sqlSession 객체
	 * @param document 등록할 문서 정보
	 * @return 성공시 1, 실패시 0
	 */
	public int insertDocument(SqlSessionTemplate sqlSession, ApprovalDoc document) {
		return sqlSession.insert("approvalMapper.insertDocument", document);
	}
	
	/**
	 * 문서 번호로 특정 문서 조회
	 * @param sqlSession 객체
	 * @param docNo 조회할 문서 번호
	 * @return 문서정보 or null
	 */
	public ApprovalDoc selectDocumentByNo(SqlSessionTemplate sqlSession, int docNo) {
		return sqlSession.selectOne("approvalMapper.selectDocumentByNo", docNo);
	}
	
	/**
	 * 문서정보 수정
	 * @param sqlSession 객체
	 * @param document 수정할 문서정보
	 * @return 성공시 1, 실패시 0
	 */
	public int updateDocument(SqlSessionTemplate sqlSession, ApprovalDoc document) {
		return sqlSession.update("approvalMapper.updateDocument", document);
	}
	
	// ====================== 결재선 관련 메소드 ======================
	
	/**
	 * 단일 결재선 등록
	 * @param sqlSession 객체
	 * @param line 등록할 결재선 정보
	 * @return 성공시 1, 실패시 0
	 */
	public int insertApprovalLine(SqlSessionTemplate sqlSession, ApprovalLine line) {
		return sqlSession.insert("approvalMapper.insertApprovalLine", line);
	}
	
	/**
	 * 문서번호로 결재선 목록 조회
	 * @param sqlSession 객체
	 * @param docNo 문서번호
	 * @return 결재선 목록
	 */
	public ArrayList<ApprovalLine> selectApprovalLineByDocNo(SqlSessionTemplate sqlSession, int docNo){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectApprovalLineByDocNo", docNo);
	}
	
	/**
	 * 결재선 정보 수정
	 * @param sqlSession 객체
	 * @param line 수정할 결재선 정보
	 * @return 성공시 1, 실패시 0
	 */
	public int updateApprovalLine(SqlSessionTemplate sqlSession, ApprovalLine line) {
		return sqlSession.update("approvalMapper.updateApprovalLine", line);
	}
	
	/**
	 * 첫번째 결재자 상태를 WAITING으로 변경
	 * @param sqlSession 객체
	 * @param docNo 문서번호
	 * @return 성공시 1, 실패시 0
	 */
	public int updateFirstApproverStatus(SqlSessionTemplate sqlSession, int docNo) {
		return sqlSession.update("approvalMapper.updateFirstApproverStatus", docNo);
	}
	
	/**
	 *  결재선 초기화(반려 후 재요청 시)
	 * @param sqlSession 객체
	 * @param docNo 문서번호
	 * @return 성공시 초기화된 개수
	 */
	public int resetApprovalLines(SqlSessionTemplate sqlSession, int docNo) {
		return sqlSession.update("approvalMapper.resetApprovalLines", docNo);
	}
	
	/**
	 * 결재선 번호로 특정 결재선 조회
	 * @param sqlSession 객체
	 * @param lineNo 결재선 번호
	 * @return 결재선 정보 or null
	 */
	public ApprovalLine selectApprovalLineByNo(SqlSessionTemplate sqlSession, int lineNo) {
		return sqlSession.selectOne("approvalMapper.selectApprovalLineByNo", lineNo);
	}
	
	/**
	 * 결재선 완전 삭제 (문서 수정 시 사용)
	 * @param sqlSession 세션
	 * @param docNo 문서번호
	 * @return 삭제된 행 수
	 */
	public int deleteApprovalLinesByDocNo(SqlSessionTemplate sqlSession, int docNo) {
	    return sqlSession.delete("approvalMapper.deleteApprovalLinesByDocNo", docNo);
	}
	
	// ====================== 문서함 관련 메서드 ======================
	
	/**
	 * 임시저장함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 임시저장 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectDraftDocuments(SqlSessionTemplate sqlSession, int empNo){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectDraftDocuments", empNo);
	}
	
	/**
	 * 상신문서함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 상신 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectRequestedDocuments(SqlSessionTemplate sqlSession, int empNo){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectRequestedDocuments", empNo);
	}
	
	/**
	 * 수신문서함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 수신 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectWaitingDocuments(SqlSessionTemplate sqlSession, int empNo){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectWaitingDocuments", empNo);
	}
	
	/**
	 * 완료문서함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 완료 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectCompletedDocuments(SqlSessionTemplate sqlSession, int empNo){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectCompletedDocuments", empNo);
	}
	
	// ====================== 사원 조회 관련 메소드 ======================
	
	/**
	 * 결재선 설정을 위한 사원 목록 조회
	 * @param sqlSession 객체
	 * @return 사원 목록
	 */
	public ArrayList<Employee> selectEmployeesForApprovalLine(SqlSessionTemplate sqlSession){
		return (ArrayList)sqlSession.selectList("approvalMapper.selectEmployeesForApprovalLine");
	}
	
	/**
	 * 검색 조건으로 사원 조회
	 * @param sqlSession 객체
	 * @param searchKeyword 검색 키워드
	 * @return 검색된 사원 목록
	 */
	public ArrayList<Employee> searchEmployeesForApprovalLine(SqlSessionTemplate sqlSession, String searchKeyword){
		return (ArrayList)sqlSession.selectList("approvalMapper.searchEmployeesForApprovalLine", searchKeyword);
	}
	
	// ====================== 첨부파일 관련 메서드 ======================
	
	/**
	 * 첨부파일 등록
	 * @param sqlSession 객체
	 * @param file 첨부파일 정보
	 * @return 성공시 1, 실패시 0
	 */
	public int insertAttachment(SqlSessionTemplate sqlSession, Attachment file) {
		return sqlSession.insert("approvalMapper.insertAttachment", file);
	}
	
	/**
	 * 문서번호로 첨부파일 조회 (단일 파일)
	 * @param sqlSession 객체
	 * @param docNo 문서번호
	 * @return 첨부파일 정보 or null
	 */
	public Attachment selectAttachmentByDocNo(SqlSessionTemplate sqlSession, int docNo) {
		return sqlSession.selectOne("approvalMapper.selectAttachmentByDocNo", docNo);
	}
	
	/**
	 * 첨부파일 번호로 특정 첨부파일 조회
	 * @param sqlSession 객체
	 * @param attachmentNo 첨부파일 번호
	 * @return 첨부파일 정보
	 */
	public Attachment selectAttachmentByNo(SqlSessionTemplate sqlSession, int attachmentNo) {
		return sqlSession.selectOne("approvalMapper.selectAttachmentByNo", attachmentNo);
	}
	
	/**
	 * 기존 첨부파일 삭제 (새 파일 업로드 시 기존 파일 교체용)
	 * @param sqlSession 객체
	 * @param docNo 문서번호
	 * @return 성공시 1, 실패시 0
	 */
	public int deleteAttachmentByDocNo(SqlSessionTemplate sqlSession, int docNo) {
		return sqlSession.update("approvalMapper.deleteAttachmentByDocNo", docNo);
	}
	
	// ====================== 페이징 처리 추가 메소드 ======================
	
	/**
	 * 임시저장함 전체 개수 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 전체 개수
	 */
	public int selectDraftDocumentsCount(SqlSessionTemplate sqlSession, int empNo) {
		return sqlSession.selectOne("approvalMapper.selectDraftDocumentsCount", empNo);
	}
	
	/**
	 * 페이징된 임시저장함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @param pageInfo 페이징 정보
	 * @return 임시저장 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectDraftDocumentsPaging(SqlSessionTemplate sqlSession, int empNo, PageInfo pageInfo) {
		int offset = (pageInfo.getCurrentPage() - 1) * pageInfo.getBoardLimit();
		return (ArrayList)sqlSession.selectList("approvalMapper.selectDraftDocuments", empNo, 
				new RowBounds(offset, pageInfo.getBoardLimit()));
	}
	
	/**
	 * 상신문서함 전체 개수 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 전체 개수
	 */
	public int selectRequestedDocumentsCount(SqlSessionTemplate sqlSession, int empNo) {
		return sqlSession.selectOne("approvalMapper.selectRequestedDocumentsCount", empNo);
	}
	
	/**
	 * 페이징된 상신문서함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @param pageInfo 페이징 정보
	 * @return 상신 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectRequestedDocumentsPaging(SqlSessionTemplate sqlSession, int empNo, PageInfo pageInfo) {
		int offset = (pageInfo.getCurrentPage() - 1) * pageInfo.getBoardLimit();
		return (ArrayList)sqlSession.selectList("approvalMapper.selectRequestedDocuments", empNo, 
				new RowBounds(offset, pageInfo.getBoardLimit()));
	}
	
	/**
	 * 수신문서함 전체 개수 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 전체 개수
	 */
	public int selectWaitingDocumentsCount(SqlSessionTemplate sqlSession, int empNo) {
		return sqlSession.selectOne("approvalMapper.selectWaitingDocumentsCount", empNo);
	}
	
	/**
	 * 페이징된 수신문서함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @param pageInfo 페이징 정보
	 * @return 수신 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectWaitingDocumentsPaging(SqlSessionTemplate sqlSession, int empNo, PageInfo pageInfo) {
		int offset = (pageInfo.getCurrentPage() - 1) * pageInfo.getBoardLimit();
		return (ArrayList)sqlSession.selectList("approvalMapper.selectWaitingDocuments", empNo, 
				new RowBounds(offset, pageInfo.getBoardLimit()));
	}
	
	/**
	 * 완료문서함 전체 개수 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @return 전체 개수
	 */
	public int selectCompletedDocumentsCount(SqlSessionTemplate sqlSession, int empNo) {
		return sqlSession.selectOne("approvalMapper.selectCompletedDocumentsCount", empNo);
	}
	
	/**
	 * 페이징된 완료문서함 조회
	 * @param sqlSession 객체
	 * @param empNo 사원번호
	 * @param pageInfo 페이징 정보
	 * @return 완료 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectCompletedDocumentsPaging(SqlSessionTemplate sqlSession, int empNo, PageInfo pageInfo) {
		int offset = (pageInfo.getCurrentPage() - 1) * pageInfo.getBoardLimit();
		return (ArrayList)sqlSession.selectList("approvalMapper.selectCompletedDocuments", empNo, 
				new RowBounds(offset, pageInfo.getBoardLimit()));
	}
	
	// ====================== 검색 기능 추가 메소드 ======================
	
	/**
	 * 검색 조건을 포함한 임시저장함 전체 개수 조회
	 * @param sqlSession 객체
	 * @param params 검색 파라미터 (empNo, searchType, keyword, dateFrom, dateTo)
	 * @return 전체 개수
	 */
	public int selectDraftDocumentsSearchCount(SqlSessionTemplate sqlSession, HashMap<String, Object> params) {
		return sqlSession.selectOne("approvalMapper.selectDraftDocumentsSearchCount", params);
	}
	
	/**
	 * 검색 조건을 포함한 페이징된 임시저장함 조회
	 * @param sqlSession 객체
	 * @param params 검색 파라미터 (empNo, searchType, keyword, dateFrom, dateTo)
	 * @param pageInfo 페이징 정보
	 * @return 임시저장 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectDraftDocumentsSearchPaging(SqlSessionTemplate sqlSession, HashMap<String, Object> params, PageInfo pageInfo) {
		int offset = (pageInfo.getCurrentPage() - 1) * pageInfo.getBoardLimit();
		return (ArrayList)sqlSession.selectList("approvalMapper.selectDraftDocumentsSearch", params, 
				new RowBounds(offset, pageInfo.getBoardLimit()));
	}
	
	/**
	 * 검색 조건을 포함한 문서 개수 조회
	 * @param sqlSession 객체
	 * @param params 검색 파라미터
	 * @return 전체 개수
	 */
	public int selectDocumentsSearchCount(SqlSessionTemplate sqlSession, HashMap<String, Object> params) {
	    return sqlSession.selectOne("approvalMapper.selectDocumentsSearchCount", params);
	}

	/**
	 * 검색 조건을 포함한 페이징된 문서 조회
	 * @param sqlSession 객체
	 * @param params 검색 파라미터
	 * @param pageInfo 페이징 정보
	 * @return 문서 목록
	 */
	public ArrayList<ApprovalDoc> selectDocumentsSearchPaging(SqlSessionTemplate sqlSession, HashMap<String, Object> params, PageInfo pageInfo) {
	    int offset = (pageInfo.getCurrentPage() - 1) * pageInfo.getBoardLimit();
	    return (ArrayList)sqlSession.selectList("approvalMapper.selectDocumentsSearch", params, 
	            new RowBounds(offset, pageInfo.getBoardLimit()));
	}
	
	// ====================== 통계 조회 관련 메서드 ======================
	/**
     * 결재 대기 건수 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @return 대기 건수
     */
    public int selectWaitingCount(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("approvalMapper.selectWaitingCount", empNo);
    }
    
    /**
     * 진행 중 결재 건수 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @return 진행 중 건수
     */
    public int selectProcessingCount(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("approvalMapper.selectProcessingCount", empNo);
    }
    
    /**
     * 결재 완료 건수 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @return 완료 건수
     */
    public int selectApprovedCount(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("approvalMapper.selectApprovedCount", empNo);
    }
    
    /**
     * 결재 반려 건수 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @return 반려 건수
     */
    public int selectRejectedCount(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("approvalMapper.selectRejectedCount", empNo);
    }
    
    /**
     * 결재자 관점의 승인 문서 건수 조회
     * 현재 로그인한 사용자가 결재자로서 승인한 문서의 개수를 반환
     * @param sqlSession MyBatis SQL 세션 객체
     * @param empNo 조회할 사원 번호 (결재자)
     * @return 해당 사원이 결재자로서 승인한 문서의 개수
     */
    public int selectApprovedCountByApprover(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("approvalMapper.selectApprovedCountByApprover", empNo);
    }

    /**
     * 결재자 관점의 반려 문서 건수 조회
     * 현재 로그인한 사용자가 결재자로서 반려한 문서의 개수를 반환
     * @param sqlSession MyBatis SQL 세션 객체
     * @param empNo 조회할 사원 번호 (결재자)
     * @return 해당 사원이 결재자로서 반려한 문서의 개수
     */
    public int selectRejectedCountByApprover(SqlSessionTemplate sqlSession, int empNo) {
        return sqlSession.selectOne("approvalMapper.selectRejectedCountByApprover", empNo);
    }
    
    // ====================== 최근 문서 조회 관련 메서드 ======================
    
    /**
     * 수신 문서함 최근 문서 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @param limit 조회 건수
     * @return 최근 수신 문서 목록
     */
    public ArrayList<ApprovalDoc> selectRecentWaitingDocuments(SqlSessionTemplate sqlSession, int empNo, int limit) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        params.put("limit", limit);
        return (ArrayList)sqlSession.selectList("approvalMapper.selectRecentWaitingDocuments", params);
    }
    
    /**
     * 내가 올린 문서 최근 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @param limit 조회 건수
     * @return 최근 내 문서 목록
     */
    public ArrayList<ApprovalDoc> selectRecentMyDocuments(SqlSessionTemplate sqlSession, int empNo, int limit) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        params.put("limit", limit);
        return (ArrayList)sqlSession.selectList("approvalMapper.selectRecentMyDocuments", params);
    }
    
    /**
     * 임시저장함 최근 문서 조회
     * @param sqlSession 객체
     * @param empNo 사원번호
     * @param limit 조회 건수
     * @return 최근 임시저장 문서 목록
     */
    public ArrayList<ApprovalDoc> selectRecentDraftDocuments(SqlSessionTemplate sqlSession, int empNo, int limit) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        params.put("limit", limit);
        return (ArrayList)sqlSession.selectList("approvalMapper.selectRecentDraftDocuments", params);
    }
    
    /**
     * 완료 문서함 최근 문서 조회
     * @param params 조회 조건 (사원번호, 제한 개수)
     * @return 완료 문서 목록
     */
    public ArrayList<ApprovalDoc> selectRecentCompletedDocuments(SqlSessionTemplate sqlSession, HashMap<String, Object> params) {
        return (ArrayList)sqlSession.selectList("approvalMapper.selectRecentCompletedDocuments", params);
    }
    
    /**
     * 부서별 직원 목록 조회
     * @param sqlSession 객체
     * @param deptNo 부서 번호
     * @return 해당 부서 직원 목록
     */
    public ArrayList<Employee> selectEmployeesByDept(SqlSessionTemplate sqlSession, int deptNo) {
        return (ArrayList)sqlSession.selectList("approvalMapper.selectEmployeesByDept", deptNo);
    }
    
    /**
     * 모든 부서 목록 조회
     * @param sqlSession SQL 세션
     * @return 부서 목록 (부서번호, 부서명)
     */
    public ArrayList<HashMap<String, Object>> selectAllDepartments(SqlSessionTemplate sqlSession) {
        return (ArrayList)sqlSession.selectList("approvalMapper.selectAllDepartments");
    }

}
