import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 숫자 포맷팅 함수
export function formatNumber(value: number): string {
  return value.toLocaleString();
}

// 금액 포맷팅 함수
export function formatCurrency(value: number): string {
  return `${value.toLocaleString()}원`;
}

// 입력값에서 숫자만 추출
export function extractNumbers(value: string): string {
  return value.replace(/[^0-9]/g, '');
}

// 안전한 숫자 변환
export function safeNumber(value: string | number, defaultValue: number = 0): number {
  if (typeof value === 'number') return value;
  const num = Number(value);
  return isNaN(num) ? defaultValue : num;
}

// 날짜 포맷팅
export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 주문 상태 한글 변환
export function getOrderStatusText(status: string): string {
  switch (status) {
    case 'PENDING':
      return '결제 대기';
    case 'PAID':
      return '결제 완료';
    case 'CANCELLED':
      return '취소됨';
    default:
      return status;
  }
}

// 주문 상태별 색상 클래스
export function getOrderStatusColor(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'text-yellow-600 bg-yellow-50 border-yellow-200';
    case 'PAID':
      return 'text-green-600 bg-green-50 border-green-200';
    case 'CANCELLED':
      return 'text-red-600 bg-red-50 border-red-200';
    default:
      return 'text-gray-600 bg-gray-50 border-gray-200';
  }
}