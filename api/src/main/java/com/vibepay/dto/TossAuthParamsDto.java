package com.vibepay.dto;

/**
 * 토스페이먼츠 인증 파라미터 응답 DTO
 * 토스페이먼츠 결제창에 필요한 모든 값들을 제공
 */
public class TossAuthParamsDto {

    /**
     * 토스페이먼츠 클라이언트 키
     */
    private String clientKey;

    /**
     * 주문번호 (유니크)
     */
    private String orderId;

    /**
     * 결제 금액
     */
    private Integer amount;

    /**
     * 주문명 (상품명)
     */
    private String orderName;

    /**
     * 구매자 이름
     */
    private String customerName;

    /**
     * 구매자 이메일
     */
    private String customerEmail;

    public TossAuthParamsDto() {}

    public TossAuthParamsDto(String clientKey, String orderId, Integer amount,
                             String orderName, String customerName, String customerEmail) {
        this.clientKey = clientKey;
        this.orderId = orderId;
        this.amount = amount;
        this.orderName = orderName;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    public String getClientKey() { return clientKey; }
    public String getOrderId() { return orderId; }
    public Integer getAmount() { return amount; }
    public String getOrderName() { return orderName; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }

    public void setClientKey(String clientKey) { this.clientKey = clientKey; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public void setOrderName(String orderName) { this.orderName = orderName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}
