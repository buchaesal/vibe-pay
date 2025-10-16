package com.vibepay.dto;

import com.vibepay.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String productName;
    private Long productPrice;
    private Integer quantity;
    private Long totalAmount;
    private Long pointAmount;
    private Long cardAmount;
    private String status;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .productName(order.getProductName())
                .productPrice(order.getProductPrice())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .pointAmount(order.getPointAmount())
                .cardAmount(order.getCardAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
