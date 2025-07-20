package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.PositionRequest; // PositionRequest 임포트
import com.flokr.groupwarebackend.entity.Position;
import com.flokr.groupwarebackend.service.PositionService;
import jakarta.validation.Valid; // @Valid 어노테이션을 위한 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // HttpStatus 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/positions") // 직급 관련 API의 기본 경로
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}) // 필요시 CORS 설정
public class PositionController {

    private final PositionService positionService;

    /**
     * 모든 활성 직급 목록 조회
     */
    @GetMapping // GET /api/positions
    public ResponseEntity<ApiResponse<List<Position>>> getAllActivePositions() {
        try {
            log.info("Request to get all active positions.");
            List<Position> positions = positionService.getAllActivePositions();
            return ResponseEntity.ok(ApiResponse.success("활성 직급 목록 조회", positions));
        } catch (Exception e) {
            log.error("Error getting all active positions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직급 목록 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 직급 번호로 직급 조회
     */
    @GetMapping("/{positionNo}") // GET /api/positions/{positionNo}
    public ResponseEntity<ApiResponse<Position>> getPositionByNo(@PathVariable Long positionNo) {
        try {
            log.info("Request to get position by positionNo: {}", positionNo);
            return positionService.getPositionByNo(positionNo)
                    .map(position -> ResponseEntity.ok(ApiResponse.success("직급 조회", position)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("직급을 찾을 수 없음", "POSITION_NOT_FOUND")));
        } catch (Exception e) {
            log.error("Error getting position by positionNo {}: {}", positionNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직급 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 새로운 직급 생성
     */
    @PostMapping // POST /api/positions
    public ResponseEntity<ApiResponse<Position>> createPosition(@Valid @RequestBody PositionRequest request) {
        try {
            log.info("Request to create position: {}", request.getPositionName());
            Position createdPosition = positionService.createPosition(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("직급 생성 성공", createdPosition));
        } catch (Exception e) {
            log.error("Error creating position: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직급 생성 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 기존 직급 정보 수정
     */
    @PutMapping("/{positionNo}") // PUT /api/positions/{positionNo}
    public ResponseEntity<ApiResponse<Position>> updatePosition(@PathVariable Long positionNo, @Valid @RequestBody PositionRequest request) {
        try {
            log.info("Request to update position positionNo {}: {}", positionNo, request.getPositionName());
            return positionService.updatePosition(positionNo, request)
                    .map(updatedPosition -> ResponseEntity.ok(ApiResponse.success("직급 수정 성공", updatedPosition)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("직급을 찾을 수 없음", "POSITION_NOT_FOUND")));
        } catch (Exception e) {
            log.error("Error updating position positionNo {}: {}", positionNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직급 수정 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 직급 삭제(비활성화)
     */
    @DeleteMapping("/{positionNo}") // DELETE /api/positions/{positionNo}
    public ResponseEntity<ApiResponse<String>> deletePosition(@PathVariable Long positionNo) {
        try {
            log.info("Request to delete position positionNo: {}", positionNo);
            boolean isDeleted = positionService.deletePosition(positionNo);
            if (isDeleted) {
                return ResponseEntity.ok(ApiResponse.success("직급 비활성화 성공", "DELETED"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("직급을 찾을 수 없음", "POSITION_NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("Error deleting position positionNo {}: {}", positionNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직급 비활성화 실패", "INTERNAL_SERVER_ERROR"));
        }
    }
}