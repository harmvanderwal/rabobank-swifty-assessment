package nl.rabobank.assessment.ui.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import nl.rabobank.assessment.service.PetService
import nl.rabobank.assessment.ui.rest.model.request.PetRequest
import nl.rabobank.assessment.ui.rest.model.response.PetResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.lang.Nullable
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(PetController.BASE_URL)
class PetController(private val petService: PetService) {

    companion object {
        const val BASE_URL = "/v1/pet"
    }

    protected val CREATING_NEW_PET = "Creating new Pet: {}"

    protected val DELETING_PET = "Deleting pet {}"

    protected val RETRIEVING_ALL_PETS = "Retrieving all pets {}"

    protected val RETRIEVING_PET = "Retrieving pet {}"

    protected val UPDATING_PET = "Updating pet {}: {}"

    private val log = LoggerFactory.getLogger(PetController::class.java)

    private val NO_PET_FOUND_WITH_ID = "No pet found with id: %s"

    @Operation(description = "Create a new pet.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPet(@RequestBody @Valid @NotNull petRequest: PetRequest): Mono<ResponseEntity<Void>> {
        log.debug(CREATING_NEW_PET, petRequest)
        return petService.createPet(petRequest)
            .map { uuid: UUID? ->
                ResponseEntity.created(URI.create(String.format("%s/%s", BASE_URL, uuid)))
                    .build() }
    }

    @Operation(description = "Delete pet.")
    @ApiResponse(responseCode = "200", description = "Ok.")
    @ApiResponse(responseCode = "404", description = "No pet found.")
    @DeleteMapping("/{id}")
    fun deletePet(@Parameter(description = "petId", required = true) @PathVariable id: UUID): Mono<Void> {
        log.debug(DELETING_PET, id)
        return petService.deletePetById(id)
    }

    @Operation(description = "Get all pets (filterable by personId).")
    @ApiResponse(responseCode = "200", description = "Ok")
    @GetMapping
    fun getAllPets(@Parameter(description = "personId") @RequestParam @Nullable personId: UUID?): Flux<PetResponse> {
        log.debug(RETRIEVING_ALL_PETS, if (personId == null) "" else String.format("matching personId %s", personId))
        return if (personId == null) petService.getAllPets() else petService.getPetsByPersonId(personId)
    }

    @Operation(description = "Get pet by id.")
    @ApiResponse(responseCode = "200", description = "Ok")
    @ApiResponse(responseCode = "404", description = "No pet found.")
    @GetMapping("/{id}")
    fun getPet(@Parameter(description = "petId", required = true) @PathVariable id: UUID): Mono<PetResponse> {
        log.debug(RETRIEVING_PET, id)
        return petService.getPetById(id)
            .switchIfEmpty(
                Mono.error(
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(NO_PET_FOUND_WITH_ID, id))))
    }

    @Operation(description = "Update pet.")
    @ApiResponse(responseCode = "200", description = "Ok.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "404", description = "No pet found.")
    @PutMapping("/{id}")
    fun updatePet(
        @Parameter(description = "petId", required = true) @PathVariable id: UUID,
        @RequestBody @Valid @NotNull petRequest: PetRequest): Mono<Void> {
        log.debug(UPDATING_PET, id, petRequest)
        return petService.updatePet(id, petRequest)
    }
}