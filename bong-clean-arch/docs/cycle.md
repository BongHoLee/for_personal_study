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

**Claude와 대화 없이 직접 구현 + 개발 일지 작성**

### 작업 흐름
```
코드 작성
  ↓
막히거나 고민 생김
  ↓
📝 current.md 개발 일지에 기록 (실시간)
  - 어떤 고민?
  - 무엇을 시도?
  - 왜 안 됨?
  - 어떻게 해결?
  ↓
코드 계속 작성
  ↓
테스트 작성 & 통과
```

### 개발 일지 작성 예시
```markdown
## 💭 개발 일지

### 2025-10-04 14:30 - Money VO 구현

**고민**: 음수 검증 위치?

**시도 1**: init { require(amount >= 0) }
- 문제: 예외 메시지 불명확
- 배운 점: require() 람다로 메시지 커스터마이징 가능

**최종 결정**: init + 명확한 메시지
- 이유: data class 장점 유지
- 트레이드오프: factory pattern 명시성은 포기

**다음 고민**: 연산자 오버로딩?
```

### 작성 시점
- ✅ 막힐 때
- ✅ 여러 옵션 고민할 때
- ✅ 실수하고 수정할 때
- ✅ "왜 이렇게 했지?" 싶을 때

### 작성 효과
- 💡 사고 과정 명확화
- 📚 나중에 복기 자료
- 🔍 멘토가 맥락 파악 쉬움
- 🧠 메타인지 훈련

**중요**: 완벽하게 쓰려 하지 말고, 생각나는 대로 빠르게 기록!

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

## 비판적 대화 패턴 ⭐

**멘토 의견을 무조건 수용하지 말고 비판적으로 검토**

### 패턴 1: "정말 그런가?" 질문
```
멘토: "Option A가 결합도가 높아서 문제야"

학습자: "결합이 정말 문제인가?
         실제로 어떤 시나리오에서 문제가 되지?
         오히려 응집도가 높아진 건 아닌가?"
```

### 패턴 2: 본질 재해석
```
멘토: "테스트가 복잡해져"

학습자: "테스트 복잡도가 본질적인 요구사항 아닌가?
         불변식을 검증하려면 어차피 두 객체 필요한 거 아닌가?"
```

### 패턴 3: 실용성 검증
```
멘토: "DDD 원칙상 도메인 서비스를 써야 해"

학습자: "원칙을 지켜서 얻는 실용적 이득이 뭐야?
         이 프로젝트 규모에서도 그 원칙이 타당해?"
```

### 패턴 4: 메타인지 질문
```
학습자: "내가 너무 강하게 주장해서
         너를 편향되게 만든 건 아닐까?"
```

### 좋은 논쟁의 특징
- 👍 근거를 요구함
- 👍 대안을 제시함
- 👍 트레이드오프를 따짐
- 👍 컨텍스트를 고려함
- 👍 상대의 재평가를 촉구함

### 논쟁 → 재평가 → 합의 사이클
```
학습자 비판
  ↓
멘토 재평가 ("좋은 지적이야")
  ↓
논리 재검토
  ↓
편향 자가 진단
  ↓
더 깊은 이해
  ↓
합의 (또는 "둘 다 타당")
```

---

## 세션 시작/종료 루틴 🔄

### 🟢 세션 시작 루틴 (새 대화 첫 메시지)

**템플릿**:
```
@CLAUDE.md
@docs/checkpoints/current.md

[간단한 상황 요약]

현재: [어디까지 했는지]
질문: [무엇을 하고 싶은지]
```

**예시**:
```
@CLAUDE.md
@docs/checkpoints/current.md

CP1 Account 도메인 모델링 중.
설계 논의 완료, 이제 구현 시작하려 함.

현재: transferTo() 방식으로 결정
질문: Account Entity부터 구현할 건데,
      Money VO를 먼저 만들어야 하나?
```

### 🔴 세션 종료 루틴 (대화 끝낼 때)

**체크리스트**:
- [ ] `docs/checkpoints/current.md` 최신 상태로 업데이트됨?
- [ ] 중요한 설계 논의가 있었다면 `cp[번호]_history.md` 작성됨?
- [ ] 다음 세션에서 할 일이 current.md에 명시됨?

**종료 전 확인**:
```
다음에 이어서 할 작업이 current.md에 기록됐나요?
중요한 논의 내용 정리가 필요하면 알려주세요.
```

---

## 빠른 참조

**1 사이클 구조**:
```
질문 → 옵션 받음 → 선택 → 구현 → 리뷰 → 토론 → 반영 → 재리뷰 → 학습 기록 → 다음
```

**논쟁 사이클** (새로 추가):
```
멘토 의견 → 학습자 비판 → 멘토 재평가 → 논리 재검토 → 더 깊은 이해
```

**핵심 원칙**:
- Claude는 옵션/방향 제시만 (코드 작성 X)
- 학습자가 직접 구현
- 피드백 루프는 만족할 때까지 반복
- 학습 기록은 학습자 주도
- **멘토 의견도 비판적으로 검토**
- **불확실성을 솔직하게 표현**
