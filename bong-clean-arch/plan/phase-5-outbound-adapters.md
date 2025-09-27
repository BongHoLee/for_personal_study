# Phase 5: 아웃바운드 어댑터 (데이터베이스) 구현

## 목표
- JPA를 이용한 영속성 어댑터 구현
- 도메인과 영속성 모델 분리
- 트랜잭션 처리 및 동시성 제어
- 데이터베이스 스키마 설계

## 이론 학습 포인트

### 아웃바운드 어댑터 역할
1. **도메인 모델과 영속성 모델 변환**
   - 도메인 객체 → JPA 엔티티
   - ORM 임피던스 미스매치 해결

2. **포트 인터페이스 구현**
   - LoadAccountPort
   - UpdateAccountStatePort
   - AccountLockPort

3. **트랜잭션 경계 관리**
   - 데이터 일관성 보장
   - 동시성 제어

## 실습 과제

### 1. JPA 엔티티 설계

```kotlin
@Entity
@Table(name = "account")
class AccountJpaEntity(
    @Id
    val id: Long,
    
    @Column(nullable = false)
    val baselineBalance: BigDecimal = BigDecimal.ZERO
)

@Entity
@Table(name = "activity")
class ActivityJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val timestamp: LocalDateTime,
    
    @Column(nullable = false)
    val ownerAccountId: Long,
    
    @Column(nullable = false)
    val sourceAccountId: Long,
    
    @Column(nullable = false)
    val targetAccountId: Long,
    
    @Column(nullable = false)
    val amount: BigDecimal
)
```

### 2. Spring Data JPA Repository

```kotlin
@Repository
interface AccountRepository : JpaRepository<AccountJpaEntity, Long> {
    fun findByIdOrNull(id: Long): AccountJpaEntity?
}

@Repository
interface ActivityRepository : JpaRepository<ActivityJpaEntity, Long> {
    
    @Query("""
        SELECT a FROM ActivityJpaEntity a 
        WHERE a.ownerAccountId = :ownerAccountId 
        AND a.timestamp >= :since 
        ORDER BY a.timestamp ASC
    """)
    fun findByOwnerSince(
        @Param("ownerAccountId") ownerAccountId: Long,
        @Param("since") since: LocalDateTime
    ): List<ActivityJpaEntity>

    @Query("""
        SELECT SUM(a.amount) FROM ActivityJpaEntity a 
        WHERE a.targetAccountId = :accountId 
        AND a.ownerAccountId = :accountId 
        AND a.timestamp < :until
    """)
    fun getDepositBalanceUntil(
        @Param("accountId") accountId: Long,
        @Param("until") until: LocalDateTime
    ): BigDecimal?

    @Query("""
        SELECT SUM(a.amount) FROM ActivityJpaEntity a 
        WHERE a.sourceAccountId = :accountId 
        AND a.ownerAccountId = :accountId 
        AND a.timestamp < :until
    """)
    fun getWithdrawalBalanceUntil(
        @Param("accountId") accountId: Long,
        @Param("until") until: LocalDateTime
    ): BigDecimal?
}
```

### 3. 영속성 어댑터 구현

```kotlin
@Component
@Transactional
class AccountPersistenceAdapter(
    private val accountRepository: AccountRepository,
    private val activityRepository: ActivityRepository,
    private val accountMapper: AccountMapper
) : LoadAccountPort, UpdateAccountStatePort {

    override fun loadAccount(
        accountId: AccountId, 
        baselineDate: LocalDateTime
    ): Account? {
        val account = accountRepository.findByIdOrNull(accountId.value)
            ?: return null

        val activities = activityRepository.findByOwnerSince(
            accountId.value, 
            baselineDate
        )

        val withdrawalBalance = activityRepository.getWithdrawalBalanceUntil(
            accountId.value, 
            baselineDate
        ) ?: BigDecimal.ZERO

        val depositBalance = activityRepository.getDepositBalanceUntil(
            accountId.value, 
            baselineDate
        ) ?: BigDecimal.ZERO

        val baselineBalance = Money.of(
            account.baselineBalance
                .add(depositBalance)
                .subtract(withdrawalBalance)
        )

        return Account(
            accountId = accountId,
            baselineBalance = baselineBalance,
            activityWindow = accountMapper.mapToActivityWindow(activities)
        )
    }

    override fun updateActivities(account: Account) {
        account.activityWindow.activities
            .filter { it.id == null }
            .forEach { activity ->
                activityRepository.save(accountMapper.mapToJpaEntity(activity))
            }
    }
}
```

### 4. 도메인-영속성 매퍼

```kotlin
@Component
class AccountMapper {

    fun mapToActivityWindow(activities: List<ActivityJpaEntity>): ActivityWindow {
        val domainActivities = activities.map { mapToDomainEntity(it) }
        return ActivityWindow(domainActivities)
    }

    fun mapToDomainEntity(activity: ActivityJpaEntity): Activity {
        return Activity(
            id = activity.id?.let { ActivityId(it) },
            ownerAccountId = AccountId(activity.ownerAccountId),
            sourceAccountId = AccountId(activity.sourceAccountId),
            targetAccountId = AccountId(activity.targetAccountId),
            timestamp = activity.timestamp,
            money = Money.of(activity.amount)
        )
    }

    fun mapToJpaEntity(activity: Activity): ActivityJpaEntity {
        return ActivityJpaEntity(
            id = activity.id?.value,
            timestamp = activity.timestamp,
            ownerAccountId = activity.ownerAccountId.value,
            sourceAccountId = activity.sourceAccountId.value,
            targetAccountId = activity.targetAccountId.value,
            amount = activity.money.amount
        )
    }
}
```

### 5. 동시성 제어 어댑터

```kotlin
@Component
class AccountLockAdapter : AccountLockPort {
    
    private val lockMap = ConcurrentHashMap<AccountId, ReentrantLock>()
    
    override fun lockAccount(accountId: AccountId) {
        val lock = lockMap.computeIfAbsent(accountId) { ReentrantLock() }
        lock.lock()
    }
    
    override fun releaseAccount(accountId: AccountId) {
        val lock = lockMap[accountId]
        lock?.unlock()
    }
}

// 또는 데이터베이스 레벨 락 사용
@Component
class DatabaseAccountLockAdapter(
    private val accountRepository: AccountRepository
) : AccountLockPort {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun lockAccount(accountId: AccountId) {
        accountRepository.findByIdOrNull(accountId.value)
    }
    
    override fun releaseAccount(accountId: AccountId) {
        // 트랜잭션 종료 시 자동 해제
    }
}
```

### 6. 데이터베이스 스키마

```sql
-- schema.sql
CREATE TABLE account (
    id BIGINT PRIMARY KEY,
    baseline_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    owner_account_id BIGINT NOT NULL,
    source_account_id BIGINT NOT NULL,
    target_account_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    
    INDEX idx_owner_timestamp (owner_account_id, timestamp),
    INDEX idx_source_account (source_account_id),
    INDEX idx_target_account (target_account_id),
    
    FOREIGN KEY (owner_account_id) REFERENCES account(id),
    FOREIGN KEY (source_account_id) REFERENCES account(id),
    FOREIGN KEY (target_account_id) REFERENCES account(id)
);

-- 초기 데이터
INSERT INTO account (id, baseline_balance) VALUES (1, 1000.00);
INSERT INTO account (id, baseline_balance) VALUES (2, 500.00);
```

## 챌린지 과제

### 1. 영속성 모델 설계
- 도메인 모델과 영속성 모델을 어떻게 분리할 것인가?
- JPA 어노테이션이 도메인을 오염시키지 않도록 하려면?
- 복잡한 도메인 객체를 어떻게 효율적으로 저장할 것인가?

### 2. 성능 최적화
- N+1 쿼리 문제를 어떻게 해결할 것인가?
- 대용량 거래 내역을 어떻게 효율적으로 조회할 것인가?
- 인덱싱 전략은?

### 3. 트랜잭션 설계
- 트랜잭션 경계를 어디에 둘 것인가?
- 읽기 전용 트랜잭션 최적화는?
- 분산 트랜잭션이 필요한 경우는?

### 4. 동시성 제어
- 낙관적 락 vs 비관적 락 선택 기준은?
- 데드락 방지 전략은?
- 락 타임아웃 설정은?

## 실습 단계

1. **JPA 엔티티 클래스 구현**
2. **Spring Data JPA Repository 구현**
3. **도메인-영속성 매퍼 구현**
4. **영속성 어댑터 구현**
5. **동시성 제어 어댑터 구현**
6. **데이터베이스 스키마 작성**
7. **통합 테스트 작성**

## 테스트 시나리오

### 영속성 테스트
- [ ] 계좌 저장/조회 기능
- [ ] 거래 내역 저장/조회 기능
- [ ] 복잡한 쿼리 성능 테스트
- [ ] 트랜잭션 롤백 테스트

### 동시성 테스트
- [ ] 동시 송금 요청 처리
- [ ] 락 획득/해제 검증
- [ ] 데드락 시나리오 테스트

### 데이터 일관성 테스트
- [ ] 잔액 계산 정확성
- [ ] 거래 기록 무결성
- [ ] 제약조건 위반 처리

## 완료 체크리스트
- [ ] JPA 엔티티 및 Repository 구현 완료
- [ ] 영속성 어댑터 구현 완료
- [ ] 동시성 제어 구현 완료
- [ ] 데이터베이스 스키마 및 초기 데이터 설정
- [ ] 포괄적인 영속성 계층 테스트 작성
- [ ] 성능 테스트 및 최적화

## 다음 Phase 미리보기
Phase 6에서는 ArchUnit을 이용하여 헥사고날 아키텍처의 규칙을 검증하고, 아키텍처 제약사항을 자동화된 테스트로 관리할 예정입니다.