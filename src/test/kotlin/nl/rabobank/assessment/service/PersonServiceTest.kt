package nl.rabobank.assessment.service

import nl.rabobank.assessment.mapper.EntityMapper
import nl.rabobank.assessment.mapper.EntityMapperImpl
import nl.rabobank.assessment.persistence.entity.Person
import nl.rabobank.assessment.persistence.repository.PersonRepository
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse
import nl.rabobank.assessment.util.ResourceHelper.Companion.getResourceAsType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.util.*
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {

    @Spy
    private val entityMapper: EntityMapper = EntityMapperImpl()

    private var person = Person(UUID.fromString("02289f75-b304-419b-b248-91cea8ece639"),
        "firstName",
        "lastName",
        LocalDate.now(),
        "street",
        12,
        null,
        "9874ES",
        "Alblasserdam",
        "Netherlands")

    @Mock
    private lateinit var personRepository: PersonRepository

    @InjectMocks
    private lateinit var personService: PersonService

    companion object {
        @JvmStatic fun testFindPersonByName_Parameters(): Stream<Arguments?>? {
            return Stream.of(
                Arguments.of("firstName", "lastName"),
                Arguments.of("firstName", null),
                Arguments.of(null, "lastName")
            )
        }
    }


    @Test
    fun testCreatePerson_AlreadyExists() {
        // Given
        val personRequest = getResourceAsType(
            "json/create_person_request_success.json",
            PersonRequest::class.java)
        `when`(
            personRepository.existsByFirstNameAndLastName(
                anyString(),
                anyString()
            )
        ).thenReturn(Mono.just(true))

        // When
        personService.createPerson(personRequest)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError(ResponseStatusException::class.java)
        verify(personRepository).existsByFirstNameAndLastName(anyString(), anyString())
        verify(entityMapper, never()).toPerson(any())
        verify(personRepository, never()).save(any())
    }

    @Test
    fun testCreatePerson_New() {
        // Given
        val personRequest = getResourceAsType(
            "json/create_person_request_success.json",
            PersonRequest::class.java)
        `when`(personRepository.existsByFirstNameAndLastName(anyString(), anyString())).thenReturn(Mono.just(false))
        `when`(personRepository.save(any())).thenReturn(Mono.just(person))

        // When
        personService.createPerson(personRequest)
            .`as` { publisher ->StepVerifier.create(publisher) }

            // Then
            .assertNext { uuid -> Assertions.assertEquals(person.id, uuid) }
            .verifyComplete()
        verify(personRepository).existsByFirstNameAndLastName(anyString(), anyString())
        verify(entityMapper).toPerson(any())
        verify(personRepository).save(any())
    }

    @ParameterizedTest
    @MethodSource("testFindPersonByName_Parameters")
    fun testFindPersonByName(firstName: String?, lastName: String?) {
        // Given
        if (firstName != null && lastName != null) {
            `when`(personRepository.findPersonByFirstNameAndLastName(firstName, lastName))
                .thenReturn(Mono.just(person))
        } else if (firstName != null) {
            `when`(personRepository.findFirstByFirstName(firstName))
                .thenReturn(Mono.just(person))
        } else if (lastName != null) {
            `when`(personRepository.findFirstByLastName(lastName))
                .thenReturn(Mono.just(person))
        }
        val expected = entityMapper.toPersonResponse(person)

        // When
        personService.findPersonByName(firstName, lastName)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { result: PersonResponse? ->
                Assertions.assertEquals(
                    expected,
                    result
                )
            }
            .verifyComplete()
    }

    @Test
    fun testFindPersonByName_IllegalArgument() {
        // Given

        // When
        personService.findPersonByName(null, null)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError(IllegalArgumentException::class.java)
        verifyNoInteractions(personRepository, entityMapper)
    }

    @Test
    fun testGetAllPersons() {
        // Given
        `when`(personRepository.findAll()).thenReturn(Flux.just(person))
        val expected = entityMapper.toPersonResponse(person)

        // When
        personService.getAllPeople()
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { actual -> Assertions.assertEquals(expected, actual) }
            .verifyComplete()
        verify(personRepository).findAll()
        verify(entityMapper, times(2)).toPersonResponse(any())
        verifyNoMoreInteractions(personRepository, entityMapper)
    }

    @Test
    fun testGetPersonById() {
        // Given
        val id = UUID.randomUUID()
        `when`(personRepository.findById(id)).thenReturn(Mono.just(person))
        val expected = entityMapper.toPersonResponse(person)

        // When
        personService.getPersonById(id)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .assertNext { actual -> Assertions.assertEquals(expected, actual) }
            .verifyComplete()
        verify(personRepository).findById(id)
        verify(entityMapper, times(2)).toPersonResponse(person)
        verifyNoMoreInteractions(personRepository, entityMapper)
    }

    @Test
    fun testUpdatePerson_NoPerson() {
        // Given
        val id = UUID.randomUUID()
        val addressRequest =
            getResourceAsType(
                "json/update_person_request_success.json",
                UpdateAddressRequest::class.java)
        `when`(personRepository.findById(id)).thenReturn(Mono.empty())

        // When
        personService.updatePersonAddress(id, addressRequest)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyError(ResponseStatusException::class.java)
        verify(personRepository).findById(id)
        verifyNoMoreInteractions(personRepository, entityMapper)
    }

    @Test
    fun testUpdatePerson_Success() {
        // Given
        val id = UUID.randomUUID()
        val addressRequest =
            getResourceAsType(
                "json/update_person_request_success.json",
                UpdateAddressRequest::class.java)
        `when`(personRepository.findById(id)).thenReturn(Mono.just(person))
        `when`(personRepository.save(person)).thenReturn(Mono.just(person))

        // When
        personService.updatePersonAddress(id, addressRequest)
            .`as` { publisher -> StepVerifier.create(publisher) }

            // Then
            .verifyComplete()
        verify(personRepository).findById(id)
        verify(entityMapper).updatePersonAddress(person, addressRequest)
        verify(personRepository).save(person)
        verifyNoMoreInteractions(personRepository, entityMapper)
    }
}