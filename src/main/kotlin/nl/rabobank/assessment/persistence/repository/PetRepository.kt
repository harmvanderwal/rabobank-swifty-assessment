package nl.rabobank.assessment.persistence.repository

import nl.rabobank.assessment.persistence.entity.Pet
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface PetRepository : ReactiveCrudRepository<Pet, UUID> {

    fun findPetsByPersonId(personId: UUID): Flux<Pet>
}