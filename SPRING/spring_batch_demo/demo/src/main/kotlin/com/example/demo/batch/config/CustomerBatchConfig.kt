package com.example.demo.batch.config

import com.example.demo.batch.processor.ExistingCustomerProcessor
import com.example.demo.batch.processor.NewCustomerProcessor
import com.example.demo.batch.reader.CustomerItemReaderConfig
import com.example.demo.domain.primary.Customer
import com.example.demo.domain.secondary.NewCustomer
import com.example.demo.dto.CustomerCsvRow
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * Spring Batch Job/Step 설정 (Step 분리 + JpaItemWriter 방식)
 *
 * CSV 파일에서 고객 데이터를 읽어 type에 따라 다른 DB에 저장합니다.
 *
 * ## 아키텍처 특징
 * - Step 분리: 각 Step이 자신의 JpaTransactionManager 사용
 * - JpaItemWriter: JPA 엔티티를 직접 영속화
 * - EntityManagerFactory와 TransactionManager가 올바르게 매핑되어야 함
 *
 * ## 흐름
 * Job: importCustomerJob
 *  ├── Step 1: existingCustomerStep (Primary DB)
 *  │    - Reader: CSV 읽기
 *  │    - Processor: EXISTING 타입만 필터링 + 변환
 *  │    - Writer: Primary DB에 JPA로 저장
 *  │
 *  └── Step 2: newCustomerStep (Secondary DB)
 *       - Reader: CSV 읽기 (동일 파일 재읽기)
 *       - Processor: NEW 타입만 필터링 + 변환
 *       - Writer: Secondary DB에 JPA로 저장
 */
@Configuration
class CustomerBatchConfig(
    private val existingCustomerProcessor: ExistingCustomerProcessor,
    private val newCustomerProcessor: NewCustomerProcessor,
    private val customerItemReaderConfig: CustomerItemReaderConfig
) {

    @Bean
    fun importCustomerJob(
        jobRepository: JobRepository,
        existingCustomerStep: Step,
        newCustomerStep: Step
    ): Job {
        return JobBuilder("importCustomerJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(existingCustomerStep)   // Step 1: EXISTING 타입 처리
            .next(newCustomerStep)          // Step 2: NEW 타입 처리
            .build()
    }

    /**
     * Step 1: EXISTING 타입 고객을 Primary DB에 저장
     *
     * JpaItemWriter는 EntityManagerFactory 기반으로 동작하며,
     * JpaTransactionManager와 함께 JPA 트랜잭션을 관리합니다.
     */
    @Bean
    fun existingCustomerStep(
        jobRepository: JobRepository,
        @Qualifier("primaryTransactionManager") transactionManager: PlatformTransactionManager,
        existingCustomerWriter: JpaItemWriter<Customer>
    ): Step {
        return StepBuilder("existingCustomerStep", jobRepository)
            .chunk<CustomerCsvRow, Customer>(10, transactionManager)
            .reader(customerItemReaderConfig.createCustomerItemReader())
            .processor(existingCustomerProcessor)
            .writer(existingCustomerWriter)
            .build()
    }

    /**
     * Step 2: NEW 타입 고객을 Secondary DB에 저장
     *
     * JpaItemWriter는 EntityManagerFactory 기반으로 동작하며,
     * JpaTransactionManager와 함께 JPA 트랜잭션을 관리합니다.
     */
    @Bean
    fun newCustomerStep(
        jobRepository: JobRepository,
        @Qualifier("secondaryTransactionManager") transactionManager: PlatformTransactionManager,
        newCustomerWriter: JpaItemWriter<NewCustomer>
    ): Step {
        return StepBuilder("newCustomerStep", jobRepository)
            .chunk<CustomerCsvRow, NewCustomer>(10, transactionManager)
            .reader(customerItemReaderConfig.createCustomerItemReader())
            .processor(newCustomerProcessor)
            .writer(newCustomerWriter)
            .build()
    }
}
