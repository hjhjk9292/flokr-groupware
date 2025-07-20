package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.dto.PositionRequest;
import com.flokr.groupwarebackend.entity.Position;
import com.flokr.groupwarebackend.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    /**
     * 모든 활성 직급 목록 조회
     * @return 활성 상태인 직급 목록
     */
    @Transactional(readOnly = true)
    public List<Position> getAllActivePositions() {
        return positionRepository.findByStatusOrderByPositionNameAsc("Y"); // 상태가 'Y'인 직급만 이름 순으로 조회
    }

    /**
     * 직급 번호로 직급 조회
     * @param positionNo 직급 번호
     * @return 해당 직급 엔티티 (존재하지 않으면 Optional.empty)
     */
    @Transactional(readOnly = true)
    public Optional<Position> getPositionByNo(Long positionNo) {
        return positionRepository.findById(positionNo);
    }

    /**
     * 새로운 직급 생성
     * @param request 직급 생성 요청 DTO
     * @return 생성된 직급 엔티티
     */
    @Transactional
    public Position createPosition(PositionRequest request) {
        Position newPosition = new Position();
        newPosition.setPositionName(request.getPositionName());
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            newPosition.setStatus(request.getStatus());
        } else {
            newPosition.setStatus("Y"); // 기본값 설정
        }
        newPosition.setCreateDate(LocalDateTime.now()); // 수동 설정 또는 CreationTimestamp에 맡김
        return positionRepository.save(newPosition);
    }

    /**
     * 기존 직급 정보 수정
     * @param positionNo 수정할 직급 번호
     * @param request 직급 수정 요청 DTO
     * @return 수정된 직급 엔티티 (직급이 존재하지 않으면 Optional.empty)
     */
    @Transactional
    public Optional<Position> updatePosition(Long positionNo, PositionRequest request) {
        return positionRepository.findById(positionNo).map(position -> {
            position.setPositionName(request.getPositionName());
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                position.setStatus(request.getStatus());
            }
            // updateDate는 Position 엔티티에 없으므로 자동 처리되지 않음. 필요시 추가
            return positionRepository.save(position);
        });
    }

    /**
     * 직급 삭제(비활성화) (실제 데이터는 유지하고 상태만 'N'으로 변경)
     * @param positionNo 삭제할 직급 번호
     * @return 삭제 성공 여부 (true: 성공, false: 직급 없음)
     */
    @Transactional
    public boolean deletePosition(Long positionNo) {
        return positionRepository.findById(positionNo).map(position -> {
            position.setStatus("N"); // 상태를 'N'으로 변경
            positionRepository.save(position); // 변경된 상태 저장
            return true;
        }).orElse(false);
    }

    /**
     * 직급별 직원 수 조회
     * @return 직급명과 해당 직급의 직원 수를 담은 Object 배열 리스트
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPositionEmployeeCounts() {
        return positionRepository.findPositionEmployeeCounts();
    }
}