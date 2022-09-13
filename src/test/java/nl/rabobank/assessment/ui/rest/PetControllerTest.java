package nl.rabobank.assessment.ui.rest;

import nl.rabobank.assessment.ui.rest.model.request.PetRequest;
import nl.rabobank.assessment.ui.rest.model.response.PetResponse;
import nl.rabobank.assessment.service.PetService;
import nl.rabobank.assessment.util.ResourceHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PetController.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class PetControllerTest {

	@MockBean
	private PetService petService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testCreatePerson_BadRequest() {
		// Given
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_bad_request.json", PetRequest.class);

		// When
		webTestClient.post()
				.uri(PetController.BASE_URL)
				.bodyValue(petRequest)
				.exchange()

				// Then
				.expectStatus().isBadRequest();
		verify(petService, never()).createPet(any(PetRequest.class));
	}

	@Test
	void testCreatePerson_Success() {
		// Given
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);
		when(petService.createPet(petRequest)).thenReturn(Mono.just(UUID.randomUUID()));

		// When
		webTestClient.post()
				.uri(PetController.BASE_URL)
				.bodyValue(petRequest)
				.exchange()

				// Then
				.expectStatus().isCreated();
		verify(petService).createPet(petRequest);
	}

	@Test
	void testGetAllPersons_Success() {
		// Given
		PetResponse petResponse = ResourceHelper
				.getResourceAsType("json/get_pet_response_success.json", PetResponse.class);
		when(petService.getAllPets()).thenReturn(Flux.just(petResponse));

		// When
		webTestClient.get()
				.uri(PetController.BASE_URL)
				.exchange()

				// Then
				.expectStatus().isOk();
		verify(petService).getAllPets();
	}

	@Test
	void testGetAllPetForPerson() {
		// Given
		UUID personId = UUID.randomUUID();
		PetResponse petResponse = ResourceHelper
				.getResourceAsType("json/get_pet_response_success.json", PetResponse.class);
		when(petService.getPetsByPersonId(personId)).thenReturn(Flux.just(petResponse));

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.queryParam("personId", personId)
						.build())
				.exchange()

				// Then
				.expectStatus().isOk();
		verify(petService).getPetsByPersonId(personId);
	}

	@Test
	void testGetPerson_Success() {
		// Given
		PetResponse petResponse = ResourceHelper
				.getResourceAsType("json/get_pet_response_success.json", PetResponse.class);
		UUID id = UUID.randomUUID();
		when(petService.getPetById(id)).thenReturn(Mono.just(petResponse));

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.pathSegment("{id}")
						.build(id))
				.exchange()

				// Then
				.expectStatus().isOk();
		verify(petService).getPetById(id);
	}

	@Test
	void testUpdatePerson_Success() {
		// Given
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.pathSegment("{id}")
						.build(UUID.randomUUID()))
				.bodyValue(petRequest)
				.exchange()

				// Then
				.expectStatus().isOk();
		verify(petService).updatePet(any(UUID.class), any(PetRequest.class));
	}

	@Test
	void testUpdatePerson_BadRequest() {
		// Given
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_bad_request.json", PetRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.pathSegment("{id}")
						.build(UUID.randomUUID()))
				.bodyValue(petRequest)
				.exchange()

				// Then
				.expectStatus().isBadRequest();

		verify(petService, never()).updatePet(any(UUID.class), any(PetRequest.class));
	}

	@Test
	void testDeletePetById_Success() {
		// Given

		// When
		webTestClient.delete()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.pathSegment("{id}")
						.build(UUID.randomUUID()))
				.exchange()

				// Then
				.expectStatus().isOk();

		verify(petService).deletePetById(any(UUID.class));
	}
}