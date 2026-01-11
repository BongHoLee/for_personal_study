package com.example.demo.batch

import com.example.demo.domain.primary.Customer
import com.example.demo.domain.secondary.NewCustomer
import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

/**
 * Spring Batch Job 통합 테스트 (다중 데이터소스)
 *
 * ## 테스트 시나리오
 * - CSV 파일에서 type 필드에 따라 다른 DB에 저장
 * - EXISTING: Primary DB (customers 테이블)
 * - NEW: Secondary DB (new_customers 테이블)
 *
 * ## 테스트 데이터 (7건)
 * - EXISTING: John(28), Jane(34), Alice(22), Charlie(17) → 3건 (Charlie 필터링)
 * - NEW: Bob(45), David(55), Emma(29) → 3건
 *
 * ## 주의: @Transactional 사용 금지
 * Spring Batch는 자체적으로 트랜잭션을 관리하므로
 * 테스트 메서드에 @Transactional을 사용하면 충돌이 발생합니다.
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class CustomerBatchJobTest {

    @Autowired
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var importCustomerJob: Job

    @Autowired
    @Qualifier("primaryEntityManagerFactory")
    private lateinit var primaryEmf: EntityManagerFactory

    @Autowired
    @Qualifier("secondaryEntityManagerFactory")
    private lateinit var secondaryEmf: EntityManagerFactory

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private lateinit var primaryJdbcTemplate: JdbcTemplate

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private lateinit var secondaryJdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        // 테스트할 Job 설정
        jobLauncherTestUtils.job = importCustomerJob

        // EntityManagerFactory 초기화 강제 (DDL 생성 트리거)
        // JdbcBatchItemWriter는 DataSource를 직접 사용하므로
        // JPA EntityManagerFactory가 먼저 초기화되어 테이블이 생성되어야 함
        primaryEmf.createEntityManager().close()
        secondaryEmf.createEntityManager().close()

        // 이전 테스트 데이터 정리
        primaryJdbcTemplate.execute("DELETE FROM customers")
        secondaryJdbcTemplate.execute("DELETE FROM new_customers")
    }

    @Test
    @DisplayName("배치 Job이 성공적으로 완료되어야 한다")
    fun shouldCompleteJobSuccessfully() {
        // Given: 고유한 파라미터로 Job 실행 준비
        val jobParameters = JobParametersBuilder()
            .addString("testRun", System.currentTimeMillis().toString())
            .toJobParameters()

        // When: Job 실행
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Job 상태 검증
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals("COMPLETED", jobExecution.exitStatus.exitCode)
    }

    @Test
    @DisplayName("EXISTING 타입은 Primary DB에 저장되어야 한다")
    fun shouldSaveExistingCustomersToPrimaryDb() {
        // Given
        val jobParameters = JobParametersBuilder()
            .addString("testRun", System.currentTimeMillis().toString())
            .toJobParameters()

        // When
        jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Primary DB (customers 테이블) 검증
        val em = primaryEmf.createEntityManager()
        try {
            val customers = em
                .createQuery("SELECT c FROM Customer c", Customer::class.java)
                .resultList

            // EXISTING 4명 중 Charlie(17세)가 필터링되어 3명
            assertEquals(3, customers.size, "Primary DB에 3명의 고객이 저장되어야 함")

            val names = customers.map { it.firstName }
            assertTrue(names.contains("JOHN"), "John이 저장되어야 함")
            assertTrue(names.contains("JANE"), "Jane이 저장되어야 함")
            assertTrue(names.contains("ALICE"), "Alice가 저장되어야 함")
            assertFalse(names.contains("CHARLIE"), "미성년자 Charlie는 필터링되어야 함")
        } finally {
            em.close()
        }
    }

    @Test
    @DisplayName("NEW 타입은 Secondary DB에 저장되어야 한다")
    fun shouldSaveNewCustomersToSecondaryDb() {
        // Given
        val jobParameters = JobParametersBuilder()
            .addString("testRun", System.currentTimeMillis().toString())
            .toJobParameters()

        // When
        jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Secondary DB (new_customers 테이블) 검증
        val em = secondaryEmf.createEntityManager()
        try {
            val newCustomers = em
                .createQuery("SELECT c FROM NewCustomer c", NewCustomer::class.java)
                .resultList

            // NEW 타입 3명 모두 성인이므로 3명 저장
            assertEquals(3, newCustomers.size, "Secondary DB에 3명의 신규 고객이 저장되어야 함")

            val names = newCustomers.map { it.firstName }
            assertTrue(names.contains("BOB"), "Bob이 저장되어야 함")
            assertTrue(names.contains("DAVID"), "David가 저장되어야 함")
            assertTrue(names.contains("EMMA"), "Emma가 저장되어야 함")
        } finally {
            em.close()
        }
    }

    @Test
    @DisplayName("18세 미만 고객은 필터링되어야 한다")
    fun shouldFilterOutMinors() {
        // Given
        val jobParameters = JobParametersBuilder()
            .addString("testRun", System.currentTimeMillis().toString())
            .toJobParameters()

        // When
        jobLauncherTestUtils.launchJob(jobParameters)

        // Then: 모든 DB에서 미성년자 확인
        val primaryEm = primaryEmf.createEntityManager()
        val secondaryEm = secondaryEmf.createEntityManager()
        try {
            val primaryCustomers = primaryEm
                .createQuery("SELECT c FROM Customer c", Customer::class.java)
                .resultList

            val secondaryCustomers = secondaryEm
                .createQuery("SELECT c FROM NewCustomer c", NewCustomer::class.java)
                .resultList

            // 모든 고객이 18세 이상인지 확인
            assertTrue(
                primaryCustomers.all { it.age >= 18 },
                "Primary DB의 모든 고객이 18세 이상이어야 함"
            )
            assertTrue(
                secondaryCustomers.all { it.age >= 18 },
                "Secondary DB의 모든 고객이 18세 이상이어야 함"
            )
        } finally {
            primaryEm.close()
            secondaryEm.close()
        }
    }

    @Test
    @DisplayName("이름은 대문자로, 이메일은 소문자로 변환되어야 한다")
    fun shouldTransformNamesAndEmails() {
        // Given
        val jobParameters = JobParametersBuilder()
            .addString("testRun", System.currentTimeMillis().toString())
            .toJobParameters()

        // When
        jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Primary DB 검증
        val primaryEm = primaryEmf.createEntityManager()
        val secondaryEm = secondaryEmf.createEntityManager()
        try {
            val primaryCustomers = primaryEm
                .createQuery("SELECT c FROM Customer c", Customer::class.java)
                .resultList

            assertTrue(
                primaryCustomers.all { it.firstName == it.firstName.uppercase() },
                "Primary DB: 모든 firstName이 대문자여야 함"
            )
            assertTrue(
                primaryCustomers.all { it.email == it.email.lowercase() },
                "Primary DB: 모든 email이 소문자여야 함"
            )

            // Then: Secondary DB 검증
            val secondaryCustomers = secondaryEm
                .createQuery("SELECT c FROM NewCustomer c", NewCustomer::class.java)
                .resultList

            assertTrue(
                secondaryCustomers.all { it.firstName == it.firstName.uppercase() },
                "Secondary DB: 모든 firstName이 대문자여야 함"
            )
            assertTrue(
                secondaryCustomers.all { it.email == it.email.lowercase() },
                "Secondary DB: 모든 email이 소문자여야 함"
            )
        } finally {
            primaryEm.close()
            secondaryEm.close()
        }
    }

    @Test
    @DisplayName("Step 실행 결과를 검증한다")
    fun shouldVerifyStepExecution() {
        // Given
        val jobParameters = JobParametersBuilder()
            .addString("testRun", System.currentTimeMillis().toString())
            .toJobParameters()

        // When
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Step 실행 정보 검증 (2개의 Step으로 분리)
        val stepExecutions = jobExecution.stepExecutions.sortedBy { it.stepName }
        assertEquals(2, stepExecutions.size, "2개의 Step이 실행되어야 함")

        // Step 1: existingCustomerStep (EXISTING 타입 처리)
        val existingStep = stepExecutions.find { it.stepName == "existingCustomerStep" }!!
        assertEquals(BatchStatus.COMPLETED, existingStep.status)
        assertEquals(7, existingStep.readCount, "7건을 읽어야 함")
        assertEquals(4, existingStep.filterCount, "4건이 필터링되어야 함 (NEW 3건 + Charlie 1건)")
        assertEquals(3, existingStep.writeCount, "3건이 쓰여야 함 (EXISTING 타입)")

        // Step 2: newCustomerStep (NEW 타입 처리)
        val newStep = stepExecutions.find { it.stepName == "newCustomerStep" }!!
        assertEquals(BatchStatus.COMPLETED, newStep.status)
        assertEquals(7, newStep.readCount, "7건을 읽어야 함")
        assertEquals(4, newStep.filterCount, "4건이 필터링되어야 함 (EXISTING 4건)")
        assertEquals(3, newStep.writeCount, "3건이 쓰여야 함 (NEW 타입)")
    }
}
