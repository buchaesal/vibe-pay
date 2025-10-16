package com.vibepay.dto;

import com.vibepay.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 승인 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentApprovalRequest {

    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    @NotNull(message = "결제 수단은 필수입니다")
    private PaymentMethod paymentMethod;

    @NotNull(message = "총 결제 금액은 필수입니다")
    @Positive(message = "총 결제 금액은 0보다 커야 합니다")
    private Long totalAmount;

    @Positive(message = "카드 결제 금액은 0보다 커야 합니다")
    private Long cardAmount;

    @Positive(message = "포인트 사용 금액은 0보다 커야 합니다")
    private Long pointAmount;

    // 이니시스 인증 응답값
    private String pgTid;
    private String mid;
    private String oid;
    private String price;
    private String currency;

    // 토스페이먼츠 인증 응답값
    private String paymentKey;

    /**
     * 카드 결제 금액 검증
     */
    public boolean hasCardAmount() {
        return cardAmount != null && cardAmount > 0;
    }

    /**
     * 포인트 사용 금액 검증
     */
    public boolean hasPointAmount() {
        return pointAmount != null && pointAmount > 0;
    }

    /**
     * 혼합 결제 여부 검증
     */
    public boolean isMixedPayment() {
        return hasCardAmount() && hasPointAmount();
    }
}