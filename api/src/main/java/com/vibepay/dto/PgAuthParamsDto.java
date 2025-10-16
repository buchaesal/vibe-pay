package com.vibepay.dto;

/**
 * PG 인증 파라미터 응답 DTO
 * 이니시스 결제 인증창에 필요한 모든 값들을 제공
 */
public class PgAuthParamsDto {

    /**
     * 상점 아이디
     */
    private String mid;

    /**
     * 주문번호 (유니크)
     */
    private String oid;

    /**
     * 결제 금액
     */
    private Integer price;

    /**
     * 타임스탬프 (yyyyMMddHHmmss)
     */
    private String timestamp;

    /**
     * 해시 서명값
     */
    private String signature;

    /**
     * 이니시스 mKey
     */
    private String mKey;

    /**
     * 통화 코드 (기본: WON)
     */
    private String currency;

    /**
     * 구매자 이름
     */
    private String buyername;

    /**
     * 구매자 이메일
     */
    private String buyeremail;

    /**
     * 구매자 연락처
     */
    private String buyertel;

    /**
     * 상품명
     */
    private String goodname;

    public PgAuthParamsDto() {}

    public PgAuthParamsDto(String mid, String oid, Integer price, String timestamp, String signature,
                          String mKey, String currency, String buyername, String buyeremail,
                          String buyertel, String goodname) {
        this.mid = mid;
        this.oid = oid;
        this.price = price;
        this.timestamp = timestamp;
        this.signature = signature;
        this.mKey = mKey;
        this.currency = currency;
        this.buyername = buyername;
        this.buyeremail = buyeremail;
        this.buyertel = buyertel;
        this.goodname = goodname;
    }

    public String getMid() { return mid; }
    public String getOid() { return oid; }
    public Integer getPrice() { return price; }
    public String getTimestamp() { return timestamp; }
    public String getSignature() { return signature; }
    public String getmKey() { return mKey; }
    public String getCurrency() { return currency; }
    public String getBuyername() { return buyername; }
    public String getBuyeremail() { return buyeremail; }
    public String getBuyertel() { return buyertel; }
    public String getGoodname() { return goodname; }

    public void setMid(String mid) { this.mid = mid; }
    public void setOid(String oid) { this.oid = oid; }
    public void setPrice(Integer price) { this.price = price; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setmKey(String mKey) { this.mKey = mKey; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setBuyername(String buyername) { this.buyername = buyername; }
    public void setBuyeremail(String buyeremail) { this.buyeremail = buyeremail; }
    public void setBuyertel(String buyertel) { this.buyertel = buyertel; }
    public void setGoodname(String goodname) { this.goodname = goodname; }
}