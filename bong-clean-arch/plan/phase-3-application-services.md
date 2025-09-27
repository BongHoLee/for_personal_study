# Phase 3: 애플리케이션 서비스 및 포트 정의

## 목표
- 유스케이스 중심의 애플리케이션 서비스 구현
- 인바운드/아웃바운드 포트 인터페이스 정의
- CQRS 패턴 적용 (Command와 Query 분리)

## 이론 학습 포인트

### 애플리케이션 서비스의 역할
1. **유스케이스 오케스트레이션**
   - 도메인 객체들의 협력 조정
   - 트랜잭션 경계 관리
   - 비즈니스 로직은 도메인에 위임

2. **포트 패턴**
   - 인바운드 포트: 애플리케이션으로 들어오는 요청
   - 아웃바운드 포트: 애플리케이션에서 나가는 요청

3. **의존성 역전**
   - 애플리케이션이 인프라에 의존하지 않음
   - 포트를 통한 추상화

## 실습 과제

### 1. 유스케이스 정의

**주요 유스케이스**:
- 송금하기 (SendMoney)
- 계좌 조회 (GetAccountBalance)
- 거래 내역 조회 (GetAccountActivity)

### 2. 인바운드 포트 정의

```kotlin
// 송금 유스케이스
interface SendMoneyUseCase {
    fun sendMoney(command: SendMoneyCommand): Boolean
}

// 계좌 조회 유스케이스  
interface GetAccountBalanceQuery {
    fun getAccountBalance(query: GetAccountBalanceQuery): Money
}

// 거래 내역 조회
interface GetAccountActivityQuery {
    fun getAccountActivity(query: GetAccountActivityQuery): List<Activity>
}
```

### 3. 아웃바운드 포트 정의

```kotlin
// 계좌 로드
interface LoadAccountPort {
    fun loadAccount(accountId: AccountId, baselineDate: LocalDateTime): Account?
}

// 계좌 상태 업데이트
interface UpdateAccountStatePort {
    fun updateActivities(account: Account)
}

// 계좌 락 (동시성 제어)
interface AccountLockPort {
    fun lockAccount(accountId: AccountId)
    fun releaseAccount(accountId: AccountId)
}
```

### 4. 애플리케이션 서비스 구현

```kotlin
@Service
@Transactional
class SendMoneyService(
    private val loadAccountPort: LoadAccountPort,
    private val accountLockPort: AccountLockPort,
    private val updateAccountStatePort: UpdateAccountStatePort,
    private val moneyTransferProperties: MoneyTransferProperties
) : SendMoneyUseCase {

    override fun sendMoney(command: SendMoneyCommand): Boolean {
        checkThreshold(command)
        
        val baselineDate = LocalDateTime.now().minusDays(10)
        
        val sourceAccount = loadAccountPort.loadAccount(
            command.sourceAccountId, baselineDate
        ) ?: throw AccountNotFoundException(command.sourceAccountId)
        
        val targetAccount = loadAccountPort.loadAccount(
            command.targetAccountId, baselineDate
        ) ?: throw AccountNotFoundException(command.targetAccountId)

        accountLockPort.lockAccount(command.sourceAccountId)
        if (!sourceAccount.withdraw(command.money, command.targetAccountId)) {
            accountLockPort.releaseAccount(command.sourceAccountId)
            return false
        }

        accountLockPort.lockAccount(command.targetAccountId)
        if (!targetAccount.deposit(command.money, command.sourceAccountId)) {
            accountLockPort.releaseAccount(command.sourceAccountId)
            accountLockPort.releaseAccount(command.targetAccountId)
            return false
        }

        updateAccountStatePort.updateActivities(sourceAccount)
        updateAccountStatePort.updateActivities(targetAccount)

        accountLockPort.releaseAccount(command.sourceAccountId)
        accountLockPort.releaseAccount(command.targetAccountId)
        
        return true
    }

    private fun checkThreshold(command: SendMoneyCommand) {
        if (command.money.isGreaterThan(moneyTransferProperties.maximumTransferThreshold)) {
            throw ThresholdExceededException(moneyTransferProperties.maximumTransferThreshold, command.money)
        }
    }
}
```

### 5. Command/Query 객체 설계

```kotlin
data class SendMoneyCommand(
    val sourceAccountId: AccountId,
    val targetAccountId: AccountId,
    val money: Money
) {
    init {
        require(!money.isNegativeOrZero()) { "Money must be positive" }
        require(sourceAccountId != targetAccountId) { "Cannot transfer money to same account" }
    }
}

data class GetAccountBalanceQuery(
    val accountId: AccountId
)

data class GetAccountActivityQuery(
    val accountId: AccountId,
    val since: LocalDateTime,
    val until: LocalDateTime
)
```

## 챌린지 과제

### 1. 동시성 제어
- 동시에 같은 계좌에서 송금하는 경우 어떻게 처리할 것인가?
- 데드락을 방지하려면 어떤 전략이 필요한가?
- 락의 범위와 시간을 어떻게 최소화할 것인가?

### 2. 트랜잭션 경계
- 송금 실패 시 롤백 전략은?
- 부분 실패 시나리오 처리는?
- 보상 트랜잭션이 필요한 경우는?

### 3. 유스케이스 분리
- 송금과 조회 기능을 어떻게 분리할 것인가?
- CQRS를 적용할 때의 장단점은?
- 읽기 모델과 쓰기 모델을 분리해야 하는가?

### 4. 에러 처리
- 어떤 종류의 예외를 정의해야 하는가?
- 도메인 예외 vs 애플리케이션 예외는?
- 예외 전파 전략은?

## 실습 단계

1. **인바운드 포트 인터페이스 정의**
2. **아웃바운드 포트 인터페이스 정의**
3. **Command/Query 객체 구현**
4. **SendMoneyService 구현**
5. **GetAccountBalanceService 구현**
6. **예외 클래스 정의**
7. **단위 테스트 작성**

## 테스트 시나리오

### 성공 시나리오
- [ ] 정상적인 송금 처리
- [ ] 계좌 잔액 조회
- [ ] 거래 내역 조회

### 실패 시나리오
- [ ] 잔액 부족으로 인한 송금 실패
- [ ] 존재하지 않는 계좌로 송금 시도
- [ ] 송금 한도 초과
- [ ] 동시 송금 요청 처리

### 경계 조건
- [ ] 0원 송금 시도
- [ ] 같은 계좌로 송금 시도
- [ ] 음수 금액 송금 시도

## 완료 체크리스트
- [ ] 모든 포트 인터페이스 정의 완료
- [ ] 애플리케이션 서비스 구현 완료
- [ ] Command/Query 객체 구현
- [ ] 포괄적인 단위 테스트 작성
- [ ] 동시성 처리 검증
- [ ] 에러 처리 시나리오 검증

## 다음 Phase 미리보기
Phase 4에서는 인바운드 어댑터를 구현하여 웹 API를 통해 애플리케이션 서비스에 접근할 수 있도록 할 예정입니다.