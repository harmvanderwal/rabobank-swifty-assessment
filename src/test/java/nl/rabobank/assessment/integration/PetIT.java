package nl.rabobank.assessment.integration;

import nl.rabobank.assessment.mapper.EntityMapper;
import nl.rabobank.assessment.persistence.entity.Pet;
import nl.rabobank.assessment.persistence.repository.PersonRepository;
import nl.rabobank.assessment.persistence.repository.PetRepository;
import nl.rabobank.assessment.service.PetService;
import nl.rabobank.assessment.ui.rest.PetController;
import nl.rabobank.assessment.ui.rest.model.request.PetRequest;
import nl.rabobank.assessment.ui.rest.model.response.PetResponse;
import nl.rabobank.assessment.util.ResourceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PetIT extends AbstractIT {

	@SpyBean
	private EntityMapper entityMapper;

	@SpyBean
	private PersonRepository personRepository;

	@SpyBean
	private PetRepository petRepository;

	@SpyBean
	private PetService petService;

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
	void testCreatePet() {
		// Given
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_without_owner_success.json", PetRequest.class);

		// When
		webTestClient.post()
				.uri(PetController.BASE_URL)
				.bodyValue(petRequest)
				.exchange()
				.expectStatus()
				.isCreated();

		// Then
		verify(petService).createPet(petRequest);
		verify(entityMapper).toPet(petRequest);
		verify(petRepository).save(any(Pet.class));
	}

	@Test
	@DirtiesContext
	void testCreatePet_NonExistentOwner() {
		// Given
		UUID personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab");
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);

		// When
		webTestClient.post()
				.uri(PetController.BASE_URL)
				.bodyValue(petRequest)
				.exchange()
				.expectStatus()
				.isBadRequest();

		// Then
		verify(petService).createPet(petRequest);
		verify(personRepository).existsById(personId);
		verify(petRepository, never()).save(any(Pet.class));
	}

	@Test
	@Sql("/sql/insert_pet.sql")
	@DirtiesContext
	void testDeletePet() {
		// Given
		UUID petId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac");

		// When
		webTestClient.delete()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.pathSegment("{petId}")
						.build(petId))
				.exchange()

				// Then
				.expectStatus()
				.isOk();

		verify(petService).deletePetById(petId);
		verify(petRepository).deleteById(petId);
	}

	@Test
	@Sql("/sql/insert_pet.sql")
	@DirtiesContext
	void testGetAllPets() {
		// Given
		List<UUID> allPets = List.of(UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac"),
				UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cad"));

		// When
		webTestClient.get()
				.uri(PetController.BASE_URL)
				.exchange()

				// Then
				.expectStatus()
				.isOk()
				.returnResult(PetResponse.class)
				.getResponseBody()
				.collectList()
				.as(StepVerifier::create)
				.assertNext(list -> assertThat(list.stream()
						.map(PetResponse::id)
						.toList())
						.containsAll(allPets));

		verify(petService).getAllPets();
		verify(petRepository).findAll();
	}

	@Test
	@Sql("/sql/insert_pet.sql")
	@DirtiesContext
	void testGetAllPetsByPersonId() {
		// Given
		UUID personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab");
		UUID petId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac");

		// When
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.queryParam("personId", personId)
						.build())
				.exchange()

				// Then
				.expectStatus()
				.isOk()
				.returnResult(PetResponse.class)
				.getResponseBody()
				.as(StepVerifier::create)
				.assertNext(actual -> assertEquals(petId, actual.id()));

		verify(petService).getPetsByPersonId(personId);
		verify(petRepository).findPetsByPersonId(personId);
	}

	@Test
	@Sql("/sql/insert_pet.sql")
	@DirtiesContext
	void testUpdatePet() {
		// Given
		UUID petId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac");
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);

		// When
		webTestClient.put()
				.uri(uriBuilder -> uriBuilder.path(PetController.BASE_URL)
						.pathSegment("{petId}")
						.build(petId))
				.bodyValue(petRequest)
				.exchange()
				.expectStatus()
				.isOk();

		// Then
		verify(petService).updatePet(petId, petRequest);
		verify(petRepository).save(any(Pet.class));
	}
}
