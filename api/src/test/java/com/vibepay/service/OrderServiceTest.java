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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PointService pointService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private OrderService orderService;

    private Long memberId;
    private OrderCreateRequest orderCreateRequest;
    private Order pendingOrder;
    private Order paidOrder;

    @BeforeEach
    void setUp() {
        memberId = 1L;

        orderCreateRequest = new OrderCreateRequest(
                "테스트 상품",
                10000L,
                2,
                "ORD20251016143025123456",
                PaymentMethod.POINT,
                20000L,
                0L,
                null,
                null,
                null,
                null,
                null
        );

        pendingOrder = Order.builder()
                .id(1L)
                .memberId(memberId)
                .orderNumber("ORD20251016143025123456")
                .productName("테스트 상품")
                .productPrice(10000L)
                .quantity(2)
                .totalAmount(20000L)
                .pointAmount(0L)
                .cardAmount(0L)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paidOrder = Order.builder()
                .id(2L)
                .memberId(memberId)
                .orderNumber("ORD20251016143025123457")
                .productName("테스트 상품2")
                .productPrice(30000L)
                .quantity(1)
                .totalAmount(30000L)
                .pointAmount(10000L)
                .cardAmount(20000L)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공 - 적립금만 사용")
    void createOrder_success_pointOnly() {
        // given
        Order paidOrderWithPoint = Order.builder()
                .id(1L)
                .memberId(memberId)
                .orderNumber("ORD20251016143025123456")
                .productName("테스트 상품")
                .productPrice(10000L)
                .quantity(2)
                .totalAmount(20000L)
                .pointAmount(20000L)
                .cardAmount(0L)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            // MyBatis는 원본 객체의 필드를 직접 수정하므로 리플렉션 사용
            try {
                java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(order, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(orderRepository).save(any(Order.class));
        given(orderRepository.findById(1L)).willReturn(Optional.of(paidOrderWithPoint));

        // when
        OrderResponse response = orderService.createOrder(orderCreateRequest, session);

        // then
        assertThat(response.getProductName()).isEqualTo("테스트 상품");
        assertThat(response.getProductPrice()).isEqualTo(10000L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualTo(20000L);
        assertThat(response.getPointAmount()).isEqualTo(20000L);
        assertThat(response.getCardAmount()).isEqualTo(0L);
        assertThat(response.getStatus()).isEqualTo("PAID");
        assertThat(response.getOrderNumber()).isEqualTo("ORD20251016143025123456");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).save(any(Order.class));
        then(pointService).should(times(1)).deduct(20000L, session);
        then(orderRepository).should(times(1)).updateStatus(1L, OrderStatus.PAID);
        then(paymentService).should(never()).approvePayment(any(), any());
    }

    @Test
    @DisplayName("주문 생성 성공 - 카드만 사용")
    void createOrder_success_cardOnly() {
        // given
        OrderCreateRequest cardRequest = new OrderCreateRequest(
                "테스트 상품",
                10000L,
                2,
                "ORD20251016143025123456",
                PaymentMethod.CARD,
                0L,
                20000L,
                "pgAuthToken123",
                "pgTid123",
                "testMid",
                "20000",
                "WON"
        );

        Order paidOrderWithCard = Order.builder()
                .id(1L)
                .memberId(memberId)
                .orderNumber("ORD20251016143025123456")
                .productName("테스트 상품")
                .productPrice(10000L)
                .quantity(2)
                .totalAmount(20000L)
                .pointAmount(0L)
                .cardAmount(20000L)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            try {
                java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(order, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(orderRepository).save(any(Order.class));
        given(orderRepository.findById(1L)).willReturn(Optional.of(paidOrderWithCard));

        // when
        OrderResponse response = orderService.createOrder(cardRequest, session);

        // then
        assertThat(response.getProductName()).isEqualTo("테스트 상품");
        assertThat(response.getTotalAmount()).isEqualTo(20000L);
        assertThat(response.getPointAmount()).isEqualTo(0L);
        assertThat(response.getCardAmount()).isEqualTo(20000L);
        assertThat(response.getStatus()).isEqualTo("PAID");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).save(any(Order.class));
        then(pointService).should(never()).deduct(anyLong(), any());
        then(orderRepository).should(never()).updateStatus(anyLong(), eq(OrderStatus.PAID));
        then(paymentService).should(times(1)).approvePayment(any(PaymentApprovalRequest.class), eq(session));
    }

    @Test
    @DisplayName("주문 생성 성공 - 혼합 결제")
    void createOrder_success_mixed() {
        // given
        OrderCreateRequest mixedRequest = new OrderCreateRequest(
                "테스트 상품",
                10000L,
                3,
                "ORD20251016143025123456",
                PaymentMethod.MIXED,
                10000L,
                20000L,
                "pgAuthToken123",
                "pgTid123",
                "testMid",
                "20000",
                "WON"
        );

        Order paidOrderMixed = Order.builder()
                .id(1L)
                .memberId(memberId)
                .orderNumber("ORD20251016143025123456")
                .productName("테스트 상품")
                .productPrice(10000L)
                .quantity(3)
                .totalAmount(30000L)
                .pointAmount(10000L)
                .cardAmount(20000L)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            try {
                java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(order, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(orderRepository).save(any(Order.class));
        given(orderRepository.findById(1L)).willReturn(Optional.of(paidOrderMixed));

        // when
        OrderResponse response = orderService.createOrder(mixedRequest, session);

        // then
        assertThat(response.getTotalAmount()).isEqualTo(30000L);
        assertThat(response.getPointAmount()).isEqualTo(10000L);
        assertThat(response.getCardAmount()).isEqualTo(20000L);
        assertThat(response.getStatus()).isEqualTo("PAID");

        then(orderRepository).should(times(1)).save(any(Order.class));
        then(pointService).should(never()).deduct(anyLong(), any());
        then(paymentService).should(times(1)).approvePayment(any(PaymentApprovalRequest.class), eq(session));
    }

    @Test
    @DisplayName("주문 생성 실패 - 로그인 안 됨")
    void createOrder_fail_notLoggedIn() {
        // given
        given(session.getAttribute("memberId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(orderCreateRequest, session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요합니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 결제 금액 불일치")
    void createOrder_fail_amountMismatch() {
        // given
        OrderCreateRequest invalidRequest = new OrderCreateRequest(
                "테스트 상품",
                10000L,
                2,
                "ORD20251016143025123456",
                PaymentMethod.POINT,
                15000L, // 잘못된 금액
                0L,
                null,
                null,
                null,
                null,
                null
        );

        given(session.getAttribute("memberId")).willReturn(memberId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 금액(포인트 + 카드)이 총 주문 금액과 일치하지 않습니다");

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 카드 결제 시 PG 정보 누락")
    void createOrder_fail_missingPgInfo() {
        // given
        OrderCreateRequest invalidRequest = new OrderCreateRequest(
                "테스트 상품",
                10000L,
                2,
                "ORD20251016143025123456",
                PaymentMethod.CARD,
                0L,
                20000L,
                null, // PG 정보 누락
                null,
                null,
                null,
                null
        );

        given(session.getAttribute("memberId")).willReturn(memberId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카드 결제 시 PG 인증 토큰은 필수입니다");

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 카드 결제 최소 금액 미만")
    void createOrder_fail_cardAmountTooSmall() {
        // given
        OrderCreateRequest invalidRequest = new OrderCreateRequest(
                "테스트 상품",
                10L,
                5,
                "ORD20251016143025123456",
                PaymentMethod.CARD,
                0L,
                50L, // 100원 미만
                "pgAuthToken123",
                "pgTid123",
                "testMid",
                "50",
                "WON"
        );

        given(session.getAttribute("memberId")).willReturn(memberId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카드 결제 금액은 100원 이상이어야 합니다");

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 목록 조회 성공")
    void getOrderList_success() {
        // given
        List<Order> orders = Arrays.asList(paidOrder, pendingOrder);
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.findByMemberId(memberId)).willReturn(orders);

        // when
        List<OrderListResponse> response = orderService.getOrderList(session);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getId()).isEqualTo(2L);
        assertThat(response.get(0).getProductName()).isEqualTo("테스트 상품2");
        assertThat(response.get(1).getId()).isEqualTo(1L);
        assertThat(response.get(1).getProductName()).isEqualTo("테스트 상품");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() {
        // given
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pendingOrder));

        // when
        OrderResponse response = orderService.getOrderDetail(1L, session);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("테스트 상품");
        assertThat(response.getStatus()).isEqualTo("PENDING");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).findById(1L);
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 주문 없음")
    void getOrderDetail_fail_orderNotFound() {
        // given
        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetail(999L, session))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).findById(999L);
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 다른 회원의 주문")
    void getOrderDetail_fail_unauthorizedAccess() {
        // given
        Long otherMemberId = 2L;
        given(session.getAttribute("memberId")).willReturn(otherMemberId);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pendingOrder));

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetail(1L, session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("해당 주문에 접근할 권한이 없습니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).findById(1L);
    }

    @Test
    @DisplayName("주문 취소 성공 - PENDING 상태")
    void cancelOrder_success_pendingStatus() {
        // given
        Order cancelledOrder = Order.builder()
                .id(1L)
                .memberId(memberId)
                .orderNumber(pendingOrder.getOrderNumber())
                .productName(pendingOrder.getProductName())
                .productPrice(pendingOrder.getProductPrice())
                .quantity(pendingOrder.getQuantity())
                .totalAmount(pendingOrder.getTotalAmount())
                .pointAmount(pendingOrder.getPointAmount())
                .cardAmount(pendingOrder.getCardAmount())
                .status(OrderStatus.CANCELLED)
                .createdAt(pendingOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.findById(1L))
                .willReturn(Optional.of(pendingOrder))
                .willReturn(Optional.of(cancelledOrder));

        // when
        OrderResponse response = orderService.cancelOrder(1L, session);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("CANCELLED");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(2)).findById(1L);
        then(orderRepository).should(times(1)).updateStatus(1L, OrderStatus.CANCELLED);
        then(pointService).should(never()).restore(anyLong(), any());
    }

    @Test
    @DisplayName("주문 취소 성공 - PAID 상태, 적립금만 사용")
    void cancelOrder_success_paidStatusWithPointOnly() {
        // given
        Order paidOrderWithPointOnly = Order.builder()
                .id(3L)
                .memberId(memberId)
                .orderNumber("ORD20251016143025123458")
                .productName("테스트 상품3")
                .productPrice(15000L)
                .quantity(1)
                .totalAmount(15000L)
                .pointAmount(15000L)
                .cardAmount(0L)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order cancelledOrder = Order.builder()
                .id(3L)
                .memberId(memberId)
                .orderNumber(paidOrderWithPointOnly.getOrderNumber())
                .productName(paidOrderWithPointOnly.getProductName())
                .productPrice(paidOrderWithPointOnly.getProductPrice())
                .quantity(paidOrderWithPointOnly.getQuantity())
                .totalAmount(paidOrderWithPointOnly.getTotalAmount())
                .pointAmount(paidOrderWithPointOnly.getPointAmount())
                .cardAmount(paidOrderWithPointOnly.getCardAmount())
                .status(OrderStatus.CANCELLED)
                .createdAt(paidOrderWithPointOnly.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.findById(3L))
                .willReturn(Optional.of(paidOrderWithPointOnly))
                .willReturn(Optional.of(cancelledOrder));

        // when
        OrderResponse response = orderService.cancelOrder(3L, session);

        // then
        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getStatus()).isEqualTo("CANCELLED");

        then(session).should(atLeastOnce()).getAttribute("memberId");
        then(orderRepository).should(times(2)).findById(3L);
        then(orderRepository).should(times(1)).updateStatus(3L, OrderStatus.CANCELLED);
        then(pointService).should(times(1)).restore(15000L, session);
    }

    @Test
    @DisplayName("주문 취소 실패 - 이미 취소된 주문")
    void cancelOrder_fail_alreadyCancelled() {
        // given
        Order cancelledOrder = Order.builder()
                .id(1L)
                .memberId(memberId)
                .orderNumber(pendingOrder.getOrderNumber())
                .productName(pendingOrder.getProductName())
                .productPrice(pendingOrder.getProductPrice())
                .quantity(pendingOrder.getQuantity())
                .totalAmount(pendingOrder.getTotalAmount())
                .pointAmount(pendingOrder.getPointAmount())
                .cardAmount(pendingOrder.getCardAmount())
                .status(OrderStatus.CANCELLED)
                .createdAt(pendingOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.findById(1L)).willReturn(Optional.of(cancelledOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(1L, session))
                .isInstanceOf(OrderAlreadyCancelledException.class)
                .hasMessage("이미 취소된 주문입니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).findById(1L);
        then(orderRepository).should(never()).updateStatus(anyLong(), any());
        then(pointService).should(never()).restore(anyLong(), any());
    }

    @Test
    @DisplayName("주문 취소 실패 - 다른 회원의 주문")
    void cancelOrder_fail_unauthorizedAccess() {
        // given
        Long otherMemberId = 2L;
        given(session.getAttribute("memberId")).willReturn(otherMemberId);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pendingOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(1L, session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("해당 주문에 접근할 권한이 없습니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).findById(1L);
        then(orderRepository).should(never()).updateStatus(anyLong(), any());
        then(pointService).should(never()).restore(anyLong(), any());
    }

    @Test
    @DisplayName("주문 목록 조회 성공 - 페이징")
    void getOrderListWithPaging_success() {
        // given
        PageRequest pageRequest = new PageRequest(0, 5);
        List<Order> orders = Arrays.asList(paidOrder, pendingOrder);
        long totalElements = 10L;

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.countByMemberId(memberId)).willReturn(totalElements);
        given(orderRepository.findByMemberIdWithPaging(memberId, 0L, 5)).willReturn(orders);

        // when
        PageResponse<OrderListResponse> response = orderService.getOrderListWithPaging(pageRequest, session);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(10L);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isFalse();

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).countByMemberId(memberId);
        then(orderRepository).should(times(1)).findByMemberIdWithPaging(memberId, 0L, 5);
    }

    @Test
    @DisplayName("주문 목록 조회 성공 - 페이징, 마지막 페이지")
    void getOrderListWithPaging_success_lastPage() {
        // given
        PageRequest pageRequest = new PageRequest(1, 5);
        List<Order> orders = Arrays.asList(pendingOrder);
        long totalElements = 6L;

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.countByMemberId(memberId)).willReturn(totalElements);
        given(orderRepository.findByMemberIdWithPaging(memberId, 5L, 5)).willReturn(orders);

        // when
        PageResponse<OrderListResponse> response = orderService.getOrderListWithPaging(pageRequest, session);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(6L);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isTrue();

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).countByMemberId(memberId);
        then(orderRepository).should(times(1)).findByMemberIdWithPaging(memberId, 5L, 5);
    }

    @Test
    @DisplayName("주문 목록 조회 성공 - 페이징, 빈 결과")
    void getOrderListWithPaging_success_emptyResult() {
        // given
        PageRequest pageRequest = new PageRequest(0, 10);
        List<Order> orders = Arrays.asList();
        long totalElements = 0L;

        given(session.getAttribute("memberId")).willReturn(memberId);
        given(orderRepository.countByMemberId(memberId)).willReturn(totalElements);
        given(orderRepository.findByMemberIdWithPaging(memberId, 0L, 10)).willReturn(orders);

        // when
        PageResponse<OrderListResponse> response = orderService.getOrderListWithPaging(pageRequest, session);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isFalse();

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(times(1)).countByMemberId(memberId);
        then(orderRepository).should(times(1)).findByMemberIdWithPaging(memberId, 0L, 10);
    }

    @Test
    @DisplayName("주문 목록 조회 실패 - 페이징, 로그인 안 됨")
    void getOrderListWithPaging_fail_notLoggedIn() {
        // given
        PageRequest pageRequest = new PageRequest(0, 10);
        given(session.getAttribute("memberId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderService.getOrderListWithPaging(pageRequest, session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요합니다");

        then(session).should(times(1)).getAttribute("memberId");
        then(orderRepository).should(never()).countByMemberId(anyLong());
        then(orderRepository).should(never()).findByMemberIdWithPaging(anyLong(), anyLong(), anyInt());
    }
}
