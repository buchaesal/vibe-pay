package com.vibepay.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 도메인 모델
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private Long id;
    private Long memberId;
    private String orderNumber;

    // 상품 정보
    private String productName;
    private Long productPrice;
    private Integer quantity;

    // 금액 정보
    private Long totalAmount;
    private Long pointAmount;
    private Long cardAmount;

    // 주문 상태
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
