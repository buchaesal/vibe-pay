package com.vibepay.service;

import com.vibepay.domain.*;
import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import com.vibepay.dto.PaymentResponse;
import com.vibepay.exception.OrderNotFoundException;
import com.vibepay.exception.PaymentNotFoundException;
import com.vibepay.exception.PaymentProcessException;
import com.vibepay.exception.UnauthorizedException;
import com.vibepay.repository.OrderRepository;
import com.vibepay.repository.PaymentRepository;
import com.vibepay.strategy.PaymentResult;
import com.vibepay.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PointService pointService;

    @Mock
    private Map<String, PaymentStrategy> paymentStrategies;

    @Mock
    private PaymentStrategy paymentStrategy;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PaymentService paymentService;

    private static final Long MEMBER_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final Long PAYMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        given(session.getAttribute("memberId")).willReturn(MEMBER_ID);
    }

    @Nested
    @DisplayName("결제 승인 테스트")
    class ApprovePaymentTest {

        private PaymentApprovalRequest createValidApprovalRequest() {
            return PaymentApprovalRequest.builder()
                    .orderId(ORDER_ID)
                    .paymentMethod(PaymentMethod.CARD)
                    .totalAmount(10000L)
                    .cardAmount(10000L)
                    .pointAmount(0L)
                    .pgTid("test_tid_123")
                    .mid("test_mid")
                    .oid("test_oid_123")
                    .price("10000")
                    .currency("KRW")
                    .build();
        }

        private Order createValidOrder() {
            return Order.builder()
                    .id(ORDER_ID)
                    .memberId(MEMBER_ID)
                    .orderNumber("ORDER_001")
                    .productName("테스트 상품")
                    .productPrice(10000L)
                    .quantity(1)
                    .totalAmount(10000L)
                    .status(OrderStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("카드 결제 승인 성공")
        void approvePayment_CardPayment_Success() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            Order order = createValidOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                return payment;
            });

            given(paymentStrategies.get("inicisPaymentStrategy")).willReturn(paymentStrategy);
            given(paymentStrategy.approve(any(PaymentApprovalRequest.class)))
                    .willReturn(PaymentResult.success("approved_tid", "auth123", "1234567890123456", "신한카드", 10000L));

            // when
            PaymentResponse response = paymentService.approvePayment(request, session);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(response.getTotalAmount()).isEqualTo(10000L);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);

            verify(paymentRepository).save(any(Payment.class));
            verify(paymentRepository).update(any(Payment.class));
            verify(orderRepository).updateStatus(ORDER_ID, OrderStatus.PAID);
        }

        @Test
        @DisplayName("혼합 결제(카드+포인트) 승인 성공")
        void approvePayment_MixedPayment_Success() {
            // given
            PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                    .orderId(ORDER_ID)
                    .paymentMethod(PaymentMethod.MIXED)
                    .totalAmount(10000L)
                    .cardAmount(7000L)
                    .pointAmount(3000L)
                    .pgTid("test_tid_123")
                    .mid("test_mid")
                    .oid("test_oid_123")
                    .price("7000")
                    .currency("KRW")
                    .build();

            Order order = createValidOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(paymentStrategies.get("inicisPaymentStrategy")).willReturn(paymentStrategy);
            given(paymentStrategy.approve(any(PaymentApprovalRequest.class)))
                    .willReturn(PaymentResult.success("approved_tid", "auth123", "1234567890123456", "신한카드", 7000L));

            // when
            PaymentResponse response = paymentService.approvePayment(request, session);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.MIXED);
            assertThat(response.getCardAmount()).isEqualTo(7000L);
            assertThat(response.getPointAmount()).isEqualTo(3000L);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);

            verify(pointService).deduct(3000L, session);
        }

        @Test
        @DisplayName("결제 승인 실패 - 주문을 찾을 수 없음")
        void approvePayment_OrderNotFound_ThrowsException() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.approvePayment(request, session))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessage("주문 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("결제 승인 실패 - 다른 회원의 주문")
        void approvePayment_UnauthorizedOrder_ThrowsException() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            Order otherMemberOrder = Order.builder()
                    .id(ORDER_ID)
                    .memberId(999L) // 다른 회원의 주문
                    .status(OrderStatus.PENDING)
                    .build();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(otherMemberOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.approvePayment(request, session))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("주문에 대한 권한이 없습니다");
        }

        @Test
        @DisplayName("결제 승인 실패 - 이미 결제된 주문")
        void approvePayment_AlreadyPaidOrder_ThrowsException() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            Order paidOrder = Order.builder()
                    .id(ORDER_ID)
                    .memberId(MEMBER_ID)
                    .status(OrderStatus.PAID) // 이미 결제된 상태
                    .build();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(paidOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.approvePayment(request, session))
                    .isInstanceOf(PaymentProcessException.class)
                    .hasMessage("결제 대기 상태가 아닌 주문입니다");
        }

        @Test
        @DisplayName("결제 승인 실패 - PG 승인 실패")
        void approvePayment_PgApprovalFailed_ThrowsException() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            Order order = createValidOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(paymentStrategies.get("inicisPaymentStrategy")).willReturn(paymentStrategy);
            given(paymentStrategy.approve(any(PaymentApprovalRequest.class)))
                    .willReturn(PaymentResult.failure("9999", "승인 거절"));

            // when & then
            assertThatThrownBy(() -> paymentService.approvePayment(request, session))
                    .isInstanceOf(PaymentProcessException.class)
                    .hasMessage("결제 승인에 실패했습니다: 승인 거절");

            verify(paymentRepository).update(any(Payment.class)); // 실패 상태로 업데이트
        }
    }

    @Nested
    @DisplayName("결제 취소 테스트")
    class CancelPaymentTest {

        private PaymentCancelRequest createValidCancelRequest() {
            return PaymentCancelRequest.builder()
                    .paymentId(PAYMENT_ID)
                    .cancelReason("고객 변심")
                    .cancelAmount(10000L)
                    .build();
        }

        private Payment createValidPayment() {
            return Payment.builder()
                    .id(PAYMENT_ID)
                    .orderId(ORDER_ID)
                    .paymentMethod(PaymentMethod.CARD)
                    .pgType(PgType.INICIS)
                    .totalAmount(10000L)
                    .cardAmount(10000L)
                    .pointAmount(0L)
                    .pgTid("approved_tid")
                    .authCode("auth123")
                    .status(PaymentStatus.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        private Order createValidOrderForCancel() {
            return Order.builder()
                    .id(ORDER_ID)
                    .memberId(MEMBER_ID)
                    .status(OrderStatus.PAID)
                    .build();
        }

        @Test
        @DisplayName("결제 취소 성공")
        void cancelPayment_Success() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            Payment payment = createValidPayment();
            Order order = createValidOrderForCancel();

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(payment);
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(paymentStrategies.get("inicisPaymentStrategy")).willReturn(paymentStrategy);
            given(paymentStrategy.cancel(any(PaymentCancelRequest.class)))
                    .willReturn(PaymentResult.cancelSuccess("approved_tid", 10000L));

            // when
            PaymentResponse response = paymentService.cancelPayment(request, session);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);

            verify(paymentRepository).update(any(Payment.class));
            verify(orderRepository).updateStatus(ORDER_ID, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("혼합 결제 취소 성공 - 포인트 복구")
        void cancelPayment_MixedPayment_RestorePoint() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            Payment mixedPayment = Payment.builder()
                    .id(PAYMENT_ID)
                    .orderId(ORDER_ID)
                    .paymentMethod(PaymentMethod.MIXED)
                    .pgType(PgType.INICIS)
                    .totalAmount(10000L)
                    .cardAmount(7000L)
                    .pointAmount(3000L) // 포인트 사용
                    .status(PaymentStatus.APPROVED)
                    .build();
            Order order = createValidOrderForCancel();

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(mixedPayment);
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(paymentStrategies.get("inicisPaymentStrategy")).willReturn(paymentStrategy);
            given(paymentStrategy.cancel(any(PaymentCancelRequest.class)))
                    .willReturn(PaymentResult.cancelSuccess("approved_tid", 10000L));

            // when
            PaymentResponse response = paymentService.cancelPayment(request, session);

            // then
            assertThat(response).isNotNull();
            verify(pointService).restore(3000L, session); // 포인트 복구 확인
        }

        @Test
        @DisplayName("결제 취소 실패 - 결제 정보를 찾을 수 없음")
        void cancelPayment_PaymentNotFound_ThrowsException() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            given(paymentRepository.findById(PAYMENT_ID)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(request, session))
                    .isInstanceOf(PaymentNotFoundException.class)
                    .hasMessage("결제 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("결제 취소 실패 - 다른 회원의 결제")
        void cancelPayment_UnauthorizedPayment_ThrowsException() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            Payment payment = createValidPayment();
            Order otherMemberOrder = Order.builder()
                    .id(ORDER_ID)
                    .memberId(999L) // 다른 회원의 주문
                    .build();

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(payment);
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(otherMemberOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(request, session))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("결제에 대한 권한이 없습니다");
        }

        @Test
        @DisplayName("결제 취소 실패 - 승인되지 않은 결제")
        void cancelPayment_NotApprovedPayment_ThrowsException() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            Payment pendingPayment = Payment.builder()
                    .id(PAYMENT_ID)
                    .orderId(ORDER_ID)
                    .status(PaymentStatus.PENDING) // 승인되지 않은 상태
                    .build();
            Order order = createValidOrderForCancel();

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(pendingPayment);
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(request, session))
                    .isInstanceOf(PaymentProcessException.class)
                    .hasMessage("승인된 결제만 취소할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("세션 인증 테스트")
    class SessionAuthTest {

        @Test
        @DisplayName("세션에 memberId가 없으면 UnauthorizedException 발생")
        void noMemberIdInSession_ThrowsUnauthorizedException() {
            // given
            given(session.getAttribute("memberId")).willReturn(null);
            PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                    .orderId(ORDER_ID)
                    .build();

            // when & then
            assertThatThrownBy(() -> paymentService.approvePayment(request, session))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("로그인이 필요합니다");
        }
    }
}