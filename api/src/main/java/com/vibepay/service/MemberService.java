package com.vibepay.service;

import com.vibepay.domain.Member;
import com.vibepay.dto.LoginRequest;
import com.vibepay.dto.MemberResponse;
import com.vibepay.dto.SignupRequest;
import com.vibepay.exception.DuplicateEmailException;
import com.vibepay.exception.InvalidCredentialsException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.MemberRepository;
import com.vibepay.repository.PointRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String SESSION_MEMBER_ID = "memberId";
    private static final Long INITIAL_POINT_BALANCE = 100000L;

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * - 이메일 중복 체크
     * - 비밀번호 암호화
     * - 회원 정보 저장
     * - 초기 적립금 100,000원 생성
     */
    @Transactional
    public MemberResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(member -> {
                    throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
                });

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 회원 정보 저장
        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .build();

        memberRepository.save(member);

        // 초기 적립금 생성
        pointRepository.createInitialPoint(member.getId(), INITIAL_POINT_BALANCE);

        return MemberResponse.from(member);
    }

    /**
     * 로그인
     * - 이메일로 회원 조회
     * - 비밀번호 검증
     * - 세션에 memberId 저장
     */
    @Transactional(readOnly = true)
    public MemberResponse login(LoginRequest request, HttpSession session) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 세션에 memberId 저장
        session.setAttribute(SESSION_MEMBER_ID, member.getId());

        return MemberResponse.from(member);
    }

    /**
     * 로그아웃
     * - 세션 무효화
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }

    /**
     * 현재 로그인한 사용자 조회
     * - 세션에서 memberId 가져오기
     * - 회원 정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getCurrentMember(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SESSION_MEMBER_ID);

        if (memberId == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다"));

        return MemberResponse.from(member);
    }
}
