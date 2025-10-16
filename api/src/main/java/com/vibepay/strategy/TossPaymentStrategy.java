package com.vibepay.strategy;

import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import com.vibepay.repository.PaymentRepository;
import com.vibepay.util.TossPaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 토스페이먼츠 결제 전략 구현체
 * 토스페이먼츠 PG사를 통한 카드 결제 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentStrategy extends CardPaymentStrategy {

    private final TossPaymentUtil tossPaymentUtil;
    private final PaymentRepository paymentRepository;

    @Override
    protected PaymentResult processApproval(PaymentApprovalRequest request) {
        try {
            // 토스페이먼츠 승인 API 호출
            Map<String, Object> response = tossPaymentUtil.requestApproval(
                    request.getPaymentKey(),
                    request.getOid(),
                    request.getTotalAmount()
            );

            // 응답 결과 처리
            String status = (String) response.get("status");

            if ("DONE".equals(status)) {
                // 승인 성공
                String paymentKey = (String) response.get("paymentKey");
                String approvedAt = (String) response.get("approvedAt");

                // 카드 정보 추출
                Map<String, Object> card = (Map<String, Object>) response.get("card");
                String cardNumber = card != null ? (String) card.get("number") : null;
                String cardIssuerCode = card != null ? (String) card.get("issuerCode") : null;

                // totalAmount는 Integer로 반환될 수 있음
                Object totalAmountObj = response.get("totalAmount");
                Long totalAmount = totalAmountObj instanceof Integer ?
                        ((Integer) totalAmountObj).longValue() :
                        (Long) totalAmountObj;

                log.info("토스페이먼츠 결제 승인 성공 - paymentKey: {}, approvedAt: {}",
                        paymentKey, approvedAt);

                return PaymentResult.success(
                        paymentKey,
                        approvedAt,
                        cardNumber,
                        cardIssuerCode,
                        totalAmount
                );
            } else {
                // 승인 실패
                String code = (String) response.get("code");
                String message = (String) response.get("message");
                log.warn("토스페이먼츠 결제 승인 실패 - code: {}, message: {}", code, message);
                return PaymentResult.failure(code, message);
            }

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 처리 중 오류 발생", e);
            return PaymentResult.failure("SYSTEM_ERROR", "시스템 오류가 발생했습니다");
        }
    }

    @Override
    protected PaymentResult processCancel(PaymentCancelRequest request) {
        try {
            // 기존 결제 정보 조회
            var payment = paymentRepository.findById(request.getPaymentId());
            if (payment == null) {
                return PaymentResult.failure("PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다");
            }

            // 취소 금액 결정 (부분취소 지원)
            Long cancelAmount = request.getCancelAmount() != null ?
                    request.getCancelAmount() : payment.getTotalAmount();

            // 토스페이먼츠 취소 API 호출
            Map<String, Object> response = tossPaymentUtil.requestCancel(
                    payment.getPgTid(),
                    request.getCancelReason(),
                    cancelAmount
            );

            // 응답 결과 처리
            String status = (String) response.get("status");

            if ("CANCELED".equals(status) || "PARTIAL_CANCELED".equals(status)) {
                // 취소 성공
                String paymentKey = (String) response.get("paymentKey");

                // cancels 배열에서 취소 금액 추출
                Object cancelsObj = response.get("cancels");
                Long actualCancelAmount = cancelAmount;

                if (cancelsObj instanceof java.util.List) {
                    java.util.List<Map<String, Object>> cancels = (java.util.List<Map<String, Object>>) cancelsObj;
                    if (!cancels.isEmpty()) {
                        Map<String, Object> lastCancel = cancels.get(cancels.size() - 1);
                        Object cancelAmountObj = lastCancel.get("cancelAmount");
                        actualCancelAmount = cancelAmountObj instanceof Integer ?
                                ((Integer) cancelAmountObj).longValue() :
                                (Long) cancelAmountObj;
                    }
                }

                log.info("토스페이먼츠 결제 취소 성공 - paymentKey: {}, cancelAmount: {}",
                        paymentKey, actualCancelAmount);

                return PaymentResult.cancelSuccess(
                        paymentKey,
                        actualCancelAmount
                );
            } else {
                // 취소 실패
                String code = (String) response.get("code");
                String message = (String) response.get("message");
                log.warn("토스페이먼츠 결제 취소 실패 - code: {}, message: {}", code, message);
                return PaymentResult.failure(code, message);
            }

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 처리 중 오류 발생", e);
            return PaymentResult.failure("SYSTEM_ERROR", "시스템 오류가 발생했습니다");
        }
    }

    @Override
    public PaymentResult networkCancel(String pgTid) {
        try {
            log.info("토스페이먼츠 망취소 처리 - paymentKey: {}", pgTid);

            // 토스페이먼츠 망취소 API 호출
            Map<String, Object> response = tossPaymentUtil.requestNetworkCancel(pgTid);

            // 응답 결과 처리
            String status = (String) response.get("status");

            if ("CANCELED".equals(status)) {
                log.info("토스페이먼츠 망취소 성공 - paymentKey: {}", pgTid);
                return PaymentResult.builder()
                        .success(true)
                        .message("망취소가 성공적으로 처리되었습니다")
                        .pgTid(pgTid)
                        .build();
            } else {
                String code = (String) response.get("code");
                String message = (String) response.get("message");
                log.warn("토스페이먼츠 망취소 실패 - code: {}, message: {}", code, message);
                return PaymentResult.failure(code, message);
            }

        } catch (Exception e) {
            log.error("토스페이먼츠 망취소 처리 중 오류 발생", e);
            return PaymentResult.failure("SYSTEM_ERROR", "망취소 처리 중 시스템 오류가 발생했습니다");
        }
    }

    @Override
    public String getSupportedPgType() {
        return "TOSS";
    }
}
