package com.vibepay.domain;

/**
 * 주문 상태
 */
public enum OrderStatus {
    /**
     * 결제 대기
     */
    PENDING,

    /**
     * 결제 완료
     */
    PAID,

    /**
     * 취소됨
     */
    CANCELLED
}
