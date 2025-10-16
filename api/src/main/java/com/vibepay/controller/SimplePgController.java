package com.vibepay.controller;

import com.vibepay.dto.PgAuthParamsDto;
import com.vibepay.service.SimplePgService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 간단한 PG 결제 관련 컨트롤러
 * 이니시스 결제 인증 파라미터 제공
 */
@RestController
@RequestMapping("/api/simple-pg")
public class SimplePgController {

    private final SimplePgService simplePgService;

    public SimplePgController(SimplePgService simplePgService) {
        this.simplePgService = simplePgService;
    }

    /**
     * PG 인증 파라미터 조회
     * 프론트엔드에서 PG 인증창 팝업에 필요한 모든 값들을 제공
     *
     * @param price 결제 금액
     * @param goodname 상품명
     * @return PG 인증 파라미터
     */
    @GetMapping("/auth-params")
    public ResponseEntity<PgAuthParamsDto> getAuthParams(
            @RequestParam Integer price,
            @RequestParam String goodname) {

        System.out.println("PG 인증 파라미터 요청 - price: " + price + ", goodname: " + goodname);

        PgAuthParamsDto authParams = simplePgService.generateAuthParams(price, goodname);

        System.out.println("PG 인증 파라미터 응답 완료 - oid: " + authParams.getOid());

        return ResponseEntity.ok(authParams);
    }
}