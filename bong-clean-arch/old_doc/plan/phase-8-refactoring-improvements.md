# Phase 8: 리팩토링 및 개선

## 목표
- 코드 품질 향상 및 리팩토링
- 성능 최적화 및 확장성 개선
- 모니터링 및 관찰 가능성 추가
- 학습 내용 정리 및 회고

## 이론 학습 포인트

### 지속적인 리팩토링
1. **기술 부채 관리**
   - 코드 냄새 식별
   - 점진적 개선
   - 품질 메트릭 활용

2. **아키텍처 진화**
   - 요구사항 변화 대응
   - 확장성 고려사항
   - 마이그레이션 전략

3. **성능 최적화**
   - 병목 지점 식별
   - 캐싱 전략
   - 데이터베이스 최적화

## 실습 과제

### 1. 코드 품질 개선

#### 도메인 모델 리팩토링
```kotlin
// Before: 원시 타입 사용
class Account(
    val id: Long,
    val balance: BigDecimal
)

// After: 값 객체 사용
class Account(
    val accountId: AccountId,
    val balance: Money
) {
    fun withdraw(amount: Money, targetAccountId: AccountId): Boolean {
        return if (canWithdraw(amount)) {
            val withdrawal = Activity.withdrawal(accountId, targetAccountId, amount)
            activityWindow.addActivity(withdrawal)
            true
        } else false
    }

    private fun canWithdraw(amount: Money): Boolean {
        return balance.isGreaterThanOrEqualTo(amount)
    }
}
```

#### 애플리케이션 서비스 개선
```kotlin
// Before: 긴 메서드
@Service
class SendMoneyService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort
) : SendMoneyUseCase {
    
    override fun sendMoney(command: SendMoneyCommand): Boolean {
        // 50줄의 복잡한 로직...
    }
}

// After: 책임 분리
@Service
class SendMoneyService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort,
    private val accountValidator: AccountValidator,
    private val moneyTransferProcessor: MoneyTransferProcessor
) : SendMoneyUseCase {
    
    override fun sendMoney(command: SendMoneyCommand): Boolean {
        val accounts = loadAccounts(command)
        accountValidator.validate(accounts, command)
        return moneyTransferProcessor.transfer(accounts, command)
    }
    
    private fun loadAccounts(command: SendMoneyCommand): AccountPair {
        // 계좌 로드 로직
    }
}

@Component
class MoneyTransferProcessor(
    private val updateAccountStatePort: UpdateAccountStatePort
) {
    fun transfer(accounts: AccountPair, command: SendMoneyCommand): Boolean {
        val sourceAccount = accounts.source
        val targetAccount = accounts.target
        
        if (!sourceAccount.withdraw(command.money, command.targetAccountId)) {
            return false
        }
        
        if (!targetAccount.deposit(command.money, command.sourceAccountId)) {
            sourceAccount.compensateWithdrawal(command.money, command.targetAccountId)
            return false
        }
        
        updateAccountStatePort.updateActivities(sourceAccount)
        updateAccountStatePort.updateActivities(targetAccount)
        
        return true
    }
}
```

### 2. 성능 최적화

#### 캐싱 전략 추가
```kotlin
@Component
class CachedAccountBalanceService(
    private val getAccountBalanceQuery: GetAccountBalanceQuery,
    private val cacheManager: CacheManager
) : GetAccountBalanceQuery {
    
    @Cacheable("account-balances", key = "#query.accountId.value")
    override fun getAccountBalance(query: GetAccountBalanceQuery): Money {
        return getAccountBalanceQuery.getAccountBalance(query)
    }
}

@Component
class AccountBalanceCacheManager(
    private val cacheManager: CacheManager
) {
    
    @EventListener
    fun handleMoneyTransferred(event: MoneyTransferredEvent) {
        evictCache(event.sourceAccountId)
        evictCache(event.targetAccountId)
    }
    
    private fun evictCache(accountId: AccountId) {
        cacheManager.getCache("account-balances")?.evict(accountId.value)
    }
}
```

#### 쿼리 최적화
```kotlin
@Repository
interface OptimizedActivityRepository : JpaRepository<ActivityJpaEntity, Long> {
    
    @Query("""
        SELECT new com.bong.buckpal.account.adapter.persistence.AccountBalanceProjection(
            a.ownerAccountId,
            COALESCE(SUM(CASE WHEN a.targetAccountId = :accountId THEN a.amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN a.sourceAccountId = :accountId THEN a.amount ELSE 0 END), 0)
        )
        FROM ActivityJpaEntity a 
        WHERE a.ownerAccountId = :accountId 
        AND a.timestamp >= :since
        GROUP BY a.ownerAccountId
    """)
    fun calculateBalanceEfficiently(
        @Param("accountId") accountId: Long,
        @Param("since") since: LocalDateTime
    ): AccountBalanceProjection?
    
    @EntityGraph(attributePaths = ["activities"])
    @Query("SELECT a FROM AccountJpaEntity a WHERE a.id IN :ids")
    fun findAllWithActivities(@Param("ids") ids: List<Long>): List<AccountJpaEntity>
}

data class AccountBalanceProjection(
    val accountId: Long,
    val balance: BigDecimal
)
```

### 3. 모니터링 및 관찰 가능성

#### 메트릭 추가
```kotlin
@Component
class MonitoringService(
    private val meterRegistry: MeterRegistry
) {
    private val transferCounter = Counter.builder("money.transfer.count")
        .description("Number of money transfers")
        .register(meterRegistry)
    
    private val transferAmountDistribution = DistributionSummary.builder("money.transfer.amount")
        .description("Money transfer amounts")
        .register(meterRegistry)
    
    fun recordTransfer(amount: Money, success: Boolean) {
        transferCounter.increment(
            Tags.of(
                Tag.of("success", success.toString()),
                Tag.of("amount_range", getAmountRange(amount))
            )
        )
        
        if (success) {
            transferAmountDistribution.record(amount.amount.toDouble())
        }
    }
    
    private fun getAmountRange(amount: Money): String {
        return when {
            amount.isLessThan(Money.of(100.toBigDecimal())) -> "small"
            amount.isLessThan(Money.of(1000.toBigDecimal())) -> "medium"
            else -> "large"
        }
    }
}
```

#### 분산 추적
```kotlin
@Service
class TracedSendMoneyService(
    private val sendMoneyService: SendMoneyUseCase,
    private val tracer: Tracer
) : SendMoneyUseCase {
    
    override fun sendMoney(command: SendMoneyCommand): Boolean {
        return tracer.nextSpan()
            .name("send-money")
            .tag("source.account.id", command.sourceAccountId.value.toString())
            .tag("target.account.id", command.targetAccountId.value.toString())
            .tag("amount", command.money.amount.toString())
            .start()
            .use { span ->
                try {
                    val result = sendMoneyService.sendMoney(command)
                    span.tag("success", result.toString())
                    result
                } catch (ex: Exception) {
                    span.tag("error", ex.message ?: "unknown")
                    throw ex
                }
            }
    }
}
```

#### 헬스 체크
```kotlin
@Component
class DatabaseHealthIndicator(
    private val accountRepository: AccountRepository
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            accountRepository.count()
            Health.up()
                .withDetail("database", "accessible")
                .build()
        } catch (ex: Exception) {
            Health.down(ex)
                .withDetail("database", "inaccessible")
                .build()
        }
    }
}

@Component
class BusinessLogicHealthIndicator(
    private val loadAccountPort: LoadAccountPort
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            // 테스트 계좌로 기본 기능 검증
            val testAccount = loadAccountPort.loadAccount(
                AccountId(-1L), // 테스트용 계좌
                LocalDateTime.now().minusDays(1)
            )
            
            Health.up()
                .withDetail("business-logic", "operational")
                .build()
        } catch (ex: Exception) {
            Health.down(ex)
                .withDetail("business-logic", "degraded")
                .build()
        }
    }
}
```

### 4. 보안 강화

#### 감사 로그
```kotlin
@Component
class AuditLogger(
    private val auditRepository: AuditRepository
) {
    
    fun logMoneyTransfer(
        command: SendMoneyCommand,
        result: Boolean,
        userId: String? = null
    ) {
        val auditEntry = AuditEntry(
            timestamp = LocalDateTime.now(),
            action = "MONEY_TRANSFER",
            userId = userId,
            details = mapOf(
                "sourceAccountId" to command.sourceAccountId.value,
                "targetAccountId" to command.targetAccountId.value,
                "amount" to command.money.amount,
                "success" to result
            ),
            ipAddress = getCurrentIpAddress()
        )
        
        auditRepository.save(auditEntry)
    }
    
    private fun getCurrentIpAddress(): String {
        return RequestContextHolder.currentRequestAttributes()
            .let { it as ServletRequestAttributes }
            .request
            .remoteAddr
    }
}
```

#### 입력 검증 강화
```kotlin
@Component
class EnhancedInputValidator {
    
    fun validateSendMoneyCommand(command: SendMoneyCommand): ValidationResult {
        val violations = mutableListOf<String>()
        
        // 비즈니스 규칙 검증
        if (command.money.isNegativeOrZero()) {
            violations.add("Transfer amount must be positive")
        }
        
        if (command.sourceAccountId == command.targetAccountId) {
            violations.add("Source and target accounts must be different")
        }
        
        // 보안 규칙 검증
        if (command.money.isGreaterThan(Money.of(10000.toBigDecimal()))) {
            violations.add("Transfer amount exceeds daily limit")
        }
        
        return if (violations.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(violations)
        }
    }
}
```

### 5. 확장성 개선

#### 이벤트 기반 아키텍처
```kotlin
// 도메인 이벤트 정의
sealed class DomainEvent(
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class MoneyTransferredEvent(
    val transferId: TransferId,
    val sourceAccountId: AccountId,
    val targetAccountId: AccountId,
    val amount: Money
) : DomainEvent()

// 이벤트 발행
@Component
class EventPublishingSendMoneyService(
    private val sendMoneyService: SendMoneyUseCase,
    private val eventPublisher: ApplicationEventPublisher
) : SendMoneyUseCase {
    
    override fun sendMoney(command: SendMoneyCommand): Boolean {
        val result = sendMoneyService.sendMoney(command)
        
        if (result) {
            val event = MoneyTransferredEvent(
                transferId = TransferId.generate(),
                sourceAccountId = command.sourceAccountId,
                targetAccountId = command.targetAccountId,
                amount = command.money
            )
            eventPublisher.publishEvent(event)
        }
        
        return result
    }
}

// 이벤트 처리
@EventListener
@Component
class NotificationEventHandler {
    
    @Async
    fun handleMoneyTransferred(event: MoneyTransferredEvent) {
        // 알림 발송 로직
        sendNotification(event.targetAccountId, "You received ${event.amount}")
    }
}
```

## 챌린지 과제

### 1. 마이크로서비스 분해 준비
- 현재 모노리스를 어떻게 마이크로서비스로 분해할 것인가?
- 바운디드 컨텍스트별 분리 전략은?
- 데이터 일관성을 어떻게 보장할 것인가?

### 2. 클라우드 네이티브 준비
- 컨테이너화 전략은?
- 무상태(stateless) 설계로의 전환은?
- 클라우드 서비스 통합 방안은?

### 3. 대용량 처리 준비
- 트래픽 증가에 대한 대비책은?
- 데이터베이스 샤딩 전략은?
- 캐시 분산 전략은?

## 실습 단계

1. **코드 스멜 식별 및 리팩토링**
2. **성능 병목 지점 최적화**
3. **모니터링 및 관찰 가능성 구현**
4. **보안 강화 조치**
5. **확장성 개선 작업**
6. **문서화 및 회고**

## 완료 체크리스트

### 품질 개선
- [ ] 코드 복잡도 감소 (Cyclomatic Complexity < 10)
- [ ] 테스트 커버리지 80% 이상
- [ ] 모든 코드 스멜 해결
- [ ] 성능 병목 지점 해결

### 운영 준비
- [ ] 모니터링 대시보드 구성
- [ ] 알림 시스템 구축
- [ ] 헬스 체크 엔드포인트 구현
- [ ] 로깅 표준화

### 보안 강화
- [ ] 입력 검증 강화
- [ ] 감사 로그 시스템
- [ ] 보안 테스트 통과
- [ ] 취약점 스캔 클리어

### 확장성 준비
- [ ] 이벤트 기반 아키텍처 적용
- [ ] 캐싱 전략 구현
- [ ] 데이터베이스 최적화
- [ ] 무상태 설계 적용

## 학습 회고 및 정리

### 헥사고날 아키텍처 학습 성과
1. **핵심 개념 이해도**: 포트와 어댑터, 의존성 역전
2. **실무 적용 능력**: 실제 비즈니스 로직 구현 경험
3. **아키텍처 검증**: ArchUnit을 통한 규칙 자동화
4. **테스트 전략**: 계층별 테스트 설계 및 구현

### 개선이 필요한 영역
- 복잡한 도메인 로직의 모델링
- 대용량 데이터 처리 전략
- 분산 시스템으로의 확장

### 다음 학습 방향
- 이벤트 소싱과 CQRS 심화
- 마이크로서비스 아키텍처 전환
- 클라우드 네이티브 패턴 적용

## 프로젝트 완성도 평가

최종 프로젝트가 다음 기준을 만족하는지 확인:

- ✅ 헥사고날 아키텍처 원칙 준수
- ✅ 도메인 중심 설계 적용
- ✅ 포괄적인 테스트 커버리지
- ✅ 프로덕션 준비 완료
- ✅ 확장 가능한 구조