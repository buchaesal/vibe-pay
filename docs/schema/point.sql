-- 적립금 정보 테이블
CREATE TABLE point (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    balance BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 적립금 이력 테이블
CREATE TABLE point_history (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    point_id BIGINT NOT NULL REFERENCES point(id),

    -- 변동 정보
    transaction_type VARCHAR(50) NOT NULL,
    -- EARN: 적립
    -- USE: 사용
    -- RESTORE: 복구

    amount BIGINT NOT NULL,
    balance_before BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,

    -- 관련 정보
    order_number VARCHAR(100),
    description VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_point_member_id ON point(member_id);
CREATE INDEX idx_point_history_member_id ON point_history(member_id);
CREATE INDEX idx_point_history_point_id ON point_history(point_id);
CREATE INDEX idx_point_history_order_number ON point_history(order_number);
CREATE INDEX idx_point_history_created_at ON point_history(created_at DESC);

-- 유니크 제약조건 (회원당 하나의 적립금 계정)
ALTER TABLE point ADD CONSTRAINT uk_point_member_id UNIQUE (member_id);

-- 코멘트
COMMENT ON TABLE point IS '적립금 정보 테이블';
COMMENT ON COLUMN point.balance IS '적립금 잔액';
COMMENT ON TABLE point_history IS '적립금 변동 이력 테이블';
COMMENT ON COLUMN point_history.transaction_type IS '변동 유형 (EARN/USE/RESTORE)';
COMMENT ON COLUMN point_history.amount IS '변동 금액';
COMMENT ON COLUMN point_history.balance_before IS '변동 전 잔액';
COMMENT ON COLUMN point_history.balance_after IS '변동 후 잔액';