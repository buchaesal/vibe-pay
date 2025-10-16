package com.vibepay.dto;

import com.vibepay.domain.PointHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryResponse {
    private Long id;
    private String transactionType;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String orderNumber;
    private String description;
    private LocalDateTime createdAt;

    public static PointHistoryResponse from(PointHistory pointHistory) {
        return PointHistoryResponse.builder()
                .id(pointHistory.getId())
                .transactionType(pointHistory.getTransactionType())
                .amount(pointHistory.getAmount())
                .balanceBefore(pointHistory.getBalanceBefore())
                .balanceAfter(pointHistory.getBalanceAfter())
                .orderNumber(pointHistory.getOrderNumber())
                .description(pointHistory.getDescription())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }

    public static List<PointHistoryResponse> from(List<PointHistory> pointHistories) {
        return pointHistories.stream()
                .map(PointHistoryResponse::from)
                .collect(Collectors.toList());
    }
}