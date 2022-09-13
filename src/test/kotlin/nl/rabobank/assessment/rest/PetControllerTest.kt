package nl.rabobank.assessment.rest

import com.fasterxml.jackson.databind.JsonNode
import nl.rabobank.assessment.service.PetService
import nl.rabobank.assessment.ui.rest.PetController
import nl.rabobank.assessment.ui.rest.model.request.PetRequest
import nl.rabobank.assessment.ui.rest.model.response.PetResponse
import nl.rabobank.assessment.util.ResourceHelper.Companion.getResourceAsType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@WebFluxTest(
    controllers = [PetController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
class PetControllerTest {

    @MockBean
    private lateinit var petService: PetService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun testCreatePet_BadRequest() {
        // Given
        val petRequest = getResourceAsType(
            "json/create_pet_request_bad_request.json",
            JsonNode::class.java)

        // When
        webTestClient.post()
            .uri(PetController.BASE_URL)
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus().isBadRequest
        Mockito.verify(petService, Mockito.never()).createPet(any())
    }

    @Test
    fun testCreatePet_Success() {
        // Given
        val petRequest = getResourceAsType(
            "json/create_pet_request_success.json",
            PetRequest::class.java)
        Mockito.`when`(petService.createPet(petRequest)).thenReturn(Mono.just(UUID.randomUUID()))

        // When
        webTestClient.post()
            .uri(PetController.BASE_URL)
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus().isCreated
        Mockito.verify(petService).createPet(petRequest)
    }

    @Test
    fun testGetAllPets_Success() {
        // Given
        val petResponse = getResourceAsType(
            "json/get_pet_response_success.json",
            PetResponse::class.java)
        Mockito.`when`(petService.getAllPets()).thenReturn(Flux.just(petResponse))

        // When
        webTestClient.get()
            .uri(PetController.BASE_URL)
            .exchange()

            // Then
            .expectStatus().isOk
        Mockito.verify(petService).getAllPets()
    }

    @Test
    fun testGetAllPetForPerson() {
        // Given
        val personId = UUID.randomUUID()
        val petResponse = getResourceAsType(
            "json/get_pet_response_success.json",
            PetResponse::class.java)
        Mockito.`when`(petService.getPetsByPersonId(personId)).thenReturn(Flux.just(petResponse))

        // When
        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .queryParam("personId", personId)
                    .build()
            }
            .exchange()

            // Then
            .expectStatus().isOk
        Mockito.verify(petService).getPetsByPersonId(personId)
    }

    @Test
    fun testGetPet_Success() {
        // Given
        val petResponse = getResourceAsType(
            "json/get_pet_response_success.json",
            PetResponse::class.java)
        val id = UUID.randomUUID()
        Mockito.`when`(petService.getPetById(id)).thenReturn(Mono.just(petResponse))

        // When
        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .pathSegment("{id}")
                    .build(id)
            }
            .exchange()

            // Then
            .expectStatus().isOk
        Mockito.verify(petService).getPetById(id)
    }

    @Test
    fun testUpdatePet_Success() {
        // Given
        val petRequest = getResourceAsType(
            "json/create_pet_request_success.json",
            PetRequest::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .pathSegment("{id}")
                    .build(UUID.randomUUID())
            }
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus().isOk
        Mockito.verify(petService).updatePet(
            any(), any()
        )
    }

    @Test
    fun testUpdatePet_BadRequest() {
        // Given
        val petRequest = getResourceAsType(
            "json/create_pet_request_bad_request.json",
            JsonNode::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .pathSegment("{id}")
                    .build(UUID.randomUUID())
            }
            .bodyValue(petRequest)
            .exchange()

            // Then
            .expectStatus().isBadRequest
        Mockito.verify(petService, Mockito.never()).updatePet(
            any(), any()
        )
    }

    @Test
    fun testDeletePetById_Success() {
        // Given

        // When
        webTestClient.delete()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PetController.BASE_URL)
                    .pathSegment("{id}")
                    .build(UUID.randomUUID())
            }
            .exchange()

            // Then
            .expectStatus().isOk
        Mockito.verify(petService).deletePetById(any())
    }
}