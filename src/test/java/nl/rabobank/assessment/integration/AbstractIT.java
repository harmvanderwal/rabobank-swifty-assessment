package nl.rabobank.assessment.integration;

import nl.rabobank.assessment.persistence.repository.PersonRepository;
import nl.rabobank.assessment.persistence.repository.PetRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class AbstractIT {

	public static final PostgreSQLContainer<?> postgreSQLContainer =
			new PostgreSQLContainer<>("postgres:14.1");

	@DynamicPropertySource
	static void postgreSQLProperties(DynamicPropertyRegistry registry) {
		registry.add("global.datasource.url", () -> postgreSQLContainer.getJdbcUrl().substring(5));
		registry.add("global.datasource.password", postgreSQLContainer::getPassword);
		registry.add("global.datasource.username", postgreSQLContainer::getUsername);
	}

	static {
		postgreSQLContainer.start();
	}
}