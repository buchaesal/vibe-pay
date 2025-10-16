'use client';

import React, { useState, useEffect } from 'react';
import { Input } from '@/components/ui/input';
import { orderApi } from '@/lib/api';

interface PointSectionProps {
  pointUsed: number;
  onPointUsedChange: (value: number) => void;
  maxUsableAmount: number;
  error?: string;
}

export const PointSection: React.FC<PointSectionProps> = ({
  pointUsed,
  onPointUsedChange,
  maxUsableAmount,
  error,
}) => {
  const [pointBalance, setPointBalance] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string>('');

  useEffect(() => {
    const fetchPointBalance = async () => {
      try {
        setLoading(true);
        setFetchError('');
        const response = await orderApi.getPointBalance();
        setPointBalance(response.balance);
      } catch (error) {
        console.error('적립금 조회 실패:', error);
        setFetchError('적립금 조회에 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchPointBalance();
  }, []);

  const handlePointChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^0-9]/g, '');
    const numValue = Number(value);

    // 보유 적립금과 사용 가능 금액 중 작은 값으로 제한
    const maxUsable = Math.min(pointBalance, maxUsableAmount);
    const finalValue = Math.min(numValue, maxUsable);

    onPointUsedChange(finalValue);
  };

  const handleUseAllPoints = () => {
    const maxUsable = Math.min(pointBalance, maxUsableAmount);
    onPointUsedChange(maxUsable);
  };

  if (loading) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          적립금 사용
        </h2>
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
          <div className="h-10 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">
        적립금 사용
      </h2>

      {fetchError ? (
        <div className="text-red-600 text-sm mb-4">
          {fetchError}
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex justify-between items-center p-3 bg-gray-50 rounded-md">
            <span className="text-sm text-gray-600">보유 적립금</span>
            <span className="text-lg font-semibold text-blue-600">
              {pointBalance.toLocaleString()}원
            </span>
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="block text-sm font-medium text-gray-700">
                사용할 적립금
              </label>
              <button
                type="button"
                onClick={handleUseAllPoints}
                className="text-sm text-blue-600 hover:text-blue-800 underline"
                disabled={pointBalance === 0 || maxUsableAmount === 0}
              >
                전액 사용
              </button>
            </div>

            <Input
              type="text"
              value={pointUsed === 0 ? '' : pointUsed.toLocaleString()}
              onChange={handlePointChange}
              placeholder="0"
              error={error}
              className="text-right"
            />

            <div className="text-xs text-gray-500">
              최대 사용 가능: {Math.min(pointBalance, maxUsableAmount).toLocaleString()}원
            </div>
          </div>
        </div>
      )}
    </div>
  );
};