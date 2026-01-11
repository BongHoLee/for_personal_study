package com.example.demo.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * Secondary DataSource 설정
 *
 * - New Customer 테이블 (물리적으로 분리된 DB)
 */
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.example.demo.repository.secondary"],
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
class SecondaryDataSourceConfig(
    private val environment: Environment
) {

    @Bean
    @ConfigurationProperties(prefix = "datasource.secondary")
    fun secondaryDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    /**
     * EntityManagerFactory 빈
     *
     * LocalContainerEntityManagerFactoryBean은 FactoryBean이므로
     * Spring이 EntityManagerFactory 타입으로 주입할 때 자동으로 .getObject()를 호출합니다.
     */
    @Bean
    fun secondaryEntityManagerFactory(
        @Qualifier("secondaryDataSource") secondaryDataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val isTestProfile = environment.activeProfiles.contains("test")

        return LocalContainerEntityManagerFactoryBean().apply {
            dataSource = secondaryDataSource
            setPackagesToScan("com.example.demo.domain.secondary")
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
     * JpaTransactionManager
     *
     * EntityManagerFactory 타입으로 주입받으면 Spring이 자동으로
     * LocalContainerEntityManagerFactoryBean에서 EntityManagerFactory를 추출합니다.
     *
     * @Qualifier 필수: @Primary가 아니므로 명시적으로 빈 이름을 지정해야 합니다.
     */
    @Bean
    fun secondaryTransactionManager(
        @Qualifier("secondaryEntityManagerFactory") secondaryEntityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(secondaryEntityManagerFactory)
    }

    @Bean
    fun secondaryJdbcTemplate(
        @Qualifier("secondaryDataSource") secondaryDataSource: DataSource
    ): JdbcTemplate {
        return JdbcTemplate(secondaryDataSource)
    }
}
