-- 회원 정보 테이블
CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_member_email ON member(email);
CREATE INDEX idx_member_created_at ON member(created_at DESC);

-- 코멘트
COMMENT ON TABLE member IS '회원 정보 테이블';
COMMENT ON COLUMN member.email IS '이메일 (로그인 ID)';
COMMENT ON COLUMN member.password IS '암호화된 비밀번호';
COMMENT ON COLUMN member.name IS '회원 이름';