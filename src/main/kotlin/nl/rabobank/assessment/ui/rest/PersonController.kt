package nl.rabobank.assessment.ui.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import nl.rabobank.assessment.service.PersonService
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(PersonController.BASE_URL)
class PersonController(private val personService: PersonService) {

    companion object {
        const val BASE_URL = "/v1/person"
    }

    private val updatingPersonWithNewAddress = "Updating person {} with new address: {}"

    private val retrievingPersonMatchingFirstNameAndLastName =
        "Retrieving person matching firstName \"{}\" and lastName \"{}\""

    private val retrievingPersonWithId = "Retrieving person with id {}"

    private val retrievingAllPeople = "Retrieving all people."

    private val creatingNewPerson = "Creating new person: {}"

    private val log = LoggerFactory.getLogger(PersonController::class.java)

    private val atLeastOneOfFirstNameOrLastNameIsMandatory =
        "At least one of firstName or lastName is mandatory."

    @Operation(description = "Create a new person.")
    @ApiResponse(responseCode = "201", description = "Person created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @PostMapping
    fun createPerson(@RequestBody personRequest: @Valid PersonRequest): Mono<ResponseEntity<Void>> {
        log.debug(creatingNewPerson, personRequest)
        return personService.createPerson(personRequest)
            .map { uuid: UUID -> ResponseEntity.created(
                URI.create(String.format("%s/%s", BASE_URL, uuid))
            ).build() }
    }

    @Operation(description = "Retrieve all people.")
    @ApiResponse(responseCode = "200", description = "Ok.")
    @GetMapping
    fun getAllPeople(): Flux<PersonResponse> {
        log.debug(retrievingAllPeople)
        return personService.getAllPeople()
    }

    @Operation(description = "Get person by id.")
    @ApiResponse(responseCode = "200", description = "Ok.")
    @ApiResponse(responseCode = "404", description = "No person found.")
    @GetMapping("/{id}")
    fun getPersonById(@Parameter(description = "id", required = true) @PathVariable id: UUID): Mono<PersonResponse> {
        log.debug(retrievingPersonWithId, id)
        return personService.getPersonById(id)
    }

    @Operation(description = "Find a person by first or last name")
    @ApiResponse(responseCode = "200", description = "Ok.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "404", description = "No person found.")
    @GetMapping("/search")
    fun getPersonByName(
        @Parameter(description = "First name of the person.") @RequestParam firstName: String?,
        @Parameter(description = "Last name of the person.") @RequestParam lastName: String?
    ): Mono<PersonResponse> {
        if (firstName == null && lastName == null) {
            return Mono.error(
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    atLeastOneOfFirstNameOrLastNameIsMandatory
                )
            )
        }
        log.debug(retrievingPersonMatchingFirstNameAndLastName, firstName, lastName)
        return personService.findPersonByName(firstName, lastName)
    }

    @Operation(description = "Update address details for a person")
    @ApiResponse(responseCode = "200", description = "Ok.")
    @ApiResponse(responseCode = "404", description = "No person found.")
    @PutMapping("/{id}")
    fun updatePerson(
        @Parameter(description = "id", required = true) @PathVariable id: UUID,
        @RequestBody updateAddressRequest: @Valid UpdateAddressRequest
    ): Mono<Void> {
        log.debug(updatingPersonWithNewAddress, id, updateAddressRequest)
        return personService.updatePersonAddress(id, updateAddressRequest)
    }
}