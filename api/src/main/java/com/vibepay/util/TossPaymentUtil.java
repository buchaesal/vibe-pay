package com.vibepay.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 PG 연동 유틸리티 클래스
 * 토스페이먼츠 결제 승인/취소 API 처리
 */
@Slf4j
@Component
public class TossPaymentUtil {

    @Value("${toss.secretKey}")
    private String secretKey;

    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 토스 결제 승인 요청
     *
     * @param paymentKey 결제 키
     * @param orderId 주문번호
     * @param amount 결제금액
     * @return 승인 결과
     */
    public Map<String, Object> requestApproval(String paymentKey, String orderId, Long amount) {
        log.info("토스페이먼츠 결제 승인 요청 - paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);

        try {
            // 승인 요청 파라미터 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentKey", paymentKey);
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount);

            // HTTP 요청 전송
            return sendHttpRequest(TOSS_API_URL + "/confirm", requestBody);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 요청 실패", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "FAILED");
            errorResult.put("message", "시스템 오류가 발생했습니다");
            return errorResult;
        }
    }

    /**
     * 토스 결제 취소 요청
     *
     * @param paymentKey 결제 키
     * @param cancelReason 취소 사유
     * @param cancelAmount 취소 금액 (null인 경우 전액 취소)
     * @return 취소 결과
     */
    public Map<String, Object> requestCancel(String paymentKey, String cancelReason, Long cancelAmount) {
        log.info("토스페이먼츠 결제 취소 요청 - paymentKey: {}, cancelAmount: {}, reason: {}",
                paymentKey, cancelAmount, cancelReason);

        try {
            // 취소 요청 파라미터 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);

            // 취소 금액이 지정된 경우에만 포함 (null이면 전액 취소)
            if (cancelAmount != null) {
                requestBody.put("cancelAmount", cancelAmount);
            }

            // HTTP 요청 전송
            return sendHttpRequest(TOSS_API_URL + "/" + paymentKey + "/cancel", requestBody);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 요청 실패", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "FAILED");
            errorResult.put("message", "시스템 오류가 발생했습니다");
            return errorResult;
        }
    }

    /**
     * 망취소 요청 (승인 실패 시 자동 호출)
     *
     * @param paymentKey 결제 키
     * @return 망취소 결과
     */
    public Map<String, Object> requestNetworkCancel(String paymentKey) {
        log.info("토스페이먼츠 망취소 요청 - paymentKey: {}", paymentKey);
        return requestCancel(paymentKey, "승인 실패로 인한 자동 취소", null);
    }

    /**
     * HTTP 요청 전송
     */
    private Map<String, Object> sendHttpRequest(String urlString, Map<String, Object> requestBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Basic 인증 헤더 생성
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // 요청 설정
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        // 요청 바디 전송
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(jsonBody);
            writer.flush();
        }

        // 응답 처리
        int responseCode = connection.getResponseCode();
        Map<String, Object> response;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 성공 응답 처리
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
                response = objectMapper.readValue(responseBody.toString(), Map.class);
            }
        } else {
            // 오류 응답 처리
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
                response = objectMapper.readValue(responseBody.toString(), Map.class);
            }
            log.warn("토스페이먼츠 API 오류 응답 - code: {}, response: {}", responseCode, response);
        }

        return response;
    }
}
