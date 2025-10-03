# 학습 사이클 가이드

> 실제 학습 루프에서 Claude와 어떻게 상호작용하는지 구체적인 예시

---

## 전체 흐름

```
📋 PLAN → 💻 IMPLEMENT → 🔍 REVIEW → 💬 REFLECT → 📝 LEARNING → ➡️ NEXT
              ↑                          ↓
              └──────── 피드백 루프 ──────┘
```

---

## 1️⃣ PLAN - 방향 설정

### 시작
```bash
$ claude
```

### 프롬프팅 예시
```
@CLAUDE.md

CP2 시작: Order 도메인 모델링

설계 고민:
1. Order와 OrderItem의 Aggregate 경계를 어떻게 설정할까?
2. 주문 상태 변경 로직을 어디에 둘까? (Entity vs Domain Service)
3. 가격 계산 로직은?

각각 2-3개 옵션 + 트레이드오프 설명해줘.
```

### Claude 응답
```markdown
## 1. Aggregate 경계

**Option A**: Order가 Aggregate Root, OrderItem은 Entity
- 장점: 일관성 보장 용이, 트랜잭션 경계 명확
- 단점: Order가 커질 수 있음

**Option B**: Order와 OrderItem 각각 독립 Aggregate
- 장점: 확장성, 동시성 제어 유리
- 단점: 일관성 유지 복잡

**추천**: Option A (주문과 항목은 항상 함께 변경되므로)

## 2. 상태 변경 로직
...
```

### 결정 후
```
Option A로 간다.

/next-checkpoint CP2
```

→ `docs/checkpoints/current.md` 자동 생성/업데이트

---

## 2️⃣ IMPLEMENT - 직접 구현

Claude와 대화 없이 직접 구현:

```
(학습자 작업)
- Order Entity 작성
- OrderItem Entity 작성
- 가격 계산 로직 구현
- 테스트 코드 작성
- 테스트 통과 확인
```

---

## 3️⃣ REVIEW - 피드백 요청

### 프롬프팅
```bash
$ claude
```

```
/review Order 도메인 구현 완료

특히 확인해줘:
1. Aggregate 경계가 적절한가?
2. 가격 계산을 Order.calculateTotal()에 뒀는데 괜찮나?
3. OrderStatus를 enum으로 했는데 나중에 확장성은?
```

### Claude 동작
1. 코드 분석 (git diff, 파일 읽기)
2. 피드백 생성
3. `docs/feedback/2025-10-03-order-domain.md` 자동 저장

### Claude 응답 예시
```markdown
# Order 도메인 리뷰

## ✅ 잘한 점
1. **Aggregate 경계**: Order-OrderItem 관계가 명확
2. **불변성**: data class + val 활용 우수

## 🤔 개선 검토 사항

### 🔥 우선순위 1: 가격 계산 로직 분리
**문제**:
- Order.calculateTotal()이 할인/세금 로직까지 포함
- 나중에 복잡한 프로모션 추가 시 Order가 비대해짐

**Option A**: PriceCalculator 도메인 서비스 분리
- 장점: SRP 준수, 테스트 용이
- 단점: 클래스 하나 추가

**Option B**: 현재 유지
- 장점: 단순
- 단점: 확장성 떨어짐

**추천**: Option A

### 🟡 우선순위 2: OrderStatus enum
현재는 괜찮음. 나중에 상태 전이 규칙 복잡해지면 State Pattern 고려

## 💡 학습 포인트
- Aggregate 내부 일관성 vs 서비스 분리 트레이드오프
```

---

## 4️⃣ REFLECT - 토론 및 개선

### 피드백에 대한 질문
```
@docs/feedback/2025-10-03-order-domain.md

질문:
- PriceCalculator를 도메인 서비스로 빼면, Order는 그걸 어떻게 호출해?
- Application Layer에서 주입하는 방식?
```

### Claude 응답
```
좋은 질문!

**Option A**: Application Layer에서 조합
```kotlin
// Application Service
fun createOrder(request: CreateOrderRequest): Order {
    val order = Order.create(...)
    val totalPrice = priceCalculator.calculate(order)
    return order.withTotalPrice(totalPrice)
}
```

**Option B**: Factory에서 처리
...

추천: Option A
```

### 반영 결정
```
알겠어. Option A로 리팩토링 한다.

(30분 작업)

/review 피드백 반영 완료
```

### Claude 재검토
```markdown
## 재리뷰

✅ PriceCalculator 분리 완료
✅ Application Layer에서 잘 조합됨

추가 피드백:
- (추가 개선사항 있으면 제시)

완성도 충분! 다음 단계로 진행해도 좋아.
```

### 피드백 루프
필요시 REVIEW ↔ REFLECT 반복:

```
구현
  ↓
/review → 피드백
  ↓
질문/토론
  ↓
수정
  ↓
/review → 재검토
  ↓
✅ 승인
```

---

## 5️⃣ LEARNING - 학습 기록

### 학습자가 직접 작성
`docs/learning/log.md` 열고:

```markdown
## 2025-10-03 - CP2: Order 도메인

### 배운 개념
- **도메인 서비스**: Entity/VO에 속하지 않는 도메인 로직
  - 여러 Entity를 조율하거나, 상태 없는 계산 담당

### 의사결정
**문제**: 가격 계산 위치

**선택**: PriceCalculator 도메인 서비스 분리
- Order는 간결하게 유지
- 복잡한 할인/프로모션 로직 분리

**트레이드오프**: 클래스 추가 vs SRP 준수 → SRP 선택

### 실수
- 처음엔 Order에 모든 로직을 넣으려 했음
- 배움: 도메인 서비스가 언제 필요한지 이해
```

### 필요 시 Claude에게 도움 요청
```
방금 작성한 학습 로그 @docs/learning/log.md 보고
빠뜨린 중요 개념 있으면 알려줘
```

---

## 6️⃣ NEXT - 다음 단계

### CP 완료 시
```
/next-checkpoint complete
```

### Claude 동작
1. 현재 CP 평가
2. `current.md` → `completed/cp2-order-domain.md` 이동
3. 다음 CP 계획 제시

### Claude 응답
```markdown
## CP2 완료 평가
✅ 모든 테스트 통과
✅ 피드백 반영 완료
✅ 학습 로그 작성됨

## CP3 제안: Application Layer
- Use Case 구현
- Port 인터페이스 설계
- Transaction 경계 고민

다음 세션에서 시작할까?
```

---

## 프롬프팅 팁

### 효과적인 질문
✅ **구체적**
```
Order.cancel()에서 환불 처리를 도메인에 둘까, 애플리케이션에 둘까?
```

❌ **모호함**
```
Order 어떻게 만들어?
```

### 옵션 요청
✅ **선택지 요청**
```
캐시 무효화 전략 2-3개 옵션 + 트레이드오프 알려줘
```

❌ **정답 요구**
```
캐시 무효화 어떻게 해야 해?
```

### 피드백 활용
✅ **반영 후 재검토**
```
/review 피드백 반영 완료
```

✅ **추가 질문**
```
@docs/feedback/... 보고 질문: 왜 DTO를 VO 대신 써야 해?
```

---

## 자주 쓰는 커맨드

| 상황 | 커맨드 | 예시 |
|------|--------|------|
| CP 시작 | `/next-checkpoint [번호]` | `/next-checkpoint CP3` |
| 피드백 요청 | `/review [내용]` | `/review Order 구현 완료` |
| 재검토 | `/review [내용]` | `/review 피드백 반영 완료` |
| 챌린지 | `/challenge [상황]` | `/challenge Order 완료, 심화 과제 원함` |
| CP 완료 | `/next-checkpoint complete` | `/next-checkpoint complete` |

---

## 자주 쓰는 프롬프트 패턴

### 1. 옵션 요청
```
[주제]에 대해 2-3개 옵션 + 트레이드오프 알려줘
```

### 2. 설계 검증
```
[구현 방법] 이렇게 했는데, 괜찮나?
다른 접근 방법은?
```

### 3. 심화 질문
```
@docs/feedback/... 에서 [개념] 언급했는데,
좀 더 자세히 설명해줘. 실전 예시는?
```

### 4. 파일 참조
```
@CLAUDE.md - 프로젝트 전체 맥락
@docs/checkpoints/current.md - 현재 진행 상황
@docs/feedback/... - 특정 피드백
@docs/learning/log.md - 학습 기록
```

---

## 빠른 참조

**1 사이클 구조**:
```
질문 → 옵션 받음 → 선택 → 구현 → 리뷰 → 토론 → 반영 → 재리뷰 → 학습 기록 → 다음
```

**핵심 원칙**:
- Claude는 옵션/방향 제시만 (코드 작성 X)
- 학습자가 직접 구현
- 피드백 루프는 만족할 때까지 반복
- 학습 기록은 학습자 주도
