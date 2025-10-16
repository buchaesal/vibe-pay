package com.vibepay.dto;

/**
 * PG 인증 파라미터 통합 응답 DTO
 * 프론트엔드에서 PG사를 구분하고 적절한 파라미터를 사용할 수 있도록 PG사 타입과 인증 파라미터를 함께 제공
 */
public class PgAuthResponse {

    /**
     * PG사 타입 ("INICIS" 또는 "TOSS")
     */
    private String pgType;

    /**
     * PG 인증 파라미터
     * INICIS의 경우 PgAuthParamsDto, TOSS의 경우 TossAuthParamsDto
     */
    private Object authParams;

    public PgAuthResponse() {}

    public PgAuthResponse(String pgType, Object authParams) {
        this.pgType = pgType;
        this.authParams = authParams;
    }

    public String getPgType() { return pgType; }
    public Object getAuthParams() { return authParams; }

    public void setPgType(String pgType) { this.pgType = pgType; }
    public void setAuthParams(Object authParams) { this.authParams = authParams; }
}
