package com.kh.flokrGroupware.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.kh.flokrGroupware.common.interceptor.WebSocketSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Bean
    public ThreadPoolTaskScheduler messageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); // 최소 2개로 증가 (안정성 확보)
        scheduler.setThreadNamePrefix("wss-scheduler-");
        scheduler.initialize(); // 스케줄러 초기화
        return scheduler;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // SimpleBroker 설정에 TaskScheduler 와 Heartbeat 설정 추가
        config.enableSimpleBroker("/topic", "/queue")
              .setTaskScheduler(messageBrokerTaskScheduler()) // 생성한 스케줄러 빈 사용
              .setHeartbeatValue(new long[] {10000, 10000}); // 서버->클라이언트, 클라이언트->서버 하트비트 간격 (ms)
        
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 시작할 주소
        registry.addEndpoint("/ws-stomp") // 예: ws://localhost:8008/flokrGroupware/ws-stomp
                .setAllowedOrigins("http://localhost:8008")   // 모든 Origin 허용 (개발용(*). 실제 운영 시에는 필요한 도메인만 명시)
                // .setAllowedOriginPatterns("*") // CORS 설정
                .addInterceptors(handshakeInterceptor()) // 세션 정보 전달 인터셉터 추가
                .withSockJS()  // WebSocket 미지원 브라우저 위한 SockJS fallback 활성화
                .setDisconnectDelay(30 * 1000) // 연결 종료 후 세션 유지 시간 (30초)
                .setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.5/sockjs.min.js");
    }
    
    // 클라이언트 메시지 송수신 채널 설정
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
            .corePoolSize(4)    // 코어 스레드 풀 크기
            .maxPoolSize(10)    // 최대 스레드 풀 크기
            .queueCapacity(50); // 큐 용량
    }
    
    // 서버 메시지 송신 채널 설정
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
            .corePoolSize(4)    // 코어 스레드 풀 크기
            .maxPoolSize(10)    // 최대 스레드 풀 크기
            .queueCapacity(50); // 큐 용량
    }
    
    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new WebSocketSessionHandshakeInterceptor();
    }
}