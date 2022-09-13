package nl.rabobank.assessment.service;

import nl.rabobank.assessment.mapper.EntityMapper;
import nl.rabobank.assessment.mapper.EntityMapperImpl;
import nl.rabobank.assessment.ui.rest.model.request.PetRequest;
import nl.rabobank.assessment.ui.rest.model.response.PetResponse;
import nl.rabobank.assessment.persistence.entity.Pet;
import nl.rabobank.assessment.persistence.repository.PersonRepository;
import nl.rabobank.assessment.persistence.repository.PetRepository;
import nl.rabobank.assessment.util.ResourceHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

	@Spy
	private EntityMapper entityMapper = new EntityMapperImpl();

	@Mock
	private PersonRepository personRepository;

	@Mock
	private Pet pet;

	@Mock
	private PetRepository petRepository;

	@InjectMocks
	private PetService petService;

	@Test
	void testCreatePet_WithNonExistentOwner() {
		// Given
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);
		when(personRepository.existsById(petRequest.personId())).thenReturn(Mono.just(false));

		// When
		petService.createPet(petRequest)
				.as(StepVerifier::create)

				// Then
				.verifyError(ResponseStatusException.class);
		verify(personRepository).existsById(petRequest.personId());
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testCreatePet_WithOwner() {
		// Given
		UUID expected = UUID.randomUUID();
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);
		when(personRepository.existsById(petRequest.personId())).thenReturn(Mono.just(true));
		when(petRepository.save(any(Pet.class))).thenAnswer(invocationOnMock -> {
			ReflectionTestUtils.setField(invocationOnMock.getArgument(0, Pet.class), "id", expected);
			return Mono.just(invocationOnMock.getArgument(0, Pet.class));
		});

		// When
		petService.createPet(petRequest)
				.as(StepVerifier::create)

				// Then
				.assertNext(actual -> assertEquals(expected, actual))
				.verifyComplete();
		verify(personRepository).existsById(petRequest.personId());
		verify(entityMapper).toPet(petRequest);
		verify(petRepository).save(any(Pet.class));
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testCreatePet_WithoutOwner() {
		// Given
		UUID expected = UUID.randomUUID();
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_without_owner_success.json", PetRequest.class);
		when(petRepository.save(any(Pet.class))).thenAnswer(invocationOnMock -> {
			ReflectionTestUtils.setField(invocationOnMock.getArgument(0, Pet.class), "id", expected);
			return Mono.just(invocationOnMock.getArgument(0, Pet.class));
		});

		// When
		petService.createPet(petRequest)
				.as(StepVerifier::create)

				// Then
				.assertNext(actual -> assertEquals(expected, actual))
				.verifyComplete();
		verify(entityMapper).toPet(petRequest);
		verify(petRepository).save(any(Pet.class));
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testDeletePet_NotFound() {
		// Given
		UUID id = UUID.randomUUID();
		when(petRepository.existsById(id)).thenReturn(Mono.just(false));

		// When
		petService.deletePetById(id)
				.as(StepVerifier::create)

				// Then
				.verifyError();
		verify(petRepository).existsById(id);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testDeletePet_Success() {
		// Given
		UUID id = UUID.randomUUID();
		when(petRepository.existsById(id)).thenReturn(Mono.just(true));
		when(petRepository.deleteById(id)).thenReturn(Mono.empty());

		// When
		petService.deletePetById(id)
				.as(StepVerifier::create)

				// Then
				.verifyComplete();
		verify(petRepository).existsById(id);
		verify(petRepository).deleteById(id);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testGetAllPets() {
		// Given
		when(petRepository.findAll()).thenReturn(Flux.just(pet));
		PetResponse petResponse = entityMapper.toPetResponse(pet);

		// When
		petService.getAllPets()
				.as(StepVerifier::create)

				// Then
				.thenConsumeWhile(petResponse::equals)
				.verifyComplete();
		verify(petRepository).findAll();
		verify(entityMapper, times(2)).toPetResponse(pet);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testGetPetById_Success() {
		// Given
		UUID id = UUID.randomUUID();
		when(petRepository.findById(id)).thenReturn(Mono.just(pet));
		PetResponse expected = entityMapper.toPetResponse(pet);

		// When
		petService.getPetById(id)
				.as(StepVerifier::create)

				// Then
				.assertNext(actual -> assertEquals(expected, actual))
				.verifyComplete();
		verify(petRepository).findById(id);
		verify(entityMapper, times(2)).toPetResponse(pet);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testGetPetById_NotFound() {
		// Given
		UUID id = UUID.randomUUID();
		when(petRepository.findById(id)).thenReturn(Mono.empty());

		// When
		petService.getPetById(id)
				.as(StepVerifier::create)

				// Then
				.verifyError(ResponseStatusException.class);
		verify(petRepository).findById(id);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testUpdatePet_NotFound() {
		// Given
		UUID id = UUID.randomUUID();
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);
		when(petRepository.findById(id)).thenReturn(Mono.empty());

		// When
		petService.updatePet(id, petRequest)
				.as(StepVerifier::create)

				// Then
				.verifyError(ResponseStatusException.class);
		verify(petRepository).findById(id);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testUpdatePet_Success() {
		// Given
		UUID id = UUID.randomUUID();
		PetRequest petRequest = ResourceHelper
				.getResourceAsType("json/create_pet_request_success.json", PetRequest.class);
		when(petRepository.findById(id)).thenReturn(Mono.just(pet));
		when(petRepository.save(pet)).thenReturn(Mono.just(pet));

		// When
		petService.updatePet(id, petRequest)
				.as(StepVerifier::create)

				// Then
				.verifyComplete();
		verify(petRepository).findById(id);
		verify(entityMapper).toPet(pet, petRequest);
		verify(petRepository).save(pet);
		verifyNoMoreInteractions(petRepository, personRepository, entityMapper);
	}

	@Test
	void testGetPetByPersonId_Success() {
		// Given
		UUID personId = UUID.randomUUID();
		when(petRepository.findPetsByPersonId(personId)).thenReturn(Flux.just(pet));
		PetResponse expected = entityMapper.toPetResponse(pet);

		// When
		petService.getPetsByPersonId(personId)
				.as(StepVerifier::create)
				// Then
				.assertNext(actual -> assertEquals(expected, actual))
				.verifyComplete();


	}
}