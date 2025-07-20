package com.kh.flokrGroupware.approval.model.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.flokrGroupware.approval.model.dao.ApprovalDao;
import com.kh.flokrGroupware.approval.model.vo.ApprovalDoc;
import com.kh.flokrGroupware.approval.model.vo.ApprovalForm;
import com.kh.flokrGroupware.approval.model.vo.ApprovalLine;
import com.kh.flokrGroupware.attachment.model.vo.Attachment;
import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.common.template.Pagination;
import com.kh.flokrGroupware.employee.model.vo.Employee;

/**
 * 전자결재 서비스 구현 클래스
 * 인터페이스에 정의된 메소드를 실제로 구현
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {
	
	@Autowired
	private ApprovalDao aDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Autowired  // CacheManager 주입
	private CacheManager cacheManager;
	
	// 양식 관련 메소드 구현

	@Override
	public ArrayList<ApprovalForm> selectAllActiveForms() {
		return aDao.selectAllActiveForms(sqlSession);
	}

	@Override
	public ApprovalForm selectFormByNo(int formNo) {
		return aDao.selectFormByNo(sqlSession, formNo);
	}
	
	// 문서 관련 메소드 구현

	@Override
	@Transactional // 트랜젝션 처리
	public int insertDocument(ApprovalDoc document) {
		return aDao.insertDocument(sqlSession, document);
	}

	@Override
	public ApprovalDoc selectDocumentByNo(int docNo) {
		return aDao.selectDocumentByNo(sqlSession, docNo);
	}

	@Override
	@Transactional // 트랜젝션 처리
	@CacheEvict(value = "draftdocuments", key = "#document.drafterEmpNo") // 임시저장함 캐시 삭제
	public int updateDocument(ApprovalDoc document) {
		return aDao.updateDocument(sqlSession, document);
	}
	
	// 결재선 관련 메소드 구현

	@Override
	@Transactional // 트랜젝션 처리
	public int insertApprovalLines(ArrayList<ApprovalLine> lines) {
		int result = 0;
		for(ApprovalLine line : lines) {
			result += aDao.insertApprovalLine(sqlSession, line);
		}
		return result;
	}

	@Override
	public ArrayList<ApprovalLine> selectApprovalLineByDocNo(int docNo) {
		return aDao.selectApprovalLineByDocNo(sqlSession, docNo);
	}

	@Override
	@Transactional // 트랜젝션 처리
	public int updateApprovalLine(ApprovalLine line) {
		return aDao.updateApprovalLine(sqlSession, line);
	}

	@Override
	@Transactional // 트랜젝션 처리
	public int updateFirstApproverStatus(int docNo) {
		return aDao.updateFirstApproverStatus(sqlSession, docNo);
	}

	@Override
	@Transactional
	public int resetApprovalLines(int docNo) {
		return aDao.resetApprovalLines(sqlSession, docNo);
	}

	@Override
	public ApprovalLine selectApprovalLineByNo(int lineNo) {
		return aDao.selectApprovalLineByNo(sqlSession, lineNo);
	}

	// 문서함 관련 메소드 구현 - Redis 캐싱 적용
	
	@Override
	@Cacheable(value = "draftDocuments", key = "#empNo") // 임시저장함 캐싱
	public ArrayList<ApprovalDoc> selectDraftDocuments(int empNo) {
		long startTime = System.currentTimeMillis();
	    ArrayList<ApprovalDoc> result = aDao.selectDraftDocuments(sqlSession, empNo);
	    long endTime = System.currentTimeMillis();
	    System.out.println("[성능측정] selectDraftDocuments - DB 조회 시간: " + (endTime - startTime) + "ms");
	    return result;
	}

	@Override
	public ArrayList<ApprovalDoc> selectRequestedDocuments(int empNo) {
		return aDao.selectRequestedDocuments(sqlSession, empNo);
	}

	@Override
	@Cacheable(value = "waitingDocuments", key = "#empNo") // 수신문서함 캐싱
	public ArrayList<ApprovalDoc> selectWaitingDocuments(int empNo) {
		long startTime = System.currentTimeMillis();
	    ArrayList<ApprovalDoc> result = aDao.selectWaitingDocuments(sqlSession, empNo);
	    long endTime = System.currentTimeMillis();
	    System.out.println("[성능측정] selectWaitingDocuments - DB 조회 시간: " + (endTime - startTime) + "ms");
	    return result;
	}

	@Override
	public ArrayList<ApprovalDoc> selectCompletedDocuments(int empNo) {
		return aDao.selectCompletedDocuments(sqlSession, empNo);
	}

	// ====================== 첨부파일 관련 메소드 구현 ======================
	@Override
	public int insertAttachment(Attachment file) {
		return aDao.insertAttachment(sqlSession, file);
	}

	@Override
	public Attachment selectAttachmentByDocNo(int docNo) {
		return aDao.selectAttachmentByDocNo(sqlSession, docNo);
	}

	@Override
	public Attachment selectAttachmentByNo(int attachmentNo) {
		return aDao.selectAttachmentByNo(sqlSession, attachmentNo);
	}

	@Override
	public int deleteAttachmentByDocNo(int docNo) {
		return aDao.deleteAttachmentByDocNo(sqlSession, docNo);
	}
	
	// ====================== 페이징 처리된 문서함 관련 메소드 구현 ======================

	@Override
	public HashMap<String, Object> selectDraftDocumentsPaging(int empNo, int currentPage) {
		int listCount = aDao.selectDraftDocumentsCount(sqlSession, empNo);
		int pageLimit = 10;     // 페이징바 개수
		int boardLimit = 10;    // 한 페이지에 보여질 문서 개수
		
		PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		ArrayList<ApprovalDoc> list = aDao.selectDraftDocumentsPaging(sqlSession, empNo, pageInfo);
		
		// null 체크 추가
        if (list == null) {
            list = new ArrayList<>();
        }
		
		HashMap<String, Object> resultMap = new HashMap<>();
		resultMap.put("pageInfo", pageInfo);
		resultMap.put("list", list);
		
		return resultMap;
	}

	@Override
	public HashMap<String, Object> selectRequestedDocumentsPaging(int empNo, int currentPage) {
		try {
	        int listCount = aDao.selectRequestedDocumentsCount(sqlSession, empNo);
	        int pageLimit = 10;
	        int boardLimit = 10;
	        
	        PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
	        ArrayList<ApprovalDoc> list = aDao.selectRequestedDocumentsPaging(sqlSession, empNo, pageInfo);
	        
	        // null 체크 추가
	        if (list == null) {
	            list = new ArrayList<>();
	        }
	        
	        HashMap<String, Object> resultMap = new HashMap<>();
	        resultMap.put("pageInfo", pageInfo);
	        resultMap.put("list", list);
	        
	        return resultMap;
	    } catch (Exception e) {
	        e.printStackTrace();
	        // 에러 발생 시 빈 결과 반환
	        HashMap<String, Object> resultMap = new HashMap<>();
	        resultMap.put("pageInfo", null);
	        resultMap.put("list", new ArrayList<>());
	        return resultMap;
	    }
	}

	@Override
	@Cacheable(value = "waitingDocuments", key = "#empNo + '_' + #currentPage")
	public HashMap<String, Object> selectWaitingDocumentsPaging(int empNo, int currentPage) {
		int listCount = aDao.selectWaitingDocumentsCount(sqlSession, empNo);
		int pageLimit = 10;
		int boardLimit = 10;
		
		PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		ArrayList<ApprovalDoc> list = aDao.selectWaitingDocumentsPaging(sqlSession, empNo, pageInfo);
		
		HashMap<String, Object> resultMap = new HashMap<>();
		resultMap.put("pageInfo", pageInfo);
		resultMap.put("list", list);
		
		return resultMap;
	}

	@Override
	public HashMap<String, Object> selectCompletedDocumentsPaging(int empNo, int currentPage) {
		int listCount = aDao.selectCompletedDocumentsCount(sqlSession, empNo);
		int pageLimit = 10;
		int boardLimit = 10;
		
		PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		ArrayList<ApprovalDoc> list = aDao.selectCompletedDocumentsPaging(sqlSession, empNo, pageInfo);
		
		HashMap<String, Object> resultMap = new HashMap<>();
		resultMap.put("pageInfo", pageInfo);
		resultMap.put("list", list);
		
		return resultMap;
	}
	
	// ====================== 검색 기능 구현 ======================

	@Override
	public HashMap<String, Object> selectDraftDocumentsSearch(int empNo, int currentPage, String searchType,
			String keyword, String dateFrom, String dateTo) {
		HashMap<String, Object> params = new HashMap<>();
		params.put("empNo", empNo);
		params.put("searchType", searchType);
		params.put("keyword", keyword);
		params.put("dateFrom", dateFrom);
		params.put("dateTo", dateTo);
		
		int listCount = aDao.selectDraftDocumentsSearchCount(sqlSession, params);
		int pageLimit = 10;
		int boardLimit = 10;
		
		PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		ArrayList<ApprovalDoc> list = aDao.selectDraftDocumentsSearchPaging(sqlSession, params, pageInfo);
		
		HashMap<String, Object> resultMap = new HashMap<>();
		resultMap.put("pageInfo", pageInfo);
		resultMap.put("list", list);
		resultMap.put("searchCondition", params);
		
		return resultMap;
	}
	
	// ====================== 사원 조회 관련 메소드 수정 ======================

	@Override
	public ArrayList<Employee> selectEmployeesForApprovalLine() {
		return aDao.selectEmployeesForApprovalLine(sqlSession);
	}

	@Override
	public ArrayList<Employee> searchEmployeesForApprovalLine(String searchKeyword) {
		return aDao.searchEmployeesForApprovalLine(sqlSession, searchKeyword);
	}
	
	// ====================== 통계 조회 메서드 구현 ======================

	@Override
	public int selectWaitingCount(int empNo) {
		return aDao.selectWaitingCount(sqlSession, empNo);
	}

	@Override
	public int selectProcessingCount(int empNo) {
		return aDao.selectProcessingCount(sqlSession, empNo);
	}

	@Override
	public int selectApprovedCount(int empNo) {
		return aDao.selectApprovedCount(sqlSession, empNo);
	}

	@Override
	public int selectRejectedCount(int empNo) {
		return aDao.selectRejectedCount(sqlSession, empNo);
	}
	
	// ====================== 최근 문서 조회 메서드 구현 ======================

	@Override
	public ArrayList<ApprovalDoc> selectRecentWaitingDocuments(int empNo, int limit) {
		return aDao.selectRecentWaitingDocuments(sqlSession, empNo, limit);
	}

	@Override
	public ArrayList<ApprovalDoc> selectRecentMyDocuments(int empNo, int limit) {
		return aDao.selectRecentMyDocuments(sqlSession, empNo, limit);
	}

	@Override
	public ArrayList<ApprovalDoc> selectRecentDraftDocuments(int empNo, int limit) {
		return aDao.selectRecentDraftDocuments(sqlSession, empNo, limit);
	}
	
	@Override
	public ArrayList<ApprovalDoc> selectRecentCompletedDocuments(int empNo, int limit) {
		try {
	        // 로그 추가
	        System.out.println("완료 문서 조회 요청: empNo=" + empNo + ", limit=" + limit);
	        
	        HashMap<String, Object> params = new HashMap<>();
	        params.put("empNo", empNo);
	        params.put("limit", limit);
	        
	        // DAO 메서드 호출
	        ArrayList<ApprovalDoc> result = aDao.selectRecentCompletedDocuments(sqlSession, params);
	        
	        // null 체크
	        if (result == null) {
	            System.out.println("완료 문서 조회 결과 null");
	            return new ArrayList<>(); // null 대신 빈 리스트 반환
	        }
	        
	        System.out.println("완료 문서 조회 결과: " + result.size() + "개");
	        return result;
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("완료 문서 조회 중 오류 발생: " + e.getMessage());
	        return new ArrayList<>(); // 예외 발생 시 빈 리스트 반환
	    }
	}

	@Override
	@Transactional
	@CacheEvict(value = {"waitingDocuments", "completedDocuments"}, key = "#currentLine.docNo") // 캐시 무효화
	public int approveDocument(ApprovalLine currentLine) {
		int result = 0;
		
		// 1. 현재 결재선 상태 업데이트 (APPROVED)
		currentLine.setLineStatus("APPROVED");
		currentLine.setProcessedDate(new Date()); // 처리일 설정
		result += aDao.updateApprovalLine(sqlSession, currentLine);
		
		// 2. 다음 결재자 확인 및 상태 업데이트
		ArrayList<ApprovalLine> allLines = aDao.selectApprovalLineByDocNo(sqlSession, currentLine.getDocNo());
		
		boolean hasNextApprover = false;
		
		// 현재 결재 순서보다 큰 다음 결재자 찾기
		for(ApprovalLine line : allLines) {
			if (line.getApprovalOrder() == currentLine.getApprovalOrder() + 1 && 
                "PENDING".equals(line.getLineStatus())) {
                line.setLineStatus("WAITING");
                aDao.updateApprovalLine(sqlSession, line);
                hasNextApprover = true;
                break;
	        }
		}
		
		// 3. 모든 결재가 완료된 경우
		if(!hasNextApprover) {
			ApprovalDoc document = aDao.selectDocumentByNo(sqlSession, currentLine.getDocNo());
			document.setDocStatus("APPROVED");
			document.setCompletedDate(new Date()); // 완료날짜 설정
			result += aDao.updateDocument(sqlSession, document);
		}
		
		// ❤️ 캐시 무효화 추가
	    refreshProcessingEfficiencyCache();
		
		return result;
		
	}

	@Override
	@Transactional
	@CacheEvict(value = {"waitingDocuments", "completedDocuments"}, key = "#currentLine.docNo") // 캐시 무효화
	public int rejectDocument(ApprovalLine currentLine) {
		int result = 0;
		
		// 1. 현재 결재선 상태 업데이트 (REJECTED)
        currentLine.setLineStatus("REJECTED");
        currentLine.setProcessedDate(new Date()); // 처리일 설정
        result += aDao.updateApprovalLine(sqlSession, currentLine);
        
        // 2. 문서 상태를 반려로 변경
        ApprovalDoc document = aDao.selectDocumentByNo(sqlSession, currentLine.getDocNo());
        document.setDocStatus("REJECTED");
        document.setCompletedDate(new Date()); // 완료일자 설정
        result += aDao.updateDocument(sqlSession, document);
        
        // 3. 다른 결재선들도 REJECTED 상태로 변경 (필요에 따라)
        ArrayList<ApprovalLine> allLines = aDao.selectApprovalLineByDocNo(sqlSession, currentLine.getDocNo());
        for (ApprovalLine line : allLines) {
            if (line.getLineNo() != currentLine.getLineNo() && 
                line.getApprovalOrder() > currentLine.getApprovalOrder() && 
                "PENDING".equals(line.getLineStatus())) {
                line.setLineStatus("REJECTED");
                aDao.updateApprovalLine(sqlSession, line);
            }
        }
        
        // ❤️ 캐시 무효화 추가
        refreshProcessingEfficiencyCache();
        
        return result;
    }

	@Override
	public ArrayList<Employee> selectEmployeesByDept(int deptNo) {
	    return aDao.selectEmployeesByDept(sqlSession, deptNo);
	}

	@Override
	public ArrayList<HashMap<String, Object>> selectAllDepartments() {
	    return aDao.selectAllDepartments(sqlSession);
	}

	@Override
	public int deleteApprovalLinesByDocNo(int docNo) {
		return aDao.deleteApprovalLinesByDocNo(sqlSession, docNo);
	}

	@Override
	public HashMap<String, Object> searchDocuments(String boxType, int empNo, int currentPage, 
	        String searchType, String keyword, String dateFrom, String dateTo, String statusFilter) {
	    
	    // 검색 파라미터 준비
	    HashMap<String, Object> params = new HashMap<>();
	    params.put("boxType", boxType);
	    params.put("empNo", empNo);
	    params.put("searchType", searchType);
	    params.put("keyword", keyword);
	    params.put("dateFrom", dateFrom);
	    params.put("dateTo", dateTo);
	    params.put("statusFilter", statusFilter);
	    
	    // 검색 조건에 맞는 전체 개수 조회
	    int listCount = aDao.selectDocumentsSearchCount(sqlSession, params);
	    
	    // 페이징 정보 설정
	    int pageLimit = 10;
	    int boardLimit = 10;
	    PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
	    
	    // 검색 결과 조회
	    ArrayList<ApprovalDoc> list = aDao.selectDocumentsSearchPaging(sqlSession, params, pageInfo);
	    
	    // 완료 문서함일 경우 통계 정보 추가
	    HashMap<String, Object> stats = new HashMap<>();
	    if ("completed".equals(boxType)) {
	        // 승인, 반려 문서 개수 계산
	        int approvedCount = 0;
	        int rejectedCount = 0;
	        
	        if (list != null) {
	            for (ApprovalDoc doc : list) {
	                if ("APPROVED".equals(doc.getDocStatus())) {
	                    approvedCount++;
	                } else if ("REJECTED".equals(doc.getDocStatus())) {
	                    rejectedCount++;
	                }
	            }
	        }
	        
	        stats.put("totalCount", listCount);
	        stats.put("approvedCount", approvedCount);
	        stats.put("rejectedCount", rejectedCount);
	        stats.put("avgProcessTime", "계산 중..."); // 필요시 계산 로직 추가
	    }
	    
	    // 결과 담기
	    HashMap<String, Object> resultMap = new HashMap<>();
	    resultMap.put("pageInfo", pageInfo);
	    resultMap.put("list", list);
	    resultMap.put("searchCondition", params);
	    resultMap.put("stats", stats);
	    
	    return resultMap;
	}

	/**
	 * 결재자 관점의 승인 문서 건수 조회
	 * 현재 로그인한 사용자가 결재자로서 승인한 문서의 개수를 반환
	 * 
	 * @param empNo 조회할 사원 번호 (결재자)
	 * @return 해당 사원이 결재자로서 승인한 문서의 개수
	 */
	@Override
	public int selectApprovedCountByApprover(int empNo) {
	    return aDao.selectApprovedCountByApprover(sqlSession, empNo);
	}

	/**
	 * 결재자 관점의 반려 문서 건수 조회
	 * 현재 로그인한 사용자가 결재자로서 반려한 문서의 개수를 반환
	 * 
	 * @param empNo 조회할 사원 번호 (결재자)
	 * @return 해당 사원이 결재자로서 반려한 문서의 개수
	 */
	@Override
	public int selectRejectedCountByApprover(int empNo) {
	    return aDao.selectRejectedCountByApprover(sqlSession, empNo);
	}

	/**
	 * 특정 사용자의 수신 문서함 캐시를 명시적으로 갱신
	 * Redis 캐시를 효과적으로 관리하기 위한 유틸리티 메서드
	 * 
	 * @param empNo 사원 번호
	 */
	@Override
	public void refreshWaitingDocumentsForUser(int empNo) {
		try {
	        // 1. 기본 캐시 키 무효화
	        cacheManager.getCache("waitingDocuments").evict(empNo);
	        
	        // 2. 페이징 관련 캐시 키 무효화 (1~10페이지 범위에서)
	        for (int page = 1; page <= 10; page++) {
	            String cacheKey = empNo + "_" + page;
	            cacheManager.getCache("waitingDocuments").evict(cacheKey);
	        }
	        
	        // 3. 로그 기록
	        System.out.println("[캐시관리] 사용자 " + empNo + "의 수신 문서함 캐시가 무효화되었습니다.");
	        
	    } catch (Exception e) {
	        // 캐시 무효화 실패해도 앱에 영향 없도록 예외 처리
	        System.err.println("[캐시관리] 오류 발생: " + e.getMessage());
	    }
	}
	
	/**
	 * 처리 효율성 지표 계산 (private 메서드)
	 */
	private HashMap<String, Object> calculateProcessingEfficiency(ArrayList<ApprovalDoc> list) {
	    HashMap<String, Object> result = new HashMap<>();
	    
	    if (list == null || list.isEmpty()) {
	        result.put("status", "-");
	        result.put("percentage", 0);
	        return result;
	    }
	    
	    int fastCount = 0;  // 24시간 이내
	    int normalCount = 0; // 1-3일
	    int delayedCount = 0; // 3일 이상
	    int totalValidDocs = 0;
	    
	    for (ApprovalDoc doc : list) {
	        if (doc.getRequestedDate() != null && doc.getCompletedDate() != null) {
	            long processHours = (doc.getCompletedDate().getTime() - doc.getRequestedDate().getTime()) 
	                              / (1000 * 60 * 60);
	            totalValidDocs++;
	            
	            if (processHours <= 24) {
	                fastCount++;
	            } else if (processHours <= 72) {
	                normalCount++;
	            } else {
	                delayedCount++;
	            }
	        }
	    }
	    
	    if (totalValidDocs == 0) {
	        result.put("status", "-");
	        result.put("percentage", 0);
	        return result;
	    }
	    
	    // 가장 높은 비율의 상태 결정
	    String status;
	    int percentage;
	    
	    if (fastCount >= normalCount && fastCount >= delayedCount) {
	        status = "신속 처리";
	        percentage = fastCount * 100 / totalValidDocs;
	    } else if (normalCount >= fastCount && normalCount >= delayedCount) {
	        status = "정상 처리";
	        percentage = normalCount * 100 / totalValidDocs;
	    } else {
	        status = "지연 처리";
	        percentage = delayedCount * 100 / totalValidDocs;
	    }
	    
	    result.put("status", status);
	    result.put("percentage", percentage);
	    
	    return result;
	}


	/**
	 * 처리 효율성 지표 계산 메서드 (인터페이스 구현)
	 */
	@Override
	@Cacheable(value = "processingEfficiency", 
	           key = "#empNo + '_' + #statusFilter + '_' + #dateFrom + '_' + #dateTo")
	public HashMap<String, Object> getProcessingEfficiency(int empNo, String statusFilter, 
	                                                   String dateFrom, String dateTo) {
	    
		long startTime = System.currentTimeMillis();
	    
	    // 원래 코드 그대로 유지
	    HashMap<String, Object> result = searchDocuments("completed", empNo, 1, 
	                                                   null, null, dateFrom, dateTo, statusFilter);
	    ArrayList<ApprovalDoc> list = (ArrayList<ApprovalDoc>) result.get("list");
	    
	    // calculateProcessingEfficiency 메소드 호출 시 필요한 파라미터 전달
	    HashMap<String, Object> efficiencyResult = calculateProcessingEfficiency(list);
	    
	    long endTime = System.currentTimeMillis();
	    System.out.println("[성능측정] getProcessingEfficiency - DB 조회 시간: " + (endTime - startTime) + "ms");
	    
	    return efficiencyResult;
	}

	/**
	 * 캐시 무효화 메서드 (인터페이스 구현)
	 */
	@Override
	@CacheEvict(value = "processingEfficiency", allEntries = true)
	public void refreshProcessingEfficiencyCache() {
		System.out.println("[캐시관리] 처리 효율성 지표 캐시가 무효화되었습니다.");
	}
	
}
