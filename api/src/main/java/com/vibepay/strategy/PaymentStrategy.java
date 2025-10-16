package com.vibepay.strategy;

import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;

/**
 * 결제 전략 인터페이스
 * 다양한 PG사별 결제 로직을 추상화
 */
public interface PaymentStrategy {

    /**
     * 결제 승인 처리
     *
     * @param request 결제 승인 요청 정보
     * @return 결제 승인 결과
     */
    PaymentResult approve(PaymentApprovalRequest request);

    /**
     * 결제 취소 처리
     *
     * @param request 결제 취소 요청 정보
     * @return 결제 취소 결과
     */
    PaymentResult cancel(PaymentCancelRequest request);

    /**
     * 망취소 처리 (승인 실패 시 자동 호출)
     *
     * @param pgTid PG사 거래번호
     * @return 망취소 결과
     */
    PaymentResult networkCancel(String pgTid);

    /**
     * 지원하는 PG사 타입 반환
     *
     * @return PG사 타입
     */
    String getSupportedPgType();
}