package nl.rabobank.assessment.persistence.repository;

import nl.rabobank.assessment.persistence.entity.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PersonRepository extends ReactiveCrudRepository<Person, UUID> {

	Mono<Boolean> existsByFirstNameAndLastName(String firstName, String lastName);

	Mono<Person> findFirstByFirstName(String firstName);

	Mono<Person> findPersonByFirstNameAndLastName(String firstName, String lastName);

	Mono<Person> findFirstByLastName(String lastName);
}
