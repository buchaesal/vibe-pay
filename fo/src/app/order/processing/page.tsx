'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { orderApi } from '@/lib/api';

export default function OrderProcessingPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState('');

  useEffect(() => {
    processOrder();
  }, []);

  const processOrder = async () => {
    try {
      const orderDataStr = sessionStorage.getItem('pendingOrder');
      if (!orderDataStr) {
        throw new Error('주문 정보를 찾을 수 없습니다');
      }

      const orderData = JSON.parse(orderDataStr);

      const pgAuthToken = searchParams.get('authToken');
      const pgTid = searchParams.get('tid');
      const resultCode = searchParams.get('resultCode');
      const resultMsg = searchParams.get('resultMsg');

      if (resultCode !== '0000') {
        sessionStorage.removeItem('pendingOrder');
        router.push(`/order/failure?reason=${encodeURIComponent(resultMsg || '결제 인증에 실패했습니다')}`);
        return;
      }

      const response = await orderApi.createOrder({
        ...orderData,
        pgAuthToken,
        pgTid,
      });

      sessionStorage.removeItem('pendingOrder');
      router.push(`/order/complete?orderId=${response.orderId}`);

    } catch (err: any) {
      console.error('주문 처리 실패:', err);
      sessionStorage.removeItem('pendingOrder');
      setError(err.message || '주문 처리 중 오류가 발생했습니다');

      setTimeout(() => {
        router.push(`/order/failure?reason=${encodeURIComponent(err.message || '주문 처리 실패')}`);
      }, 2000);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div className="text-center">
          {error ? (
            <>
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
                <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </div>
              <h2 className="text-xl font-semibold text-gray-900 mb-2">결제 처리 실패</h2>
              <p className="text-gray-600 mb-4">{error}</p>
              <p className="text-sm text-gray-500">잠시 후 실패 화면으로 이동합니다...</p>
            </>
          ) : (
            <>
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 mb-4">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
              <h2 className="text-xl font-semibold text-gray-900 mb-2">결제 처리 중...</h2>
              <p className="text-gray-600">잠시만 기다려주세요.</p>
              <p className="text-sm text-gray-500 mt-4">창을 닫지 마세요.</p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
