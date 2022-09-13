package nl.rabobank.assessment.config

import nl.rabobank.assessment.persistence.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(private val userRepository: UserRepository) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers(HttpMethod.PUT, "/v1/person/*").hasRole("ADMIN")
            .anyExchange()
            .permitAll()
            .and()
            .httpBasic()
        return http.build()
    }

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService? {
        return ReactiveUserDetailsService { username: String -> userRepository.findByUsername(username) }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder? {
        return BCryptPasswordEncoder()
    }
}