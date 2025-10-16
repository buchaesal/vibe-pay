package com.vibepay.strategy;

import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import com.vibepay.repository.PaymentRepository;
import com.vibepay.util.InicisPaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 이니시스 결제 전략 구현체
 * 이니시스 PG사를 통한 카드 결제 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InicisPaymentStrategy extends CardPaymentStrategy {

    private final InicisPaymentUtil inicisPaymentUtil;
    private final PaymentRepository paymentRepository;

    @Override
    protected PaymentResult processApproval(PaymentApprovalRequest request) {
        try {
            // 이니시스 승인 API 호출
            Map<String, String> response = inicisPaymentUtil.requestApproval(
                    request.getPgTid(),
                    request.getOid(),
                    request.getPrice(),
                    request.getCurrency()
            );

            // 응답 결과 처리
            String resultCode = response.get("resultcode");
            String resultMsg = response.get("resultmsg");

            if ("00".equals(resultCode)) {
                // 승인 성공
                log.info("이니시스 결제 승인 성공 - pgTid: {}, authCode: {}",
                        response.get("tid"), response.get("authcode"));

                return PaymentResult.success(
                        response.get("tid"),
                        response.get("authcode"),
                        response.get("cardnumber"),
                        response.get("cardname"),
                        Long.parseLong(response.get("price"))
                );
            } else {
                // 승인 실패
                log.warn("이니시스 결제 승인 실패 - resultCode: {}, resultMsg: {}", resultCode, resultMsg);
                return PaymentResult.failure(resultCode, resultMsg);
            }

        } catch (Exception e) {
            log.error("이니시스 결제 승인 처리 중 오류 발생", e);
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

            // 이니시스 취소 API 호출
            Map<String, String> response = inicisPaymentUtil.requestCancel(
                    payment.getPgTid(),
                    String.valueOf(cancelAmount),
                    request.getCancelReason()
            );

            // 응답 결과 처리
            String resultCode = response.get("resultcode");
            String resultMsg = response.get("resultmsg");

            if ("00".equals(resultCode)) {
                // 취소 성공
                log.info("이니시스 결제 취소 성공 - pgTid: {}, cancelAmount: {}",
                        payment.getPgTid(), cancelAmount);

                return PaymentResult.cancelSuccess(
                        response.get("tid"),
                        Long.parseLong(response.get("price"))
                );
            } else {
                // 취소 실패
                log.warn("이니시스 결제 취소 실패 - resultCode: {}, resultMsg: {}", resultCode, resultMsg);
                return PaymentResult.failure(resultCode, resultMsg);
            }

        } catch (Exception e) {
            log.error("이니시스 결제 취소 처리 중 오류 발생", e);
            return PaymentResult.failure("SYSTEM_ERROR", "시스템 오류가 발생했습니다");
        }
    }

    @Override
    public PaymentResult networkCancel(String pgTid) {
        try {
            log.info("이니시스 망취소 처리 - pgTid: {}", pgTid);

            // 이니시스 망취소 API 호출
            Map<String, String> response = inicisPaymentUtil.requestNetworkCancel(pgTid);

            // 응답 결과 처리
            String resultCode = response.get("resultcode");
            String resultMsg = response.get("resultmsg");

            if ("00".equals(resultCode)) {
                log.info("이니시스 망취소 성공 - pgTid: {}", pgTid);
                return PaymentResult.builder()
                        .success(true)
                        .message("망취소가 성공적으로 처리되었습니다")
                        .pgTid(pgTid)
                        .build();
            } else {
                log.warn("이니시스 망취소 실패 - resultCode: {}, resultMsg: {}", resultCode, resultMsg);
                return PaymentResult.failure(resultCode, resultMsg);
            }

        } catch (Exception e) {
            log.error("이니시스 망취소 처리 중 오류 발생", e);
            return PaymentResult.failure("SYSTEM_ERROR", "망취소 처리 중 시스템 오류가 발생했습니다");
        }
    }

    @Override
    public String getSupportedPgType() {
        return "INICIS";
    }
}