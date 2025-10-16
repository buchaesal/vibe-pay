-- 결제 테이블 생성 스크립트
-- PostgreSQL 기준

CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- CARD, POINT, MIXED
    pg_type VARCHAR(50), -- INICIS, TOSS

    -- 결제 금액 정보
    total_amount BIGINT NOT NULL,
    card_amount BIGINT DEFAULT 0,
    point_amount BIGINT DEFAULT 0,

    -- PG 관련 정보
    pg_tid VARCHAR(100), -- PG사 거래번호
    auth_code VARCHAR(100), -- 승인번호
    card_number VARCHAR(100), -- 카드번호 (마스킹)
    card_name VARCHAR(100), -- 카드사명

    -- 상태 정보
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: 승인 대기
    -- APPROVED: 승인 완료
    -- CANCELLED: 취소됨
    -- FAILED: 승인 실패

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 외래키 제약조건
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payment(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_pg_tid ON payment(pg_tid);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment(status);
CREATE INDEX IF NOT EXISTS idx_payment_created_at ON payment(created_at);

-- 복합 인덱스 (회원별 결제 내역 조회 최적화)
CREATE INDEX IF NOT EXISTS idx_payment_member_created
ON payment(order_id, created_at DESC)
WHERE status IN ('APPROVED', 'CANCELLED');

-- 코멘트 추가
COMMENT ON TABLE payment IS '결제 정보 테이블';
COMMENT ON COLUMN payment.id IS '결제 ID';
COMMENT ON COLUMN payment.order_id IS '주문 ID';
COMMENT ON COLUMN payment.payment_method IS '결제 수단 (CARD, POINT, MIXED)';
COMMENT ON COLUMN payment.pg_type IS 'PG사 타입 (INICIS, TOSS)';
COMMENT ON COLUMN payment.total_amount IS '총 결제 금액';
COMMENT ON COLUMN payment.card_amount IS '카드 결제 금액';
COMMENT ON COLUMN payment.point_amount IS '포인트 사용 금액';
COMMENT ON COLUMN payment.pg_tid IS 'PG사 거래번호';
COMMENT ON COLUMN payment.auth_code IS '승인번호';
COMMENT ON COLUMN payment.card_number IS '카드번호 (마스킹)';
COMMENT ON COLUMN payment.card_name IS '카드사명';
COMMENT ON COLUMN payment.status IS '결제 상태 (PENDING, APPROVED, CANCELLED, FAILED)';
COMMENT ON COLUMN payment.created_at IS '생성일시';
COMMENT ON COLUMN payment.updated_at IS '수정일시';