# Phase 7: 통합 테스트 및 엔드투엔드 테스트

## 목표
- 전체 시스템 통합 테스트 구현
- 실제 데이터베이스를 이용한 테스트
- API 레벨 엔드투엔드 테스트
- 테스트 데이터 관리 전략

## 이론 학습 포인트

### 테스트 피라미드
1. **단위 테스트 (Unit Tests)**
   - 개별 컴포넌트 검증
   - 빠른 피드백

2. **통합 테스트 (Integration Tests)**
   - 컴포넌트 간 상호작용 검증
   - 외부 시스템과의 통합 검증

3. **엔드투엔드 테스트 (E2E Tests)**
   - 전체 사용자 시나리오 검증
   - 실제 환경에 가까운 테스트

### 테스트 더블 전략
- **Mock**: 행위 검증
- **Stub**: 상태 반환
- **Fake**: 단순한 구현체

## 실습 과제

### 1. 통합 테스트 설정

```kotlin
@SpringBootTest
@Testcontainers
@Transactional
class AccountIntegrationTest {

    @Container
    companion object {
        @JvmStatic
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }

    @DynamicPropertySource
    companion object {
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
        }
    }

    @Autowired
    lateinit var sendMoneyUseCase: SendMoneyUseCase

    @Autowired
    lateinit var loadAccountPort: LoadAccountPort

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var activityRepository: ActivityRepository
}
```

### 2. 데이터 주도 통합 테스트

```kotlin
class SendMoneyIntegrationTest : AccountIntegrationTest() {

    @Test
    fun `송금 성공 시나리오`() {
        // given
        val sourceAccountId = AccountId(1L)
        val targetAccountId = AccountId(2L)
        val transferAmount = Money.of(500.toBigDecimal())

        givenAnAccountWithId(sourceAccountId)
        givenAnAccountWithId(targetAccountId)
        
        val command = SendMoneyCommand(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            money = transferAmount
        )

        // when
        val success = sendMoneyUseCase.sendMoney(command)

        // then
        assertThat(success).isTrue()
        
        val sourceAccount = loadAccountPort.loadAccount(
            sourceAccountId, 
            LocalDateTime.now().minusDays(10)
        )
        val targetAccount = loadAccountPort.loadAccount(
            targetAccountId, 
            LocalDateTime.now().minusDays(10)
        )

        assertThat(sourceAccount!!.calculateBalance())
            .isEqualTo(Money.of(500.toBigDecimal()))
        assertThat(targetAccount!!.calculateBalance())
            .isEqualTo(Money.of(1000.toBigDecimal()))

        // 거래 내역 검증
        val sourceActivities = activityRepository.findByOwnerSince(
            sourceAccountId.value, LocalDateTime.now().minusMinutes(1)
        )
        val targetActivities = activityRepository.findByOwnerSince(
            targetAccountId.value, LocalDateTime.now().minusMinutes(1)
        )

        assertThat(sourceActivities).hasSize(1)
        assertThat(targetActivities).hasSize(1)
        assertThat(sourceActivities[0].amount).isEqualTo(transferAmount.negate().amount)
        assertThat(targetActivities[0].amount).isEqualTo(transferAmount.amount)
    }

    @Test
    fun `잔액 부족으로 송금 실패`() {
        // given
        val sourceAccountId = AccountId(1L)
        val targetAccountId = AccountId(2L)
        val transferAmount = Money.of(1500.toBigDecimal())

        givenAnAccountWithId(sourceAccountId, Money.of(1000.toBigDecimal()))
        givenAnAccountWithId(targetAccountId)
        
        val command = SendMoneyCommand(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            money = transferAmount
        )

        // when
        val success = sendMoneyUseCase.sendMoney(command)

        // then
        assertThat(success).isFalse()
        
        // 잔액 변화 없음 검증
        val sourceAccount = loadAccountPort.loadAccount(
            sourceAccountId, 
            LocalDateTime.now().minusDays(10)
        )
        assertThat(sourceAccount!!.calculateBalance())
            .isEqualTo(Money.of(1000.toBigDecimal()))
    }

    private fun givenAnAccountWithId(
        accountId: AccountId, 
        balance: Money = Money.of(1000.toBigDecimal())
    ) {
        accountRepository.save(
            AccountJpaEntity(
                id = accountId.value,
                baselineBalance = balance.amount
            )
        )
    }
}
```

### 3. 웹 계층 통합 테스트

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation::class)
class AccountWebIntegrationTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Container
    companion object {
        @JvmStatic
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }

    @Test
    @Order(1)
    fun `송금 API 테스트`() {
        // given
        val request = SendMoneyRequest(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = 500.toBigDecimal()
        )

        // when
        val response = testRestTemplate.postForEntity(
            "/accounts/1/send",
            request,
            SendMoneyResponse::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
    }

    @Test
    @Order(2)
    fun `잔액 조회 API 테스트`() {
        // when
        val response = testRestTemplate.getForEntity(
            "/accounts/1/balance",
            AccountBalanceResponse::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.balance).isEqualByComparingTo(500.toBigDecimal())
    }

    @Test
    fun `잘못된 요청 데이터 처리`() {
        // given
        val invalidRequest = mapOf(
            "sourceAccountId" to 1L,
            "targetAccountId" to 2L,
            "amount" to -100  // 음수 금액
        )

        // when
        val response = testRestTemplate.postForEntity(
            "/accounts/1/send",
            invalidRequest,
            ErrorResponse::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.code).isEqualTo("VALIDATION_ERROR")
    }
}
```

### 4. 동시성 통합 테스트

```kotlin
@SpringBootTest
@Testcontainers
class ConcurrencyIntegrationTest : AccountIntegrationTest() {

    @Test
    fun `동시 송금 요청 처리 테스트`() {
        // given
        val sourceAccountId = AccountId(1L)
        val targetAccountId = AccountId(2L)
        val transferAmount = Money.of(100.toBigDecimal())
        val numberOfThreads = 10

        givenAnAccountWithId(sourceAccountId, Money.of(1000.toBigDecimal()))
        givenAnAccountWithId(targetAccountId, Money.of(0.toBigDecimal()))

        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)

        // when
        repeat(numberOfThreads) {
            executor.submit {
                try {
                    val command = SendMoneyCommand(
                        sourceAccountId = sourceAccountId,
                        targetAccountId = targetAccountId,
                        money = transferAmount
                    )
                    val success = sendMoneyUseCase.sendMoney(command)
                    if (success) {
                        successCount.incrementAndGet()
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // then
        val sourceAccount = loadAccountPort.loadAccount(
            sourceAccountId, 
            LocalDateTime.now().minusDays(10)
        )
        val targetAccount = loadAccountPort.loadAccount(
            targetAccountId, 
            LocalDateTime.now().minusDays(10)
        )

        val expectedSourceBalance = Money.of(
            1000.toBigDecimal().minus(transferAmount.amount.multiply(successCount.get().toBigDecimal()))
        )
        val expectedTargetBalance = Money.of(
            transferAmount.amount.multiply(successCount.get().toBigDecimal())
        )

        assertThat(sourceAccount!!.calculateBalance()).isEqualTo(expectedSourceBalance)
        assertThat(targetAccount!!.calculateBalance()).isEqualTo(expectedTargetBalance)
        assertThat(successCount.get()).isLessThanOrEqualTo(10)
    }
}
```

### 5. 테스트 데이터 빌더

```kotlin
class AccountTestDataBuilder {
    private var accountId: AccountId = AccountId(1L)
    private var baselineBalance: Money = Money.of(1000.toBigDecimal())
    private var activities: MutableList<Activity> = mutableListOf()

    fun withAccountId(accountId: AccountId) = apply {
        this.accountId = accountId
    }

    fun withBaselineBalance(balance: Money) = apply {
        this.baselineBalance = balance
    }

    fun withActivity(
        targetAccountId: AccountId,
        amount: Money,
        timestamp: LocalDateTime = LocalDateTime.now()
    ) = apply {
        activities.add(
            Activity(
                id = null,
                ownerAccountId = this.accountId,
                sourceAccountId = this.accountId,
                targetAccountId = targetAccountId,
                timestamp = timestamp,
                money = amount.negate()
            )
        )
        activities.add(
            Activity(
                id = null,
                ownerAccountId = targetAccountId,
                sourceAccountId = this.accountId,
                targetAccountId = targetAccountId,
                timestamp = timestamp,
                money = amount
            )
        )
    }

    fun build(): Account {
        return Account(
            accountId = accountId,
            baselineBalance = baselineBalance,
            activityWindow = ActivityWindow(activities)
        )
    }
}

// 사용 예
fun givenAccountWithActivity(): Account {
    return AccountTestDataBuilder()
        .withAccountId(AccountId(1L))
        .withBaselineBalance(Money.of(1000.toBigDecimal()))
        .withActivity(
            targetAccountId = AccountId(2L),
            amount = Money.of(500.toBigDecimal())
        )
        .build()
}
```

## 챌린지 과제

### 1. 테스트 데이터 관리
- 테스트 간의 데이터 격리를 어떻게 보장할 것인가?
- 대용량 테스트 데이터를 어떻게 효율적으로 관리할 것인가?
- 테스트 데이터의 일관성을 어떻게 유지할 것인가?

### 2. 테스트 성능 최적화
- 느린 통합 테스트를 어떻게 최적화할 것인가?
- 테스트 병렬 실행 시 리소스 경합을 어떻게 방지할 것인가?
- 선택적 테스트 실행 전략은?

### 3. 외부 의존성 관리
- 실제 외부 서비스 vs 테스트 더블 선택 기준은?
- 네트워크 지연이나 장애 시뮬레이션 방법은?
- 테스트 환경의 일관성을 어떻게 보장할 것인가?

## 실습 단계

1. **Testcontainers 설정 및 기본 통합 테스트**
2. **송금 기능 통합 테스트 작성**
3. **웹 API 엔드투엔드 테스트 작성**
4. **동시성 시나리오 테스트 작성**
5. **테스트 데이터 빌더 패턴 구현**
6. **테스트 성능 최적화**

## 테스트 시나리오

### 해피 패스 시나리오
- [ ] 정상적인 송금 플로우
- [ ] 잔액 조회 및 거래 내역 조회
- [ ] 다양한 금액의 송금 테스트

### 에러 시나리오
- [ ] 잔액 부족 상황
- [ ] 존재하지 않는 계좌
- [ ] 잘못된 입력 데이터
- [ ] 시스템 에러 상황

### 동시성 시나리오
- [ ] 동시 송금 요청
- [ ] 동시 조회 요청
- [ ] 락 경합 상황

### 성능 시나리오
- [ ] 대용량 거래 처리
- [ ] 높은 동시 접속
- [ ] 메모리 사용량 모니터링

## 완료 체크리스트
- [ ] 모든 주요 기능 통합 테스트 작성
- [ ] 웹 API 엔드투엔드 테스트 작성
- [ ] 동시성 및 성능 테스트 작성
- [ ] 테스트 데이터 관리 전략 구현
- [ ] CI/CD 파이프라인 통합
- [ ] 테스트 커버리지 목표 달성

## 다음 Phase 미리보기
Phase 8에서는 리팩토링과 개선을 통해 코드 품질을 향상시키고, 학습한 내용을 정리할 예정입니다.