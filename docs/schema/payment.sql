-- 결제 정보 테이블
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    member_id BIGINT NOT NULL REFERENCES member(id),

    -- PG사 정보
    pg_provider VARCHAR(50) NOT NULL,
    -- INICIS: 이니시스
    -- TOSS: 토스페이먼츠

    -- 결제 정보
    payment_method VARCHAR(50) NOT NULL,
    -- CARD: 카드
    -- POINT: 적립금
    -- MIXED: 복합결제

    amount BIGINT NOT NULL,

    -- PG 거래 정보
    pg_transaction_id VARCHAR(255),
    pg_auth_code VARCHAR(100),
    pg_response_code VARCHAR(50),
    pg_response_message VARCHAR(500),

    -- 결제 상태
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: 승인 대기
    -- APPROVED: 승인 완료
    -- CANCELLED: 취소됨
    -- FAILED: 실패

    -- 취소 정보
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_payment_order_id ON payment(order_id);
CREATE INDEX idx_payment_member_id ON payment(member_id);
CREATE INDEX idx_payment_pg_transaction_id ON payment(pg_transaction_id);
CREATE INDEX idx_payment_status ON payment(status);
CREATE INDEX idx_payment_created_at ON payment(created_at DESC);

-- 코멘트
COMMENT ON TABLE payment IS '결제 정보 테이블';
COMMENT ON COLUMN payment.pg_provider IS 'PG사 (INICIS/TOSS)';
COMMENT ON COLUMN payment.payment_method IS '결제 수단 (CARD/POINT/MIXED)';
COMMENT ON COLUMN payment.amount IS '결제 금액';
COMMENT ON COLUMN payment.pg_transaction_id IS 'PG사 거래 ID';
COMMENT ON COLUMN payment.pg_auth_code IS 'PG사 승인 코드';
COMMENT ON COLUMN payment.status IS '결제 상태 (PENDING/APPROVED/CANCELLED/FAILED)';