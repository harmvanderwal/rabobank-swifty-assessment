package nl.rabobank.assessment.service

import nl.rabobank.assessment.mapper.EntityMapper
import nl.rabobank.assessment.persistence.entity.Pet
import nl.rabobank.assessment.persistence.repository.PersonRepository
import nl.rabobank.assessment.persistence.repository.PetRepository
import nl.rabobank.assessment.ui.rest.model.request.PetRequest
import nl.rabobank.assessment.ui.rest.model.response.PetResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class PetService(private val entityMapper: EntityMapper,
                 private val personRepository: PersonRepository,
                 private val petRepository: PetRepository) {

    protected val noPetFoundWithId = "No pet found with ID %s"

    private val noPersonWithThatIdAvailable = ResponseStatusException(
        HttpStatus.BAD_REQUEST, "No person with ID %s is registered. Try registering" +
                " your pet to an actual person."
    )

    fun createPet(petRequest: PetRequest): Mono<UUID> {
        return if (petRequest.personId != null) {
            personRepository.existsById(petRequest.personId)
                .flatMap { exists: Boolean ->
                    if (exists) petRepository.save(
                        entityMapper.toPet(petRequest)
                    )
                        .mapNotNull(Pet::getId)
                    else Mono.error(
                        noPersonWithThatIdAvailable
                    )
                }
        } else petRepository.save(entityMapper.toPet(petRequest))
            .mapNotNull(Pet::getId)
    }

    fun deletePetById(id: UUID): Mono<Void> {
        return petRepository.existsById(id)
            .flatMap { exists ->
                if (exists) petRepository.deleteById(id) else Mono.error(
                    ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(noPetFoundWithId, id))) }
    }

    fun getAllPets(): Flux<PetResponse> {
        return petRepository.findAll()
            .mapNotNull { pet: Pet -> entityMapper.toPetResponse(pet) }
    }

    fun getPetById(petId: UUID): Mono<PetResponse> {
        return petRepository.findById(petId)
            .switchIfEmpty(
                Mono.error(
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(noPetFoundWithId, petId)
                    )
                )
            )
            .mapNotNull { pet: Pet -> entityMapper.toPetResponse(pet) }
    }

    fun getPetsByPersonId(personId: UUID): Flux<PetResponse> {
        return petRepository.findPetsByPersonId(personId)
            .mapNotNull { pet: Pet -> entityMapper.toPetResponse(pet) }
    }

    fun updatePet(id: UUID, petRequest: PetRequest): Mono<Void> {
        return petRepository.findById(id)
            .switchIfEmpty(
                Mono.error(
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(noPetFoundWithId, id)
                    )
                )
            )
            .map { pet -> entityMapper.toPet(pet, petRequest) }
            .flatMap(petRepository::save)
            .then()
    }
}