package com.vibepay.service;

import com.vibepay.domain.Order;
import com.vibepay.domain.OrderStatus;
import com.vibepay.dto.OrderCreateRequest;
import com.vibepay.dto.OrderListResponse;
import com.vibepay.dto.OrderResponse;
import com.vibepay.exception.OrderAlreadyCancelledException;
import com.vibepay.exception.OrderNotFoundException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.OrderRepository;
import com.vibepay.util.OrderNumberGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 서비스
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String SESSION_MEMBER_ID = "memberId";

    private final OrderRepository orderRepository;
    private final PointService pointService;

    /**
     * 주문 생성
     * - HttpSession에서 memberId 가져오기
     * - 주문번호 생성
     * - totalAmount 계산 (productPrice * quantity)
     * - Order 객체 생성 및 저장
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        // 주문번호 생성
        String orderNumber = OrderNumberGenerator.generate();

        // 총 금액 계산
        Long totalAmount = request.getProductPrice() * request.getQuantity();

        // 주문 객체 생성
        Order order = Order.builder()
                .memberId(memberId)
                .orderNumber(orderNumber)
                .productName(request.getProductName())
                .productPrice(request.getProductPrice())
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .pointAmount(0L)
                .cardAmount(0L)
                .status(OrderStatus.PENDING)
                .build();

        // 저장
        orderRepository.save(order);

        return OrderResponse.from(order);
    }

    /**
     * 주문 목록 조회
     * - HttpSession에서 memberId 가져오기
     * - memberId로 주문 목록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrderList(HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        List<Order> orders = orderRepository.findByMemberId(memberId);

        return orders.stream()
                .map(OrderListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 상세 조회
     * - orderId로 주문 조회
     * - 주문의 memberId와 세션의 memberId 비교
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다"));

        // 권한 체크
        if (!order.getMemberId().equals(memberId)) {
            throw new UnauthorizedException("해당 주문에 접근할 권한이 없습니다");
        }

        return OrderResponse.from(order);
    }

    /**
     * 주문 취소
     * - orderId로 주문 조회
     * - 주문의 memberId와 세션의 memberId 비교
     * - 주문 상태가 CANCELLED이면 예외 발생
     * - 주문 상태를 CANCELLED로 업데이트
     * - 결제 완료된 주문(PAID)인 경우 환불 처리
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다"));

        // 권한 체크
        if (!order.getMemberId().equals(memberId)) {
            throw new UnauthorizedException("해당 주문에 접근할 권한이 없습니다");
        }

        // 이미 취소된 주문인지 체크
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException("이미 취소된 주문입니다");
        }

        // 주문 상태를 CANCELLED로 업데이트
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);

        // 결제 완료된 주문인 경우 환불 처리
        if (order.getStatus() == OrderStatus.PAID) {
            // 적립금 환불
            if (order.getPointAmount() > 0) {
                pointService.restore(order.getPointAmount(), session);
            }

            // 카드 결제 취소
            if (order.getCardAmount() > 0) {
                // TODO: Iteration 4에서 구현 예정
                // paymentService.cancel(orderId, order.getCardAmount());
            }
        }

        // 업데이트된 주문 조회 후 반환
        Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다"));

        return OrderResponse.from(updatedOrder);
    }

    /**
     * 세션에서 memberId 가져오기
     */
    private Long getMemberIdFromSession(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SESSION_MEMBER_ID);

        if (memberId == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }

        return memberId;
    }
}
