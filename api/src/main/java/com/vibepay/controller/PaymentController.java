package com.vibepay.controller;

import com.vibepay.dto.PageRequest;
import com.vibepay.dto.PageResponse;
import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import com.vibepay.dto.PaymentResponse;
import com.vibepay.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 API 컨트롤러
 * 결제 승인, 취소, 조회 등의 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 승인
     * POST /api/payment/approve
     *
     * @param request 결제 승인 요청 정보
     * @param session HTTP 세션 (회원 인증 정보 포함)
     * @return 결제 승인 결과
     */
    @PostMapping("/approve")
    public ResponseEntity<PaymentResponse> approvePayment(
            @Valid @RequestBody PaymentApprovalRequest request,
            HttpSession session) {

        log.info("결제 승인 요청 - orderId: {}, totalAmount: {}, paymentMethod: {}",
                request.getOrderId(), request.getTotalAmount(), request.getPaymentMethod());

        PaymentResponse response = paymentService.approvePayment(request, session);

        log.info("결제 승인 완료 - paymentId: {}, status: {}",
                response.getId(), response.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 결제 취소
     * POST /api/payment/cancel
     *
     * @param request 결제 취소 요청 정보
     * @param session HTTP 세션 (회원 인증 정보 포함)
     * @return 결제 취소 결과
     */
    @PostMapping("/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Valid @RequestBody PaymentCancelRequest request,
            HttpSession session) {

        log.info("결제 취소 요청 - paymentId: {}, cancelReason: {}",
                request.getPaymentId(), request.getCancelReason());

        PaymentResponse response = paymentService.cancelPayment(request, session);

        log.info("결제 취소 완료 - paymentId: {}, status: {}",
                response.getId(), response.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * 회원별 결제 내역 조회 (페이징)
     * GET /api/payment/history?page=0&size=10
     *
     * @param pageRequest 페이징 요청 정보
     * @param session     HTTP 세션 (회원 인증 정보 포함)
     * @return 결제 내역 목록
     */
    @GetMapping("/history")
    public ResponseEntity<PageResponse<PaymentResponse>> getPaymentHistory(
            @Valid PageRequest pageRequest,
            HttpSession session) {

        log.info("결제 내역 조회 요청 - page: {}, size: {}", pageRequest.getPage(), pageRequest.getSize());

        List<PaymentResponse> payments = paymentService.getPaymentHistory(
                session, pageRequest.getPage(), pageRequest.getSize());

        // 실제 서비스에서는 전체 개수도 조회해야 하지만, 여기서는 간소화
        PageResponse<PaymentResponse> response = PageResponse.<PaymentResponse>builder()
                .content(payments)
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .totalElements((long) payments.size())
                .totalPages(1)
                .first(pageRequest.getPage() == 0)
                .last(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 결제 상세 조회
     * GET /api/payment/{paymentId}
     *
     * @param paymentId 결제 ID
     * @param session   HTTP 세션 (회원 인증 정보 포함)
     * @return 결제 상세 정보
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentDetail(
            @PathVariable Long paymentId,
            HttpSession session) {

        log.info("결제 상세 조회 요청 - paymentId: {}", paymentId);

        PaymentResponse response = paymentService.getPaymentDetail(paymentId, session);

        return ResponseEntity.ok(response);
    }
}