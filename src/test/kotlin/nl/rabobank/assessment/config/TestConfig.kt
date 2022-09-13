package nl.rabobank.assessment.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
class TestConfig {

    /**
     * Necessary to run sql scripts with @Sql
     */
    @Bean("dataSource")
    fun dataSource(
        @Value("\${global.datasource.url}") url: String,
        @Value("\${global.datasource.username}") username: String,
        @Value("\${global.datasource.password}") password: String?
    ): DataSource? {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.url = "jdbc:$url"
        dataSource.username = username
        dataSource.password = password
        return dataSource
    }
}