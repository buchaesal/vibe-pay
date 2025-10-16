package com.vibepay.exception;

/**
 * 이미 취소된 주문을 취소하려고 할 때 발생하는 예외
 */
public class OrderAlreadyCancelledException extends RuntimeException {
    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}
