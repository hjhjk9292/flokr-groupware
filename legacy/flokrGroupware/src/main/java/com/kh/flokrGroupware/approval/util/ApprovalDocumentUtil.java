package com.kh.flokrGroupware.approval.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 결재 문서의 JSON 내용을 처리하기 위한 유틸리티 클래스
 */
public class ApprovalDocumentUtil {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * 문서 내용을 JSON 형식으로 변환
     * @param formType 양식 유형 (vacation, expense, remoteWork, businessTrip)
     * @param dataObject 문서 데이터를 담은 JsonObject
     * @return JSON 문자열
     */
    public static String createDocumentJson(String formType, JsonObject dataObject) {
        JsonObject root = new JsonObject();
        root.addProperty("formType", formType);
        root.add("data", dataObject);
        return gson.toJson(root);
    }
    
    /**
     * JSON 문자열에서 formType 추출
     * @param jsonContent JSON 형식의 문서 내용
     * @return 양식 유형 문자열
     */
    public static String getFormType(String jsonContent) {
        try {
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            return root.get("formType").getAsString();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * JSON 문자열에서 데이터 객체 추출
     * @param jsonContent JSON 형식의 문서 내용
     * @return 데이터를 담은 JsonObject
     */
    public static JsonObject getDataObject(String jsonContent) {
        try {
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            return root.getAsJsonObject("data");
        } catch (Exception e) {
            return new JsonObject();
        }
    }
    
    /**
     * JSON 문서 내용에서 간략한 텍스트 요약 생성
     * @param jsonContent JSON 형식의 문서 내용
     * @return 요약 텍스트
     */
    public static String generateSummary(String jsonContent) {
        try {
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            String formType = root.get("formType").getAsString();
            JsonObject data = root.getAsJsonObject("data");
            
            switch (formType) {
                case "vacation":
                    return generateVacationSummary(data);
                case "expense":
                    return generateExpenseSummary(data);
                case "remoteWork":
                    return generateRemoteWorkSummary(data);
                case "businessTrip":
                    return generateBusinessTripSummary(data);
                default:
                    return "문서 내용을 확인할 수 없습니다.";
            }
        } catch (Exception e) {
            return "잘못된 형식의 문서입니다.";
        }
    }
    
    private static String generateVacationSummary(JsonObject data) {
        String vacationType = data.get("vacationType").getAsString();
        String startDate = data.get("startDate").getAsString();
        String endDate = data.get("endDate").getAsString();
        String days = data.get("days").getAsString();
        String reason = data.get("reason").getAsString();
        
        return String.format("%s에서 %s까지 %s %s을 신청합니다. 사유: %s", 
                startDate, endDate, days, vacationType, reason);
    }
    
    private static String generateExpenseSummary(JsonObject data) {
        String expenseType = data.get("expenseType").getAsString();
        String totalAmount = data.get("totalAmount").getAsString();
        String description = data.get("description").getAsString();
        
        return String.format("%s 관련 지출 %s원에 대한 결의를 신청합니다. 내용: %s", 
                expenseType, totalAmount, description);
    }
    
    private static String generateRemoteWorkSummary(JsonObject data) {
        String workType = data.get("workType").getAsString();
        String startDate = data.get("startDate").getAsString();
        String endDate = data.get("endDate").getAsString();
        String days = data.get("days").getAsString();
        String reason = data.get("reason").getAsString();
        
        return String.format("%s에서 %s까지 %s %s을 신청합니다. 사유: %s", 
                startDate, endDate, days, workType, reason);
    }
    
    private static String generateBusinessTripSummary(JsonObject data) {
        String tripType = data.get("tripType").getAsString();
        String location = data.get("location").getAsString();
        String startDate = data.get("startDate").getAsString();
        String endDate = data.get("endDate").getAsString();
        String purpose = data.get("purpose").getAsString();
        
        return String.format("%s로 %s에서 %s까지 %s을 신청합니다. 목적: %s", 
                location, startDate, endDate, tripType, purpose);
    }
}