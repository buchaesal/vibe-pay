// 이니시스 결제 처리 로직

import { paymentApi } from '../api';
import type { PgAuthParams, InicisAuthResponse, PaymentApprovalRequest } from '@/types/payment';

declare global {
  interface Window {
    INIStdPay?: {
      pay: (formId: string) => void;
    };
  }
}

/**
 * 이니시스 JS 스크립트 동적 로드
 */
export function loadInicisScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    // 이미 로드되어 있으면 바로 리턴
    if (window.INIStdPay) {
      resolve();
      return;
    }

    // 스크립트 태그가 이미 있는지 확인
    const existingScript = document.querySelector('script[src*="stdpay"]');
    if (existingScript) {
      existingScript.addEventListener('load', () => resolve());
      existingScript.addEventListener('error', () => reject(new Error('이니시스 스크립트 로드 실패')));
      return;
    }

    // 새로 스크립트 로드
    const script = document.createElement('script');
    script.src = 'https://stgstdpay.inicis.com/stdjs/INIStdPay.js';
    script.charset = 'UTF-8';
    script.async = true;

    script.onload = () => resolve();
    script.onerror = () => reject(new Error('이니시스 스크립트 로드 실패'));

    document.head.appendChild(script);
  });
}

/**
 * 이니시스 결제 요청
 */
export async function requestInicisPayment(
  orderId: number,
  totalAmount: number,
  cardAmount: number,
  pointAmount: number,
  productName: string
): Promise<PaymentApprovalRequest> {
  try {
    // 1. 이니시스 JS 로드
    await loadInicisScript();

    // 2. PG 인증 파라미터 조회
    const authParams: PgAuthParams = await paymentApi.getPgAuthParams(cardAmount, productName);

    // 3. 결제 폼 생성
    const form = createPaymentForm(authParams);
    document.body.appendChild(form);

    // 4. 이니시스 결제창 호출 및 응답 대기
    const authResponse = await openInicisPaymentWindow(form.id);

    // 5. 폼 제거
    document.body.removeChild(form);

    // 6. 인증 응답 검증
    if (authResponse.resultCode !== '0000') {
      throw new Error(authResponse.resultMsg || '결제 인증에 실패했습니다');
    }

    // 7. 승인 요청 데이터 생성
    const approvalRequest: PaymentApprovalRequest = {
      orderId,
      paymentMethod: pointAmount > 0 ? 'MIXED' : 'CARD',
      totalAmount,
      cardAmount,
      pointAmount,
      pgAuthToken: authResponse.authToken,
      pgTid: authResponse.tid,
      authResultCode: authResponse.resultCode,
      authResultMsg: authResponse.resultMsg,
    };

    return approvalRequest;

  } catch (error) {
    console.error('이니시스 결제 요청 실패:', error);
    throw error;
  }
}

/**
 * 결제 폼 생성
 */
function createPaymentForm(authParams: PgAuthParams): HTMLFormElement {
  const formId = `inicis-payment-form-${Date.now()}`;
  const form = document.createElement('form');
  form.id = formId;
  form.name = formId;
  form.method = 'POST';
  form.style.display = 'none';

  // 필수 파라미터 추가
  const params: Record<string, string> = {
    version: '1.0',
    mid: authParams.mid,
    goodname: authParams.goodname,
    oid: authParams.oid,
    price: String(authParams.price),
    currency: authParams.currency,
    buyername: authParams.buyername,
    buyertel: authParams.buyertel,
    buyeremail: authParams.buyeremail,
    timestamp: authParams.timestamp,
    signature: authParams.signature,
    mKey: authParams.mKey,
    returnUrl: `${window.location.origin}/api/payment/inicis/callback`,
    closeUrl: `${window.location.origin}/api/payment/inicis/close`,
    popupUrl: `${window.location.origin}/api/payment/inicis/popup`,
  };

  Object.entries(params).forEach(([key, value]) => {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = key;
    input.value = value;
    form.appendChild(input);
  });

  return form;
}

/**
 * 이니시스 결제창 열기 및 응답 대기
 */
function openInicisPaymentWindow(formId: string): Promise<InicisAuthResponse> {
  return new Promise((resolve, reject) => {
    if (!window.INIStdPay) {
      reject(new Error('이니시스 스크립트가 로드되지 않았습니다'));
      return;
    }

    // 결제 응답 처리 콜백 함수를 전역에 등록
    const callbackName = `inicisCallback_${Date.now()}`;
    (window as any)[callbackName] = (response: InicisAuthResponse) => {
      delete (window as any)[callbackName];
      resolve(response);
    };

    // 결제 실패 시 타임아웃 (5분)
    const timeout = setTimeout(() => {
      delete (window as any)[callbackName];
      reject(new Error('결제 시간이 초과되었습니다'));
    }, 5 * 60 * 1000);

    // 폼에 콜백 함수명 추가
    const form = document.getElementById(formId) as HTMLFormElement;
    if (form) {
      const callbackInput = document.createElement('input');
      callbackInput.type = 'hidden';
      callbackInput.name = 'resultCallback';
      callbackInput.value = callbackName;
      form.appendChild(callbackInput);
    }

    try {
      // 이니시스 결제창 호출
      window.INIStdPay.pay(formId);
    } catch (error) {
      clearTimeout(timeout);
      delete (window as any)[callbackName];
      reject(error);
    }
  });
}

/**
 * 결제 승인 요청
 */
export async function approvePayment(request: PaymentApprovalRequest): Promise<any> {
  try {
    const response = await paymentApi.approvePayment(request);
    return response;
  } catch (error) {
    console.error('결제 승인 실패:', error);
    throw error;
  }
}
