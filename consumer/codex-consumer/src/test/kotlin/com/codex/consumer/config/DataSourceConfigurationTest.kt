package com.codex.consumer.config

import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.sql.DataSource

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:ds-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.hikari.pool-name=Phase5Pool",
        "spring.datasource.hikari.maximum-pool-size=7",
        "spring.datasource.hikari.minimum-idle=2",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.sql.init.mode=never"
    ]
)
@DisplayName("DataSource configuration")
class DataSourceConfigurationTest {

    @Autowired
    private lateinit var dataSource: DataSource

    @Test
    fun `Hikari 설정이 적용된다`() {
        val hikari = dataSource as HikariDataSource
        assertThat(hikari.poolName).isEqualTo("Phase5Pool")
        assertThat(hikari.maximumPoolSize).isEqualTo(7)
        assertThat(hikari.minimumIdle).isEqualTo(2)
    }
}
