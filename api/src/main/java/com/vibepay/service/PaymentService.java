package com.vibepay.service;

import com.vibepay.domain.*;
import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import com.vibepay.dto.PaymentResponse;
import com.vibepay.exception.OrderNotFoundException;
import com.vibepay.exception.PaymentNotFoundException;
import com.vibepay.exception.PaymentProcessException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.OrderRepository;
import com.vibepay.repository.PaymentRepository;
import com.vibepay.strategy.PaymentResult;
import com.vibepay.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 결제 서비스
 * 결제 승인, 취소 등의 핵심 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String SESSION_MEMBER_ID = "memberId";

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final Map<String, PaymentStrategy> paymentStrategies;

    /**
     * 결제 승인 처리
     * 1. 요청 검증 (주문 존재 여부, 회원 권한)
     * 2. 포인트 차감 (혼합결제 시)
     * 3. PG 승인 요청
     * 4. 승인 성공 시: 결제 정보 저장, 주문 상태 업데이트
     * 5. 승인 실패 시: 포인트 복구, 망취소 처리
     */
    @Transactional
    public PaymentResponse approvePayment(PaymentApprovalRequest request, HttpSession session) {
        log.info("결제 승인 처리 시작 - orderId: {}, amount: {}",
                request.getOrderId(), request.getTotalAmount());

        Long memberId = getMemberIdFromSession(session);

        // 1. 주문 정보 검증
        Order order = validateOrderForPayment(request.getOrderId(), memberId);

        // 2. 결제 정보 생성 (PENDING 상태)
        Payment payment = createPendingPayment(request, order);
        paymentRepository.save(payment);

        // 3. 포인트 차감 (혼합결제인 경우)
        if (request.hasPointAmount()) {
            try {
                pointService.deduct(request.getPointAmount(), session);
                log.info("포인트 차감 완료 - amount: {}", request.getPointAmount());
            } catch (Exception e) {
                log.error("포인트 차감 실패", e);
                throw new PaymentProcessException("포인트 차감에 실패했습니다: " + e.getMessage());
            }
        }

        try {
            // 4. PG 승인 처리
            PaymentStrategy strategy = getPaymentStrategy(request);
            PaymentResult result = strategy.approve(request);

            if (result.isSuccess()) {
                // 승인 성공 처리
                return handleApprovalSuccess(payment, result, order);
            } else {
                // 승인 실패 처리
                return handleApprovalFailure(payment, result, request, session);
            }

        } catch (Exception e) {
            log.error("결제 승인 처리 중 오류 발생", e);
            // 포인트 복구
            if (request.hasPointAmount()) {
                try {
                    pointService.restore(request.getPointAmount(), session);
                    log.info("승인 실패로 인한 포인트 복구 완료 - amount: {}", request.getPointAmount());
                } catch (Exception restoreException) {
                    log.error("포인트 복구 실패", restoreException);
                }
            }
            throw new PaymentProcessException("결제 처리 중 시스템 오류가 발생했습니다");
        }
    }

    /**
     * 결제 취소 처리
     * 1. 결제 정보 검증 (존재 여부, 회원 권한, 취소 가능 상태)
     * 2. PG 취소 요청
     * 3. 취소 성공 시: 결제 상태 업데이트, 주문 상태 업데이트, 포인트 복구
     */
    @Transactional
    public PaymentResponse cancelPayment(PaymentCancelRequest request, HttpSession session) {
        log.info("결제 취소 처리 시작 - paymentId: {}", request.getPaymentId());

        Long memberId = getMemberIdFromSession(session);

        // 1. 결제 정보 검증
        Payment payment = validatePaymentForCancel(request.getPaymentId(), memberId);

        try {
            // 2. PG 취소 처리
            PaymentStrategy strategy = getPaymentStrategyByPgType(payment.getPgType());
            PaymentResult result = strategy.cancel(request);

            if (result.isSuccess()) {
                // 취소 성공 처리
                return handleCancelSuccess(payment, result, session);
            } else {
                // 취소 실패
                log.warn("결제 취소 실패 - paymentId: {}, error: {}",
                        request.getPaymentId(), result.getMessage());
                throw new PaymentProcessException("결제 취소에 실패했습니다: " + result.getMessage());
            }

        } catch (Exception e) {
            log.error("결제 취소 처리 중 오류 발생", e);
            throw new PaymentProcessException("결제 취소 처리 중 시스템 오류가 발생했습니다");
        }
    }

    /**
     * 회원별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(HttpSession session, int page, int size) {
        Long memberId = getMemberIdFromSession(session);

        int offset = page * size;
        List<Payment> payments = paymentRepository.findByMemberId(memberId, offset, size);

        return payments.stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /**
     * 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(Long paymentId, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("결제 정보를 찾을 수 없습니다");
        }

        // 회원 권한 검증
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("주문 정보를 찾을 수 없습니다"));

        if (!order.getMemberId().equals(memberId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        return PaymentResponse.from(payment);
    }

    // === Private Methods ===

    /**
     * 주문 정보 검증
     */
    private Order validateOrderForPayment(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문 정보를 찾을 수 없습니다"));

        if (!order.getMemberId().equals(memberId)) {
            throw new UnauthorizedException("주문에 대한 권한이 없습니다");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentProcessException("결제 대기 상태가 아닌 주문입니다");
        }

        return order;
    }

    /**
     * 결제 정보 검증
     */
    private Payment validatePaymentForCancel(Long paymentId, Long memberId) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("결제 정보를 찾을 수 없습니다");
        }

        // 회원 권한 검증
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("주문 정보를 찾을 수 없습니다"));

        if (!order.getMemberId().equals(memberId)) {
            throw new UnauthorizedException("결제에 대한 권한이 없습니다");
        }

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new PaymentProcessException("승인된 결제만 취소할 수 있습니다");
        }

        return payment;
    }

    /**
     * 대기 상태 결제 정보 생성
     */
    private Payment createPendingPayment(PaymentApprovalRequest request, Order order) {
        return Payment.builder()
                .orderId(request.getOrderId())
                .paymentMethod(request.getPaymentMethod())
                .pgType(PgType.INICIS) // 현재는 이니시스만 지원
                .totalAmount(request.getTotalAmount())
                .cardAmount(request.getCardAmount() != null ? request.getCardAmount() : 0L)
                .pointAmount(request.getPointAmount() != null ? request.getPointAmount() : 0L)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 승인 성공 처리
     */
    private PaymentResponse handleApprovalSuccess(Payment payment, PaymentResult result, Order order) {
        log.info("결제 승인 성공 - paymentId: {}, pgTid: {}", payment.getId(), result.getPgTid());

        // 결제 정보 업데이트
        Payment approvedPayment = payment.approve(
                result.getPgTid(),
                result.getAuthCode(),
                result.getCardNumber(),
                result.getCardName()
        );
        paymentRepository.update(approvedPayment);

        // 주문 상태 업데이트
        orderRepository.updateStatus(order.getId(), OrderStatus.PAID);

        return PaymentResponse.from(approvedPayment);
    }

    /**
     * 승인 실패 처리
     */
    private PaymentResponse handleApprovalFailure(Payment payment, PaymentResult result,
                                                PaymentApprovalRequest request, HttpSession session) {
        log.warn("결제 승인 실패 - paymentId: {}, error: {}", payment.getId(), result.getMessage());

        // 포인트 복구
        if (request.hasPointAmount()) {
            try {
                pointService.restore(request.getPointAmount(), session);
                log.info("승인 실패로 인한 포인트 복구 완료 - amount: {}", request.getPointAmount());
            } catch (Exception e) {
                log.error("포인트 복구 실패", e);
            }
        }

        // 결제 상태를 실패로 업데이트
        Payment failedPayment = payment.fail();
        paymentRepository.update(failedPayment);

        throw new PaymentProcessException("결제 승인에 실패했습니다: " + result.getMessage());
    }

    /**
     * 취소 성공 처리
     */
    private PaymentResponse handleCancelSuccess(Payment payment, PaymentResult result, HttpSession session) {
        log.info("결제 취소 성공 - paymentId: {}, pgTid: {}", payment.getId(), payment.getPgTid());

        // 결제 상태 업데이트
        Payment cancelledPayment = payment.cancel();
        paymentRepository.update(cancelledPayment);

        // 주문 상태 업데이트
        orderRepository.updateStatus(payment.getOrderId(), OrderStatus.CANCELLED);

        // 포인트 복구 (혼합결제였던 경우)
        if (payment.getPointAmount() > 0) {
            try {
                pointService.restore(payment.getPointAmount(), session);
                log.info("취소로 인한 포인트 복구 완료 - amount: {}", payment.getPointAmount());
            } catch (Exception e) {
                log.error("포인트 복구 실패", e);
            }
        }

        return PaymentResponse.from(cancelledPayment);
    }

    /**
     * 결제 전략 조회 (요청 기반)
     */
    private PaymentStrategy getPaymentStrategy(PaymentApprovalRequest request) {
        // 현재는 이니시스만 지원
        String strategyKey = "inicisPaymentStrategy";
        PaymentStrategy strategy = paymentStrategies.get(strategyKey);

        if (strategy == null) {
            throw new PaymentProcessException("지원하지 않는 결제 방식입니다");
        }

        return strategy;
    }

    /**
     * 결제 전략 조회 (PG 타입 기반)
     */
    private PaymentStrategy getPaymentStrategyByPgType(PgType pgType) {
        String strategyKey = switch (pgType) {
            case INICIS -> "inicisPaymentStrategy";
            case TOSS -> throw new PaymentProcessException("토스페이먼츠는 아직 지원하지 않습니다");
            default -> throw new PaymentProcessException("지원하지 않는 PG사입니다: " + pgType);
        };

        PaymentStrategy strategy = paymentStrategies.get(strategyKey);
        if (strategy == null) {
            throw new PaymentProcessException("결제 전략을 찾을 수 없습니다: " + strategyKey);
        }

        return strategy;
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