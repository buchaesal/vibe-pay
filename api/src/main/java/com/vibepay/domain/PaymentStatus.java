package com.vibepay.domain;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    /**
     * 승인 대기
     */
    PENDING,

    /**
     * 승인 완료
     */
    APPROVED,

    /**
     * 취소됨
     */
    CANCELLED,

    /**
     * 승인 실패
     */
    FAILED
}