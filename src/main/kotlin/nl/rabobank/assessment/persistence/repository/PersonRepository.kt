package nl.rabobank.assessment.persistence.repository

import nl.rabobank.assessment.persistence.entity.Person
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface PersonRepository : ReactiveCrudRepository<Person, UUID> {

    fun existsByFirstNameAndLastName(firstName: String, lastName: String): Mono<Boolean>

    fun findFirstByFirstName(firstName: String): Mono<Person>

    fun findPersonByFirstNameAndLastName(firstName: String, lastName: String): Mono<Person>

    fun findFirstByLastName(lastName: String): Mono<Person>
}