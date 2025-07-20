package com.kh.flokrGroupware.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 캐싱 설정 클래스
 * 캐싱 기능을 사용하기 위한 기본 설정을 제공함
 */
@Configuration 	// 스프링 설정 클래스임을 나타냄
@EnableCaching 	// 캐싱 기능 활성화
@PropertySource("classpath:application-dev.properties")	// 개발 환경 설정 파일 로드
public class RedisCacheConfig {
	
	// application.properties에서 Redis 서버 정보 가져오기
	@Value("${redis.host:localhost}") // 기본값은 localhost
	private String redisHost;
	
	@Value("${redis.port:6379}") // 기본값은 6379
	private int redisPort;
	
	/**
	 * Redis 연결 팩토리 생성
	 * Redis 서버와의 연결을 설정함
	 */
	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
		return new JedisConnectionFactory(config);
	}
	
	/**
	 * Redis 템플릿 생성
	 * Redis와 통신하기 위한 템플릿 객체를 설정함
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(){
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new JdkSerializationRedisSerializer());
		return template;
	}
	
	/**
	 * 캐시 매니저 생성
	 * 캐시의 전반적인 설정을 관리함
	 */
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// 기본 캐시 설정
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(30)) 	// 캐시 유효 시간: 30분
				.disableCachingNullValues();		// null값은 캐싱하지 않음
		
		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(config)
				.build();
	}
	

}
