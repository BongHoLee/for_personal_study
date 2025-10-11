# CP1: Account 도메인 기본 모델링

**시작일**: 2025-10-03
**상태**: 진행 중

---

## 학습 목표

- [X]  행위 기반 설계 vs 데이터 중심 설계 이해
- [ ]  Aggregate 패턴 적용 (Account를 Aggregate Root로)
- [ ]  도메인 서비스의 역할과 사용 시점 이해
- [ ]  Rich Domain Model 구현 (비즈니스 로직을 도메인에)
- [ ]  Value Object 활용 (Money, AccountStatus 등)

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

- [X]  설계 고민 및 방향 결정
- [ ]  Account Entity 구현
- [ ]  Money VO 구현
- [ ]  TransferService 구현
- [ ]  Activity 구현
- [ ]  단위 테스트 작성
- [ ]  도메인 규칙 테스트 
- [ ]  첫 리뷰 요청
- [ ]  피드백 반영
- [ ]  최종 리뷰 통과

---

## 완료 기준

1. [ ]  @DOMAIN_MODEL.md의 모든 비즈니스 규칙 구현 및 테스트 통과
2. [ ]  Domain Layer에 인프라 의존성 없음 (Spring, JPA 금지)
3. [ ]  아키텍처 레이어 경계 준수
4. [ ]  피드백 우선순위 1 모두 반영
5. [ ]  학습 로그 작성 완료 (docs/learning/log.md)

---

## 💭 개발 일지 (학습자 자유 기록)

## 2025-10-06
### 도메인 오브젝트의 식별자 부여

음... Account Entity와 같은 도메인 오브젝트 개발 시작 시에는 최대한 JPA 의존성을 배제하고 시작하려고 하는데..
이런 경우 entity 식별자를 위한 구현, 테스트는 어떻게 해야할까? 그냥 믿고 가야하나?
식별자를 부여하는건 Persistence의 영역이라고 보고, 이런 테스트도 Persistence에 대한 테스트로 해야하나? 통합 테스트처럼?

### 계좌번호 Natural Id

일반적으로 엔티티 id는 auto increment와 같은 대리키로 두는게 권장되는 방식이니 그렇게 하도록 하고
계좌번호인 `accountNumber`도 Natural ID로써 두는게 좋을 것 같다.

모든 Account에 대해서 고유해야 하니까 동등성에 대한 로직을 accountNumber로 해야할까 싶기도 한데..
이미 accountId로 식별성을 정의하는걸로 했으니, accountNumber는 UUID로 정의하고 영속성 수준에서는 unique key 정도로 갈음하자.

### Account 도메인 모델 진화와 반영

생각해보니 Account 도메인을 좀 더 풍부하게 진화시켜보는게 좋겠다.
'개설 신청', '개설 완료', '개설 거절', '정지', '해지'와 같은 상태를 갖는다면 더 풍부하게 모델링을 해볼 수 있을 것 같다.

음... 계좌 상태와 관련된 도메인 오브젝트를 별도로 추출/개념화 해보자.

- 유저가 최초 계좌 개설을 신청한다. -> 개설 신청 상태, 아직 계좌번호는 발급되지 않은 상태
- 개설 신청 상태에서는 이후 운영자가 심사를 거쳐 상태를 업데이트한다. -> 계좌 개설, 개설 거절
  - '심사'와 관련된 컨텍스트는 별도의 도메인(애그리거트? 바운디드컨텍스트?)로 분리하도록 하고, 여기서는 우선 Account에 대해서만 고려하자.
  - 그런데 Account의 상태를 갱신해주는 행위를 '심사 승인', '심사 거절'이라는 표현으로 하는게 맞을까? '심사'라는 컨텍스트가 Account에 스며드는게 개념적 결합도를 높이는건 아닐까?
  - 음... 이 경계를 어느정도 수준으로 끊어내야 할까? 우선은 '심사 승인/거절'과 같은 표현이 당장 더 직관적일 것 같긴 한데... 일단은 당장 '심사' 라는 개념에 결합되어도 딱히 문제가 될 소지는 없어보인다. 이후 문제가 되면 리팩토링 하도록 하고, 일단은 직관적으로 진행해보자.
    - 혹시 이런 상황에서는 일반적으로 어떤 선택을 하는게 권장되는지 모르겠다.
- 계좌 개설 후 이상 거래 등의 탐지가 발생했을 때는 운영자가 계좌를 정지한다. -> 게좌 정지
- 유저의 신청 등으로 계좌를 해지할 수 있다. -> 계좌 해지

위의 자연어로 나열된 내용을 도메인 모델 문서에 업데이트 했다.

음.. 각 상태 전이에 대한 기술적인 부분은 도메인 이벤트, 메시지 큐 등으로 이후에 구체화 하도록 하자.

