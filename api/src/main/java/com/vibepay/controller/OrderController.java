package com.vibepay.controller;

import com.vibepay.dto.OrderCreateRequest;
import com.vibepay.dto.OrderListResponse;
import com.vibepay.dto.OrderResponse;
import com.vibepay.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 컨트롤러
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            HttpSession session
    ) {
        OrderResponse response = orderService.createOrder(request, session);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 주문 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<OrderListResponse>> getOrderList(HttpSession session) {
        List<OrderListResponse> response = orderService.getOrderList(session);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable Long orderId,
            HttpSession session
    ) {
        OrderResponse response = orderService.getOrderDetail(orderId, session);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            HttpSession session
    ) {
        OrderResponse response = orderService.cancelOrder(orderId, session);
        return ResponseEntity.ok(response);
    }
}
