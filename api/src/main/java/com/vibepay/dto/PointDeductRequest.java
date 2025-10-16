package com.vibepay.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointDeductRequest {

    @NotNull(message = "차감할 금액은 필수입니다")
    @Min(value = 1, message = "차감할 금액은 1원 이상이어야 합니다")
    private Long amount;
}
