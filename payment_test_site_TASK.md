# TASK (Task Breakdown Document)
# 결제 테스트 사이트 개발

## 프로젝트 개요
- **개발 도구**: Claude Code
- **개발 방식**: 에이전트 기반 개발 (api-agent, fo-agent, sql-agent, architect-agent)
- **개발 기간**: Iteration 1-5 (단계별 진행)

---

## Iteration 1: 프로젝트 초기 설정 및 기본 구조

### 목표
개발 환경 설정 및 기본 패키지/디렉토리 구조 생성

### Task 1-1: Backend 패키지 구조 생성
**담당 에이전트**: api-agent

- [x] src/main/java/com/vibepay/ 하위 패키지 생성
  - controller
  - service
  - repository
  - domain
  - dto
  - config
  - util
- [x] src/main/resources/mapper 디렉토리 생성

### Task 1-2: Backend 프로젝트 설정
**담당 에이전트**: api-agent

- [x] pom.xml 의존성 설정
  - Spring Web
  - Spring Session
  - MyBatis
  - PostgreSQL Driver
  - Lombok
  - Validation
- [x] application.yml 기본 설정
  - 환경별 프로파일 (local, dev, stg, cbo, prod)
  - 데이터베이스 연결 설정
  - CORS 설정

### Task 1-3: Frontend 디렉토리 구조 생성
**담당 에이전트**: fo-agent

- [x] src 하위 디렉토리 생성
  - app (이미 존재할 수 있음)
  - components
  - lib
  - hooks
  - types
- [x] public 디렉토리 확인

### Task 1-4: Frontend 프로젝트 설정
**담당 에이전트**: fo-agent

- [x] package.json 의존성 확인 및 추가 설치
  - React 19
  - TypeScript
  - Tailwind CSS
- [x] Tailwind CSS 설정 (tailwind.config.js)
- [x] API 클라이언트 유틸리티 기본 구조 (lib/api.ts)
- [x] 환경 변수 설정 (.env.local)

**완료 기준**: 프로젝트가 정상적으로 빌드되고 실행됨

---

## Iteration 2: 데이터베이스 및 회원 인증 기능

### 목표
회원가입/로그인 기능 구현

**전제 조건**: 
- 회원 정보 테이블 (member) 생성 완료
- 적립금 정보 테이블 (point) 생성 완료

### Task 2-1: 회원 인증 API 개발
**담당 에이전트**: api-agent

- [x] Member Domain 클래스 생성
- [x] MemberDto (요청/응답) 생성
- [x] MemberRepository (MyBatis Mapper) 생성
- [x] MemberService 생성
  - 회원가입 로직 (비밀번호 암호화)
  - 로그인 로직 (세션 생성)
  - 로그아웃 로직 (세션 삭제)
- [x] AuthController 생성
  - POST /api/auth/signup
  - POST /api/auth/login
  - POST /api/auth/logout
  - GET /api/auth/check
- [x] 인증 체크 인터셉터 생성
- [x] 서비스 레벨 테스트 코드 작성

### Task 2-2: 회원 인증 화면 개발
**담당 에이전트**: fo-agent

- [x] 로그인 페이지 (/login) 생성
  - LoginForm 컴포넌트
  - API 연동 (로그인)
- [x] 회원가입 페이지 (/signup) 생성
  - SignupForm 컴포넌트
  - API 연동 (회원가입)
- [x] 인증 상태 관리 (Context 또는 상태관리)
- [x] 인증 체크 미들웨어 생성
- [x] 로그아웃 기능 구현

**완료 기준**: 회원가입, 로그인, 로그아웃이 정상 동작함

---

## Iteration 3: 주문서 및 적립금 기능

### 목표
주문서 화면 및 적립금 관련 기능 구현

**전제 조건**: 
- 주문 정보 테이블 (orders) 생성 완료

### Task 3-1: 적립금 API 개발
**담당 에이전트**: api-agent

- [x] Point Domain 클래스 생성
- [x] PointDto (요청/응답) 생성
- [x] PointRepository (MyBatis Mapper) 생성
- [x] PointService 생성
  - 잔액 조회 로직
  - 적립금 차감 로직
  - 적립금 복구 로직
- [x] PointController 생성
  - GET /api/point/balance
  - POST /api/point/deduct
  - POST /api/point/restore
- [x] 서비스 레벨 테스트 코드 작성

### Task 3-2: 주문 API 개발
**담당 에이전트**: api-agent

- [x] Order Domain 클래스 생성
- [x] OrderDto (요청/응답) 생성
- [x] OrderRepository (MyBatis Mapper) 생성
- [x] OrderService 생성
  - 주문 생성 로직
  - 주문 목록 조회 로직
  - 주문 상세 조회 로직
  - 주문 취소 로직
- [x] OrderController 생성
  - POST /api/order/create
  - GET /api/order/list
  - GET /api/order/{orderId}
  - POST /api/order/{orderId}/cancel
- [x] 서비스 레벨 테스트 코드 작성

### Task 3-3: 주문서 화면 개발
**담당 에이전트**: fo-agent

- [x] 주문서 페이지 (/order) 생성
  - OrderForm 컴포넌트
  - 상품 정보 입력 (상품명, 상품금액, 수량)
  - 총 주문금액 자동 계산
  - 약관 동의 체크박스
- [x] 적립금 표시 컴포넌트
  - 적립금 잔액 조회 API 연동
  - 사용할 적립금 입력

### Task 3-4: 주문 목록 화면 개발
**담당 에이전트**: fo-agent

- [x] 주문 목록 페이지 (/order/list) 생성
  - OrderList 컴포넌트
  - 주문 목록 조회 API 연동
  - 주문 상세 정보 표시

**완료 기준**: 주문서 작성, 적립금 조회가 정상 동작함

---

## Iteration 4: 결제 기능 구현 (이니시스)

### 목표
이니시스 PG사를 통한 카드 결제 기능 구현

**전제 조건**:
- 결제 정보 테이블 (payment) 생성 완료

### 카드 결제 프로세스
```
1. 주문서에서 "결제하기" 클릭
   ↓
2. PG 인증창 호출 (이니시스/토스 팝업)
   ↓
3. 사용자가 카드 정보 입력 및 인증 완료
   ↓
4. "결제 중..." 화면으로 이동 (로딩 상태)
   ↓
5. 인증 응답값 수신
   ↓
6. 주문하기 API 호출 (POST /api/order/create)
   - 내부적으로 결제 승인 API 호출 (POST /api/payment/approve)
   ↓
7-1. 성공: 주문 완료 화면 (/order/complete)
7-2. 실패: 주문 실패 화면 (에러 메시지 표시)
```

**중요**:
- 주문 생성과 결제 승인을 분리하지 않고, 주문 API 내부에서 결제 승인을 처리
- PG 인증 후 바로 승인 API를 호출하는 것이 아니라, 주문 API를 통해 승인 처리
- 결제 중 화면에서 사용자에게 로딩 상태를 명확히 표시

### Task 4-1: PG 인증값 조회 API 개발
**담당 에이전트**: api-agent

**참고 문서**:
- 이니시스 PC 표준결제: https://manual.inicis.com/pay/stdpay_pc.html

**이니시스 환경 변수**:
```
mid=INIpayTest
signKey=SU5JTElURV9UUklQTEVERVNfS0VZU1RS
iniApiKey=ItEQKi3rY7uvDS8l
hashKey=3CB8183A4BE283555ACC8363C0360223
```

- [x] PgAuthParamsDto 생성
- [x] PgService 생성
  - 이니시스 인증에 필요한 값 생성 로직
    - mid, timestamp, signKey 기반 해시 생성
    - oid (주문번호) 생성
- [x] PgController 생성
  - GET /api/pg/auth-params
- [x] 환경 변수 설정 (이니시스 mid, signKey, iniApiKey, hashKey)

### Task 4-2: 결제 승인/취소 API 개발 (이니시스)
**담당 에이전트**: api-agent

**참고 문서**:
- 이니시스 PC 표준결제: https://manual.inicis.com/pay/stdpay_pc.html

- [x] Payment Domain 클래스 생성
- [x] PaymentDto (요청/응답) 생성
- [x] PaymentRepository (MyBatis Mapper) 생성
- [x] 결제 전략 인터페이스 설계
  - PaymentStrategy 인터페이스
  - CardPaymentStrategy 추상 클래스
  - InicisPaymentStrategy 구현 클래스
- [x] PaymentService 생성
  - 결제 승인 로직
    - 승인 요청 (PG사 API 호출)
    - 승인 성공 시 결제 정보 저장
    - 승인 실패 (응답값 상이) 시 망취소 자동 처리
  - 결제 취소 로직
    - 취소 요청 (PG사 API 호출)
    - 결제 정보 업데이트
- [x] PaymentController 생성
  - POST /api/payment/approve
  - POST /api/payment/cancel
- [x] 이니시스 PG 연동 유틸리티 클래스 생성
- [x] 서비스 레벨 테스트 코드 작성

### Task 4-3: 결제 화면 개발 (이니시스)
**담당 에이전트**: fo-agent

**참고 문서**:
- 이니시스 PC 표준결제: https://manual.inicis.com/pay/stdpay_pc.html

- [x] PaymentSelector 컴포넌트 생성
  - 결제수단 선택 (카드, 적립금, 복합)
  - 결제 금액 입력
  - 금액 검증 (100원 초과, 총액 일치)
- [x] 이니시스 결제 처리 로직 구현
  - PG 인증값 조회 API 호출
  - 이니시스 JS 동적 다운로드
  - 결제 인증창 팝업 호출
  - 인증 응답 수신
  - 승인 요청 API 호출
- [x] 주문완료 페이지 (/order/complete) 생성
  - 결제 완료 정보 표시
  - 승인취소 버튼

### Task 4-4: 복합 결제 및 취소 기능 구현
**담당 에이전트**: fo-agent

- [x] 복합 결제 로직 구현
  - 적립금 차감 API 호출
  - 카드 결제 진행
  - 실패 시 적립금 복구
- [x] 주문 취소 기능 구현
  - 취소 버튼 클릭 시 취소 API 호출
  - 취소 완료 처리

**완료 기준**: 이니시스를 통한 카드 결제, 적립금 결제, 복합 결제, 취소가 정상 동작함

---

## Iteration 5: 토스페이먼츠 연동 및 PG사 가중치

### 목표
토스페이먼츠 PG사 추가 및 PG사 선택 가중치 구현

### Task 5-1: 토스페이먼츠 승인/취소 API 개발
**담당 에이전트**: api-agent

**참고 문서**: 
- 토스페이먼츠 결제 승인: https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
- 토스페이먼츠 결제 취소: https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%B7%A8%EC%86%8C

**토스페이먼츠 환경 변수**:
```
clientKey=test_ck_DpexMgkW36PL5OnYYn7drGbR5ozO  # PG 인증용
secretKey=test_sk_P9BRQmyarY56W4lPgbnNrJ07KzLN  # 승인용
```

- [ ] TossPaymentStrategy 구현 클래스 생성
- [ ] PgService에 토스 인증값 생성 로직 추가
  - clientKey, secretKey 기반 인증값 생성
- [ ] PaymentService에 토스 승인/취소 로직 추가
- [ ] 토스 PG 연동 유틸리티 클래스 생성
- [ ] 환경 변수 설정 (토스 clientKey, secretKey)
- [ ] 서비스 레벨 테스트 코드 작성

### Task 5-2: PG사 선택 로직 구현
**담당 에이전트**: api-agent

- [ ] PG사 가중치 설정 (이니시스 50%, 토스 50%)
- [ ] PG사 선택 알고리즘 구현
  - 랜덤 또는 라운드로빈 방식
- [ ] PG 인증값 조회 API에 선택된 PG사 정보 포함

### Task 5-3: 토스페이먼츠 결제 화면 개발
**담당 에이전트**: fo-agent

**참고 문서**: 
- 토스페이먼츠 결제 승인: https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
- 토스페이먼츠 결제 취소: https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%B7%A8%EC%86%8C

- [ ] 토스페이먼츠 결제 처리 로직 구현
  - 토스 JS 동적 다운로드
  - 토스 결제 인증창 팝업 호출
  - 인증 응답 수신
  - 승인 요청 API 호출
- [ ] PG사별 결제 플로우 분기 처리
- [ ] 토스 결제 테스트

### Task 5-4: 통합 테스트 및 버그 수정
**담당 에이전트**: architect-agent

- [ ] 전체 결제 플로우 통합 테스트
  - 이니시스 카드 결제
  - 토스 카드 결제
  - 적립금 결제
  - 복합 결제
  - 주문 취소
- [ ] PG사 가중치 동작 확인
- [ ] 에러 케이스 테스트
  - 100원 이하 결제 차단
  - 적립금 부족
  - 결제 금액 불일치
  - 승인 실패 시 망취소
- [ ] 버그 수정 및 리팩토링

### Task 5-5: 문서화 및 배포 준비
**담당 에이전트**: architect-agent

- [ ] API 문서 작성
- [ ] 환경별 설정 가이드 작성
- [ ] 배포 스크립트 작성
- [ ] README.md 작성

**완료 기준**: 모든 결제 시나리오가 정상 동작하고 문서화 완료

---

## 체크리스트

### Iteration 1
- [x] Backend 패키지 구조 생성 완료
- [x] Backend 프로젝트 설정 완료
- [x] Frontend 디렉토리 구조 생성 완료
- [x] Frontend 프로젝트 설정 완료

### Iteration 2
- [x] 회원 인증 API 개발 완료
- [x] 회원 인증 화면 개발 완료

### Iteration 3
- [x] 적립금/주문 API 개발 완료
- [x] 주문서/주문목록 화면 개발 완료

### Iteration 4
- [x] 이니시스 PG 인증값 조회 API 개발 완료
- [x] 이니시스 결제 API 개발 완료
- [x] 이니시스 결제 화면 개발 완료
- [x] 복합 결제 및 취소 기능 완료

### Iteration 5
- [ ] 토스페이먼츠 API 개발 완료
- [ ] PG사 선택 로직 완료
- [ ] 토스페이먼츠 화면 개발 완료
- [ ] 통합 테스트 완료
- [ ] 문서화 완료

---

## 우선순위

### P0 (필수)
- Iteration 1: 프로젝트 초기 설정
- Iteration 2: 회원 인증 기능
- Iteration 3: 주문서 및 적립금 기능
- Iteration 4: 이니시스 결제 기능

### P1 (중요)
- Iteration 5: 토스페이먼츠 연동
- Iteration 5: PG사 가중치 구현

### P2 (선택)
- 서비스 레벨 테스트 코드 확대
- 에러 처리 고도화
- 로깅 및 모니터링

---

## 개발 시 주의사항

1. **에이전트 기반 개발 준수**
   - 모든 코드는 해당 영역의 에이전트를 통해 작성
   - 컨벤션 문서를 철저히 준수

2. **Iteration 순서 준수**
   - 이전 Iteration이 완료되어야 다음 Iteration 진행
   - 각 Iteration의 완료 기준을 충족해야 함

3. **테스트 코드 작성**
   - 서비스 레이어의 주요 비즈니스 로직은 테스트 코드 필수

4. **환경 변수 관리**
   - 민감 정보는 반드시 환경 변수로 관리
   - 환경별 설정 파일 분리

5. **에러 처리**
   - 모든 API는 적절한 에러 처리 및 응답 필요
   - 사용자 친화적인 에러 메시지 제공

---

**이 문서는 Claude Code를 활용한 단계별 개발을 위한 작업 분해 문서입니다.**
