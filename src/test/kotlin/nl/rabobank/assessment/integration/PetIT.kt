package nl.rabobank.assessment.integration

import nl.rabobank.assessment.mapper.EntityMapper
import nl.rabobank.assessment.persistence.entity.Pet
import nl.rabobank.assessment.persistence.repository.PersonRepository
import nl.rabobank.assessment.persistence.repository.PetRepository
import nl.rabobank.assessment.service.PetService
import nl.rabobank.assessment.ui.rest.PetController
import nl.rabobank.assessment.ui.rest.model.request.PetRequest
import nl.rabobank.assessment.ui.rest.model.response.PetResponse
import nl.rabobank.assessment.util.ResourceHelper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.StepVerifier.FirstStep
import java.util.*

class PetIT : AbstractIT() {

    @SpyBean
    private lateinit var entityMapper: EntityMapper

    @SpyBean
    private lateinit var personRepository: PersonRepository

    @SpyBean
    private lateinit var petRepository: PetRepository

    @SpyBean
    private lateinit var petService: PetService

    @Captor
    private lateinit var petCaptor: ArgumentCaptor<Pet>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @AfterEach
    fun tearDown() {
        petRepository.deleteAll()
            .then(personRepository.deleteAll())
            .block()
    }

    @Test
    @DirtiesContext
    fun testCreatePet() {
        // Given
        val petRequest = ResourceHelper
            .getResourceAsType("json/create_pet_request_without_owner_success.json", PetRequest::class.java)

        // When
        webTestClient.post()
            .uri(PetController.BASE_URL)
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus()
            .isCreated
        verify(petService).createPet(petRequest)
        verify(entityMapper).toPet(petRequest)
        verify(petRepository).save(ArgumentMatchers.any(Pet::class.java))
    }

    @Test
    @DirtiesContext
    fun testCreatePet_NonExistentOwner() {
        // Given
        val personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab")
        val petRequest = ResourceHelper
            .getResourceAsType("json/create_pet_request_success.json", PetRequest::class.java)

        // When
        webTestClient.post()
            .uri(PetController.BASE_URL)
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus()
            .isBadRequest
        verify(petService).createPet(petRequest)
        verify(personRepository).existsById(personId)
        verify(petRepository, never()).save(ArgumentMatchers.any(Pet::class.java))
    }

    @Test
    @DirtiesContext
    @Sql("/sql/insert_person.sql")
    fun testCreatePet_WithOwner() {
        // Given
        val personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab")
        val petRequest = ResourceHelper
            .getResourceAsType("json/create_pet_request_success.json", PetRequest::class.java)

        // When
        webTestClient.post()
            .uri(PetController.BASE_URL)
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus()
            .isCreated
        verify(petService).createPet(petRequest)
        verify(personRepository).existsById(personId)
        verify(petRepository).save(petCaptor.capture())
        println(petCaptor.value)
    }

    @Test
    @Sql("/sql/insert_pet.sql")
    @DirtiesContext
    fun testDeletePet() {
        // Given
        val petId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac")

        // When
        webTestClient.delete()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .pathSegment("{petId}")
                    .build(petId)
            }
            .exchange()

            // Then
            .expectStatus()
            .isOk
        verify(petService).deletePetById(petId)
        verify(petRepository).deleteById(petId)
    }

    @Test
    @Sql("/sql/insert_pet.sql")
    @DirtiesContext
    fun testGetAllPets() {
        // Given
        val allPets = listOf(
            UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac"),
            UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cad")
        )

        // When
        webTestClient.get()
            .uri(PetController.BASE_URL)
            .exchange() // Then
            .expectStatus()
            .isOk
            .returnResult(PetResponse::class.java)
            .responseBody
            .collectList()
            .`as`<FirstStep<List<PetResponse>>> { publisher ->StepVerifier.create(publisher) }
            .assertNext { list: List<PetResponse> ->
                Assertions.assertThat(list.stream().map(PetResponse::id).toList()).containsAll(allPets) }
        verify(petService).getAllPets()
        verify(petRepository).findAll()
    }

    @Test
    @Sql("/sql/insert_pet.sql")
    @DirtiesContext
    fun testGetAllPetsByPersonId() {
        // Given
        val personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab")
        val petId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac")

        // When
        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .queryParam("personId", personId)
                    .build()
            }
            .exchange() // Then
            .expectStatus()
            .isOk
            .returnResult(PetResponse::class.java)
            .responseBody
            .`as` { publisher -> StepVerifier.create(publisher) }
            .assertNext { petResponse ->
                assertEquals(petId, petResponse.id)
            }
            .verifyComplete()
        verify(petService).getPetsByPersonId(personId)
        verify(petRepository).findPetsByPersonId(personId)
    }

    @Test
    @Sql("/sql/insert_pet.sql")
    @DirtiesContext
    fun testUpdatePet() {
        // Given
        val petId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cac")
        val petRequest = ResourceHelper
            .getResourceAsType("json/create_pet_request_success.json", PetRequest::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .pathSegment("{petId}")
                    .build(petId)
            }
            .bodyValue(petRequest)
            .exchange()
            .expectStatus()
            .isOk

        // Then
        verify(petService).updatePet(petId, petRequest)
        verify(petRepository).save(ArgumentMatchers.any(Pet::class.java))
    }
}