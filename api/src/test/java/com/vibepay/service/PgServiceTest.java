package com.vibepay.service;

import com.vibepay.domain.Member;
import com.vibepay.dto.PgAuthParamsDto;
import com.vibepay.dto.TossAuthParamsDto;
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

    private com.vibepay.dto.MemberResponse testMemberResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 이니시스 설정값 주입
        ReflectionTestUtils.setField(pgService, "mid", "INIpayTest");
        ReflectionTestUtils.setField(pgService, "signKey", "SU5JTElURV9UUklQTEVERVNfS0VZU1RS");
        ReflectionTestUtils.setField(pgService, "iniApiKey", "ItEQKi3rY7uvDS8l");
        ReflectionTestUtils.setField(pgService, "hashKey", "3CB8183A4BE283555ACC8363C0360223");

        // 테스트용 토스 설정값 주입
        ReflectionTestUtils.setField(pgService, "tossClientKey", "test_ck_DnyRpQWGrNqGpjjZKxVLGKwvzBYd");

        // 테스트용 회원 응답 데이터
        testMemberResponse = com.vibepay.dto.MemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트사용자")
                .build();
    }

    @Test
    @DisplayName("PG사 선택 - INICIS 또는 TOSS 반환")
    void selectPgType_ReturnsInicisOrToss() {
        // when
        String pgType = pgService.selectPgType();

        // then
        assertThat(pgType).isIn("INICIS", "TOSS");
    }

    @Test
    @DisplayName("PG 인증 파라미터 생성 - 자동 선택")
    void generateAuthParams_AutoSelect() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";
        when(memberService.getCurrentMember(session)).thenReturn(testMemberResponse);

        // when
        Object result = pgService.generateAuthParams(session, price, goodname);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOfAny(PgAuthParamsDto.class, TossAuthParamsDto.class);
    }

    @Test
    @DisplayName("PG 인증 파라미터 생성 - INICIS 지정")
    void generateAuthParamsWithPgType_Inicis() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";
        String pgType = "INICIS";
        when(memberService.getCurrentMember(session)).thenReturn(testMemberResponse);

        // when
        Object result = pgService.generateAuthParamsWithPgType(session, price, goodname, pgType);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PgAuthParamsDto.class);

        PgAuthParamsDto inicisParams = (PgAuthParamsDto) result;
        assertThat(inicisParams.getMid()).isEqualTo("INIpayTest");
        assertThat(inicisParams.getPrice()).isEqualTo(price);
        assertThat(inicisParams.getCurrency()).isEqualTo("WON");
        assertThat(inicisParams.getBuyername()).isEqualTo("테스트사용자");
        assertThat(inicisParams.getBuyeremail()).isEqualTo("test@example.com");
        assertThat(inicisParams.getBuyertel()).isEqualTo("010-1234-5678");
        assertThat(inicisParams.getGoodname()).isEqualTo(goodname);
        assertThat(inicisParams.getmKey()).isEqualTo("3CB8183A4BE283555ACC8363C0360223");
        assertThat(inicisParams.getOid()).startsWith("ORD");
        assertThat(inicisParams.getTimestamp()).hasSize(14);
        assertThat(inicisParams.getSignature()).hasSize(128);
    }

    @Test
    @DisplayName("PG 인증 파라미터 생성 - TOSS 지정")
    void generateAuthParamsWithPgType_Toss() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";
        String pgType = "TOSS";
        when(memberService.getCurrentMember(session)).thenReturn(testMemberResponse);

        // when
        Object result = pgService.generateAuthParamsWithPgType(session, price, goodname, pgType);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TossAuthParamsDto.class);

        TossAuthParamsDto tossParams = (TossAuthParamsDto) result;
        assertThat(tossParams.getClientKey()).isEqualTo("test_ck_DnyRpQWGrNqGpjjZKxVLGKwvzBYd");
        assertThat(tossParams.getAmount()).isEqualTo(price);
        assertThat(tossParams.getOrderName()).isEqualTo(goodname);
        assertThat(tossParams.getCustomerName()).isEqualTo("테스트사용자");
        assertThat(tossParams.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(tossParams.getOrderId()).startsWith("ORD");
    }

    @Test
    @DisplayName("토스 인증 파라미터 생성 성공")
    void generateTossAuthParams_Success() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";
        when(memberService.getCurrentMember(session)).thenReturn(testMemberResponse);

        // when
        TossAuthParamsDto result = pgService.generateTossAuthParams(session, price, goodname);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getClientKey()).isEqualTo("test_ck_DnyRpQWGrNqGpjjZKxVLGKwvzBYd");
        assertThat(result.getAmount()).isEqualTo(price);
        assertThat(result.getOrderName()).isEqualTo(goodname);
        assertThat(result.getCustomerName()).isEqualTo("테스트사용자");
        assertThat(result.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(result.getOrderId()).isNotNull();
        assertThat(result.getOrderId()).startsWith("ORD");
    }

    @Test
    @DisplayName("서로 다른 주문번호 생성 확인 - INICIS")
    void generateAuthParamsWithPgType_DifferentOrderIds() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";
        when(memberService.getCurrentMember(session)).thenReturn(testMemberResponse);

        // when
        PgAuthParamsDto result1 = (PgAuthParamsDto) pgService.generateAuthParamsWithPgType(
                session, price, goodname, "INICIS");
        PgAuthParamsDto result2 = (PgAuthParamsDto) pgService.generateAuthParamsWithPgType(
                session, price, goodname, "INICIS");

        // then
        assertThat(result1.getOid()).isNotEqualTo(result2.getOid());
        assertThat(result1.getSignature()).isNotEqualTo(result2.getSignature());
    }

    @Test
    @DisplayName("다른 금액에 대해 다른 해시 생성 확인 - INICIS")
    void generateAuthParamsWithPgType_DifferentHashForDifferentPrice() {
        // given
        String goodname = "테스트 상품";
        when(memberService.getCurrentMember(session)).thenReturn(testMemberResponse);

        // when
        PgAuthParamsDto result1 = (PgAuthParamsDto) pgService.generateAuthParamsWithPgType(
                session, 10000, goodname, "INICIS");
        PgAuthParamsDto result2 = (PgAuthParamsDto) pgService.generateAuthParamsWithPgType(
                session, 20000, goodname, "INICIS");

        // then
        assertThat(result1.getSignature()).isNotEqualTo(result2.getSignature());
        assertThat(result1.getPrice()).isNotEqualTo(result2.getPrice());
    }
}