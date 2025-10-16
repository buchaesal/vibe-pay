'use client';

import React, { useState, useCallback } from 'react';
import { ProductInfoSection } from './product-info-section';
import { PointSection } from './point-section';
import { OrderSummary } from './order-summary';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { orderApi } from '@/lib/api';
import { CreateOrderRequest } from '@/types/order';

interface OrderFormProps {
  onOrderComplete?: (orderId: string) => void;
}

interface FormErrors {
  productName?: string;
  productPrice?: string;
  quantity?: string;
  pointUsed?: string;
  terms?: string;
}

export const OrderForm: React.FC<OrderFormProps> = ({ onOrderComplete }) => {
  // Form state
  const [productName, setProductName] = useState('');
  const [productPrice, setProductPrice] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [pointUsed, setPointUsed] = useState(0);
  const [agreedToTerms, setAgreedToTerms] = useState(false);

  // UI state
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  // Calculated values
  const productAmount = productPrice * quantity;
  const finalAmount = Math.max(0, productAmount - pointUsed);

  // Validation
  const validateForm = useCallback((): FormErrors => {
    const newErrors: FormErrors = {};

    if (!productName.trim()) {
      newErrors.productName = '상품명을 입력해주세요.';
    } else if (productName.length > 100) {
      newErrors.productName = '상품명은 100자 이내로 입력해주세요.';
    }

    if (productPrice <= 0) {
      newErrors.productPrice = '상품금액을 입력해주세요.';
    } else if (productPrice > 10000000) {
      newErrors.productPrice = '상품금액은 1,000만원을 초과할 수 없습니다.';
    }

    if (quantity < 1 || quantity > 99) {
      newErrors.quantity = '수량은 1~99개 사이로 입력해주세요.';
    }

    if (pointUsed > productAmount) {
      newErrors.pointUsed = '사용할 적립금이 상품금액을 초과할 수 없습니다.';
    }

    if (!agreedToTerms) {
      newErrors.terms = '약관에 동의해주세요.';
    }

    return newErrors;
  }, [productName, productPrice, quantity, pointUsed, productAmount, agreedToTerms]);

  // Handlers
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const formErrors = validateForm();
    if (Object.keys(formErrors).length > 0) {
      setErrors(formErrors);
      return;
    }

    setIsSubmitting(true);
    setSubmitError('');
    setErrors({});

    try {
      const orderData: CreateOrderRequest = {
        productName: productName.trim(),
        productPrice,
        quantity,
        pointUsed,
        totalAmount: finalAmount,
        agreedToTerms,
      };

      const response = await orderApi.createOrder(orderData);

      if (response.orderId) {
        // 적립금 사용이 있다면 차감 처리
        if (pointUsed > 0) {
          await orderApi.deductPoint({
            amount: pointUsed,
            orderId: response.orderId,
          });
        }

        onOrderComplete?.(response.orderId);
      }
    } catch (error: any) {
      console.error('주문 생성 실패:', error);
      setSubmitError(
        error.message || '주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleProductNameChange = useCallback((value: string) => {
    setProductName(value);
    if (errors.productName) {
      setErrors(prev => ({ ...prev, productName: undefined }));
    }
  }, [errors.productName]);

  const handleProductPriceChange = useCallback((value: number) => {
    setProductPrice(value);
    if (errors.productPrice) {
      setErrors(prev => ({ ...prev, productPrice: undefined }));
    }
  }, [errors.productPrice]);

  const handleQuantityChange = useCallback((value: number) => {
    setQuantity(value);
    if (errors.quantity) {
      setErrors(prev => ({ ...prev, quantity: undefined }));
    }
  }, [errors.quantity]);

  const handlePointUsedChange = useCallback((value: number) => {
    setPointUsed(value);
    if (errors.pointUsed) {
      setErrors(prev => ({ ...prev, pointUsed: undefined }));
    }
  }, [errors.pointUsed]);

  const handleTermsChange = useCallback((checked: boolean) => {
    setAgreedToTerms(checked);
    if (errors.terms) {
      setErrors(prev => ({ ...prev, terms: undefined }));
    }
  }, [errors.terms]);

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <ProductInfoSection
        productName={productName}
        productPrice={productPrice}
        quantity={quantity}
        onProductNameChange={handleProductNameChange}
        onProductPriceChange={handleProductPriceChange}
        onQuantityChange={handleQuantityChange}
        errors={{
          productName: errors.productName,
          productPrice: errors.productPrice,
          quantity: errors.quantity,
        }}
      />

      <PointSection
        pointUsed={pointUsed}
        onPointUsedChange={handlePointUsedChange}
        maxUsableAmount={productAmount}
        error={errors.pointUsed}
      />

      <OrderSummary
        productAmount={productAmount}
        pointUsed={pointUsed}
        finalAmount={finalAmount}
      />

      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <Checkbox
          checked={agreedToTerms}
          onChange={(e) => handleTermsChange(e.target.checked)}
          label="주문 약관에 동의합니다. (필수)"
          error={errors.terms}
        />

        <div className="mt-3 text-xs text-gray-500">
          주문 완료 후 취소는 고객센터로 문의해주세요.
        </div>
      </div>

      {submitError && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-600 text-sm">{submitError}</p>
        </div>
      )}

      <Button
        type="submit"
        size="lg"
        loading={isSubmitting}
        disabled={isSubmitting}
        className="w-full"
      >
        {isSubmitting ? '주문 처리 중...' : `${finalAmount.toLocaleString()}원 주문하기`}
      </Button>
    </form>
  );
};