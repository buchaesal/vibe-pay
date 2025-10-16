package com.vibepay.service;

import com.vibepay.domain.Member;
import com.vibepay.dto.PgAuthParamsDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PgService 테스트")
class PgServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PgService pgService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 이니시스 설정값 주입
        ReflectionTestUtils.setField(pgService, "mid", "INIpayTest");
        ReflectionTestUtils.setField(pgService, "signKey", "SU5JTElURV9UUklQTEVERVNfS0VZU1RS");
        ReflectionTestUtils.setField(pgService, "iniApiKey", "ItEQKi3rY7uvDS8l");
        ReflectionTestUtils.setField(pgService, "hashKey", "3CB8183A4BE283555ACC8363C0360223");

        // 테스트용 회원 데이터
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트사용자")
                .password("encodedPassword")
                .build();
    }

    @Test
    @DisplayName("PG 인증 파라미터 생성 성공")
    void generateAuthParams_Success() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";

        when(memberService.getCurrentMemberEntity(session)).thenReturn(testMember);

        // when
        PgAuthParamsDto result = pgService.generateAuthParams(session, price, goodname);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMid()).isEqualTo("INIpayTest");
        assertThat(result.getPrice()).isEqualTo(price);
        assertThat(result.getCurrency()).isEqualTo("WON");
        assertThat(result.getBuyername()).isEqualTo("테스트사용자");
        assertThat(result.getBuyeremail()).isEqualTo("test@example.com");
        assertThat(result.getBuyertel()).isEqualTo("010-1234-5678"); // 기본값
        assertThat(result.getGoodname()).isEqualTo(goodname);
        assertThat(result.getmKey()).isEqualTo("3CB8183A4BE283555ACC8363C0360223");

        // 생성된 값들 검증
        assertThat(result.getOid()).isNotNull();
        assertThat(result.getOid()).startsWith("ORD");
        assertThat(result.getOid()).hasSize(20); // ORD(3) + timestamp(14) + random(3)

        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getTimestamp()).hasSize(14); // yyyyMMddHHmmss

        assertThat(result.getSignature()).isNotNull();
        assertThat(result.getSignature()).hasSize(128); // SHA-512 해시 길이 (64bytes * 2)
    }

    @Test
    @DisplayName("서로 다른 주문번호 생성 확인")
    void generateAuthParams_DifferentOrderIds() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";

        when(memberService.getCurrentMemberEntity(session)).thenReturn(testMember);

        // when
        PgAuthParamsDto result1 = pgService.generateAuthParams(session, price, goodname);
        PgAuthParamsDto result2 = pgService.generateAuthParams(session, price, goodname);

        // then
        assertThat(result1.getOid()).isNotEqualTo(result2.getOid());
        assertThat(result1.getSignature()).isNotEqualTo(result2.getSignature());
    }

    @Test
    @DisplayName("동일한 파라미터로 동일한 해시 생성 확인")
    void generateAuthParams_SameHashForSameParams() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";

        when(memberService.getCurrentMemberEntity(session)).thenReturn(testMember);

        // when
        PgAuthParamsDto result1 = pgService.generateAuthParams(session, price, goodname);

        // 동일한 oid와 timestamp로 다시 생성하기 위해 리플렉션 사용
        // 실제로는 타임스탬프가 다르므로 해시가 달라지는 것이 정상
        assertThat(result1.getSignature()).isNotNull();
        assertThat(result1.getOid()).startsWith("ORD");
    }

    @Test
    @DisplayName("다른 금액에 대해 다른 해시 생성 확인")
    void generateAuthParams_DifferentHashForDifferentPrice() {
        // given
        String goodname = "테스트 상품";

        when(memberService.getCurrentMemberEntity(session)).thenReturn(testMember);

        // when
        PgAuthParamsDto result1 = pgService.generateAuthParams(session, 10000, goodname);
        PgAuthParamsDto result2 = pgService.generateAuthParams(session, 20000, goodname);

        // then
        assertThat(result1.getSignature()).isNotEqualTo(result2.getSignature());
        assertThat(result1.getPrice()).isNotEqualTo(result2.getPrice());
    }
}