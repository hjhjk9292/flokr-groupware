package com.kh.flokrGroupware.approval.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.flokrGroupware.approval.model.service.ApprovalService;

@Controller
public class CacheTestController {

    @Autowired
    private ApprovalService aService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @GetMapping("/test-waiting-documents")
    @ResponseBody
    public Map<String, Object> testWaitingDocuments() {
        Map<String, Object> result = new HashMap<>();
        int empNo = 3; // 테스트할 사원 번호
        
        // 캐시 초기화
        if (cacheManager.getCache("waitingDocuments") != null) {
            cacheManager.getCache("waitingDocuments").clear();
        }
        
        // 첫 번째 호출 (DB 조회)
        long start1 = System.currentTimeMillis();
        aService.selectWaitingDocuments(empNo);
        long time1 = System.currentTimeMillis() - start1;
        
        // 두 번째 호출 (캐시 조회)
        long start2 = System.currentTimeMillis();
        aService.selectWaitingDocuments(empNo);
        long time2 = System.currentTimeMillis() - start2;
        
        result.put("method", "selectWaitingDocuments");
        result.put("db_time", time1);
        result.put("cache_time", time2);
        result.put("improvement", (time1 - time2) * 100.0 / time1);
        
        return result;
    }
    
    @GetMapping("/test-draft-documents")
    @ResponseBody
    public Map<String, Object> testDraftDocuments() {
        Map<String, Object> result = new HashMap<>();
        int empNo = 1; // 테스트할 사원 번호
        
        // 캐시 초기화
        if (cacheManager.getCache("draftDocuments") != null) {
            cacheManager.getCache("draftDocuments").clear();
        }
        
        // 첫 번째 호출 (DB 조회)
        long start1 = System.currentTimeMillis();
        aService.selectDraftDocuments(empNo);
        long time1 = System.currentTimeMillis() - start1;
        
        // 두 번째 호출 (캐시 조회)
        long start2 = System.currentTimeMillis();
        aService.selectDraftDocuments(empNo);
        long time2 = System.currentTimeMillis() - start2;
        
        result.put("method", "selectDraftDocuments");
        result.put("db_time", time1);
        result.put("cache_time", time2);
        result.put("improvement", (time1 - time2) * 100.0 / time1);
        
        return result;
    }
    
    @GetMapping("/test-processing-efficiency")
    @ResponseBody
    public Map<String, Object> testProcessingEfficiency() {
        Map<String, Object> result = new HashMap<>();
        int empNo = 1; // 테스트할 사원 번호
        
        // 캐시 초기화
        if (cacheManager.getCache("processingEfficiency") != null) {
            cacheManager.getCache("processingEfficiency").clear();
        }
        
        // 첫 번째 호출 (DB 조회)
        long start1 = System.currentTimeMillis();
        aService.getProcessingEfficiency(empNo, null, null, null);
        long time1 = System.currentTimeMillis() - start1;
        
        // 두 번째 호출 (캐시 조회)
        long start2 = System.currentTimeMillis();
        aService.getProcessingEfficiency(empNo, null, null, null);
        long time2 = System.currentTimeMillis() - start2;
        
        result.put("method", "getProcessingEfficiency");
        result.put("db_time", time1);
        result.put("cache_time", time2);
        result.put("improvement", (time1 - time2) * 100.0 / time1);
        
        return result;
    }
    
    // 전체 메소드 테스트 (한 번에 모든 메소드 테스트)
    @GetMapping("/test-all-cache")
    @ResponseBody
    public Map<String, Object> testAllCache() {
        Map<String, Object> result = new HashMap<>();
        ArrayList<Map<String, Object>> methods = new ArrayList<>();
        
        // 각 메소드별 테스트 결과 추가
        methods.add(testWaitingDocuments());
        methods.add(testDraftDocuments());
        methods.add(testProcessingEfficiency());
        
        result.put("methods", methods);
        return result;
    }
}
