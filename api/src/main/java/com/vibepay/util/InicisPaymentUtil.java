package com.vibepay.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 이니시스 PG 연동 유틸리티 클래스
 * 이니시스 표준결제 API를 통한 결제 승인/취소 처리
 */
@Slf4j
@Component
public class InicisPaymentUtil {

    @Value("${payment.inicis.api-url:https://iniapi.inicis.com}")
    private String apiUrl;

    @Value("${payment.inicis.mid}")
    private String mid;

    @Value("${payment.inicis.signkey}")
    private String signKey;

    // 이니시스 API 엔드포인트
    private static final String APPROVAL_URL = "/api/v1/formpay";
    private static final String CANCEL_URL = "/api/v1/refund";

    /**
     * 결제 승인 요청
     *
     * @param pgTid PG 거래번호
     * @param oid 상점 주문번호
     * @param price 결제금액
     * @param currency 통화코드
     * @return 승인 결과
     */
    public Map<String, String> requestApproval(String pgTid, String oid, String price, String currency) {
        log.info("이니시스 결제 승인 요청 - pgTid: {}, oid: {}, price: {}", pgTid, oid, price);

        try {
            // 승인 요청 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("type", "pay");                          // 거래구분 (승인: pay)
            params.put("paymethod", "Card");                    // 결제수단
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("clientip", "127.0.0.1");              // 고객 IP
            params.put("mid", mid);                            // 상점아이디
            params.put("tid", pgTid);                          // 거래번호
            params.put("oid", oid);                            // 주문번호
            params.put("price", price);                        // 결제금액
            params.put("currency", currency);                  // 통화코드

            // 해시 서명 생성
            String hashData = generateHashData(params);
            params.put("hashdata", hashData);

            // HTTP 요청 전송
            return sendHttpRequest(apiUrl + APPROVAL_URL, params);

        } catch (Exception e) {
            log.error("이니시스 결제 승인 요청 실패", e);
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("resultcode", "9999");
            errorResult.put("resultmsg", "시스템 오류가 발생했습니다");
            return errorResult;
        }
    }

    /**
     * 결제 취소 요청
     *
     * @param pgTid PG 거래번호
     * @param price 취소금액
     * @param reason 취소사유
     * @return 취소 결과
     */
    public Map<String, String> requestCancel(String pgTid, String price, String reason) {
        log.info("이니시스 결제 취소 요청 - pgTid: {}, price: {}, reason: {}", pgTid, price, reason);

        try {
            // 취소 요청 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("type", "refund");                       // 거래구분 (취소: refund)
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("clientip", "127.0.0.1");              // 고객 IP
            params.put("mid", mid);                            // 상점아이디
            params.put("tid", pgTid);                          // 거래번호
            params.put("price", price);                        // 취소금액
            params.put("msg", reason);                         // 취소사유

            // 해시 서명 생성
            String hashData = generateHashData(params);
            params.put("hashdata", hashData);

            // HTTP 요청 전송
            return sendHttpRequest(apiUrl + CANCEL_URL, params);

        } catch (Exception e) {
            log.error("이니시스 결제 취소 요청 실패", e);
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("resultcode", "9999");
            errorResult.put("resultmsg", "시스템 오류가 발생했습니다");
            return errorResult;
        }
    }

    /**
     * 망취소 요청 (승인 실패 시 자동 호출)
     *
     * @param pgTid PG 거래번호
     * @return 망취소 결과
     */
    public Map<String, String> requestNetworkCancel(String pgTid) {
        log.info("이니시스 망취소 요청 - pgTid: {}", pgTid);

        try {
            // 망취소 요청 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("type", "netcancel");                    // 거래구분 (망취소: netcancel)
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("clientip", "127.0.0.1");              // 고객 IP
            params.put("mid", mid);                            // 상점아이디
            params.put("tid", pgTid);                          // 거래번호

            // 해시 서명 생성
            String hashData = generateHashData(params);
            params.put("hashdata", hashData);

            // HTTP 요청 전송
            return sendHttpRequest(apiUrl + CANCEL_URL, params);

        } catch (Exception e) {
            log.error("이니시스 망취소 요청 실패", e);
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("resultcode", "9999");
            errorResult.put("resultmsg", "시스템 오류가 발생했습니다");
            return errorResult;
        }
    }

    /**
     * 해시 데이터 생성
     * 이니시스 보안을 위한 해시 서명 생성
     */
    private String generateHashData(Map<String, String> params) throws Exception {
        StringBuilder hashString = new StringBuilder();

        // 이니시스 해시 데이터 생성 규칙에 따라 구성
        if (params.containsKey("type")) {
            if ("pay".equals(params.get("type"))) {
                // 승인용 해시데이터
                hashString.append(signKey)
                        .append(params.get("type"))
                        .append(params.get("paymethod"))
                        .append(params.get("timestamp"))
                        .append(params.get("clientip"))
                        .append(params.get("mid"))
                        .append(params.get("tid"))
                        .append(params.get("oid"))
                        .append(params.get("price"))
                        .append(params.get("currency"));
            } else {
                // 취소/망취소용 해시데이터
                hashString.append(signKey)
                        .append(params.get("type"))
                        .append(params.get("timestamp"))
                        .append(params.get("clientip"))
                        .append(params.get("mid"))
                        .append(params.get("tid"));

                if (params.containsKey("price")) {
                    hashString.append(params.get("price"));
                }
            }
        }

        // SHA-512 해시 생성
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashBytes = md.digest(hashString.toString().getBytes(StandardCharsets.UTF_8));

        // Base64 인코딩
        return java.util.Base64.getEncoder().encodeToString(hashBytes);
    }

    /**
     * HTTP 요청 전송
     */
    private Map<String, String> sendHttpRequest(String urlString, Map<String, String> params) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 요청 설정
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        // 요청 파라미터 전송
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(buildQueryString(params));
            writer.flush();
        }

        // 응답 처리
        Map<String, String> response = new HashMap<>();
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder responseBody = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }

                // 응답 파싱 (이니시스는 query string 형태로 응답)
                String[] pairs = responseBody.toString().split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        response.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                    }
                }
            }
        } else {
            response.put("resultcode", "9999");
            response.put("resultmsg", "HTTP 통신 오류: " + responseCode);
        }

        return response;
    }

    /**
     * 쿼리 스트링 빌드
     */
    private String buildQueryString(Map<String, String> params) throws Exception {
        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                query.append("&");
            }
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }

        return query.toString();
    }
}