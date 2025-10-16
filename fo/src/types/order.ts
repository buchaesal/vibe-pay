// 주문 관련 타입 정의
export interface OrderItem {
  productName: string;
  productPrice: number;
  quantity: number;
}

export interface OrderSummary {
  productAmount: number;
  pointUsed: number;
  finalAmount: number;
}

export interface CreateOrderRequest {
  orderNumber: string;
  productName: string;
  productPrice: number;
  quantity: number;
  paymentMethod: 'CARD' | 'POINT' | 'MIXED';
  pointAmount: number;
  cardAmount: number;
  pgAuthToken?: string;
  pgTid?: string;
  mid?: string;
  price?: string;
  currency?: string;
}

export interface CreateOrderResponse {
  orderId: string;
  status: string;
  message: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  productName: string;
  productPrice: number;
  quantity: number;
  totalAmount: number;
  pointAmount: number;
  cardAmount: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

export interface OrderListResponse {
  orders: Order[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface PointBalance {
  balance: number;
}

export interface PointDeductRequest {
  amount: number;
  orderId: string;
}

export interface PointDeductResponse {
  success: boolean;
  remainingBalance: number;
  message: string;
}