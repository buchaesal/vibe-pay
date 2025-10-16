package com.vibepay.strategy;

import com.vibepay.domain.Payment;
import com.vibepay.domain.PaymentMethod;
import com.vibepay.domain.PaymentStatus;
import com.vibepay.domain.PgType;
import com.vibepay.dto.PaymentApprovalRequest;
import com.vibepay.dto.PaymentCancelRequest;
import com.vibepay.repository.PaymentRepository;
import com.vibepay.util.TossPaymentUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TossPaymentStrategy 테스트")
class TossPaymentStrategyTest {

    @Mock
    private TossPaymentUtil tossPaymentUtil;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private TossPaymentStrategy tossPaymentStrategy;

    private static final Long ORDER_ID = 1L;
    private static final Long PAYMENT_ID = 1L;
    private static final String PAYMENT_KEY = "test_payment_key_123";
    private static final String ORDER_ID_STRING = "test_order_123";

    @Nested
    @DisplayName("결제 승인 테스트")
    class ApprovalTest {

        private PaymentApprovalRequest createValidApprovalRequest() {
            return PaymentApprovalRequest.builder()
                    .orderId(ORDER_ID)
                    .paymentMethod(PaymentMethod.CARD)
                    .totalAmount(10000L)
                    .cardAmount(10000L)
                    .paymentKey(PAYMENT_KEY)
                    .oid(ORDER_ID_STRING)
                    .build();
        }

        private Map<String, Object> createSuccessResponse() {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DONE");
            response.put("paymentKey", PAYMENT_KEY);
            response.put("approvedAt", "2025-01-17T10:00:00");
            response.put("totalAmount", 10000);

            Map<String, Object> card = new HashMap<>();
            card.put("number", "1234567890123456");
            card.put("issuerCode", "71");
            response.put("card", card);

            return response;
        }

        @Test
        @DisplayName("토스페이먼츠 결제 승인 성공")
        void processApproval_Success() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            Map<String, Object> successResponse = createSuccessResponse();

            given(tossPaymentUtil.requestApproval(PAYMENT_KEY, ORDER_ID_STRING, 10000L))
                    .willReturn(successResponse);

            // when
            PaymentResult result = tossPaymentStrategy.approve(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getPgTid()).isEqualTo(PAYMENT_KEY);
            assertThat(result.getAuthCode()).isEqualTo("2025-01-17T10:00:00");
            assertThat(result.getCardNumber()).isEqualTo("1234567890123456");
            assertThat(result.getCardName()).isEqualTo("71");
            assertThat(result.getApprovedAmount()).isEqualTo(10000L);

            verify(tossPaymentUtil).requestApproval(PAYMENT_KEY, ORDER_ID_STRING, 10000L);
        }

        @Test
        @DisplayName("토스페이먼츠 결제 승인 실패 - PG사 응답 실패")
        void processApproval_PgFailure() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();
            Map<String, Object> failureResponse = new HashMap<>();
            failureResponse.put("status", "FAILED");
            failureResponse.put("code", "INVALID_CARD_NUMBER");
            failureResponse.put("message", "유효하지 않은 카드번호입니다");

            given(tossPaymentUtil.requestApproval(PAYMENT_KEY, ORDER_ID_STRING, 10000L))
                    .willReturn(failureResponse);

            // when
            PaymentResult result = tossPaymentStrategy.approve(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("INVALID_CARD_NUMBER");
            assertThat(result.getMessage()).isEqualTo("유효하지 않은 카드번호입니다");

            verify(tossPaymentUtil).requestApproval(PAYMENT_KEY, ORDER_ID_STRING, 10000L);
        }

        @Test
        @DisplayName("토스페이먼츠 결제 승인 실패 - 시스템 오류")
        void processApproval_SystemError() {
            // given
            PaymentApprovalRequest request = createValidApprovalRequest();

            given(tossPaymentUtil.requestApproval(PAYMENT_KEY, ORDER_ID_STRING, 10000L))
                    .willThrow(new RuntimeException("네트워크 오류"));

            // when
            PaymentResult result = tossPaymentStrategy.approve(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("SYSTEM_ERROR");
            assertThat(result.getMessage()).isEqualTo("시스템 오류가 발생했습니다");

            verify(tossPaymentUtil).requestApproval(PAYMENT_KEY, ORDER_ID_STRING, 10000L);
        }

        @Test
        @DisplayName("결제 금액이 0 이하인 경우 검증 실패")
        void processApproval_InvalidAmount() {
            // given
            PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                    .orderId(ORDER_ID)
                    .paymentMethod(PaymentMethod.CARD)
                    .totalAmount(0L)
                    .cardAmount(0L)
                    .paymentKey(PAYMENT_KEY)
                    .oid(ORDER_ID_STRING)
                    .build();

            // when
            PaymentResult result = tossPaymentStrategy.approve(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("INVALID_AMOUNT");
            assertThat(result.getMessage()).isEqualTo("결제 금액이 올바르지 않습니다");

            verify(tossPaymentUtil, never()).requestApproval(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("결제 취소 테스트")
    class CancelTest {

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
                    .pgType(PgType.TOSS)
                    .totalAmount(10000L)
                    .cardAmount(10000L)
                    .pointAmount(0L)
                    .pgTid(PAYMENT_KEY)
                    .status(PaymentStatus.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        private Map<String, Object> createCancelSuccessResponse(Long cancelAmount) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "CANCELED");
            response.put("paymentKey", PAYMENT_KEY);

            Map<String, Object> cancel = new HashMap<>();
            cancel.put("cancelAmount", cancelAmount.intValue());
            cancel.put("cancelReason", "고객 변심");
            response.put("cancels", List.of(cancel));

            return response;
        }

        @Test
        @DisplayName("토스페이먼츠 결제 취소 성공")
        void processCancel_Success() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            Payment payment = createValidPayment();
            Map<String, Object> successResponse = createCancelSuccessResponse(10000L);

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(payment);
            given(tossPaymentUtil.requestCancel(PAYMENT_KEY, "고객 변심", 10000L))
                    .willReturn(successResponse);

            // when
            PaymentResult result = tossPaymentStrategy.cancel(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getPgTid()).isEqualTo(PAYMENT_KEY);
            assertThat(result.getApprovedAmount()).isEqualTo(10000L);

            verify(paymentRepository).findById(PAYMENT_ID);
            verify(tossPaymentUtil).requestCancel(PAYMENT_KEY, "고객 변심", 10000L);
        }

        @Test
        @DisplayName("토스페이먼츠 부분 취소 성공")
        void processCancel_PartialCancel_Success() {
            // given
            PaymentCancelRequest request = PaymentCancelRequest.builder()
                    .paymentId(PAYMENT_ID)
                    .cancelReason("부분 취소")
                    .cancelAmount(5000L)
                    .build();
            Payment payment = createValidPayment();
            Map<String, Object> successResponse = createCancelSuccessResponse(5000L);
            successResponse.put("status", "PARTIAL_CANCELED");

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(payment);
            given(tossPaymentUtil.requestCancel(PAYMENT_KEY, "부분 취소", 5000L))
                    .willReturn(successResponse);

            // when
            PaymentResult result = tossPaymentStrategy.cancel(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getApprovedAmount()).isEqualTo(5000L);

            verify(tossPaymentUtil).requestCancel(PAYMENT_KEY, "부분 취소", 5000L);
        }

        @Test
        @DisplayName("토스페이먼츠 결제 취소 실패 - 결제 정보 없음")
        void processCancel_PaymentNotFound() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(null);

            // when
            PaymentResult result = tossPaymentStrategy.cancel(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("PAYMENT_NOT_FOUND");
            assertThat(result.getMessage()).isEqualTo("결제 정보를 찾을 수 없습니다");

            verify(paymentRepository).findById(PAYMENT_ID);
            verify(tossPaymentUtil, never()).requestCancel(any(), any(), any());
        }

        @Test
        @DisplayName("토스페이먼츠 결제 취소 실패 - PG사 응답 실패")
        void processCancel_PgFailure() {
            // given
            PaymentCancelRequest request = createValidCancelRequest();
            Payment payment = createValidPayment();
            Map<String, Object> failureResponse = new HashMap<>();
            failureResponse.put("status", "FAILED");
            failureResponse.put("code", "ALREADY_CANCELED");
            failureResponse.put("message", "이미 취소된 결제입니다");

            given(paymentRepository.findById(PAYMENT_ID)).willReturn(payment);
            given(tossPaymentUtil.requestCancel(PAYMENT_KEY, "고객 변심", 10000L))
                    .willReturn(failureResponse);

            // when
            PaymentResult result = tossPaymentStrategy.cancel(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("ALREADY_CANCELED");
            assertThat(result.getMessage()).isEqualTo("이미 취소된 결제입니다");
        }

        @Test
        @DisplayName("취소 금액이 0 이하인 경우 검증 실패")
        void processCancel_InvalidCancelAmount() {
            // given
            PaymentCancelRequest request = PaymentCancelRequest.builder()
                    .paymentId(PAYMENT_ID)
                    .cancelReason("고객 변심")
                    .cancelAmount(-1000L)
                    .build();

            // when
            PaymentResult result = tossPaymentStrategy.cancel(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("INVALID_CANCEL_AMOUNT");
            assertThat(result.getMessage()).isEqualTo("취소 금액이 올바르지 않습니다");

            verify(paymentRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("망취소 테스트")
    class NetworkCancelTest {

        @Test
        @DisplayName("토스페이먼츠 망취소 성공")
        void networkCancel_Success() {
            // given
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("status", "CANCELED");
            successResponse.put("paymentKey", PAYMENT_KEY);

            given(tossPaymentUtil.requestNetworkCancel(PAYMENT_KEY))
                    .willReturn(successResponse);

            // when
            PaymentResult result = tossPaymentStrategy.networkCancel(PAYMENT_KEY);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getPgTid()).isEqualTo(PAYMENT_KEY);
            assertThat(result.getMessage()).isEqualTo("망취소가 성공적으로 처리되었습니다");

            verify(tossPaymentUtil).requestNetworkCancel(PAYMENT_KEY);
        }

        @Test
        @DisplayName("토스페이먼츠 망취소 실패")
        void networkCancel_Failure() {
            // given
            Map<String, Object> failureResponse = new HashMap<>();
            failureResponse.put("status", "FAILED");
            failureResponse.put("code", "NETWORK_ERROR");
            failureResponse.put("message", "네트워크 오류가 발생했습니다");

            given(tossPaymentUtil.requestNetworkCancel(PAYMENT_KEY))
                    .willReturn(failureResponse);

            // when
            PaymentResult result = tossPaymentStrategy.networkCancel(PAYMENT_KEY);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("NETWORK_ERROR");
            assertThat(result.getMessage()).isEqualTo("네트워크 오류가 발생했습니다");
        }

        @Test
        @DisplayName("토스페이먼츠 망취소 시스템 오류")
        void networkCancel_SystemError() {
            // given
            given(tossPaymentUtil.requestNetworkCancel(PAYMENT_KEY))
                    .willThrow(new RuntimeException("시스템 오류"));

            // when
            PaymentResult result = tossPaymentStrategy.networkCancel(PAYMENT_KEY);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo("SYSTEM_ERROR");
            assertThat(result.getMessage()).isEqualTo("망취소 처리 중 시스템 오류가 발생했습니다");
        }
    }

    @Nested
    @DisplayName("PG 타입 테스트")
    class PgTypeTest {

        @Test
        @DisplayName("지원하는 PG 타입은 TOSS")
        void getSupportedPgType() {
            // when
            String pgType = tossPaymentStrategy.getSupportedPgType();

            // then
            assertThat(pgType).isEqualTo("TOSS");
        }
    }
}
