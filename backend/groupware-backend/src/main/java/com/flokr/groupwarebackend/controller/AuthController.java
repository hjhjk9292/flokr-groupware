package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.LoginRequest;
import com.flokr.groupwarebackend.dto.LoginResponse;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.security.JwtTokenProvider;
import com.flokr.groupwarebackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 임포트 제거

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    // private final PasswordEncoder passwordEncoder; // PasswordEncoder 주입 제거

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login request received for user: {}", loginRequest.getEmpId());

            LoginResponse loginResponse = authService.login(loginRequest);

            return ResponseEntity.ok(
                    ApiResponse.success("로그인에 성공했습니다.", loginResponse)
            );

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - {}", loginRequest.getEmpId(), e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("로그인에 실패했습니다.", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Login error for user: {} - {}", loginRequest.getEmpId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR")
            );
        }
    }

    /**
     * 현재 사용자 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Employee>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String empId = authentication.getName();

            log.debug("Current user info request for: {}", empId);

            Employee currentUser = authService.getCurrentUser(empId);

            return ResponseEntity.ok(
                    ApiResponse.success("사용자 정보를 조회했습니다.", currentUser)
            );

        } catch (Exception e) {
            log.error("Get current user error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("사용자 정보 조회에 실패했습니다.", "INTERNAL_SERVER_ERROR")
            );
        }
    }

    /**
     * 로그아웃 API (클라이언트에서 토큰 삭제하면 됨)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String empId = authentication.getName();

            log.info("Logout request for user: {}", empId);

            // JWT는 stateless하므로 서버에서 특별한 처리 불필요
            // 클라이언트에서 토큰을 삭제하면 됨

            return ResponseEntity.ok(
                    ApiResponse.success("로그아웃되었습니다.", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("로그아웃 처리 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR")
            );
        }
    }

    /**
     * 토큰 유효성 검증 API
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = jwtTokenProvider.validateToken(token);

                return ResponseEntity.ok(
                        ApiResponse.success("토큰 검증 완료", isValid)
                );
            }

            return ResponseEntity.badRequest().body(
                    ApiResponse.success("유효하지 않은 토큰 형식", false)
            );

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    ApiResponse.success("토큰 검증 실패", false)
            );
        }
    }

    /**
     * API 상태 확인 (인증 불필요)
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth API is running", "OK")
        );
    }

    /*
    // 비밀번호 해싱 테스트 API (디버깅용)
    @GetMapping("/hash-test")
    public ResponseEntity<ApiResponse<String>> hashTest(@RequestParam String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        log.info("Plain Password: {}, Hashed Password: {}", plainPassword, hashedPassword);
        return ResponseEntity.ok(
                ApiResponse.success("비밀번호 해싱 테스트 완료", hashedPassword)
        );
    }
    */
}