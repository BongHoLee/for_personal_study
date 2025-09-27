# Phase 2: 도메인 모델링 및 핵심 비즈니스 로직

## 목표
- 도메인 주도 설계(DDD) 원칙 적용
- Account 및 Activity 엔티티 설계
- 핵심 비즈니스 로직 구현

## 이론 학습 포인트

### 도메인 모델링 원칙
1. **엔티티 vs 값 객체**
   - 엔티티: 식별자가 있고, 생명주기를 가짐
   - 값 객체: 불변이며, 값 자체가 의미를 가짐

2. **애그리게이트 설계**
   - 데이터 일관성 경계
   - 트랜잭션 경계
   - 애그리게이트 루트를 통한 접근

3. **도메인 서비스 vs 엔티티 메서드**
   - 엔티티에 속하지 않는 도메인 로직
   - 여러 엔티티에 걸친 비즈니스 규칙

## 실습 과제

### 1. Account 엔티티 설계

**핵심 속성**:
- AccountId (식별자)
- ActivityWindow (최근 거래 내역)
- 기준 잔액 (baselineBalance)

**핵심 메서드**:
```kotlin
class Account(
    val accountId: AccountId,
    val baselineBalance: Money,
    val activityWindow: ActivityWindow
) {
    fun calculateBalance(): Money
    fun withdraw(money: Money, targetAccountId: AccountId): Boolean
    fun deposit(money: Money, sourceAccountId: AccountId): Boolean
    fun mayWithdraw(money: Money): Boolean
}
```

### 2. Activity 엔티티 설계

**핵심 속성**:
- ActivityId
- 출금/입금 계좌 ID
- 금액
- 타임스탬프

```kotlin
class Activity(
    val id: ActivityId?,
    val ownerAccountId: AccountId,
    val sourceAccountId: AccountId,
    val targetAccountId: AccountId,
    val timestamp: LocalDateTime,
    val money: Money
) {
    fun isWithdrawal(): Boolean
    fun isDeposit(): Boolean
}
```

### 3. ActivityWindow 값 객체

**역할**:
- Activity들의 컬렉션 관리
- 특정 기간의 잔액 계산
- 송금 한도 검증

```kotlin
class ActivityWindow(
    private val activities: List<Activity>
) {
    fun calculateBalance(accountId: AccountId): Money
    fun addActivity(activity: Activity): ActivityWindow
    fun getActivitiesInPeriod(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity>
}
```

### 4. 비즈니스 규칙 구현

**송금 한도 검증**:
- 일일 송금 한도 체크
- 계좌별 송금 한도 체크
- 잔액 부족 체크

**거래 생성 로직**:
- 출금과 입금은 하나의 거래로 처리
- 거래 ID 생성
- 타임스탬프 기록

## 챌린지 과제

### 1. 도메인 불변식 보장
- Account 생성 시 어떤 검증이 필요한가?
- Activity 생성 시 어떤 규칙을 지켜야 하는가?
- 음수 잔액을 어떻게 방지할 것인가?

### 2. 애그리게이트 경계 설정
- Account가 애그리게이트 루트인가?
- Activity는 독립적인 엔티티인가, 값 객체인가?
- ActivityWindow는 어디에 속해야 하는가?

### 3. 성능 vs 일관성 트레이드오프
- 모든 Activity를 메모리에 올려야 하는가?
- 어떻게 대용량 거래 내역을 효율적으로 처리할 것인가?
- 실시간 잔액 계산 vs 캐시된 잔액 사용?

## 실습 단계

1. **Money 값 객체 완성** (Phase 1에서 시작)
2. **AccountId, ActivityId 값 객체 구현**
3. **Activity 엔티티 구현**
4. **ActivityWindow 값 객체 구현**
5. **Account 엔티티 구현**
6. **단위 테스트 작성**

## 테스트 시나리오

### 필수 테스트 케이스
- [ ] 계좌 잔액 계산 정확성
- [ ] 출금 시 잔액 부족 검증
- [ ] 입금 시 잔액 증가 확인
- [ ] Activity 생성 시 불변식 검증
- [ ] ActivityWindow 기간별 조회 기능

### 경계 조건 테스트
- [ ] 0원 송금 시도
- [ ] 음수 금액 처리
- [ ] null 값 처리
- [ ] 동일 계좌 간 송금

## 완료 체크리스트
- [ ] 모든 도메인 엔티티 구현 완료
- [ ] 핵심 비즈니스 로직 구현
- [ ] 포괄적인 단위 테스트 작성
- [ ] 도메인 불변식 보장 검증
- [ ] 코드 리뷰 및 피드백 반영

## 다음 Phase 미리보기
Phase 3에서는 애플리케이션 서비스를 구현하고, 포트 인터페이스를 정의하여 도메인과 외부 세계를 연결하는 계층을 만들 예정입니다.