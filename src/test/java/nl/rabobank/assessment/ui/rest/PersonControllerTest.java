package nl.rabobank.assessment.ui.rest;

import nl.rabobank.assessment.ui.rest.model.request.PersonRequest;
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest;
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse;
import nl.rabobank.assessment.service.PersonService;
import nl.rabobank.assessment.util.ResourceHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PersonController.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class PersonControllerTest {
	
	@MockBean
	private PersonService personService;

	@Autowired
	private WebTestClient webTestClient;
	
	@Test
	void testCreatePerson_BadRequest() {
		// Given
		PersonRequest personRequest = ResourceHelper
				.getResourceAsType("json/create_person_request_bad_request.json", PersonRequest.class);

		// When
		webTestClient.post()
				.uri(PersonController.BASE_URL)
				.bodyValue(personRequest)
				.exchange()

				// Then
				.expectStatus().isBadRequest();

		verify(personService, never()).createPerson(any(PersonRequest.class));
	}

	@Test
	void testCreatePerson_Success() {
		// Given
		PersonRequest personRequest = ResourceHelper
				.getResourceAsType("json/create_person_request_success.json", PersonRequest.class);
		when(personService.createPerson(personRequest)).thenReturn(Mono.just(UUID.randomUUID()));

		// When
		webTestClient.post()
				.uri(PersonController.BASE_URL)
				.bodyValue(personRequest)
				.exchange()

				// Then
				.expectStatus().isCreated();

		verify(personService).createPerson(personRequest);
	}

	@Test
	void testGetAllPersons_Success() {
		// Given
		PersonResponse personResponse = ResourceHelper
				.getResourceAsType("json/get_person_response_success.json", PersonResponse.class);
		when(personService.getAllPeople()).thenReturn(Flux.just(personResponse));

		// When
		webTestClient.get()
				.uri(PersonController.BASE_URL)
				.exchange()

				// Then
				.expectStatus().isOk();
	}

	@ParameterizedTest
	@MethodSource("testGetPersonByName_Success_Parameters")
	void testGetPersonByName_Success(String firstName, String lastName) {
		// Given
		PersonResponse personResponse = ResourceHelper
				.getResourceAsType("json/get_person_response_success.json", PersonResponse.class);
		when(personService.findPersonByName(firstName, lastName)).thenReturn(Mono.just(personResponse));

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("search")
						.queryParam("firstName", firstName)
						.queryParam("lastName", lastName)
						.build())
				.exchange()

				// Then
				.expectStatus().isOk();
	}

	private static Stream<Arguments> testGetPersonByName_Success_Parameters() {
		return Stream.of(
				Arguments.of("firstName", "lastName"),
				Arguments.of(null, "lastName"),
				Arguments.of("firstName", null)
		);
	}

	@Test
	void testGetPersonByName_BadRequest() {
		// Given

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("search")
						.build())
				.exchange()

				// Then
				.expectStatus().isBadRequest();
	}

	@Test
	void testGetPerson_Success() {
		// Given
		PersonResponse personResponse = ResourceHelper
				.getResourceAsType("json/get_person_response_success.json", PersonResponse.class);
		UUID id = UUID.randomUUID();
		when(personService.getPersonById(id)).thenReturn(Mono.just(personResponse));

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("{id}")
						.build(id))
				.exchange()

				// Then
				.expectStatus().isOk();
	}

	@Test
	void testUpdatePerson_Success() {
		// Given
		UpdateAddressRequest addressRequest = ResourceHelper
				.getResourceAsType("json/update_person_request_success.json", UpdateAddressRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("{id}")
						.build(UUID.randomUUID()))
				.bodyValue(addressRequest)
				.exchange()

				// Then
				.expectStatus().isOk();

		verify(personService).updatePersonAddress(any(UUID.class), any(UpdateAddressRequest.class));
	}

	@Test
	void testUpdatePerson_BadRequest() {
		// Given
		UpdateAddressRequest addressRequest = ResourceHelper
				.getResourceAsType("json/update_person_request_bad_request.json", UpdateAddressRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PersonController.BASE_URL)
						.pathSegment("{id}")
						.build(UUID.randomUUID()))
				.bodyValue(addressRequest)
				.exchange()

				// Then
				.expectStatus().isBadRequest();

		verify(personService, never()).updatePersonAddress(any(UUID.class), any(UpdateAddressRequest.class));
	}
}