'use client';

import React from 'react';
import { Input } from '@/components/ui/input';

interface ProductInfoSectionProps {
  productName: string;
  productPrice: number;
  quantity: number;
  onProductNameChange: (value: string) => void;
  onProductPriceChange: (value: number) => void;
  onQuantityChange: (value: number) => void;
  errors: {
    productName?: string;
    productPrice?: string;
    quantity?: string;
  };
}

export const ProductInfoSection: React.FC<ProductInfoSectionProps> = ({
  productName,
  productPrice,
  quantity,
  onProductNameChange,
  onProductPriceChange,
  onQuantityChange,
  errors,
}) => {
  const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^0-9]/g, '');
    onProductPriceChange(Number(value));
  };

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Math.max(1, Math.min(99, Number(e.target.value)));
    onQuantityChange(value);
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">
        상품 정보
      </h2>

      <div className="space-y-4">
        <Input
          label="상품명"
          required
          value={productName}
          onChange={(e) => onProductNameChange(e.target.value)}
          placeholder="상품명을 입력하세요"
          error={errors.productName}
          maxLength={100}
        />

        <Input
          label="상품금액"
          required
          type="text"
          value={productPrice === 0 ? '' : productPrice.toLocaleString()}
          onChange={handlePriceChange}
          placeholder="0"
          error={errors.productPrice}
          className="text-right"
        />

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            수량 <span className="text-red-500">*</span>
          </label>
          <div className="flex items-center space-x-3">
            <button
              type="button"
              onClick={() => onQuantityChange(Math.max(1, quantity - 1))}
              className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-50 disabled:opacity-50"
              disabled={quantity <= 1}
            >
              -
            </button>
            <Input
              type="number"
              value={quantity}
              onChange={handleQuantityChange}
              min={1}
              max={99}
              className="w-20 text-center"
              error={errors.quantity}
            />
            <button
              type="button"
              onClick={() => onQuantityChange(Math.min(99, quantity + 1))}
              className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-50 disabled:opacity-50"
              disabled={quantity >= 99}
            >
              +
            </button>
          </div>
          {errors.quantity && (
            <p className="text-sm text-red-600 mt-1" role="alert">
              {errors.quantity}
            </p>
          )}
        </div>
      </div>
    </div>
  );
};