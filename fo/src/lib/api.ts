const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public data?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

interface RequestOptions {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  headers?: Record<string, string>;
  body?: any;
}

async function request<T>(endpoint: string, options: RequestOptions): Promise<T> {
  const { method, headers = {}, body } = options;

  const config: RequestInit = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    credentials: 'include', // 세션 쿠키 전송
  };

  if (body) {
    config.body = JSON.stringify(body);
  }

  const url = `${API_BASE_URL}${endpoint}`;

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new ApiError(
        errorData.message || `HTTP ${response.status}: ${response.statusText}`,
        response.status,
        errorData
      );
    }

    // 응답이 비어있는 경우 (예: 204 No Content)
    if (response.status === 204) {
      return {} as T;
    }

    return await response.json();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.',
      0
    );
  }
}

export const api = {
  get: <T>(endpoint: string, headers?: Record<string, string>) =>
    request<T>(endpoint, { method: 'GET', headers }),

  post: <T>(endpoint: string, body?: any, headers?: Record<string, string>) =>
    request<T>(endpoint, { method: 'POST', body, headers }),

  put: <T>(endpoint: string, body?: any, headers?: Record<string, string>) =>
    request<T>(endpoint, { method: 'PUT', body, headers }),

  delete: <T>(endpoint: string, headers?: Record<string, string>) =>
    request<T>(endpoint, { method: 'DELETE', headers }),
};

// 주문 관련 API
export const orderApi = {
  // 적립금 잔액 조회
  getPointBalance: () => api.get<{ balance: number }>('/api/point/balance'),

  // 주문 생성
  createOrder: (orderData: {
    productName: string;
    productPrice: number;
    quantity: number;
    pointUsed: number;
    totalAmount: number;
    agreedToTerms: boolean;
  }) => api.post<{ orderId: string; status: string; message: string }>('/api/order/create', orderData),

  // 적립금 차감
  deductPoint: (data: { amount: number; orderId: string }) =>
    api.post<{ success: boolean; remainingBalance: number; message: string }>('/api/point/deduct', data),

  // 주문 목록 조회
  getOrderList: () => api.get<any[]>('/api/order/list'),

  // 주문 목록 조회 (페이징)
  getOrderListWithPaging: (page: number = 0, size: number = 10) =>
    api.get<any>(`/api/order/page?page=${page}&size=${size}`),

  // 주문 상세 조회
  getOrderDetail: (orderId: string) => api.get<any>(`/api/order/${orderId}`),

  // 주문 취소
  cancelOrder: (orderId: string) => api.post<any>(`/api/order/${orderId}/cancel`),
};

// 결제 관련 API
export const paymentApi = {
  // PG 인증 파라미터 조회
  getPgAuthParams: (price: number, goodname: string, pgType?: string) => {
    const url = pgType
      ? `/api/pg/auth-params?price=${price}&goodname=${encodeURIComponent(goodname)}&pgType=${pgType}`
      : `/api/pg/auth-params?price=${price}&goodname=${encodeURIComponent(goodname)}`;
    return api.get<any>(url);
  },

  // 결제 승인
  approvePayment: (request: any) =>
    api.post<any>('/api/payment/approve', request),

  // 결제 취소
  cancelPayment: (request: any) =>
    api.post<any>('/api/payment/cancel', request),

  // 결제 내역 조회
  getPaymentHistory: (page: number = 0, size: number = 10) =>
    api.get<any>(`/api/payment/history?page=${page}&size=${size}`),

  // 결제 상세 조회
  getPaymentDetail: (paymentId: number) =>
    api.get<any>(`/api/payment/${paymentId}`),
};
