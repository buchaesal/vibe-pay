package com.vibepay.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotBlank(message = "상품명은 필수입니다")
    private String productName;

    @NotNull(message = "상품 가격은 필수입니다")
    @Min(value = 1, message = "상품 가격은 1원 이상이어야 합니다")
    private Long productPrice;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;
}
