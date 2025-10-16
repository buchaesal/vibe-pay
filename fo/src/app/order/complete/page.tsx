'use client';

import React, { useEffect, useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { isAuthenticated, authApi } from '@/lib/auth';

function OrderCompleteContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isLoading, setIsLoading] = useState(true);
  const orderId = searchParams.get('orderId');

  useEffect(() => {
    // 인증 확인
    if (!isAuthenticated()) {
      router.push('/login');
      return;
    }

    // 주문 ID가 없으면 주문 페이지로 리다이렉트
    if (!orderId) {
      router.push('/order');
      return;
    }

    setIsLoading(false);
  }, [router, orderId]);

  const handleNewOrder = () => {
    router.push('/order');
  };

  const handleOrderList = () => {
    router.push('/order/list');
  };

  const handleLogout = async () => {
    await authApi.logout();
    router.push('/login');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <h1 className="text-xl font-semibold text-gray-900">
              주문 완료
            </h1>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-600 hover:text-gray-900 underline"
            >
              로그아웃
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="max-w-md mx-auto">
          <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-200 text-center">
            {/* Success Icon */}
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg
                className="w-8 h-8 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>

            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              주문이 완료되었습니다!
            </h2>

            <p className="text-gray-600 mb-6">
              주문번호: <span className="font-mono font-medium">{orderId}</span>
            </p>

            <div className="space-y-3">
              <Button
                onClick={handleNewOrder}
                size="lg"
                className="w-full"
              >
                새 주문하기
              </Button>

              <Button
                onClick={handleOrderList}
                variant="outline"
                size="lg"
                className="w-full"
              >
                주문 목록 보기
              </Button>

              <Button
                onClick={handleLogout}
                variant="outline"
                size="lg"
                className="w-full"
              >
                로그아웃
              </Button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default function OrderCompletePage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    }>
      <OrderCompleteContent />
    </Suspense>
  );
}