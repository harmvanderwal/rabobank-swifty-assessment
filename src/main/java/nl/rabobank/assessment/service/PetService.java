package nl.rabobank.assessment.service;

import lombok.RequiredArgsConstructor;
import nl.rabobank.assessment.mapper.EntityMapper;
import nl.rabobank.assessment.ui.rest.model.request.PetRequest;
import nl.rabobank.assessment.ui.rest.model.response.PetResponse;
import nl.rabobank.assessment.persistence.entity.Pet;
import nl.rabobank.assessment.persistence.repository.PersonRepository;
import nl.rabobank.assessment.persistence.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PetService {

	protected static final String NO_PET_FOUND_WITH_ID = "No pet found with ID %s";

	private static final ResponseStatusException NO_PERSON_WITH_THAT_ID_AVAILABLE =
			new ResponseStatusException(HttpStatus.BAD_REQUEST, "No person with ID %s is registered. Try registering" +
					" your pet to an actual person.");

	private final EntityMapper entityMapper;

	private final PersonRepository personRepository;

	private final PetRepository petRepository;

	public Mono<UUID> createPet(PetRequest petRequest) {
		if (petRequest.personId() != null) {
			return personRepository.existsById(petRequest.personId())
					.flatMap(exists -> exists ? petRepository.save(entityMapper.toPet(petRequest))
							.mapNotNull(Pet::getId)
									: Mono.error(NO_PERSON_WITH_THAT_ID_AVAILABLE));
		}
		return petRepository.save(entityMapper.toPet(petRequest))
				.mapNotNull(Pet::getId);
	}

	public Mono<Void> deletePetById(UUID id) {
		return petRepository.existsById(id)
				.flatMap(exists -> exists ? petRepository.deleteById(id) :
						Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
								String.format(NO_PET_FOUND_WITH_ID, id))));
	}

	public Flux<PetResponse> getAllPets() {
		return petRepository.findAll()
				.map(entityMapper::toPetResponse);
	}

	public Mono<PetResponse> getPetById(UUID petId) {
		return petRepository.findById(petId)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(NO_PET_FOUND_WITH_ID, petId))))
				.mapNotNull(entityMapper::toPetResponse);
	}

	public Flux<PetResponse> getPetsByPersonId(UUID personId) {
		return petRepository.findPetsByPersonId(personId)
				.mapNotNull(entityMapper::toPetResponse);
	}

	public Mono<Void> updatePet(UUID id, PetRequest petRequest) {
		return petRepository.findById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(NO_PET_FOUND_WITH_ID, id))))
				.map(pet -> entityMapper.toPet(pet, petRequest))
				.flatMap(petRepository::save)
				.then();
	}
}
