package com.vibepay.controller;

import com.vibepay.dto.PgAuthResponse;
import com.vibepay.dto.PgAuthParamsDto;
import com.vibepay.dto.TossAuthParamsDto;
import com.vibepay.service.PgService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PG 결제 관련 컨트롤러
 * 이니시스, 토스페이먼츠 결제 인증 파라미터 제공
 */
@RestController
@RequestMapping("/api/pg")
public class PgController {

    private final PgService pgService;

    public PgController(PgService pgService) {
        this.pgService = pgService;
    }

    /**
     * PG 인증 파라미터 조회
     * 프론트엔드에서 PG 인증창 팝업에 필요한 모든 값들을 제공
     * pgType 파라미터가 없으면 자동으로 PG사를 선택 (이니시스 50%, 토스 50%)
     *
     * @param session HTTP 세션 (회원 인증 확인용)
     * @param price 결제 금액
     * @param goodname 상품명
     * @param pgType PG사 타입 (선택적, "INICIS" 또는 "TOSS")
     * @return PG 인증 파라미터 (PG사 타입과 인증 파라미터 포함)
     */
    @GetMapping("/auth-params")
    public ResponseEntity<PgAuthResponse> getAuthParams(
            HttpSession session,
            @RequestParam Integer price,
            @RequestParam String goodname,
            @RequestParam(required = false) String pgType) {

        System.out.println("PG 인증 파라미터 요청 - price: " + price + ", goodname: " + goodname + ", pgType: " + pgType);

        String selectedPgType;
        Object authParams;

        if (pgType != null && !pgType.isBlank()) {
            // PG사가 지정된 경우
            selectedPgType = pgType;
            authParams = pgService.generateAuthParamsWithPgType(session, price, goodname, pgType);
        } else {
            // PG사가 지정되지 않은 경우 자동 선택
            selectedPgType = pgService.selectPgType();
            System.out.println("선택된 PG사: " + selectedPgType);
            authParams = pgService.generateAuthParamsWithPgType(session, price, goodname, selectedPgType);
        }

        // 응답 래핑
        PgAuthResponse response = new PgAuthResponse(selectedPgType, authParams);

        // 로그 출력 (PG사에 따라 다르게)
        if ("TOSS".equals(selectedPgType) && authParams instanceof TossAuthParamsDto) {
            System.out.println("PG 인증 파라미터 응답 완료 - orderId: " + ((TossAuthParamsDto) authParams).getOrderId());
        } else if ("INICIS".equals(selectedPgType) && authParams instanceof PgAuthParamsDto) {
            System.out.println("PG 인증 파라미터 응답 완료 - oid: " + ((PgAuthParamsDto) authParams).getOid());
        }

        return ResponseEntity.ok(response);
    }
}