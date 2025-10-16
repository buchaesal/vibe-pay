'use client';

import React, { useMemo } from 'react';
import { Input } from '@/components/ui/input';
import type { PaymentMethod } from '@/types/payment';

interface PaymentSelectorProps {
  totalAmount: number;
  pointBalance: number;
  productName: string;
  paymentMethod: PaymentMethod;
  pointAmount: number;
  onPaymentMethodChange: (method: PaymentMethod) => void;
  onPointAmountChange: (amount: number) => void;
}

export const PaymentSelector: React.FC<PaymentSelectorProps> = ({
  totalAmount,
  pointBalance,
  productName,
  paymentMethod,
  pointAmount,
  onPaymentMethodChange,
  onPointAmountChange,
}) => {
  const cardAmount = useMemo(() => {
    if (paymentMethod === 'POINT') return 0;
    if (paymentMethod === 'MIXED') return totalAmount - pointAmount;
    return totalAmount;
  }, [paymentMethod, totalAmount, pointAmount]);

  const validationError = useMemo(() => {
    if (paymentMethod === 'MIXED') {
      if (pointAmount <= 0) {
        return '사용할 적립금을 입력해주세요.';
      }
      if (pointAmount > pointBalance) {
        return '보유 적립금을 초과할 수 없습니다.';
      }
      if (pointAmount > totalAmount) {
        return '총 주문금액을 초과할 수 없습니다.';
      }
      if (cardAmount < 100) {
        return '카드 결제 금액은 100원 이상이어야 합니다.';
      }
    }

    if (paymentMethod === 'POINT' && totalAmount > pointBalance) {
      return '적립금이 부족합니다.';
    }

    return '';
  }, [paymentMethod, pointAmount, cardAmount, totalAmount, pointBalance]);

  const handlePaymentMethodChange = (method: PaymentMethod) => {
    onPaymentMethodChange(method);

    if (method === 'POINT') {
      onPointAmountChange(totalAmount);
    } else if (method === 'CARD') {
      onPointAmountChange(0);
    }
  };

  const handlePointAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^0-9]/g, '');
    const numValue = value ? parseInt(value, 10) : 0;
    onPointAmountChange(Math.min(numValue, pointBalance, totalAmount));
  };

  const handleUseAllPoints = () => {
    const maxUsable = Math.min(pointBalance, totalAmount - 100);
    onPointAmountChange(maxUsable);
  };

  return (
    <div className="space-y-6">
      {/* 결제 정보 */}
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">결제 정보</h3>

        <div className="space-y-3">
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">총 주문금액</span>
            <span className="font-semibold text-gray-900">{totalAmount.toLocaleString()}원</span>
          </div>

          <div className="flex justify-between text-sm">
            <span className="text-gray-600">보유 적립금</span>
            <span className="font-semibold text-blue-600">{pointBalance.toLocaleString()}원</span>
          </div>
        </div>
      </div>

      {/* 결제수단 선택 */}
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">결제수단 선택</h3>

        <div className="space-y-3">
          <label className={`flex items-center p-4 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'CARD' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-gray-300'
          }`}>
            <input
              type="radio"
              name="paymentMethod"
              value="CARD"
              checked={paymentMethod === 'CARD'}
              onChange={() => handlePaymentMethodChange('CARD')}
              className="w-4 h-4 text-blue-600"
            />
            <span className="ml-3 font-medium text-gray-900">카드 전액 결제</span>
          </label>

          <label className={`flex items-center p-4 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'POINT' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-gray-300'
          } ${pointBalance < totalAmount ? 'opacity-50 cursor-not-allowed' : ''}`}>
            <input
              type="radio"
              name="paymentMethod"
              value="POINT"
              checked={paymentMethod === 'POINT'}
              onChange={() => handlePaymentMethodChange('POINT')}
              disabled={pointBalance < totalAmount}
              className="w-4 h-4 text-blue-600"
            />
            <span className="ml-3 font-medium text-gray-900">적립금 전액 결제</span>
            {pointBalance < totalAmount && (
              <span className="ml-2 text-xs text-red-600">(잔액 부족)</span>
            )}
          </label>

          <label className={`flex items-center p-4 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'MIXED' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-gray-300'
          }`}>
            <input
              type="radio"
              name="paymentMethod"
              value="MIXED"
              checked={paymentMethod === 'MIXED'}
              onChange={() => handlePaymentMethodChange('MIXED')}
              className="w-4 h-4 text-blue-600"
            />
            <span className="ml-3 font-medium text-gray-900">복합 결제 (적립금 + 카드)</span>
          </label>
        </div>

        {paymentMethod === 'MIXED' && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center justify-between mb-2">
              <label className="text-sm font-medium text-gray-700">사용할 적립금</label>
              <button
                type="button"
                onClick={handleUseAllPoints}
                className="text-sm text-blue-600 hover:text-blue-800 underline"
              >
                최대 사용
              </button>
            </div>

            <Input
              type="text"
              value={pointAmount === 0 ? '' : pointAmount.toLocaleString()}
              onChange={handlePointAmountChange}
              placeholder="0"
              className="text-right"
            />

            <div className="mt-2 text-xs text-gray-500">
              최대 사용 가능: {Math.min(pointBalance, totalAmount - 100).toLocaleString()}원
              <br />
              (카드 결제 최소 금액: 100원)
            </div>
          </div>
        )}
      </div>

      {/* 결제 금액 요약 */}
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">최종 결제 금액</h3>

        <div className="space-y-2">
          {pointAmount > 0 && (
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">적립금 사용</span>
              <span className="text-blue-600">-{pointAmount.toLocaleString()}원</span>
            </div>
          )}

          {cardAmount > 0 && (
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">카드 결제</span>
              <span className="text-gray-900">{cardAmount.toLocaleString()}원</span>
            </div>
          )}

          <div className="pt-2 border-t border-gray-200 flex justify-between">
            <span className="font-semibold text-gray-900">총 결제금액</span>
            <span className="text-xl font-bold text-gray-900">{totalAmount.toLocaleString()}원</span>
          </div>
        </div>
      </div>

      {validationError && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-600 text-sm text-center">{validationError}</p>
        </div>
      )}
    </div>
  );
};
