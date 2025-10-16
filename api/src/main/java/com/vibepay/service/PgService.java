package com.vibepay.service;

import com.vibepay.domain.Member;
import com.vibepay.dto.PgAuthParamsDto;
import com.vibepay.dto.TossAuthParamsDto;
import com.vibepay.dto.MemberResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PG 결제 서비스
 * 이니시스, 토스페이먼츠 결제 인증에 필요한 파라미터 생성 담당
 */
@Service
public class PgService {

    private final MemberService memberService;

    @Value("${payment.inicis.mid:INIpayTest}")
    private String mid;

    @Value("${payment.inicis.signKey:SU5JTElURV9UUklQTEVERVNfS0VZU1RS}")
    private String signKey;

    @Value("${payment.inicis.iniApiKey:ItEQKi3rY7uvDS8l}")
    private String iniApiKey;

    @Value("${payment.inicis.hashKey:3CB8183A4BE283555ACC8363C0360223}")
    private String hashKey;

    @Value("${toss.client-key}")
    private String tossClientKey;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public PgService(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * PG사 선택 (이니시스 50%, 토스 50%)
     *
     * @return 선택된 PG사 타입 ("INICIS" 또는 "TOSS")
     */
    public String selectPgType() {
        // 50:50 비율로 랜덤 선택
        double random = Math.random();
        if (random < 0.5) {
            return "INICIS";
        } else {
            return "TOSS";
        }
    }

    /**
     * PG 인증 파라미터 생성 (PG사 자동 선택)
     *
     * @param session HTTP 세션 (회원 인증 확인용)
     * @param price 결제 금액
     * @param goodname 상품명
     * @return PG 인증 파라미터 (PG사에 따라 PgAuthParamsDto 또는 TossAuthParamsDto)
     */
    public Object generateAuthParams(HttpSession session, Integer price, String goodname) {
        String pgType = selectPgType();
        System.out.println("선택된 PG사: " + pgType);

        if ("TOSS".equals(pgType)) {
            return generateTossAuthParams(session, price, goodname);
        } else {
            return generateInicisAuthParams(session, price, goodname);
        }
    }

    /**
     * PG 인증 파라미터 생성 (PG사 지정)
     *
     * @param session HTTP 세션 (회원 인증 확인용)
     * @param price 결제 금액
     * @param goodname 상품명
     * @param pgType PG사 타입 ("INICIS" 또는 "TOSS")
     * @return PG 인증 파라미터
     */
    public Object generateAuthParamsWithPgType(HttpSession session, Integer price, String goodname, String pgType) {
        if ("TOSS".equals(pgType)) {
            return generateTossAuthParams(session, price, goodname);
        } else {
            return generateInicisAuthParams(session, price, goodname);
        }
    }

    /**
     * 이니시스 PG 인증 파라미터 생성
     *
     * @param session HTTP 세션 (회원 인증 확인용)
     * @param price 결제 금액
     * @param goodname 상품명
     * @return 이니시스 PG 인증 파라미터
     */
    private PgAuthParamsDto generateInicisAuthParams(HttpSession session, Integer price, String goodname) {
        // 현재 로그인한 회원 정보 조회
        MemberResponse currentMember = memberService.getCurrentMember(session);

        // 타임스탬프 생성
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 주문번호 생성 (타임스탬프 기반 유니크 값)
        String oid = generateOrderId(timestamp);

        // 해시 서명 생성
        String signature = generateSignature(oid, price, timestamp);

        System.out.println("PG 인증 파라미터 생성 완료 - memberId: " + currentMember.getId() + ", oid: " + oid + ", price: " + price);

        return new PgAuthParamsDto(
                mid,
                oid,
                price,
                timestamp,
                signature,
                hashKey,
                "WON",
                currentMember.getName(),
                currentMember.getEmail(),
                extractPhoneNumber(currentMember),
                goodname
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

    /**
     * 토스페이먼츠 PG 인증 파라미터 생성
     *
     * @param session HTTP 세션 (회원 인증 확인용)
     * @param price 결제 금액
     * @param goodname 상품명
     * @return 토스페이먼츠 인증 파라미터
     */
    public TossAuthParamsDto generateTossAuthParams(HttpSession session, Integer price, String goodname) {
        // 현재 로그인한 회원 정보 조회
        MemberResponse currentMember = memberService.getCurrentMember(session);

        // 타임스탬프 생성
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 주문번호 생성 (타임스탬프 기반 유니크 값)
        String orderId = generateOrderId(timestamp);

        System.out.println("토스페이먼츠 인증 파라미터 생성 완료 - memberId: " + currentMember.getId() +
                ", orderId: " + orderId + ", amount: " + price);

        return new TossAuthParamsDto(
                tossClientKey,
                orderId,
                price,
                goodname,
                currentMember.getName(),
                currentMember.getEmail()
        );
    }

    /**
     * 회원의 연락처 정보 추출
     * 현재는 기본값 반환, 향후 회원 테이블에 연락처 필드 추가 시 수정 필요
     *
     * @param member 회원 정보
     * @return 연락처 (기본값)
     */
    private String extractPhoneNumber(MemberResponse member) {
        // TODO: 회원 테이블에 연락처 필드 추가 후 실제 값 반환하도록 수정
        return "010-1234-5678";
    }
}