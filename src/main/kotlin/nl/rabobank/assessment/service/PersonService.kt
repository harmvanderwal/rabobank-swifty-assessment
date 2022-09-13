package nl.rabobank.assessment.service

import nl.rabobank.assessment.mapper.EntityMapper
import nl.rabobank.assessment.persistence.entity.Person
import nl.rabobank.assessment.persistence.repository.PersonRepository
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class PersonService(private val entityMapper: EntityMapper,
                    private val personRepository: PersonRepository) {

    private val duplicateByNameException = ResponseStatusException(
        HttpStatus.BAD_REQUEST, "Another person with the same full name is already " +
                "registered."
    )

    private val noPersonFoundWithId = "No person found with ID %s"

    private val firstNameAndLastNameCantBothBeEmpty = "firstName and lastName can't both be empty."

    private val noPersonFoundWithName = "No person found with name: %s"

    fun createPerson(personRequest: PersonRequest): Mono<UUID> {
        return personRepository.existsByFirstNameAndLastName(personRequest.firstName, personRequest.lastName)
            .flatMap { exists: Boolean ->
                if (exists) Mono.error(duplicateByNameException)
                else personRepository.save(entityMapper.toPerson(personRequest))
                    .mapNotNull { obj: Person -> obj.id }
            }
    }

    fun findPersonByName(firstName: String?, lastName: String?): Mono<PersonResponse> {
        if (firstName == null && lastName == null) {
            return Mono.error(IllegalArgumentException(firstNameAndLastNameCantBothBeEmpty))
        }
        val personResponseMono: Mono<Person> = if (firstName != null && lastName != null) {
            personRepository.findPersonByFirstNameAndLastName(firstName, lastName)
        } else if (firstName != null) {
            personRepository.findFirstByFirstName(firstName)
        } else {
            personRepository.findFirstByLastName(lastName!!)
        }
        return personResponseMono
            .map { person: Person -> entityMapper.toPersonResponse(person) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format(noPersonFoundWithName, java.lang.String.join(" ", firstName, lastName)))))
    }

    fun getAllPeople(): Flux<PersonResponse> {
        return personRepository.findAll()
            .map { person: Person -> entityMapper.toPersonResponse(person) }
    }

    fun getPersonById(id: UUID): Mono<PersonResponse> {
        return personRepository.findById(id)
            .map { person: Person -> entityMapper.toPersonResponse(person) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format(noPersonFoundWithId, id))))
    }

    fun updatePersonAddress(id: UUID, updateAddressRequest: UpdateAddressRequest?): Mono<Void> {
        return personRepository.findById(id)
            .map { person: Person -> entityMapper.updatePersonAddress(person, updateAddressRequest) }
            .switchIfEmpty(Mono.error( ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format(noPersonFoundWithId, id))))
            .flatMap { entity: Person -> personRepository.save(entity) }
            .then()
    }
}