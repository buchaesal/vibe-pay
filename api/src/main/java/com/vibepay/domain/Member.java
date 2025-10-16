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
public class Member {
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
