'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { OrderForm } from '@/components/order/order-form';
import { isAuthenticated, authApi } from '@/lib/auth';

export default function OrderPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 인증 확인
    if (!isAuthenticated()) {
      router.push('/login');
      return;
    }
    setIsLoading(false);
  }, [router]);

  const handleOrderComplete = (orderId: string) => {
    // 주문 완료 후 결과 페이지로 이동
    router.push(`/order/complete?orderId=${orderId}`);
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
              주문서
            </h1>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.push('/order/list')}
                className="text-sm text-blue-600 hover:text-blue-800 underline"
              >
                주문 목록
              </button>
              <button
                onClick={handleLogout}
                className="text-sm text-gray-600 hover:text-gray-900 underline"
              >
                로그아웃
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="max-w-2xl mx-auto">
          <OrderForm onOrderComplete={handleOrderComplete} />
        </div>
      </main>
    </div>
  );
}