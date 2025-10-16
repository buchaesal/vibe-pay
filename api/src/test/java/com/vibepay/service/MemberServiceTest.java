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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    @InjectMocks
    private MemberService memberService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private Member member;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest("test@example.com", "password123", "테스트");
        loginRequest = new LoginRequest("test@example.com", "password123");
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        given(memberRepository.findByEmail(signupRequest.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");

        // when
        MemberResponse response = memberService.signup(signupRequest);

        // then
        assertThat(response.getEmail()).isEqualTo(signupRequest.getEmail());
        assertThat(response.getName()).isEqualTo(signupRequest.getName());
        then(memberRepository).should(times(1)).findByEmail(signupRequest.getEmail());
        then(passwordEncoder).should(times(1)).encode(signupRequest.getPassword());
        then(memberRepository).should(times(1)).save(any(Member.class));
        then(pointRepository).should(times(1)).createInitialPoint(anyLong(), anyLong());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_duplicateEmail() {
        // given
        given(memberRepository.findByEmail(signupRequest.getEmail())).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> memberService.signup(signupRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다");

        then(memberRepository).should(times(1)).findByEmail(signupRequest.getEmail());
        then(passwordEncoder).should(never()).encode(anyString());
        then(memberRepository).should(never()).save(any(Member.class));
        then(pointRepository).should(never()).createInitialPoint(anyLong(), anyLong());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        given(memberRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())).willReturn(true);

        // when
        MemberResponse response = memberService.login(loginRequest, session);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getName()).isEqualTo(member.getName());
        then(memberRepository).should(times(1)).findByEmail(loginRequest.getEmail());
        then(passwordEncoder).should(times(1)).matches(loginRequest.getPassword(), member.getPassword());
        then(session).should(times(1)).setAttribute("memberId", member.getId());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_emailNotFound() {
        // given
        given(memberRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.login(loginRequest, session))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다");

        then(memberRepository).should(times(1)).findByEmail(loginRequest.getEmail());
        then(passwordEncoder).should(never()).matches(anyString(), anyString());
        then(session).should(never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        // given
        given(memberRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.login(loginRequest, session))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다");

        then(memberRepository).should(times(1)).findByEmail(loginRequest.getEmail());
        then(passwordEncoder).should(times(1)).matches(loginRequest.getPassword(), member.getPassword());
        then(session).should(never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        // when
        memberService.logout(session);

        // then
        then(session).should(times(1)).invalidate();
    }

    @Test
    @DisplayName("현재 사용자 조회 성공")
    void getCurrentMember_success() {
        // given
        given(session.getAttribute("memberId")).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getCurrentMember(session);

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getName()).isEqualTo(member.getName());
        then(session).should(times(1)).getAttribute("memberId");
        then(memberRepository).should(times(1)).findById(1L);
    }

    @Test
    @DisplayName("현재 사용자 조회 실패 - 세션에 memberId 없음")
    void getCurrentMember_fail_noMemberIdInSession() {
        // given
        given(session.getAttribute("memberId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> memberService.getCurrentMember(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요합니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(memberRepository).should(never()).findById(anyLong());
    }

    @Test
    @DisplayName("현재 사용자 조회 실패 - 회원 정보 없음")
    void getCurrentMember_fail_memberNotFound() {
        // given
        given(session.getAttribute("memberId")).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getCurrentMember(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요합니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(memberRepository).should(times(1)).findById(1L);
    }
}
