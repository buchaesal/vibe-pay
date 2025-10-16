package com.vibepay.dto;

import com.vibepay.domain.Payment;
import com.vibepay.domain.PaymentMethod;
import com.vibepay.domain.PaymentStatus;
import com.vibepay.domain.PgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PgType pgType;

    // 결제 금액 정보
    private Long totalAmount;
    private Long cardAmount;
    private Long pointAmount;

    // PG 관련 정보
    private String pgTid;
    private String authCode;
    private String cardNumber;  // 마스킹된 카드번호
    private String cardName;

    // 상태 정보
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Payment 도메인 객체로부터 PaymentResponse 생성
     */
    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .pgType(payment.getPgType())
                .totalAmount(payment.getTotalAmount())
                .cardAmount(payment.getCardAmount())
                .pointAmount(payment.getPointAmount())
                .pgTid(payment.getPgTid())
                .authCode(payment.getAuthCode())
                .cardNumber(payment.getCardNumber())
                .cardName(payment.getCardName())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}