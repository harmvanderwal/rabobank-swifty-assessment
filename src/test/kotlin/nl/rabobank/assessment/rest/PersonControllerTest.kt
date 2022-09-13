package nl.rabobank.assessment.rest

import com.fasterxml.jackson.databind.JsonNode
import nl.rabobank.assessment.service.PersonService
import nl.rabobank.assessment.ui.rest.PersonController
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse
import nl.rabobank.assessment.util.ResourceHelper
import nl.rabobank.assessment.util.ResourceHelper.Companion.getResourceAsType
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Stream

@WebFluxTest(
    controllers = [PersonController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
class PersonControllerTest {

    companion object {
        @JvmStatic private fun testGetPersonByName_Success_Parameters(): Stream<Arguments?>? {
            return Stream.of(
                Arguments.of("firstName", "lastName"),
                Arguments.of(null, "lastName"),
                Arguments.of("firstName", null)
            )
        }
    }

    @MockBean
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun testCreatePerson_BadRequest() {
        // Given
        val personRequest = getResourceAsType(
            "json/create_person_request_bad_request.json",
            JsonNode::class.java)

        // When
        webTestClient.post()
            .uri(PersonController.BASE_URL)
            .bodyValue(personRequest)
            .exchange() // Then
            .expectStatus().isBadRequest
        verify(personService, Mockito.never()).createPerson(any())
    }

    @Test
    fun testCreatePerson_Success() {
        // Given
        val personRequest = getResourceAsType(
            "json/create_person_request_success.json",
            PersonRequest::class.java)
        Mockito.`when`(personService.createPerson(personRequest)).thenReturn(Mono.just(UUID.randomUUID()))

        // When
        webTestClient.post()
            .uri(PersonController.BASE_URL)
            .bodyValue(personRequest)
            .exchange() // Then
            .expectStatus().isCreated
        verify(personService).createPerson(personRequest)
    }

    @Test
    fun testGetAllPersons_Success() {
        // Given
        val personResponse = getResourceAsType(
            "json/get_person_response_success.json",
            PersonResponse::class.java)
        Mockito.`when`(personService.getAllPeople()).thenReturn(Flux.just(personResponse))

        // When
        webTestClient.get()
            .uri(PersonController.BASE_URL)
            .exchange() // Then
            .expectStatus().isOk
    }

    @ParameterizedTest
    @MethodSource("testGetPersonByName_Success_Parameters")
    fun testGetPersonByName_Success(firstName: String?, lastName: String?) {
        // Given
        val personResponse = getResourceAsType(
            "json/get_person_response_success.json",
            PersonResponse::class.java)
        Mockito.`when`(personService!!.findPersonByName(firstName, lastName)).thenReturn(Mono.just(personResponse))

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("search")
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .build()
            }
            .exchange() // Then
            .expectStatus().isOk
    }

    @Test
    fun testGetPersonByName_BadRequest() {
        // Given

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("search")
                    .build()
            }
            .exchange()

            // Then
            .expectStatus().isBadRequest
    }

    @Test
    fun testGetPerson_Success() {
        // Given
        val personResponse = getResourceAsType(
            "json/get_person_response_success.json",
            PersonResponse::class.java)
        val id = UUID.randomUUID()
        Mockito.`when`(personService.getPersonById(id)).thenReturn(Mono.just(personResponse))

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("{id}")
                    .build(id)
            }
            .exchange() // Then
            .expectStatus().isOk
    }

    @Test
    fun testUpdatePerson_Success() {
        // Given
        val addressRequest = getResourceAsType(
            "json/update_person_request_success.json",
            UpdateAddressRequest::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("{id}")
                    .build(UUID.randomUUID())
            }
            .bodyValue(addressRequest)
            .exchange()

            // Then
            .expectStatus().isOk
        verify(personService).updatePersonAddress(any(), any())
    }

    @Test
    fun testUpdatePerson_BadRequest() {
        // Given
        val addressRequest = getResourceAsType(
            "json/update_person_request_bad_request.json",
            JsonNode::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("{id}")
                    .build(UUID.randomUUID())
            }
            .bodyValue(addressRequest)
            .exchange() // Then
            .expectStatus().isBadRequest
        verify(personService, Mockito.never()).updatePersonAddress(any(), any())
    }
}