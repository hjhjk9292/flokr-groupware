package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.dto.LoginRequest;
import com.flokr.groupwarebackend.dto.LoginResponse;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Value("${jwt.token-validity-in-seconds}")
    private long tokenValidityInSeconds;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            log.debug("Login attempt for user: {}", loginRequest.getEmpId());
            
            // Spring Security를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmpId(),
                    loginRequest.getPassword()
                )
            );
            
            // 인증 성공 시 Employee 정보 조회
            Employee employee = userDetailsService.findByEmpId(loginRequest.getEmpId());
            
            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.createToken(employee);
            
            log.info("Login successful for user: {}", employee.getEmpId());
            
            // 응답 생성
            return LoginResponse.from(employee, accessToken, tokenValidityInSeconds);
            
        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {} - {}", loginRequest.getEmpId(), e.getMessage());
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }
    
    @Transactional(readOnly = true)
    public Employee getCurrentUser(String empId) {
        return userDetailsService.findByEmpId(empId);
    }
}
