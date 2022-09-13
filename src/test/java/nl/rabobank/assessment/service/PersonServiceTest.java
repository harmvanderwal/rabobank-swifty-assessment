package nl.rabobank.assessment.service;

import nl.rabobank.assessment.mapper.EntityMapper;
import nl.rabobank.assessment.mapper.EntityMapperImpl;
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest;
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest;
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse;
import nl.rabobank.assessment.persistence.entity.Person;
import nl.rabobank.assessment.persistence.repository.PersonRepository;
import nl.rabobank.assessment.util.ResourceHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PersonServiceTest {

	@Spy
	private EntityMapper entityMapper = new EntityMapperImpl();

	@Mock
	private Person person;

	@Mock
	private PersonRepository personRepository;

	@InjectMocks
	private PersonService personService;

	private static Stream<Arguments> testFindPersonByName_Parameters() {
		return Stream.of(
				Arguments.of("firstName", "lastName"),
				Arguments.of("firstName", null),
				Arguments.of(null, "lastName")
		);
	}

	@Test
	void testCreatePerson_AlreadyExists() {
		// Given
		PersonRequest personRequest = ResourceHelper
				.getResourceAsType("json/create_person_request_success.json", PersonRequest.class);
		when(personRepository.existsByFirstNameAndLastName(anyString(), anyString())).thenReturn(Mono.just(true));

		// When
		personService.createPerson(personRequest)
				.as(StepVerifier::create)

				// Then
				.verifyError(ResponseStatusException.class);
		verify(personRepository).existsByFirstNameAndLastName(anyString(), anyString());
		verify(entityMapper, never()).toPerson(any(PersonRequest.class));
		verify(personRepository, never()).save(any(Person.class));
	}

	@Test
	void testCreatePerson_New() {
		// Given
		PersonRequest personRequest = ResourceHelper
				.getResourceAsType("json/create_person_request_success.json", PersonRequest.class);
		when(personRepository.existsByFirstNameAndLastName(anyString(), anyString())).thenReturn(Mono.just(false));
		when(personRepository.save(any(Person.class))).thenReturn(Mono.just(person));
		UUID id = UUID.randomUUID();
		when(person.getId()).thenReturn(id);

		// When
		personService.createPerson(personRequest)
				.as(StepVerifier::create)

				// Then
				.assertNext(uuid -> assertEquals(id, uuid))
				.verifyComplete();
		verify(personRepository).existsByFirstNameAndLastName(anyString(), anyString());
		verify(entityMapper).toPerson(any(PersonRequest.class));
		verify(personRepository).save(any(Person.class));
	}

	@ParameterizedTest
	@MethodSource("testFindPersonByName_Parameters")
	void testFindPersonByName(String firstName, String lastName) {
		// Given
		lenient().when(personRepository.findFirstByFirstName(firstName)).thenReturn(Mono.just(person));
		lenient().when(personRepository.findFirstByLastName(lastName)).thenReturn(Mono.just(person));
		lenient().when(personRepository.findPersonByFirstNameAndLastName(firstName, lastName)).thenReturn(Mono.just(person));
		PersonResponse expected = entityMapper.toPersonResponse(person);

		// When
		personService.findPersonByName(firstName, lastName)
				.as(StepVerifier::create)

				// Then
				.assertNext(result -> assertEquals(expected, result))
				.verifyComplete();
	}

	@Test
	void testFindPersonByName_IllegalArgument() {
		// Given

		// When
		personService.findPersonByName(null, null)
				.as(StepVerifier::create)

				// Then
				.verifyError(IllegalArgumentException.class);
		verifyNoInteractions(personRepository, entityMapper);
	}

	@Test
	void testGetAllPersons() {
		// Given
		when(personRepository.findAll()).thenReturn(Flux.just(person));
		PersonResponse expected = entityMapper.toPersonResponse(person);

		// When
		personService.getAllPeople()
				.as(StepVerifier::create)

				// Then
				.assertNext(actual -> assertEquals(expected, actual))
				.verifyComplete();
		verify(personRepository).findAll();
		verify(entityMapper, times(2)).toPersonResponse(any(Person.class));
		verifyNoMoreInteractions(personRepository, entityMapper);
	}

	@Test
	void testGetPersonById() {
		// Given
		UUID id = UUID.randomUUID();
		when(personRepository.findById(id)).thenReturn(Mono.just(person));
		PersonResponse expected = entityMapper.toPersonResponse(person);

		// When
		personService.getPersonById(id)
				.as(StepVerifier::create)

				// Then
				.assertNext(actual -> assertEquals(expected, actual))
				.verifyComplete();
		verify(personRepository).findById(id);
		verify(entityMapper, times(2)).toPersonResponse(person);
		verifyNoMoreInteractions(personRepository, entityMapper);
	}

	@Test
	void testUpdatePerson_NoPerson() {
		// Given
		UUID id = UUID.randomUUID();
		UpdateAddressRequest addressRequest = ResourceHelper
				.getResourceAsType("json/update_person_request_success.json", UpdateAddressRequest.class);
		when(personRepository.findById(id)).thenReturn(Mono.empty());

		// When
		personService.updatePersonAddress(id, addressRequest)
				.as(StepVerifier::create)

				// Then
				.verifyError(ResponseStatusException.class);
		verify(personRepository).findById(id);
		verifyNoMoreInteractions(personRepository, entityMapper);
	}

	@Test
	void testUpdatePerson_Success() {
		// Given
		UUID id = UUID.randomUUID();
		UpdateAddressRequest addressRequest = ResourceHelper
				.getResourceAsType("json/update_person_request_success.json", UpdateAddressRequest.class);
		when(personRepository.findById(id)).thenReturn(Mono.just(person));
		when(personRepository.save(person)).thenReturn(Mono.just(person));

		// When
		personService.updatePersonAddress(id, addressRequest)
				.as(StepVerifier::create)

				// Then
				.verifyComplete();
		verify(personRepository).findById(id);
		verify(entityMapper).updatePersonAddress(person, addressRequest);
		verify(personRepository).save(person);
		verifyNoMoreInteractions(personRepository, entityMapper);
	}
}