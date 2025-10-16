package com.vibepay.service;

import com.vibepay.dto.PgAuthParamsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimplePgService 테스트")
class SimplePgServiceTest {

    private SimplePgService simplePgService;

    @BeforeEach
    void setUp() {
        simplePgService = new SimplePgService();

        // 테스트용 이니시스 설정값 주입
        ReflectionTestUtils.setField(simplePgService, "mid", "INIpayTest");
        ReflectionTestUtils.setField(simplePgService, "signKey", "SU5JTElURV9UUklQTEVERVNfS0VZU1RS");
        ReflectionTestUtils.setField(simplePgService, "hashKey", "3CB8183A4BE283555ACC8363C0360223");
    }

    @Test
    @DisplayName("PG 인증 파라미터 생성 성공")
    void generateAuthParams_Success() {
        // given
        Integer price = 10000;
        String goodname = "테스트 상품";

        // when
        PgAuthParamsDto result = simplePgService.generateAuthParams(price, goodname);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMid()).isEqualTo("INIpayTest");
        assertThat(result.getPrice()).isEqualTo(price);
        assertThat(result.getCurrency()).isEqualTo("WON");
        assertThat(result.getBuyername()).isEqualTo("테스트사용자");
        assertThat(result.getBuyeremail()).isEqualTo("test@example.com");
        assertThat(result.getBuyertel()).isEqualTo("010-1234-5678");
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

        // when
        PgAuthParamsDto result1 = simplePgService.generateAuthParams(price, goodname);
        PgAuthParamsDto result2 = simplePgService.generateAuthParams(price, goodname);

        // then
        assertThat(result1.getOid()).isNotEqualTo(result2.getOid());
        assertThat(result1.getSignature()).isNotEqualTo(result2.getSignature());
    }

    @Test
    @DisplayName("다른 금액에 대해 다른 해시 생성 확인")
    void generateAuthParams_DifferentHashForDifferentPrice() {
        // given
        String goodname = "테스트 상품";

        // when
        PgAuthParamsDto result1 = simplePgService.generateAuthParams(10000, goodname);
        PgAuthParamsDto result2 = simplePgService.generateAuthParams(20000, goodname);

        // then
        assertThat(result1.getSignature()).isNotEqualTo(result2.getSignature());
        assertThat(result1.getPrice()).isNotEqualTo(result2.getPrice());
    }
}