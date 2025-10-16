package com.vibepay.service;

import com.vibepay.domain.Point;
import com.vibepay.dto.BalanceResponse;
import com.vibepay.exception.InsufficientBalanceException;
import com.vibepay.exception.PointNotFoundException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.PointRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final String SESSION_MEMBER_ID = "memberId";

    private final PointRepository pointRepository;

    /**
     * 잔액 조회
     * - HttpSession에서 memberId 가져오기
     * - memberId로 적립금 조회
     * - 없으면 예외 발생 (PointNotFoundException)
     * - BalanceResponse 반환
     */
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        Point point = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PointNotFoundException("적립금 정보를 찾을 수 없습니다"));

        return BalanceResponse.from(point);
    }

    /**
     * 적립금 차감
     * - HttpSession에서 memberId 가져오기
     * - memberId로 적립금 조회
     * - 잔액 부족 체크 (balance < amount)
     * - 차감 (balance = balance - amount)
     * - 업데이트된 잔액 반환
     */
    @Transactional
    public BalanceResponse deduct(Long amount, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        Point point = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PointNotFoundException("적립금 정보를 찾을 수 없습니다"));

        // 잔액 부족 체크
        if (point.getBalance() < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다");
        }

        // 차감
        pointRepository.deductBalance(memberId, amount);

        // 업데이트된 정보 조회 후 반환
        Point updatedPoint = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PointNotFoundException("적립금 정보를 찾을 수 없습니다"));

        return BalanceResponse.from(updatedPoint);
    }

    /**
     * 적립금 복구
     * - HttpSession에서 memberId 가져오기
     * - memberId로 적립금 조회
     * - 복구 (balance = balance + amount)
     * - 업데이트된 잔액 반환
     */
    @Transactional
    public BalanceResponse restore(Long amount, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        Point point = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PointNotFoundException("적립금 정보를 찾을 수 없습니다"));

        // 복구
        pointRepository.restoreBalance(memberId, amount);

        // 업데이트된 정보 조회 후 반환
        Point updatedPoint = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PointNotFoundException("적립금 정보를 찾을 수 없습니다"));

        return BalanceResponse.from(updatedPoint);
    }

    /**
     * 세션에서 memberId 가져오기
     */
    private Long getMemberIdFromSession(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SESSION_MEMBER_ID);

        if (memberId == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }

        return memberId;
    }
}
