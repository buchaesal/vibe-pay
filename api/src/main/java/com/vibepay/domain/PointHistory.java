package com.vibepay.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {
    private Long id;
    private Long memberId;
    private Long pointId;
    private String transactionType;  // EARN, USE, RESTORE
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String orderNumber;
    private String description;
    private LocalDateTime createdAt;
}