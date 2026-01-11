package com.example.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * Primary DataSource 설정
 *
 * - Batch 메타데이터 테이블
 * - Customer 테이블
 *
 * Spring Batch는 기본적으로 @Primary로 지정된 DataSource를 사용합니다.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.example.demo.repository.primary"],
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
class PrimaryDataSourceConfig(
    private val environment: Environment
) {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "datasource.primary")
    fun primaryDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Primary
    @Bean
    fun primaryEntityManagerFactory(primaryDataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val isTestProfile = environment.activeProfiles.contains("test")

        return LocalContainerEntityManagerFactoryBean().apply {
            dataSource = primaryDataSource
            setPackagesToScan("com.example.demo.domain.primary")
            jpaVendorAdapter = HibernateJpaVendorAdapter().apply {
                setShowSql(true)
                setGenerateDdl(isTestProfile)
            }
            setJpaPropertyMap(mapOf(
                "hibernate.format_sql" to "true",
                "hibernate.hbm2ddl.auto" to if (isTestProfile) "create-drop" else "none"
            ))
        }
    }

    /**
     * JPA용 트랜잭션 매니저 (Repository 작업용)
     */
    @Primary
    @Bean
    fun primaryTransactionManager(primaryEntityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        return JpaTransactionManager(primaryEntityManagerFactory.`object`!!)
    }

    @Primary
    @Bean
    fun primaryJdbcTemplate(primaryDataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(primaryDataSource)
    }
}
