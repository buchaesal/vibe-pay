package com.vibepay.dto;

import com.vibepay.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponse {
    private Long id;
    private String orderNumber;
    private String productName;
    private Long totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public static OrderListResponse from(Order order) {
        return OrderListResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .productName(order.getProductName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
