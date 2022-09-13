package nl.rabobank.assessment.persistence.repository;

import nl.rabobank.assessment.persistence.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

	@Query("SELECT * FROM \"user\" WHERE \"user\".username = $1")
	Mono<UserDetails> findByUsername(String username);
}
