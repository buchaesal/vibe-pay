'use client';

import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { orderApi } from '@/lib/api';
import { Order } from '@/types/order';
import { formatDate, getOrderStatusText, getOrderStatusColor } from '@/lib/utils';

interface OrderListProps {
  onOrderDetail?: (orderId: string) => void;
}

export const OrderList: React.FC<OrderListProps> = ({ onOrderDetail }) => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalCount, setTotalCount] = useState(0);
  const [cancellingOrder, setCancellingOrder] = useState<string>('');

  const pageSize = 10;

  const fetchOrders = async (page: number = 0) => {
    try {
      setLoading(true);
      setError('');
      const response = await orderApi.getOrderListWithPaging(page, pageSize);

      setOrders(response.orders || []);
      setTotalPages(response.totalPages || 0);
      setTotalCount(response.totalCount || 0);
      setCurrentPage(page);
    } catch (err: any) {
      console.error('주문 목록 조회 실패:', err);
      setError('주문 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders(0);
  }, []);

  const handlePageChange = (page: number) => {
    if (page >= 0 && page < totalPages) {
      fetchOrders(page);
    }
  };

  const handleCancelOrder = async (orderId: string) => {
    if (!confirm('정말로 주문을 취소하시겠습니까?')) {
      return;
    }

    try {
      setCancellingOrder(orderId);
      await orderApi.cancelOrder(orderId);

      // 목록 새로고침
      await fetchOrders(currentPage);
      alert('주문이 취소되었습니다.');
    } catch (err: any) {
      console.error('주문 취소 실패:', err);
      alert('주문 취소에 실패했습니다: ' + (err.message || '알 수 없는 오류'));
    } finally {
      setCancellingOrder('');
    }
  };

  const canCancelOrder = (status: string) => {
    return status === 'PENDING' || status === 'PAID';
  };

  if (loading && orders.length === 0) {
    return (
      <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-200">
        <div className="animate-pulse space-y-4">
          <div className="h-4 bg-gray-200 rounded w-1/4"></div>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-200 text-center">
        <div className="text-red-600 mb-4">{error}</div>
        <Button onClick={() => fetchOrders(currentPage)}>다시 시도</Button>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-200 text-center">
        <div className="text-gray-500 mb-4">주문 내역이 없습니다.</div>
        <Button onClick={() => window.location.href = '/order'}>
          첫 주문하기
        </Button>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex justify-between items-center">
          <h2 className="text-lg font-semibold text-gray-900">
            주문 목록
          </h2>
          <div className="text-sm text-gray-500">
            총 {totalCount}개의 주문
          </div>
        </div>
      </div>

      {/* Order List */}
      <div className="divide-y divide-gray-200">
        {orders.map((order) => (
          <div key={order.id} className="p-6 hover:bg-gray-50">
            <div className="flex items-center justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-4 mb-2">
                  <h3 className="font-medium text-gray-900">
                    {order.productName}
                  </h3>
                  <span
                    className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getOrderStatusColor(order.status)}`}
                  >
                    {getOrderStatusText(order.status)}
                  </span>
                </div>

                <div className="text-sm text-gray-500 space-y-1">
                  <div>주문번호: {order.orderNumber}</div>
                  <div>주문일시: {formatDate(order.createdAt)}</div>
                  <div className="flex items-center space-x-4">
                    <span>수량: {order.quantity}개</span>
                    <span>금액: {order.totalAmount.toLocaleString()}원</span>
                    {order.pointAmount > 0 && (
                      <span className="text-blue-600">
                        적립금: {order.pointAmount.toLocaleString()}원 사용
                      </span>
                    )}
                  </div>
                </div>
              </div>

              <div className="flex items-center space-x-2 ml-4">
                {onOrderDetail && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => onOrderDetail(order.id)}
                  >
                    상세보기
                  </Button>
                )}

                {canCancelOrder(order.status) && (
                  <Button
                    size="sm"
                    variant="danger"
                    loading={cancellingOrder === order.id}
                    disabled={cancellingOrder === order.id}
                    onClick={() => handleCancelOrder(order.id)}
                  >
                    취소
                  </Button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="px-6 py-4 border-t border-gray-200">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-500">
              페이지 {currentPage + 1} / {totalPages}
            </div>

            <div className="flex items-center space-x-2">
              <Button
                size="sm"
                variant="outline"
                disabled={currentPage === 0 || loading}
                onClick={() => handlePageChange(currentPage - 1)}
              >
                이전
              </Button>

              <div className="flex items-center space-x-1">
                {[...Array(Math.min(5, totalPages))].map((_, i) => {
                  const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
                  return (
                    <Button
                      key={pageNum}
                      size="sm"
                      variant={pageNum === currentPage ? 'primary' : 'outline'}
                      disabled={loading}
                      onClick={() => handlePageChange(pageNum)}
                    >
                      {pageNum + 1}
                    </Button>
                  );
                })}
              </div>

              <Button
                size="sm"
                variant="outline"
                disabled={currentPage === totalPages - 1 || loading}
                onClick={() => handlePageChange(currentPage + 1)}
              >
                다음
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};