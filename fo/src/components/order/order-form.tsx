'use client';

import React, { useState, useCallback, useEffect } from 'react';
import { ProductInfoSection } from './product-info-section';
import { PaymentSelector } from '@/components/payment/PaymentSelector';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { orderApi, paymentApi } from '@/lib/api';
import { requestInicisPayment } from '@/lib/payment/inicis';
import { requestTossPayment } from '@/lib/payment/toss';
import type { PaymentMethod, PgAuthResponse, PgAuthParams, TossAuthParams } from '@/types/payment';

interface OrderFormProps {
  onOrderComplete?: (orderId: string) => void;
}

interface FormErrors {
  productName?: string;
  productPrice?: string;
  quantity?: string;
  terms?: string;
}

export const OrderForm: React.FC<OrderFormProps> = ({ onOrderComplete }) => {
  const [productName, setProductName] = useState('');
  const [productPrice, setProductPrice] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CARD');
  const [pointAmount, setPointAmount] = useState(0);
  const [pointBalance, setPointBalance] = useState(0);
  const [agreedToTerms, setAgreedToTerms] = useState(false);

  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const totalAmount = productPrice * quantity;
  const cardAmount = paymentMethod === 'POINT' ? 0
    : paymentMethod === 'MIXED' ? totalAmount - pointAmount
    : totalAmount;

  useEffect(() => {
    const fetchPointBalance = async () => {
      try {
        const response = await orderApi.getPointBalance();
        setPointBalance(response.balance);
      } catch (error) {
        console.error('적립금 조회 실패:', error);
      }
    };
    fetchPointBalance();
  }, []);

  const generateOrderNumber = (): string => {
    const now = new Date();
    const timestamp = now.getFullYear().toString() +
      (now.getMonth() + 1).toString().padStart(2, '0') +
      now.getDate().toString().padStart(2, '0') +
      now.getHours().toString().padStart(2, '0') +
      now.getMinutes().toString().padStart(2, '0') +
      now.getSeconds().toString().padStart(2, '0');
    const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
    return `ORD${timestamp}${random}`;
  };

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

    if (!agreedToTerms) {
      newErrors.terms = '약관에 동의해주세요.';
    }

    return newErrors;
  }, [productName, productPrice, quantity, agreedToTerms]);

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
      const orderNumber = generateOrderNumber();

      const orderData = {
        orderNumber,
        productName: productName.trim(),
        productPrice,
        quantity,
        paymentMethod,
        pointAmount,
        cardAmount,
      };

      sessionStorage.setItem('pendingOrder', JSON.stringify(orderData));

      if (paymentMethod === 'POINT') {
        const response = await orderApi.createOrder({
          ...orderData,
          pgAuthToken: undefined,
          pgTid: undefined,
          mid: undefined,
          price: undefined,
          currency: undefined,
        });

        sessionStorage.removeItem('pendingOrder');
        onOrderComplete?.(response.orderId);

      } else {
        // 카드 결제 포함 - PG사 선택 후 인증창 호출
        const pgResponse: PgAuthResponse = await paymentApi.getPgAuthParams(cardAmount, productName);

        if (pgResponse.pgType === 'TOSS') {
          await requestTossPayment(
            0,
            totalAmount,
            cardAmount,
            pointAmount,
            productName
          );
        } else {
          await requestInicisPayment(
            0,
            totalAmount,
            cardAmount,
            pointAmount,
            productName
          );
        }
      }

    } catch (error: any) {
      console.error('결제 처리 실패:', error);
      sessionStorage.removeItem('pendingOrder');
      setSubmitError(
        error.message || '결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.'
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

      <PaymentSelector
        totalAmount={totalAmount}
        pointBalance={pointBalance}
        productName={productName}
        paymentMethod={paymentMethod}
        pointAmount={pointAmount}
        onPaymentMethodChange={setPaymentMethod}
        onPointAmountChange={setPointAmount}
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
        {isSubmitting ? '처리 중...' : `${totalAmount.toLocaleString()}원 결제하기`}
      </Button>
    </form>
  );
};
