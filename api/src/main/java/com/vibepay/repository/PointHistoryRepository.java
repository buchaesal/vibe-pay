package com.vibepay.repository;

import com.vibepay.domain.PointHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointHistoryRepository {

    /**
     * 적립금 변동 이력 생성
     */
    void insertHistory(PointHistory pointHistory);

    /**
     * 회원의 적립금 변동 이력 조회 (최신순)
     */
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    /**
     * 특정 주문에 대한 적립금 변동 이력 조회
     */
    List<PointHistory> findByOrderNumber(@Param("orderNumber") String orderNumber);
}