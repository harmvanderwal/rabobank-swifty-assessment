package nl.rabobank.assessment.config;

import lombok.RequiredArgsConstructor;
import nl.rabobank.assessment.persistence.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

	private static final String[] AUTH_WHITELIST = {"/webjars/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
			"/actuator/**"};

	private final UserRepository userRepository;

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
				.csrf().disable()
				.authorizeExchange()
				.pathMatchers(HttpMethod.GET, AUTH_WHITELIST).permitAll()
				.pathMatchers(HttpMethod.PUT, "/v1/person/*").hasRole("ADMIN")
				.anyExchange()
				.authenticated()
				.and()
				.httpBasic();
		return http.build();
	}

	@Bean
	public ReactiveUserDetailsService userDetailsService() {
		return userRepository::findByUsername;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
