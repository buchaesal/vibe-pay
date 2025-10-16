package com.vibepay.domain;

/**
 * 결제 수단
 */
public enum PaymentMethod {
    /**
     * 카드 결제
     */
    CARD,

    /**
     * 포인트 결제
     */
    POINT,

    /**
     * 혼합 결제 (카드 + 포인트)
     */
    MIXED
}