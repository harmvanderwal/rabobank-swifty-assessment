package nl.rabobank.assessment.ui.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest;
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest;
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse;
import nl.rabobank.assessment.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(PersonController.BASE_URL)
@RequiredArgsConstructor
public class PersonController {

	protected static final String UPDATING_PERSON_WITH_NEW_ADDRESS = "Updating person {} with new address: {}";

	protected static final String RETRIEVING_PERSON_MATCHIN_FIRST_NAME_AND_LAST_NAME = "Retrieving person matchin firstName \"{}\" and lastName \"{}\"";

	protected static final String RETRIEVING_PERSON_WITH_ID = "Retrieving person with id {}";

	protected static final String RETRIEVING_ALL_PEOPLE = "Retrieving all people.";

	protected static final String CREATING_NEW_PERSON = "Creating new person: {}";

	private static final Logger log = LoggerFactory.getLogger(PersonController.class);

	public static final String BASE_URL = "/v1/person";

	protected static final String AT_LEAST_ONE_OF_FIRST_NAME_OR_LAST_NAME_MUST_BE_FILLED_TO_SEARCH = "At least one of firstName or lastName must be filled to search.";


	private final PersonService personService;

	@Operation(description = "Create a new person.")
	@ApiResponse(responseCode = "201", description = "Person created.")
	@ApiResponse(responseCode = "400", description = "Bad request.")
	@PostMapping
	public Mono<ResponseEntity<Void>> createPerson(@Valid @NotNull @RequestBody PersonRequest personRequest) {
		log.debug(CREATING_NEW_PERSON, personRequest);
		return personService.createPerson(personRequest)
				.map(uuid -> ResponseEntity.created(URI.create(String.format("%s/%s", BASE_URL, uuid))).build());
	}

	@Operation(description = "Retrieve all people.")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@GetMapping
	public Flux<PersonResponse> getAllPeople() {
		log.debug(RETRIEVING_ALL_PEOPLE);
		return personService.getAllPeople();
	}

	@Operation(description = "Get person by id.")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@ApiResponse(responseCode = "404", description = "No person found.")
	@GetMapping("/{id}")
	public Mono<PersonResponse> getPersonById(@Parameter(description = "id", required = true)
	                                          @PathVariable UUID id) {
		log.debug(RETRIEVING_PERSON_WITH_ID, id);
		return personService.getPersonById(id);
	}

	@Operation(description = "Find a person by first or last name")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@ApiResponse(responseCode = "400", description = "Bad request.")
	@ApiResponse(responseCode = "404", description = "No person found.")
	@GetMapping("/search")
	public Mono<PersonResponse> getPersonByName(@Parameter(description = "First name of the person.")
	                                            @RequestParam @Nullable String firstName,
	                                            @Parameter(description = "Last name of the person.")
	                                            @RequestParam @Nullable String lastName) {
		if (firstName == null && lastName == null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
					AT_LEAST_ONE_OF_FIRST_NAME_OR_LAST_NAME_MUST_BE_FILLED_TO_SEARCH));
		}
		log.debug(RETRIEVING_PERSON_MATCHIN_FIRST_NAME_AND_LAST_NAME, firstName, lastName);
		return personService.findPersonByName(firstName, lastName);
	}

	@Operation(description = "Update address details for a person")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@ApiResponse(responseCode = "404", description = "No person found.")
	@PutMapping("/{id}")
	public Mono<Void> updatePerson(@Parameter(description = "id", required = true)
	                               @PathVariable UUID id,
	                               @Valid @NotNull @RequestBody UpdateAddressRequest updateAddressRequest) {
		log.debug(UPDATING_PERSON_WITH_NEW_ADDRESS, id, updateAddressRequest);
		return personService.updatePersonAddress(id, updateAddressRequest);
	}
}
