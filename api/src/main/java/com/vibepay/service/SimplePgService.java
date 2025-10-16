package com.vibepay.service;

import com.vibepay.dto.PgAuthParamsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 간단한 PG 결제 서비스
 * 이니시스 결제 인증에 필요한 파라미터 생성 담당
 */
@Service
public class SimplePgService {

    @Value("${payment.inicis.mid:INIpayTest}")
    private String mid;

    @Value("${payment.inicis.signKey:SU5JTElURV9UUklQTEVERVNfS0VZU1RS}")
    private String signKey;

    @Value("${payment.inicis.hashKey:3CB8183A4BE283555ACC8363C0360223}")
    private String hashKey;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * PG 인증 파라미터 생성
     *
     * @param price 결제 금액
     * @param goodname 상품명
     * @return PG 인증 파라미터
     */
    public PgAuthParamsDto generateAuthParams(Integer price, String goodname) {
        // 타임스탬프 생성
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 주문번호 생성 (타임스탬프 기반 유니크 값)
        String oid = generateOrderId(timestamp);

        // 해시 서명 생성
        String signature = generateSignature(oid, price, timestamp);

        System.out.println("PG 인증 파라미터 생성 완료 - oid: " + oid + ", price: " + price);

        return new PgAuthParamsDto(
                mid,                           // mid
                oid,                           // oid
                price,                         // price
                timestamp,                     // timestamp
                signature,                     // signature
                hashKey,                       // mKey
                "WON",                         // currency
                "테스트사용자",                   // buyername
                "test@example.com",           // buyeremail
                "010-1234-5678",              // buyertel
                goodname                       // goodname
        );
    }

    /**
     * 주문번호 생성 (타임스탬프 기반 유니크)
     *
     * @param timestamp 타임스탬프
     * @return 주문번호
     */
    private String generateOrderId(String timestamp) {
        // ORD + 타임스탬프 + 3자리 랜덤 숫자로 유니크 보장
        int randomSuffix = (int) (Math.random() * 900) + 100;
        return "ORD" + timestamp + randomSuffix;
    }

    /**
     * 이니시스 표준 해시 서명 생성
     * 해시 알고리즘: SHA-512
     * 해시 대상: oid + price + timestamp + signKey
     *
     * @param oid 주문번호
     * @param price 결제 금액
     * @param timestamp 타임스탬프
     * @return 해시 서명값
     */
    private String generateSignature(String oid, Integer price, String timestamp) {
        try {
            // 해시 대상 문자열 생성: oid + price + timestamp + signKey
            String hashTarget = oid + price + timestamp + signKey;

            System.out.println("해시 대상 문자열: " + hashTarget);

            // SHA-512 해시 생성
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(hashTarget.getBytes(StandardCharsets.UTF_8));

            // 바이트를 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String signature = hexString.toString();
            System.out.println("생성된 서명: " + signature);

            return signature;

        } catch (NoSuchAlgorithmException e) {
            System.err.println("해시 알고리즘 오류: " + e.getMessage());
            throw new RuntimeException("해시 서명 생성 중 오류가 발생했습니다", e);
        }
    }
}