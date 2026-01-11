package com.example.demo.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.jdbc.core.JdbcTemplate
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

    @Bean
    fun secondaryTransactionManager(secondaryEntityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        return JpaTransactionManager(secondaryEntityManagerFactory.`object`!!)
    }

    @Bean
    fun secondaryJdbcTemplate(
        @Qualifier("secondaryDataSource") secondaryDataSource: DataSource
    ): JdbcTemplate {
        return JdbcTemplate(secondaryDataSource)
    }
}
