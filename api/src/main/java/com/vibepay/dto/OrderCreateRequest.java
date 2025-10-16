package com.vibepay.dto;

import com.vibepay.domain.PaymentMethod;
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

    @NotBlank(message = "주문번호는 필수입니다")
    private String orderNumber;

    @NotNull(message = "결제 수단은 필수입니다")
    private PaymentMethod paymentMethod;

    private Long pointAmount;

    private Long cardAmount;

    private String pgAuthToken;

    private String pgTid;

    private String mid;

    private String price;

    private String currency;

    /**
     * 요청 검증
     * - CARD/MIXED 결제 시 pgAuthToken, pgTid, mid, price, currency 필수
     * - totalAmount = pointAmount + cardAmount
     * - cardAmount >= 100 (카드 결제가 있는 경우)
     */
    public void validate(Long totalAmount) {
        // CARD 또는 MIXED 결제 시 PG 정보 필수
        if (paymentMethod == PaymentMethod.CARD || paymentMethod == PaymentMethod.MIXED) {
            if (pgAuthToken == null || pgAuthToken.isBlank()) {
                throw new IllegalArgumentException("카드 결제 시 PG 인증 토큰은 필수입니다");
            }
            if (pgTid == null || pgTid.isBlank()) {
                throw new IllegalArgumentException("카드 결제 시 PG 거래 ID는 필수입니다");
            }
            if (mid == null || mid.isBlank()) {
                throw new IllegalArgumentException("카드 결제 시 상점 ID는 필수입니다");
            }
            if (price == null || price.isBlank()) {
                throw new IllegalArgumentException("카드 결제 시 결제 금액은 필수입니다");
            }
            if (currency == null || currency.isBlank()) {
                throw new IllegalArgumentException("카드 결제 시 통화 코드는 필수입니다");
            }
        }

        long point = pointAmount != null ? pointAmount : 0L;
        long card = cardAmount != null ? cardAmount : 0L;

        // 총 금액 검증
        if (point + card != totalAmount) {
            throw new IllegalArgumentException("결제 금액(포인트 + 카드)이 총 주문 금액과 일치하지 않습니다");
        }

        // 카드 결제 최소 금액 검증
        if (card > 0 && card < 100) {
            throw new IllegalArgumentException("카드 결제 금액은 100원 이상이어야 합니다");
        }
    }

    /**
     * pointAmount getter (null-safe, 기본값 0)
     */
    public Long getPointAmount() {
        return pointAmount != null ? pointAmount : 0L;
    }

    /**
     * cardAmount getter (null-safe, 기본값 0)
     */
    public Long getCardAmount() {
        return cardAmount != null ? cardAmount : 0L;
    }
}
