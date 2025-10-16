package com.vibepay.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 도메인 모델
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PgType pgType;

    // 결제 금액 정보
    private Long totalAmount;
    private Long cardAmount;
    private Long pointAmount;

    // PG 관련 정보
    private String pgTid;           // PG사 거래번호
    private String authCode;        // 승인번호
    private String cardNumber;      // 카드번호 (마스킹)
    private String cardName;        // 카드사명

    // 상태 정보
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 결제 승인 완료 처리
     */
    public Payment approve(String pgTid, String authCode, String cardNumber, String cardName) {
        return Payment.builder()
                .id(this.id)
                .orderId(this.orderId)
                .paymentMethod(this.paymentMethod)
                .pgType(this.pgType)
                .totalAmount(this.totalAmount)
                .cardAmount(this.cardAmount)
                .pointAmount(this.pointAmount)
                .pgTid(pgTid)
                .authCode(authCode)
                .cardNumber(maskCardNumber(cardNumber))
                .cardName(cardName)
                .status(PaymentStatus.APPROVED)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 결제 취소 처리
     */
    public Payment cancel() {
        return Payment.builder()
                .id(this.id)
                .orderId(this.orderId)
                .paymentMethod(this.paymentMethod)
                .pgType(this.pgType)
                .totalAmount(this.totalAmount)
                .cardAmount(this.cardAmount)
                .pointAmount(this.pointAmount)
                .pgTid(this.pgTid)
                .authCode(this.authCode)
                .cardNumber(this.cardNumber)
                .cardName(this.cardName)
                .status(PaymentStatus.CANCELLED)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 결제 실패 처리
     */
    public Payment fail() {
        return Payment.builder()
                .id(this.id)
                .orderId(this.orderId)
                .paymentMethod(this.paymentMethod)
                .pgType(this.pgType)
                .totalAmount(this.totalAmount)
                .cardAmount(this.cardAmount)
                .pointAmount(this.pointAmount)
                .pgTid(this.pgTid)
                .authCode(this.authCode)
                .cardNumber(this.cardNumber)
                .cardName(this.cardName)
                .status(PaymentStatus.FAILED)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 카드번호 마스킹 처리
     * 앞 6자리와 뒤 4자리만 보여주고 나머지는 *로 처리
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return cardNumber;
        }

        String front = cardNumber.substring(0, 6);
        String back = cardNumber.substring(cardNumber.length() - 4);
        String middle = "*".repeat(cardNumber.length() - 10);

        return front + middle + back;
    }
}