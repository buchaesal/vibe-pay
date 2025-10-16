package com.vibepay.service;

import com.vibepay.domain.Order;
import com.vibepay.domain.OrderStatus;
import com.vibepay.domain.PaymentMethod;
import com.vibepay.dto.OrderCreateRequest;
import com.vibepay.dto.OrderListResponse;
import com.vibepay.dto.OrderResponse;
import com.vibepay.dto.PageRequest;
import com.vibepay.dto.PageResponse;
import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.exception.OrderAlreadyCancelledException;
import com.vibepay.exception.OrderNotFoundException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.OrderRepository;
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
    private final PaymentService paymentService;

    /**
     * 주문 생성 및 결제 처리
     * - HttpSession에서 memberId 가져오기
     * - 요청 검증
     * - Order 객체 생성 및 저장 (PENDING 상태)
     * - 결제 처리 (적립금 차감 또는 카드 결제 승인)
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        // 총 금액 계산
        Long totalAmount = request.getProductPrice() * request.getQuantity();

        // 요청 검증
        request.validate(totalAmount);

        // 주문 객체 생성
        Order order = Order.builder()
                .memberId(memberId)
                .orderNumber(request.getOrderNumber())
                .productName(request.getProductName())
                .productPrice(request.getProductPrice())
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .pointAmount(request.getPointAmount())
                .cardAmount(request.getCardAmount())
                .status(OrderStatus.PENDING)
                .build();

        // 저장 (MyBatis가 order 객체에 ID를 설정함)
        orderRepository.save(order);

        // 결제 처리
        if (request.getPaymentMethod() == PaymentMethod.POINT) {
            // 적립금만 사용하는 경우
            pointService.deduct(request.getPointAmount(), session);
            orderRepository.updateStatus(order.getId(), OrderStatus.PAID);
        } else if (request.getCardAmount() > 0) {
            // 카드 결제가 포함된 경우 (CARD 또는 MIXED)
            PaymentApprovalRequest paymentRequest = PaymentApprovalRequest.builder()
                    .orderId(order.getId())
                    .paymentMethod(request.getPaymentMethod())
                    .totalAmount(totalAmount)
                    .cardAmount(request.getCardAmount())
                    .pointAmount(request.getPointAmount())
                    .pgTid(request.getPgTid())
                    .mid(request.getMid())
                    .oid(request.getOrderNumber())
                    .price(request.getPrice())
                    .currency(request.getCurrency())
                    .build();

            paymentService.approvePayment(paymentRequest, session);
        }

        // 업데이트된 주문 조회 후 반환
        Order updatedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다"));

        return OrderResponse.from(updatedOrder);
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
     * 주문 목록 조회 (페이징)
     * - HttpSession에서 memberId 가져오기
     * - memberId로 주문 목록 조회 (페이징, 최신순)
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderListResponse> getOrderListWithPaging(PageRequest pageRequest, HttpSession session) {
        Long memberId = getMemberIdFromSession(session);

        // 총 주문 개수 조회
        long totalElements = orderRepository.countByMemberId(memberId);

        // 페이징된 주문 목록 조회
        List<Order> orders = orderRepository.findByMemberIdWithPaging(
                memberId,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        List<OrderListResponse> content = orders.stream()
                .map(OrderListResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(content, pageRequest.getPage(), pageRequest.getSize(), totalElements);
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
