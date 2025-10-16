package com.vibepay.repository;

import com.vibepay.domain.Order;
import com.vibepay.domain.OrderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 주문 Repository
 */
@Mapper
public interface OrderRepository {

    /**
     * 주문 생성
     */
    void save(Order order);

    /**
     * ID로 주문 조회
     */
    Optional<Order> findById(Long id);

    /**
     * 주문번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 회원의 주문 목록 조회 (최신순)
     */
    List<Order> findByMemberId(Long memberId);

    /**
     * 주문 상태 업데이트
     */
    void updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);

    /**
     * 결제 금액 업데이트
     */
    void updatePaymentAmounts(@Param("id") Long id,
                             @Param("pointAmount") Long pointAmount,
                             @Param("cardAmount") Long cardAmount);
}
