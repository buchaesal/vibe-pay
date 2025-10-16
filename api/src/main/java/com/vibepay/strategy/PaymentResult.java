package com.vibepay.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 처리 결과
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResult {

    private boolean success;
    private String message;
    private String errorCode;

    // PG사 응답 정보
    private String pgTid;           // PG사 거래번호
    private String authCode;        // 승인번호
    private String cardNumber;      // 카드번호
    private String cardName;        // 카드사명
    private Long approvedAmount;    // 승인금액

    /**
     * 성공 결과 생성
     */
    public static PaymentResult success(String pgTid, String authCode, String cardNumber,
                                      String cardName, Long approvedAmount) {
        return PaymentResult.builder()
                .success(true)
                .message("결제가 성공적으로 처리되었습니다")
                .pgTid(pgTid)
                .authCode(authCode)
                .cardNumber(cardNumber)
                .cardName(cardName)
                .approvedAmount(approvedAmount)
                .build();
    }

    /**
     * 실패 결과 생성
     */
    public static PaymentResult failure(String errorCode, String message) {
        return PaymentResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    /**
     * 취소 성공 결과 생성
     */
    public static PaymentResult cancelSuccess(String pgTid, Long cancelledAmount) {
        return PaymentResult.builder()
                .success(true)
                .message("결제가 성공적으로 취소되었습니다")
                .pgTid(pgTid)
                .approvedAmount(cancelledAmount)
                .build();
    }
}