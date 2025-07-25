package com.flokr.groupwarebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple Broker 활성화
        // /queue: 개인 메시지 (1:1) - 예: /user/{empId}/queue/notification
        // /topic: 브로드캐스트 메시지 (1:N) - 예: /topic/facility.updates
        config.enableSimpleBroker("/queue", "/topic");

        // 클라이언트에서 서버로 메시지 보낼 때 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");

        // 개인 메시지용 prefix (/user/{empId}/queue/notification)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 등록
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")  // CORS 허용 (개발환경용)
                .withSockJS();  // SockJS fallback 지원

        // 필요시 추가 엔드포인트
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*");
    }
}