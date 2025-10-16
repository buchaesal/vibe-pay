'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { OrderList } from '@/components/order/order-list';
import { isAuthenticated, authApi } from '@/lib/auth';

export default function OrderListPage() {
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

  const handleOrderDetail = (orderId: string) => {
    // 주문 상세 페이지로 이동 (현재는 구현되지 않음)
    console.log('주문 상세:', orderId);
    alert(`주문 상세 기능은 추후 구현 예정입니다. (주문 ID: ${orderId})`);
  };

  const handleNewOrder = () => {
    router.push('/order');
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
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <h1 className="text-xl font-semibold text-gray-900">
              주문 목록
            </h1>
            <div className="flex items-center space-x-4">
              <Button
                onClick={handleNewOrder}
                size="sm"
              >
                새 주문하기
              </Button>
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
      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <OrderList onOrderDetail={handleOrderDetail} />
      </main>
    </div>
  );
}