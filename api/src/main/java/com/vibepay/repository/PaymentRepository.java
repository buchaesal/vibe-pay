package com.vibepay.repository;

import com.vibepay.domain.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 결제 정보 데이터 액세스 레포지토리
 */
@Mapper
public interface PaymentRepository {

    /**
     * 결제 정보 저장
     *
     * @param payment 결제 정보
     * @return 저장된 행 수
     */
    int save(Payment payment);

    /**
     * 결제 정보 업데이트
     *
     * @param payment 결제 정보
     * @return 업데이트된 행 수
     */
    int update(Payment payment);

    /**
     * ID로 결제 정보 조회
     *
     * @param id 결제 ID
     * @return 결제 정보
     */
    Payment findById(@Param("id") Long id);

    /**
     * 주문 ID로 결제 정보 조회
     *
     * @param orderId 주문 ID
     * @return 결제 정보 목록
     */
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    /**
     * PG 거래번호로 결제 정보 조회
     *
     * @param pgTid PG 거래번호
     * @return 결제 정보
     */
    Payment findByPgTid(@Param("pgTid") String pgTid);

    /**
     * 회원 ID로 결제 정보 목록 조회 (페이징)
     *
     * @param memberId 회원 ID
     * @param offset   조회 시작 위치
     * @param limit    조회 개수
     * @return 결제 정보 목록
     */
    List<Payment> findByMemberId(@Param("memberId") Long memberId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    /**
     * 회원별 결제 건수 조회
     *
     * @param memberId 회원 ID
     * @return 결제 건수
     */
    int countByMemberId(@Param("memberId") Long memberId);

    /**
     * 결제 상태별 목록 조회
     *
     * @param status 결제 상태
     * @param offset 조회 시작 위치
     * @param limit  조회 개수
     * @return 결제 정보 목록
     */
    List<Payment> findByStatus(@Param("status") String status,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    /**
     * 결제 정보 삭제 (물리적 삭제)
     *
     * @param id 결제 ID
     * @return 삭제된 행 수
     */
    int deleteById(@Param("id") Long id);
}