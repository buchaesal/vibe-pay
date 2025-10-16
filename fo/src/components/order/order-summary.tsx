'use client';

import React from 'react';

interface OrderSummaryProps {
  productAmount: number;
  pointUsed: number;
  finalAmount: number;
}

export const OrderSummary: React.FC<OrderSummaryProps> = ({
  productAmount,
  pointUsed,
  finalAmount,
}) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">
        주문 금액
      </h2>

      <div className="space-y-3">
        <div className="flex justify-between items-center">
          <span className="text-gray-600">상품 금액</span>
          <span className="text-lg font-medium">
            {productAmount.toLocaleString()}원
          </span>
        </div>

        {pointUsed > 0 && (
          <div className="flex justify-between items-center">
            <span className="text-gray-600">적립금 사용</span>
            <span className="text-lg font-medium text-red-600">
              -{pointUsed.toLocaleString()}원
            </span>
          </div>
        )}

        <hr className="border-gray-200" />

        <div className="flex justify-between items-center">
          <span className="text-lg font-semibold text-gray-900">
            최종 결제 금액
          </span>
          <span className="text-xl font-bold text-blue-600">
            {finalAmount.toLocaleString()}원
          </span>
        </div>
      </div>
    </div>
  );
};