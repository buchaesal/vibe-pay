-- 주문 정보 테이블
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    order_number VARCHAR(100) UNIQUE NOT NULL,

    -- 상품 정보
    product_name VARCHAR(255) NOT NULL,
    product_price BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,

    -- 금액 정보
    total_amount BIGINT NOT NULL,
    point_amount BIGINT DEFAULT 0,
    card_amount BIGINT DEFAULT 0,

    -- 주문 상태
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: 결제 대기
    -- PAID: 결제 완료
    -- CANCELLED: 취소됨

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_orders_member_id ON orders(member_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- 코멘트
COMMENT ON TABLE orders IS '주문 정보 테이블';
COMMENT ON COLUMN orders.order_number IS '주문번호 (ex: ORD20250116123456789)';
COMMENT ON COLUMN orders.total_amount IS '총 주문금액';
COMMENT ON COLUMN orders.point_amount IS '적립금 사용 금액';
COMMENT ON COLUMN orders.card_amount IS '카드 결제 금액';
COMMENT ON COLUMN orders.status IS '주문 상태 (PENDING/PAID/CANCELLED)';
