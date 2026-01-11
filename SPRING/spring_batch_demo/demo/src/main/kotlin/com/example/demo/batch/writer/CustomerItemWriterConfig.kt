package com.example.demo.batch.writer

import com.example.demo.domain.primary.Customer
import com.example.demo.domain.secondary.NewCustomer
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import javax.sql.DataSource

/**
 * 다중 데이터소스 ItemWriter 설정 (JdbcBatchItemWriter 방식)
 *
 * ## JdbcBatchItemWriter의 장점
 * - DataSource 기반으로 동작하여 트랜잭션 관리가 단순
 * - Spring Batch의 트랜잭션과 자연스럽게 통합
 * - 배치 INSERT로 높은 성능
 * - JPA 세션 관리 이슈 없음
 *
 * ## 트레이드오프
 * - SQL을 직접 작성해야 함
 * - JPA의 더티 체킹, 캐시 등 기능 사용 불가
 * - 엔티티 생명주기 관리 없음
 *
 * 대용량 배치 처리에서는 JdbcBatchItemWriter가 더 적합합니다.
 */
@Configuration
class CustomerItemWriterConfig {

    /**
     * Primary DB용 Writer (Customer)
     *
     * DataSourceTransactionManager와 함께 동작하므로
     * 트랜잭션이 자동으로 관리됩니다.
     *
     * @DependsOn: EntityManagerFactory가 먼저 초기화되어 DDL이 생성되도록 보장
     */
    @Bean
    @DependsOn("primaryEntityManagerFactory")
    fun existingCustomerWriter(
        @Qualifier("primaryDataSource") dataSource: DataSource
    ): JdbcBatchItemWriter<Customer> {
        return JdbcBatchItemWriterBuilder<Customer>()
            .dataSource(dataSource)
            .sql("""
                INSERT INTO customers (firstName, lastName, email, age, isActive)
                VALUES (:firstName, :lastName, :email, :age, :isActive)
            """.trimIndent())
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .build()
    }

    /**
     * Secondary DB용 Writer (NewCustomer)
     *
     * DataSourceTransactionManager와 함께 동작하므로
     * 트랜잭션이 자동으로 관리됩니다.
     *
     * @DependsOn: EntityManagerFactory가 먼저 초기화되어 DDL이 생성되도록 보장
     */
    @Bean
    @DependsOn("secondaryEntityManagerFactory")
    fun newCustomerWriter(
        @Qualifier("secondaryDataSource") dataSource: DataSource
    ): JdbcBatchItemWriter<NewCustomer> {
        return JdbcBatchItemWriterBuilder<NewCustomer>()
            .dataSource(dataSource)
            .sql("""
                INSERT INTO new_customers (firstName, lastName, email, age, isActive)
                VALUES (:firstName, :lastName, :email, :age, :isActive)
            """.trimIndent())
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .build()
    }
}
