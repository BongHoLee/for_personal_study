package com.example.demo.batch.writer

import com.example.demo.domain.primary.Customer
import com.example.demo.domain.secondary.NewCustomer
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 다중 데이터소스 ItemWriter 설정 (JpaItemWriter 방식)
 *
 * ## JpaItemWriter의 장점
 * - JPA 엔티티를 직접 사용하여 타입 안전성 보장
 * - SQL 작성 불필요
 * - JPA의 더티 체킹, 캐시 등 기능 활용 가능
 * - 엔티티 생명주기 자동 관리
 *
 * ## 주의사항
 * - EntityManagerFactory와 JpaTransactionManager가 올바르게 매핑되어야 함
 * - Step에서 사용하는 TransactionManager와 Writer의 EntityManagerFactory가 일치해야 함
 */
@Configuration
class CustomerItemWriterConfig {

    /**
     * Primary DB용 Writer (Customer)
     *
     * JpaItemWriter는 EntityManagerFactory를 통해 엔티티를 영속화합니다.
     * Step의 JpaTransactionManager와 동일한 EntityManagerFactory를 사용해야 합니다.
     */
    @Bean
    fun existingCustomerWriter(
        @Qualifier("primaryEntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): JpaItemWriter<Customer> {
        return JpaItemWriterBuilder<Customer>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }

    /**
     * Secondary DB용 Writer (NewCustomer)
     *
     * JpaItemWriter는 EntityManagerFactory를 통해 엔티티를 영속화합니다.
     * Step의 JpaTransactionManager와 동일한 EntityManagerFactory를 사용해야 합니다.
     */
    @Bean
    fun newCustomerWriter(
        @Qualifier("secondaryEntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): JpaItemWriter<NewCustomer> {
        return JpaItemWriterBuilder<NewCustomer>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
}
