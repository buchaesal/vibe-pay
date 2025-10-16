package com.vibepay.dto;

import com.vibepay.domain.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {
    private Long memberId;
    private Long balance;

    public static BalanceResponse from(Point point) {
        return BalanceResponse.builder()
                .memberId(point.getMemberId())
                .balance(point.getBalance())
                .build();
    }
}
