package com.vibepay.controller;

import com.vibepay.dto.BalanceResponse;
import com.vibepay.dto.PointDeductRequest;
import com.vibepay.dto.PointHistoryResponse;
import com.vibepay.dto.PointRestoreRequest;
import com.vibepay.service.PointService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 잔액 조회
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(HttpSession session) {
        BalanceResponse response = pointService.getBalance(session);
        return ResponseEntity.ok(response);
    }

    /**
     * 적립금 차감
     */
    @PostMapping("/deduct")
    public ResponseEntity<BalanceResponse> deduct(
            @Valid @RequestBody PointDeductRequest request,
            HttpSession session) {
        BalanceResponse response = pointService.deduct(request.getAmount(), session);
        return ResponseEntity.ok(response);
    }

    /**
     * 적립금 복구
     */
    @PostMapping("/restore")
    public ResponseEntity<BalanceResponse> restore(
            @Valid @RequestBody PointRestoreRequest request,
            HttpSession session) {
        BalanceResponse response = pointService.restore(request.getAmount(), session);
        return ResponseEntity.ok(response);
    }

    /**
     * 적립금 변동 이력 조회
     */
    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(HttpSession session) {
        List<PointHistoryResponse> response = pointService.getPointHistory(session);
        return ResponseEntity.ok(response);
    }
}
