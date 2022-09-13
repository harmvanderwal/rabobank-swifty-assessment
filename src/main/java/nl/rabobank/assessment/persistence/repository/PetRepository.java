package nl.rabobank.assessment.persistence.repository;

import nl.rabobank.assessment.persistence.entity.Pet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PetRepository extends ReactiveCrudRepository<Pet, UUID> {

	Flux<Pet> findPetsByPersonId(UUID personId);
}
