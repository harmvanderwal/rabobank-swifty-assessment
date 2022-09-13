package nl.rabobank.assessment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class TestConfig {

	/**
	 * Necessary to run sql scripts with @Sql
	 */
	@Bean
	public DataSource dataSource(@Value("${global.datasource.url}") String url,
	                             @Value("${global.datasource.username}") String username,
	                             @Value("${global.datasource.password}") String password) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:" + url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

}
