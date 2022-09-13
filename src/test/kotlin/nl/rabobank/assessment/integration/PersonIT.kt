package nl.rabobank.assessment.integration

import nl.rabobank.assessment.mapper.EntityMapper
import nl.rabobank.assessment.persistence.entity.Person
import nl.rabobank.assessment.persistence.repository.PersonRepository
import nl.rabobank.assessment.persistence.repository.PetRepository
import nl.rabobank.assessment.service.PersonService
import nl.rabobank.assessment.ui.rest.PersonController
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse
import nl.rabobank.assessment.util.ResourceHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

class PersonIT : AbstractIT() {

    private val ADMIN_BASIC_HEADER = "Basic YWRtaW46YWRtaW4="

    private val USER_BASIC_HEADER = "Basic dXNlcjp1c2Vy"

    @SpyBean
    protected lateinit var personRepository: PersonRepository

    @SpyBean
    protected lateinit var petRepository: PetRepository

    @SpyBean
    private lateinit var entityMapper: EntityMapper

    @SpyBean
    private lateinit var personService: PersonService

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
    fun testCreatePerson() {
        // Given
        val personRequest = ResourceHelper.getResourceAsType(
                "json/create_person_request_success.json",
            PersonRequest::class.java)

        // When
        webTestClient.post()
            .uri(PersonController.BASE_URL)
            .bodyValue(personRequest)
            .exchange()
            .expectStatus()
            .isCreated
        personRepository.existsByFirstNameAndLastName("Harm", "van der Wal")
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { condition -> Assertions.assertTrue(condition) }
            .verifyComplete()
        verify(personService).createPerson(personRequest)
        verify(personRepository, Mockito.times(2))
            .existsByFirstNameAndLastName(anyString(), anyString())
        verify(entityMapper).toPerson(any())
        verify(personRepository).save(any())
        verifyNoMoreInteractions(personService, entityMapper)
    }

    @Test
    @Sql("/sql/insert_person.sql")
    @DirtiesContext
    fun testGetAllPeople() {
        // Given
        val expected = ResourceHelper.getResourceAsType(
            "json/get_person_response_success.json",
            PersonResponse::class.java)

        // When
        webTestClient.get()
            .uri(PersonController.BASE_URL)
            .exchange() // Then
            .expectStatus().isOk
            .returnResult(PersonResponse::class.java)
            .responseBody
            .`as` { publisher: Flux<PersonResponse> -> StepVerifier.create(publisher) }
            .assertNext { actual: PersonResponse? ->
                Assertions.assertEquals(expected, actual)
                verify(personService).getAllPeople()
                verify(personRepository).findAll()
                verify(entityMapper)
                    .toPersonResponse(any())
                verifyNoMoreInteractions(personService, entityMapper)
            }
            .verifyComplete()
    }

    @Test
    @Sql("/sql/insert_person.sql")
    @DirtiesContext
    fun testGetPersonById() {
        // Given
        val personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab")
        val expected = ResourceHelper.getResourceAsType(
            "json/get_person_response_success.json",
            PersonResponse::class.java)

        // When
        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("{id}")
                    .build(personId) }
            .exchange() // Then
            .expectStatus().isOk
            .returnResult(PersonResponse::class.java)
            .responseBody
            .`as` { publisher -> StepVerifier.create(publisher) }
            .assertNext { actual ->
                Assertions.assertEquals(expected, actual)
                verify(personService).getPersonById(personId)
                verify(personRepository).findById(personId)
                verify(entityMapper)
                    .toPersonResponse(any())
                verifyNoMoreInteractions(personService, entityMapper)
            }
            .verifyComplete()
    }

    @Test
    @Sql("/sql/insert_person.sql")
    @DirtiesContext
    fun testGetPersonByName() {
        // Given
        val firstName = "Harm"
        val lastName = "van der Wal"
        val expected = ResourceHelper.getResourceAsType(
            "json/get_person_response_success.json",
            PersonResponse::class.java)

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("search")
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .build() }
            .exchange() // Then
            .expectStatus().isOk
            .returnResult(PersonResponse::class.java)
            .responseBody
            .`as` { publisher -> StepVerifier.create(publisher) }
            .assertNext { actual ->
                Assertions.assertEquals(expected, actual)
                verify(personService).findPersonByName(firstName, lastName)
                verify(personRepository).findPersonByFirstNameAndLastName(firstName, lastName)
                verify(entityMapper)
                    .toPersonResponse(any())
                verifyNoMoreInteractions(personService, entityMapper)
            }
            .verifyComplete()
    }

    @Test
    @Sql("/sql/insert_person.sql")
    @DirtiesContext
    fun testGetPersonByName_NoResults() {
        // Given
        val firstName = "Hans"
        val lastName = "Klok"

        // When
        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("search")
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .build()
            }
            .exchange() // Then
            .expectStatus().isNotFound
        verify(personService).findPersonByName(firstName, lastName)
        verify(personRepository).findPersonByFirstNameAndLastName(firstName, lastName)
    }

    @Test
    @Sql("/sql/insert_person.sql")
    @DirtiesContext
    fun testUpdatePerson_AdminAuth() {
        // Given
        val personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab")
        val updateAddressRequest = ResourceHelper.getResourceAsType(
            "json/update_person_request_success.json",
            UpdateAddressRequest::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("{id}")
                    .build(personId)
            }
            .headers { httpHeaders: HttpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, ADMIN_BASIC_HEADER) }
            .bodyValue(updateAddressRequest)
            .exchange()
            .expectStatus()
            .isOk

        // Then
        verify(personService).updatePersonAddress(
            any(), any()
        )
        verify(personRepository).findById(personId)
        verify(personRepository).save(any())
        verify(entityMapper)
            .updatePersonAddress(any(), eq(updateAddressRequest))
        verifyNoMoreInteractions(personService, entityMapper)
    }

    @Test
    @Sql("/sql/insert_person.sql")
    @DirtiesContext
    fun testUpdatePerson_UserAuth() {
        // Given
        val personId = UUID.fromString("0fa281f4-9507-40dd-9165-7d6f49631cab")
        val updateAddressRequest = ResourceHelper.getResourceAsType(
            "json/update_person_request_success.json",
            UpdateAddressRequest::class.java)

        // When
        webTestClient.put()
            .uri { uriBuilder: UriBuilder -> uriBuilder.path(PersonController.BASE_URL)
                    .pathSegment("{id}")
                    .build(personId) }
            .headers { httpHeaders ->
                httpHeaders.add(HttpHeaders.AUTHORIZATION, USER_BASIC_HEADER) }
            .bodyValue(updateAddressRequest)
            .exchange()
            .expectStatus()
            .isForbidden

        // Then
        verifyNoMoreInteractions(personRepository)
        Mockito.verifyNoInteractions(personService, entityMapper)
    }
}