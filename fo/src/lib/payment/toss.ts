// 토스페이먼츠 결제 처리 로직

import { paymentApi } from '../api';
import type { TossAuthParams, PgAuthResponse, PaymentApprovalRequest } from '@/types/payment';

declare global {
  interface Window {
    TossPayments?: any;
  }
}

/**
 * 토스페이먼츠 JS SDK 동적 로드
 */
export function loadTossScript(clientKey: string): Promise<any> {
  return new Promise((resolve, reject) => {
    if (window.TossPayments) {
      resolve(window.TossPayments(clientKey));
      return;
    }

    const existingScript = document.querySelector('script[src*="tosspayments"]');
    if (existingScript) {
      existingScript.addEventListener('load', () => {
        resolve(window.TossPayments(clientKey));
      });
      existingScript.addEventListener('error', () => reject(new Error('토스페이먼츠 스크립트 로드 실패')));
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://js.tosspayments.com/v1/payment';
    script.async = true;

    script.onload = () => {
      resolve(window.TossPayments(clientKey));
    };
    script.onerror = () => reject(new Error('토스페이먼츠 스크립트 로드 실패'));

    document.head.appendChild(script);
  });
}

/**
 * 토스페이먼츠 결제 요청
 */
export async function requestTossPayment(
  orderId: number,
  totalAmount: number,
  cardAmount: number,
  pointAmount: number,
  productName: string
): Promise<PaymentApprovalRequest> {
  try {
    const response: PgAuthResponse = await paymentApi.getPgAuthParams(cardAmount, productName, 'TOSS');

    if (response.pgType !== 'TOSS') {
      throw new Error('토스페이먼츠 결제만 지원합니다');
    }

    const authParams = response.authParams as TossAuthParams;
    const tossPayments = await loadTossScript(authParams.clientKey);

    await tossPayments.requestPayment('카드', {
      amount: cardAmount,
      orderId: authParams.orderId,
      orderName: authParams.orderName,
      customerName: authParams.customerName,
      customerEmail: authParams.customerEmail,
      successUrl: `${window.location.origin}/order/processing`,
      failUrl: `${window.location.origin}/order/failure`,
    });

    return {
      orderId,
      paymentMethod: pointAmount > 0 ? 'MIXED' : 'CARD',
      totalAmount,
      cardAmount,
      pointAmount,
    };

  } catch (error) {
    console.error('토스페이먼츠 결제 요청 실패:', error);
    throw error;
  }
}
