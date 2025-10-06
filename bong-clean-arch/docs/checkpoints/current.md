# CP1: Account 도메인 기본 모델링

**시작일**: 2025-10-03
**상태**: 진행 중

---

## 학습 목표

- [x] 행위 기반 설계 vs 데이터 중심 설계 이해
- [ ] Aggregate 패턴 적용 (Account를 Aggregate Root로)
- [ ] 도메인 서비스의 역할과 사용 시점 이해
- [ ] Rich Domain Model 구현 (비즈니스 로직을 도메인에)
- [ ] Value Object 활용 (Money, AccountStatus 등)

---

## 구현 범위

### Domain Layer
**Entity**:
- `Account`: Aggregate Root
  - 상태: Active/Suspended
  - 행위: transferTo(), withdraw(), debit(), credit()
  - 잔고 관리 책임

- `Activity`: 거래 기록 Entity/VO
  - 거래 유형, 시각, 금액, 거래 후 잔고

**Value Object**:
- `Money`: 금액 표현
- `AccountId`: 계좌 식별자
- `AccountStatus`: 계좌 상태
- `ActivityType`: 거래 유형 (TRANSFER_OUT, TRANSFER_IN, WITHDRAWAL)

**도메인 서비스**:
- `TransferService`: 두 Account 간 송금 조율
  - 수수료 계산 (확장 고려)
  - 한도 체크 (확장 고려)
  - Account.transferTo() 호출

### Application Layer
(나중 단계)

### Adapter Layer
(나중 단계)

### Test
- **단위 테스트**: Account, Money, TransferService 로직
- **도메인 규칙 테스트**: @DOMAIN_MODEL.md의 규칙 R1-R14 검증

---

## 설계 고민 포인트

### 1. 거래 행위를 어떻게 모델링할까?

**질문**:
- Account가 직접 송금 책임? (Account.transferTo)
- 별도 도메인 서비스? (TransferService.transfer)
- Transaction Entity가 행위 수행? (Transaction.transfer)

**옵션들**:
- **Option A**: Account.transferTo(target, money) - 직관적이지만 Aggregate 경계 애매
- **Option B**: TransferService.transfer() + Account.debit/credit() - 책임 분리 명확
- **Option C**: Account.transferTo() + TransferService (하이브리드)

**선택한 방향**: **Option C (하이브리드)**

```kotlin
// TransferService에서 부가 기능 처리
class TransferService {
    fun transfer(source: Account, target: Account, money: Money) {
        val fee = feeCalculator.calculate(money)  // 수수료 (확장)
        source.transferTo(target, money + fee)
    }
}

// Account가 송금 행위 수행
class Account {
    fun transferTo(target: Account, money: Money) {
        this.debit(money)
        target.credit(money)
    }
}
```

**이유**:
- ✅ 직관성: "계좌가 송금한다"는 도메인 언어
- ✅ 책임 분리: 수수료 등 부가 기능은 서비스에서
- ✅ 확장성: TransferService에서 추가 로직 처리
- ⚠️ 트레이드오프: Aggregate 간 직접 결합 (트랜잭션은 Application Layer 관리)

### 2. Aggregate 경계

**결정**: 각 Account가 독립적인 Aggregate Root
- Account + Activity들 = 하나의 Aggregate
- 송금 시 두 Aggregate를 한 트랜잭션에 묶음 (Application Layer에서)

### 3. Activity 모델링

**결정**: Entity로 구현 (식별자 필요)
- 거래 이력 추적 필요
- 나중에 조회/페이징 필요

---

## 진행 상황

- [x] 설계 고민 및 방향 결정
- [ ] Account Entity 구현
- [ ] Money VO 구현
- [ ] TransferService 구현
- [ ] Activity 구현
- [ ] 단위 테스트 작성
- [ ] 도메인 규칙 테스트 (R1-R14)
- [ ] 첫 리뷰 요청
- [ ] 피드백 반영
- [ ] 최종 리뷰 통과

---

## 완료 기준

1. [ ] @DOMAIN_MODEL.md의 모든 비즈니스 규칙 (R1-R14) 구현 및 테스트 통과
2. [ ] Domain Layer에 인프라 의존성 없음 (Spring, JPA 금지)
3. [ ] 아키텍처 레이어 경계 준수
4. [ ] 피드백 우선순위 1 모두 반영
5. [ ] 학습 로그 작성 완료 (docs/learning/log.md)

---

## 참고 링크
- **도메인 명세**: @DOMAIN_MODEL.md
- **프로젝트 가이드**: @CLAUDE.md
- **학습 사이클**: @docs/cycle.md
- **설계 논쟁 히스토리**: @docs/checkpoints/cp1_history.md ⭐

---

## 📍 다음 세션 시작 가이드

### 세션 재개 시 프롬프트 예시:
```
@CLAUDE.md
@docs/checkpoints/current.md
@docs/checkpoints/cp1_history.md

CP1 Account 도메인 모델링 중.
PLAN 단계 완료 → IMPLEMENT 단계 시작.

설계 결정:
- Option C (하이브리드): Account.transferTo() + TransferService
- 상세 논의는 cp1_history.md 참조

질문: [구현 관련 질문]
```

### 다음 단계 (IMPLEMENT)
**구현 순서 권장**:
1. Money VO (가장 기본)
2. AccountId, AccountStatus VO
3. Account Entity (transferTo, debit, credit)
4. Activity Entity
5. TransferService
6. 테스트 (도메인 규칙 R1-R14)

**주의사항**:
- Domain Layer에 Spring/JPA 의존성 넣지 않기
- Kotlin data class + val로 불변성 유지
- 테스트는 Kotest 우선

**막히면**:
- 구현 중 설계 고민 생기면 언제든 PLAN으로 돌아가기
- "/review 부분 구현 완료" 로 중간 피드백 받기 가능
