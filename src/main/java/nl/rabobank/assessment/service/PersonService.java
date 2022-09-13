package nl.rabobank.assessment.service;

import lombok.RequiredArgsConstructor;
import nl.rabobank.assessment.mapper.EntityMapper;
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest;
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest;
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse;
import nl.rabobank.assessment.persistence.entity.Person;
import nl.rabobank.assessment.persistence.repository.PersonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

	private static final ResponseStatusException DUPLICATE_BY_NAME_EXCEPTION =
			new ResponseStatusException(HttpStatus.BAD_REQUEST, "Another person with the same full name is already " +
					"registered.");

	protected static final String NO_PERSON_FOUND_WITH_ID = "No person found with ID %s";

	protected static final String FIRST_NAME_AND_LAST_NAME_CAN_T_BOTH_BE_EMPTY = "firstName and lastName can't both be empty.";

	private static final String NO_PERSON_FOUND_WITH_NAME = "No person found with name: %s";

	private final EntityMapper entityMapper;

	private final PersonRepository personRepository;

	public Mono<UUID> createPerson(PersonRequest personRequest) {
		return personRepository.existsByFirstNameAndLastName(personRequest.firstName(), personRequest.lastName())
				.flatMap(exists -> exists ? Mono.error(DUPLICATE_BY_NAME_EXCEPTION) :
						personRepository.save(entityMapper.toPerson(personRequest))
								.mapNotNull(Person::getId));
	}

	public Mono<PersonResponse> findPersonByName(String firstName, String lastName) {
		if (firstName == null && lastName == null) {
			return Mono.error(new IllegalArgumentException(FIRST_NAME_AND_LAST_NAME_CAN_T_BOTH_BE_EMPTY));
		}
		Mono<Person> personResponseMono;
		if (firstName != null && lastName != null) {
			personResponseMono = personRepository.findPersonByFirstNameAndLastName(firstName, lastName);
		} else if (firstName != null) {
			personResponseMono = personRepository.findFirstByFirstName(firstName);
		} else {
			personResponseMono = personRepository.findFirstByLastName(lastName);
		}
		return personResponseMono
				.map(entityMapper::toPersonResponse)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(NO_PERSON_FOUND_WITH_NAME, String.join(" ", firstName, lastName)))));
	}

	public Flux<PersonResponse> getAllPeople() {
		return personRepository.findAll()
				.map(entityMapper::toPersonResponse);
	}

	public Mono<PersonResponse> getPersonById(UUID id) {
		return personRepository.findById(id)
				.map(entityMapper::toPersonResponse)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(NO_PERSON_FOUND_WITH_ID, id))));
	}

	public Mono<Void> updatePersonAddress(UUID id, UpdateAddressRequest updateAddressRequest) {
		return personRepository.findById(id)
				.map(person -> entityMapper.updatePersonAddress(person, updateAddressRequest))
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(NO_PERSON_FOUND_WITH_ID, id))))
				.flatMap(personRepository::save)
				.then();
	}
}
