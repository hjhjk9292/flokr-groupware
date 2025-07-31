package com.flokr.groupwarebackend.config;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.exception.ReservationConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ReservationConflictException 처리 (예약 시간 충돌)
     */
    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservationConflictException(
            ReservationConflictException e) {

        log.warn("Reservation conflict occurred: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
    }

}
