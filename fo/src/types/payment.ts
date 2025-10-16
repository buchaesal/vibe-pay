// 결제 관련 타입 정의

export type PaymentMethod = 'CARD' | 'POINT' | 'MIXED';

export interface PaymentApprovalRequest {
  orderId: number;
  paymentMethod: PaymentMethod;
  totalAmount: number;
  cardAmount?: number;
  pointAmount?: number;
  pgAuthToken?: string;  // 이니시스 인증 토큰
  pgTid?: string;        // 이니시스 거래 ID
  authResultCode?: string;
  authResultMsg?: string;
}

export interface PaymentCancelRequest {
  paymentId: number;
  cancelReason?: string;
}

export interface PaymentResponse {
  id: number;
  orderId: number;
  paymentMethod: PaymentMethod;
  pgType: string;
  totalAmount: number;
  cardAmount: number;
  pointAmount: number;
  status: string;
  pgTid?: string;
  pgAuthCode?: string;
  cardNumber?: string;
  cardName?: string;
  createdAt: string;
}

export interface PgAuthParams {
  mid: string;
  oid: string;
  price: number;
  timestamp: string;
  signature: string;
  mKey: string;
  currency: string;
  buyername: string;
  buyeremail: string;
  buyertel: string;
  goodname: string;
}

export interface InicisAuthResponse {
  resultCode: string;
  resultMsg: string;
  authToken?: string;
  tid?: string;
  // 기타 이니시스 응답 필드
  [key: string]: any;
}