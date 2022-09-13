package nl.rabobank.assessment.integration;

import nl.rabobank.assessment.mapper.EntityMapper;
import nl.rabobank.assessment.persistence.entity.Pet;
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest;
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest;
import nl.rabobank.assessment.persistence.entity.Person;
import nl.rabobank.assessment.persistence.repository.PersonRepository;
import nl.rabobank.assessment.persistence.repository.PetRepository;
import nl.rabobank.assessment.service.PersonService;
import nl.rabobank.assessment.ui.rest.PersonController;
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse;
import nl.rabobank.assessment.util.ResourceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@DirtiesContext
class PersonIT extends AbstractIT {

	private static final String ADMIN_BASIC_HEADER = "Basic YWRtaW46YWRtaW4=";

	private static final String USER_BASIC_HEADER = "Basic dXNlcjp1c2Vy";

	@SpyBean
	protected PersonRepository personRepository;

	@SpyBean
	protected PetRepository petRepository;

	@SpyBean
	private EntityMapper entityMapper;

	@SpyBean
	private PersonService personService;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void tearDown() {
		petRepository.deleteAll()
				.then(personRepository.deleteAll())
				.block();
	}

	@Test
	@DirtiesContext
	void testCreatePerson() {
		// Given
		PersonRequest personRequest = ResourceHelper
				.getResourceAsType("json/create_person_request_success.json", PersonRequest.class);

		// When
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL).build())
				.bodyValue(personRequest)
				.exchange()
				.expectStatus()
				.isCreated();

		personRepository.existsByFirstNameAndLastName("Harm", "van der Wal")
				.as(StepVerifier::create)
				.assertNext(Assertions::assertTrue)
				.verifyComplete();

		// Then
		verify(personService).createPerson(personRequest);
		verify(personRepository, times(2)).existsByFirstNameAndLastName(anyString(), anyString());
		verify(entityMapper).toPerson(any(PersonRequest.class));
		verify(personRepository).save(any(Person.class));
		verifyNoMoreInteractions(personService, entityMapper);
	}

	@Test
	@Sql("/sql/insert_person.sql")
	@DirtiesContext
	void testGetAllPeople() {
		// Given
		PersonResponse expected = ResourceHelper.getResourceAsType("json/get_person_response_success.json",
				PersonResponse.class);

		// When
		webTestClient.get()
				.uri(PersonController.BASE_URL)
				.exchange()

				// Then
				.expectStatus().isOk()
				.returnResult(PersonResponse.class)
				.getResponseBody()
				.as(StepVerifier::create)
				.assertNext(actual -> {
					assertEquals(expected, actual);
					verify(personService).getAllPeople();
					verify(personRepository).findAll();
					verify(entityMapper).toPersonResponse(any(Person.class));
					verifyNoMoreInteractions(personService, entityMapper);
				})
				.verifyComplete();
	}

	@Test
	@Sql("/sql/insert_person.sql")
	@DirtiesContext
	void testGetPersonById() {
		// Given
		UUID personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab");
		PersonResponse expected = ResourceHelper.getResourceAsType("json/get_person_response_success.json",
				PersonResponse.class);

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("{id}")
						.build(personId))
				.exchange()

				// Then
				.expectStatus().isOk()
				.returnResult(PersonResponse.class)
				.getResponseBody()
				.as(StepVerifier::create)
				.assertNext(actual -> {
					assertEquals(expected, actual);
					verify(personService).getPersonById(personId);
					verify(personRepository).findById(personId);
					verify(entityMapper).toPersonResponse(any(Person.class));
					verifyNoMoreInteractions(personService, entityMapper);
				})
				.verifyComplete();
	}

	@Test
	@Sql("/sql/insert_person.sql")
	@DirtiesContext
	void testGetPersonByName() {
		// Given
		String firstName = "Harm";
		String lastName = "van der Wal";
		PersonResponse expected = ResourceHelper.getResourceAsType("json/get_person_response_success.json",
				PersonResponse.class);

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("search")
						.queryParam("firstName", firstName)
						.queryParam("lastName", lastName)
						.build())
				.exchange()

				// Then
				.expectStatus().isOk()
				.returnResult(PersonResponse.class)
				.getResponseBody()
				.as(StepVerifier::create)
				.assertNext(actual -> {
					assertEquals(expected, actual);
					verify(personService).findPersonByName(firstName, lastName);
					verify(personRepository).findPersonByFirstNameAndLastName(firstName, lastName);
					verify(entityMapper).toPersonResponse(any(Person.class));
					verifyNoMoreInteractions(personService, entityMapper);
				})
				.verifyComplete();
	}

	@Test
	@Sql("/sql/insert_person.sql")
	@DirtiesContext
	void testGetPersonByName_NoResults() {
		// Given
		String firstName = "Hans";
		String lastName = "Klok";

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("search")
						.queryParam("firstName", firstName)
						.queryParam("lastName", lastName)
						.build())
				.exchange()

				// Then
				.expectStatus().isNotFound();
		verify(personService).findPersonByName(firstName, lastName);
		verify(personRepository).findPersonByFirstNameAndLastName(firstName, lastName);
	}

	@Test
	@Sql("/sql/insert_person.sql")
	@DirtiesContext
	void testUpdatePerson_AdminAuth() {
		// Given
		UUID personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab");
		UpdateAddressRequest updateAddressRequest = ResourceHelper
				.getResourceAsType("json/update_person_request_success.json", UpdateAddressRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("{id}")
						.build(personId))
				.headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, ADMIN_BASIC_HEADER))
				.bodyValue(updateAddressRequest)
				.exchange()
				.expectStatus()
				.isOk();

		// Then
		verify(personService).updatePersonAddress(any(UUID.class), any(UpdateAddressRequest.class));
		verify(personRepository).findById(personId);
		verify(personRepository).save(any(Person.class));
		verify(entityMapper).updatePersonAddress(any(Person.class), eq(updateAddressRequest));
		verifyNoMoreInteractions(personService, entityMapper);
	}

	@Test
	@Sql("/sql/insert_person.sql")
	@DirtiesContext
	void testUpdatePerson_UserAuth() {
		// Given
		UUID personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab");
		UpdateAddressRequest updateAddressRequest = ResourceHelper
				.getResourceAsType("json/update_person_request_success.json", UpdateAddressRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("{id}")
						.build(personId))
				.headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, USER_BASIC_HEADER))
				.bodyValue(updateAddressRequest)
				.exchange()
				.expectStatus()
				.isForbidden();

		// Then
		verifyNoMoreInteractions(personRepository);
		verifyNoInteractions(personService, entityMapper);
	}
}
