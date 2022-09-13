package nl.rabobank.assessment.service

import nl.rabobank.assessment.mapper.EntityMapper
import nl.rabobank.assessment.mapper.EntityMapperImpl
import nl.rabobank.assessment.persistence.entity.Pet
import nl.rabobank.assessment.persistence.repository.PersonRepository
import nl.rabobank.assessment.persistence.repository.PetRepository
import nl.rabobank.assessment.ui.rest.model.request.PetRequest
import nl.rabobank.assessment.ui.rest.model.response.PetResponse
import nl.rabobank.assessment.util.ResourceHelper.Companion.getResourceAsType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.StepVerifier.FirstStep
import java.util.*

@ExtendWith(MockitoExtension::class)
class PetServiceTest {

    @Spy
    private val entityMapper: EntityMapper = EntityMapperImpl()

    @Mock
    private lateinit var personRepository: PersonRepository

    private var pet = Pet(UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab"), "Dog", 12)

    @Mock
    private lateinit var petRepository: PetRepository

    @InjectMocks
    private lateinit var petService: PetService

    @Test
    fun testCreatePet_WithNonExistentOwner() {
        // Given
        val petRequest = getResourceAsType(
            "json/create_pet_request_success.json",
            PetRequest::class.java)
        `when`(Objects.requireNonNull(petRequest.personId)?.let { personRepository.existsById(it) })
            .thenReturn(Mono.just(false))

        // When
        petService.createPet(petRequest)
            .`as`{ publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError(ResponseStatusException::class.java)
        verify(personRepository).existsById(petRequest.personId!!)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testCreatePet_WithOwner() {
        // Given
        val expected = UUID.randomUUID()
        val petRequest = getResourceAsType(
            "json/create_pet_request_success.json",
            PetRequest::class.java)

        `when`(personRepository.existsById(petRequest.personId!!)).thenReturn(Mono.just(true))
        `when`(petRepository.save(any()))
            .thenAnswer { invocationOnMock ->
                ReflectionTestUtils.setField(invocationOnMock.getArgument(0, Pet::class.java), "id", expected)
                Mono.just(invocationOnMock.getArgument(0, Pet::class.java))
            }

        // When
        petService.createPet(petRequest)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { actual -> Assertions.assertEquals(expected, actual) }
            .verifyComplete()
        verify(personRepository).existsById(petRequest.personId!!)
        verify(entityMapper).toPet(petRequest)
        verify(petRepository).save(ArgumentMatchers.any(Pet::class.java))
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testCreatePet_WithoutOwner() {
        // Given
        val expected = UUID.randomUUID()
        val petRequest = getResourceAsType(
            "json/create_pet_request_without_owner_success.json",
            PetRequest::class.java)
        `when`(petRepository.save(any()))
            .thenAnswer { invocationOnMock: InvocationOnMock ->
                ReflectionTestUtils.setField(invocationOnMock.getArgument(0, Pet::class.java), "id", expected)
                Mono.just(invocationOnMock.getArgument(0, Pet::class.java))
            }

        // When
        petService.createPet(petRequest)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { actual: UUID? -> Assertions.assertEquals(expected, actual) }
            .verifyComplete()
        verify(entityMapper).toPet(petRequest)
        verify(petRepository).save(ArgumentMatchers.any(Pet::class.java))
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testDeletePet_NotFound() {
        // Given
        val id = UUID.randomUUID()
        `when`(petRepository.existsById(id)).thenReturn(Mono.just(false))

        // When
        petService.deletePetById(id)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError()
        verify(petRepository).existsById(id)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testDeletePet_Success() {
        // Given
        val id = UUID.randomUUID()
        `when`(petRepository.existsById(id)).thenReturn(Mono.just(true))
        `when`(petRepository.deleteById(id)).thenReturn(Mono.empty())

        // When
        petService.deletePetById(id)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyComplete()
        verify(petRepository).existsById(id)
        verify(petRepository).deleteById(id)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testGetAllPets() {
        // Given
        `when`(petRepository.findAll()).thenReturn(Flux.just(pet))
        val petResponse = entityMapper.toPetResponse(pet)

        // When
        petService.getAllPets()
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .thenConsumeWhile { current -> current.equals(petResponse) }
            .verifyComplete()
        verify(petRepository).findAll()
        verify(entityMapper, times(2)).toPetResponse(pet)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testGetPetById_Success() {
        // Given
        val id = UUID.randomUUID()
        `when`(petRepository.findById(id)).thenReturn(Mono.just(pet))
        val expected = entityMapper.toPetResponse(pet)

        // When
        petService.getPetById(id)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { actual -> Assertions.assertEquals(expected, actual) }
            .verifyComplete()
        verify(petRepository).findById(id)
        verify(entityMapper, times(2)).toPetResponse(pet)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testGetPetById_NotFound() {
        // Given
        val id = UUID.randomUUID()
        `when`(petRepository.findById(id)).thenReturn(Mono.empty())

        // When
        petService.getPetById(id)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError(ResponseStatusException::class.java)
        verify(petRepository).findById(id)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testUpdatePet_NotFound() {
        // Given
        val id = UUID.randomUUID()
        val petRequest = getResourceAsType(
            "json/create_pet_request_success.json",
            PetRequest::class.java)
        `when`(petRepository.findById(id)).thenReturn(Mono.empty())

        // When
        petService.updatePet(id, petRequest)
            .`as`<FirstStep<Void>> { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError(ResponseStatusException::class.java)
        verify(petRepository).findById(id)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testUpdatePet_Success() {
        // Given
        val id = UUID.randomUUID()
        val petRequest = getResourceAsType(
            "json/create_pet_request_success.json",
            PetRequest::class.java)
        `when`(petRepository.findById(id)).thenReturn(Mono.just(pet))
        `when`(petRepository.save(pet)).thenReturn(Mono.just(pet))

        // When
        petService.updatePet(id, petRequest)
            .`as`<FirstStep<Void>> { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyComplete()
        verify(petRepository).findById(id)
        verify(entityMapper).toPet(pet, petRequest)
        verify(petRepository).save(pet)
        verifyNoMoreInteractions(petRepository, personRepository, entityMapper)
    }

    @Test
    fun testGetPetByPersonId_Success() {
        // Given
        val personId = UUID.randomUUID()
        `when`(petRepository.findPetsByPersonId(personId)).thenReturn(Flux.just(pet))
        val expected = entityMapper.toPetResponse(pet)

        // When
        petService.getPetsByPersonId(personId)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { actual: PetResponse? -> Assertions.assertEquals(expected, actual) }
            .verifyComplete()
    }
}