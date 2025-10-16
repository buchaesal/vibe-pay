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
public class Point {
    private Long id;
    private Long memberId;
    private Long balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
