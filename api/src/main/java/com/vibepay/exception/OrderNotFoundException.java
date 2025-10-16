package com.vibepay.exception;

/**
 * 주문을 찾을 수 없는 경우 발생하는 예외
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
