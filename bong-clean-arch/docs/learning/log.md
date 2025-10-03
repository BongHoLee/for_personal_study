# 학습 일지

> **학습자 주도로 작성**하는 일지입니다.
> LEARNING 단계에서 핵심 개념, 트레이드오프, 의사결정 이유를 기록하세요.
> 필요시 Claude에게 정리 도움을 요청할 수 있습니다.

---

## YYYY-MM-DD - [주제/체크포인트]

### 배운 개념
- **[개념1]**: 한 줄 설명
  - 상세 내용
  - 왜 중요한가?

- **[개념2]**: ...

### 의사결정 기록
**문제**: ...

**고려한 옵션들**:
1. Option A: ... (선택함 ✅)
   - 장점: ...
   - 단점: ...
2. Option B: ...
   - 장점: ...
   - 단점: ...

**선택 이유**: ...

**트레이드오프**: ...

### 실수와 배움
- **실수**: ...
  - **배움**: ...

### 참고 자료
- [링크 제목](URL)
- 서적: ...

---

<!--
아래는 작성 예시입니다. 실제 사용 시 삭제하거나 참고하세요.

## 2025-10-03 - CP1: Account 도메인 모델링

### 배운 개념
- **Value Object의 불변성**: 식별자가 아닌 속성으로 동등성 판단
  - Kotlin의 `data class`와 `val`을 활용하면 자연스럽게 불변성 확보
  - 왜 중요한가? 도메인 모델의 일관성 보장, 사이드 이펙트 방지

- **Rich Domain Model**: 비즈니스 로직을 도메인 객체에 위치
  - Anemic Domain Model을 피하기 위해 행위를 도메인에 배치
  - 예: `Account.withdraw()` vs `AccountService.withdraw(account)`

### 의사결정 기록
**문제**: Transaction(거래)을 어떻게 모델링할 것인가?

**고려한 옵션들**:
1. Transaction을 Entity로 (선택함 ✅)
   - 장점: 거래 이력 추적 가능, 식별자 존재
   - 단점: Aggregate 경계 고민 필요
2. Transaction을 VO로
   - 장점: 단순함
   - 단점: 이력 관리 어려움

**선택 이유**: 거래는 고유한 식별자가 필요하고, 시간에 따른 변화를 추적해야 함

**트레이드오프**: Aggregate 경계를 Account에 포함시킬 것인지, 별도 Aggregate로 분리할 것인지 추가 고민 필요

### 실수와 배움
- **실수**: 처음에 Account에 Spring JPA 어노테이션을 넣음
  - **배움**: Domain Layer는 인프라 의존성이 없어야 함. Adapter에서 변환 처리

### 참고 자료
- DDD Distilled (Vaughn Vernon)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

-->
