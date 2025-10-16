package com.vibepay.strategy;

import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 결제 전략 추상 클래스
 * 카드 결제 공통 로직을 처리하고, PG사별 구현은 하위 클래스에서 담당
 */
@Slf4j
public abstract class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult approve(PaymentApprovalRequest request) {
        log.info("카드 결제 승인 시작 - orderId: {}, amount: {}",
                request.getOrderId(), request.getTotalAmount());

        // 승인 전 검증
        PaymentResult validationResult = validateApprovalRequest(request);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // PG사별 승인 처리
        PaymentResult result = processApproval(request);

        // 승인 실패 시 망취소 처리
        if (!result.isSuccess() && result.getPgTid() != null) {
            log.warn("결제 승인 실패로 인한 망취소 처리 - pgTid: {}", result.getPgTid());
            networkCancel(result.getPgTid());
        }

        return result;
    }

    @Override
    public PaymentResult cancel(PaymentCancelRequest request) {
        log.info("카드 결제 취소 시작 - paymentId: {}, amount: {}",
                request.getPaymentId(), request.getCancelAmount());

        // 취소 전 검증
        PaymentResult validationResult = validateCancelRequest(request);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // PG사별 취소 처리
        return processCancel(request);
    }

    /**
     * 승인 요청 검증
     */
    protected PaymentResult validateApprovalRequest(PaymentApprovalRequest request) {
        if (request.getTotalAmount() <= 0) {
            return PaymentResult.failure("INVALID_AMOUNT", "결제 금액이 올바르지 않습니다");
        }

        if (request.hasCardAmount() && request.getCardAmount() <= 0) {
            return PaymentResult.failure("INVALID_CARD_AMOUNT", "카드 결제 금액이 올바르지 않습니다");
        }

        return PaymentResult.builder().success(true).build();
    }

    /**
     * 취소 요청 검증
     */
    protected PaymentResult validateCancelRequest(PaymentCancelRequest request) {
        if (request.getCancelAmount() != null && request.getCancelAmount() <= 0) {
            return PaymentResult.failure("INVALID_CANCEL_AMOUNT", "취소 금액이 올바르지 않습니다");
        }

        return PaymentResult.builder().success(true).build();
    }

    /**
     * PG사별 승인 처리 (하위 클래스에서 구현)
     */
    protected abstract PaymentResult processApproval(PaymentApprovalRequest request);

    /**
     * PG사별 취소 처리 (하위 클래스에서 구현)
     */
    protected abstract PaymentResult processCancel(PaymentCancelRequest request);
}