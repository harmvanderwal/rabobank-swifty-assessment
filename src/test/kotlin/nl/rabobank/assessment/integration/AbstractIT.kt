package nl.rabobank.assessment.integration

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
abstract class AbstractIT {

    companion object {
        @Container
        private val postgreSQLContainer : PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:14.1")

        init {
            postgreSQLContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun postgreSQLProperties(registry: DynamicPropertyRegistry) {
            registry.add("global.datasource.url") {
                postgreSQLContainer.jdbcUrl.substring(5)
            }
            registry.add("global.datasource.password") { postgreSQLContainer.password }
            registry.add("global.datasource.username") { postgreSQLContainer.username }
        }
    }
}