package com.vibepay.service;

import com.vibepay.domain.Point;
import com.vibepay.dto.BalanceResponse;
import com.vibepay.exception.InsufficientBalanceException;
import com.vibepay.exception.PointNotFoundException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.PointRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PointService pointService;

    private Point point;
    private Long memberId;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        point = Point.builder()
                .id(1L)
                .memberId(memberId)
                .balance(100000L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("잔액 조회 성공")
    void getBalance_success() {
        // given
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));

        // when
        BalanceResponse response = pointService.getBalance(session);

        // then
        assertThat(response.getMemberId()).isEqualTo(memberId);
        assertThat(response.getBalance()).isEqualTo(100000L);
        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("잔액 조회 실패 - 세션에 memberId 없음")
    void getBalance_fail_noMemberIdInSession() {
        // given
        given(session.getAttribute("memberId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> pointService.getBalance(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요합니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(never()).findByMemberId(anyLong());
    }

    @Test
    @DisplayName("잔액 조회 실패 - 적립금 정보 없음")
    void getBalance_fail_pointNotFound() {
        // given
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.getBalance(session))
                .isInstanceOf(PointNotFoundException.class)
                .hasMessage("적립금 정보를 찾을 수 없습니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("적립금 차감 성공")
    void deduct_success() {
        // given
        Long deductAmount = 30000L;
        Point updatedPoint = Point.builder()
                .id(1L)
                .memberId(memberId)
                .balance(70000L)
                .createdAt(point.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId))
                .willReturn(Optional.of(point))
                .willReturn(Optional.of(updatedPoint));

        // when
        BalanceResponse response = pointService.deduct(deductAmount, session);

        // then
        assertThat(response.getMemberId()).isEqualTo(memberId);
        assertThat(response.getBalance()).isEqualTo(70000L);
        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(2)).findByMemberId(memberId);
        then(pointRepository).should(times(1)).deductBalance(memberId, deductAmount);
    }

    @Test
    @DisplayName("적립금 차감 실패 - 잔액 부족")
    void deduct_fail_insufficientBalance() {
        // given
        Long deductAmount = 150000L;
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));

        // when & then
        assertThatThrownBy(() -> pointService.deduct(deductAmount, session))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("잔액이 부족합니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(1)).findByMemberId(memberId);
        then(pointRepository).should(never()).deductBalance(anyLong(), anyLong());
    }

    @Test
    @DisplayName("적립금 차감 실패 - 적립금 정보 없음")
    void deduct_fail_pointNotFound() {
        // given
        Long deductAmount = 30000L;
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.deduct(deductAmount, session))
                .isInstanceOf(PointNotFoundException.class)
                .hasMessage("적립금 정보를 찾을 수 없습니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(1)).findByMemberId(memberId);
        then(pointRepository).should(never()).deductBalance(anyLong(), anyLong());
    }

    @Test
    @DisplayName("적립금 복구 성공")
    void restore_success() {
        // given
        Long restoreAmount = 50000L;
        Point updatedPoint = Point.builder()
                .id(1L)
                .memberId(memberId)
                .balance(150000L)
                .createdAt(point.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId))
                .willReturn(Optional.of(point))
                .willReturn(Optional.of(updatedPoint));

        // when
        BalanceResponse response = pointService.restore(restoreAmount, session);

        // then
        assertThat(response.getMemberId()).isEqualTo(memberId);
        assertThat(response.getBalance()).isEqualTo(150000L);
        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(2)).findByMemberId(memberId);
        then(pointRepository).should(times(1)).restoreBalance(memberId, restoreAmount);
    }

    @Test
    @DisplayName("적립금 복구 실패 - 적립금 정보 없음")
    void restore_fail_pointNotFound() {
        // given
        Long restoreAmount = 50000L;
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.restore(restoreAmount, session))
                .isInstanceOf(PointNotFoundException.class)
                .hasMessage("적립금 정보를 찾을 수 없습니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(pointRepository).should(times(1)).findByMemberId(memberId);
        then(pointRepository).should(never()).restoreBalance(anyLong(), anyLong());
    }
}
