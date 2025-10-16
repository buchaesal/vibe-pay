package com.vibepay.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 주문번호 생성 유틸리티
 * 형식: ORD + yyyyMMddHHmmssSSS + 랜덤3자리
 */
public class OrderNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Random RANDOM = new Random();

    /**
     * 주문번호 생성
     * @return 생성된 주문번호 (예: ORD20251016143025123456)
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String random = String.format("%03d", RANDOM.nextInt(1000));
        return "ORD" + timestamp + random;
    }
}
