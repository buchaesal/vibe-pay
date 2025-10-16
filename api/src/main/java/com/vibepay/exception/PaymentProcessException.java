package com.vibepay.exception;

/**
 * 결제 처리 중 발생하는 비즈니스 예외
 */
public class PaymentProcessException extends RuntimeException {
    public PaymentProcessException(String message) {
        super(message);
    }

    public PaymentProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}