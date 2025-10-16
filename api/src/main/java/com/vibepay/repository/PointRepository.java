package com.vibepay.repository;

import com.vibepay.domain.Point;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface PointRepository {

    /**
     * 회원가입 시 초기 적립금 생성
     */
    void createInitialPoint(@Param("memberId") Long memberId, @Param("balance") Long balance);

    /**
     * 회원의 적립금 조회
     */
    Optional<Point> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 잔액 업데이트
     */
    void updateBalance(@Param("memberId") Long memberId, @Param("balance") Long balance);

    /**
     * 잔액 차감
     */
    void deductBalance(@Param("memberId") Long memberId, @Param("amount") Long amount);

    /**
     * 잔액 복구
     */
    void restoreBalance(@Param("memberId") Long memberId, @Param("amount") Long amount);
}
