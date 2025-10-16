package com.vibepay.exception;

/**
 * 결제 정보를 찾을 수 없을 때 발생하는 예외
 */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}