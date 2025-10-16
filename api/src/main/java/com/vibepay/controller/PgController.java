package com.vibepay.controller;

import com.vibepay.dto.PgAuthParamsDto;
import com.vibepay.service.PgService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PG 결제 관련 컨트롤러
 * 이니시스 결제 인증 파라미터 제공
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
     *
     * @param session HTTP 세션 (회원 인증 확인용)
     * @param price 결제 금액
     * @param goodname 상품명
     * @return PG 인증 파라미터
     */
    @GetMapping("/auth-params")
    public ResponseEntity<PgAuthParamsDto> getAuthParams(
            HttpSession session,
            @RequestParam Integer price,
            @RequestParam String goodname) {

        System.out.println("PG 인증 파라미터 요청 - price: " + price + ", goodname: " + goodname);

        PgAuthParamsDto authParams = pgService.generateAuthParams(session, price, goodname);

        System.out.println("PG 인증 파라미터 응답 완료 - oid: " + authParams.getOid());

        return ResponseEntity.ok(authParams);
    }
}