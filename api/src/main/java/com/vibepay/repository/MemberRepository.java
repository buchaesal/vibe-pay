package com.vibepay.repository;

import com.vibepay.domain.Member;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MemberRepository {

    /**
     * 이메일로 회원 조회
     */
    Optional<Member> findByEmail(String email);

    /**
     * 회원 정보 저장
     */
    void save(Member member);

    /**
     * ID로 회원 조회
     */
    Optional<Member> findById(Long id);
}
