'use client';

import React, { Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';

function FailureContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const reason = searchParams.get('reason') || '알 수 없는 오류가 발생했습니다';

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div className="text-center">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
            <svg className="h-10 w-10 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>

          <h1 className="text-2xl font-bold text-gray-900 mb-3">주문 실패</h1>

          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <p className="text-red-800 text-sm">{reason}</p>
          </div>

          <p className="text-gray-600 mb-8">
            주문 처리 중 문제가 발생했습니다.<br />
            다시 시도해주세요.
          </p>

          <div className="space-y-3">
            <Button
              onClick={() => router.push('/order')}
              size="lg"
              className="w-full"
            >
              다시 주문하기
            </Button>

            <Button
              onClick={() => router.push('/order/list')}
              variant="outline"
              size="lg"
              className="w-full"
            >
              주문 목록 보기
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function OrderFailurePage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <FailureContent />
    </Suspense>
  );
}
