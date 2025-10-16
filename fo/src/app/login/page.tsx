'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { isAuthenticated, authApi } from '@/lib/auth';

export default function LoginPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [errors, setErrors] = useState<{
    username?: string;
    password?: string;
    submit?: string;
  }>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // 이미 로그인된 경우 주문 페이지로 리다이렉트
    if (isAuthenticated()) {
      router.push('/order');
    }
  }, [router]);

  const validateForm = () => {
    const newErrors: typeof errors = {};

    if (!formData.username.trim()) {
      newErrors.username = '사용자명을 입력해주세요.';
    }

    if (!formData.password.trim()) {
      newErrors.password = '비밀번호를 입력해주세요.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setErrors({});

    try {
      const response = await authApi.login(formData);

      if (response.success) {
        // 로그인 성공 시 주문 페이지로 이동
        router.push('/order');
      } else {
        setErrors({ submit: response.message || '로그인에 실패했습니다.' });
      }
    } catch (error: any) {
      setErrors({
        submit: error.message || '로그인 중 오류가 발생했습니다.'
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (field: keyof typeof formData) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: e.target.value,
    }));

    // 에러 클리어
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: undefined,
      }));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-md w-full">
        <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-200">
          <div className="text-center mb-8">
            <h1 className="text-2xl font-bold text-gray-900">
              Vibe Pay
            </h1>
            <p className="text-gray-600 mt-2">
              로그인 후 주문을 시작하세요
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="사용자명"
              required
              value={formData.username}
              onChange={handleInputChange('username')}
              error={errors.username}
              placeholder="사용자명을 입력하세요"
              autoComplete="username"
            />

            <Input
              label="비밀번호"
              type="password"
              required
              value={formData.password}
              onChange={handleInputChange('password')}
              error={errors.password}
              placeholder="비밀번호를 입력하세요"
              autoComplete="current-password"
            />

            {errors.submit && (
              <div className="bg-red-50 border border-red-200 rounded-md p-3">
                <p className="text-red-600 text-sm">{errors.submit}</p>
              </div>
            )}

            <Button
              type="submit"
              size="lg"
              loading={isLoading}
              disabled={isLoading}
              className="w-full"
            >
              {isLoading ? '로그인 중...' : '로그인'}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-xs text-gray-500">
              테스트용 계정을 사용해주세요
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}