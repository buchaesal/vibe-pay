package com.vibepay.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 취소 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCancelRequest {

    @NotNull(message = "결제 ID는 필수입니다")
    private Long paymentId;

    @NotNull(message = "취소 사유는 필수입니다")
    private String cancelReason;

    @Positive(message = "취소 금액은 0보다 커야 합니다")
    private Long cancelAmount;
}