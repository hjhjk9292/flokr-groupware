package com.kh.flokrGroupware.common.interceptor;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.kh.flokrGroupware.employee.model.vo.Employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketSessionHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // HTTP 요청에서 HttpSession 객체를 가져옴
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession httpSession = servletRequest.getServletRequest().getSession(false);
            
            if (httpSession != null) {
                // HTTP 세션에서 로그인 사용자 정보를 가져와 WebSocket 세션 속성에 추가
                Employee loginUser = (Employee) httpSession.getAttribute("loginUser");
                
                if (loginUser != null) {
                    logger.info("WebSocket 핸드셰이크: 로그인 사용자 정보 복사 - empId: {}, empNo: {}", 
                               loginUser.getEmpId(), loginUser.getEmpNo());
                    
                    // WebSocket 세션에 로그인 사용자 정보 추가
                    attributes.put("loginUser", loginUser);
                    attributes.put("empId", loginUser.getEmpId());
                    attributes.put("empNo", loginUser.getEmpNo());
                    attributes.put("deptNo", loginUser.getDeptNo());
                    
                    // 추가 세션 정보 복사
                    attributes.put("sessionId", httpSession.getId());
                    
                    return true;
                } else {
                    // 비로그인 사용자도 일부 알림 구독은 가능하도록 허용
                    logger.warn("WebSocket 핸드셰이크: 로그인 정보 없음 - 제한된 접근 허용");
                    return true;
                }
            } else {
                // 세션이 없어도 연결은 허용 (익명 사용자)
                logger.warn("WebSocket 핸드셰이크: HTTP 세션이 없음 - 제한된 접근 허용");
                return true;
            }
        }
        
        // 기본적으로 연결 허용
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception ex) {
        // 핸드셰이크 완료 후 로깅
        if (ex != null) {
            logger.error("WebSocket 핸드셰이크 후 오류 발생", ex);
        } else {
            logger.debug("WebSocket 핸드셰이크 완료");
        }
    }
}