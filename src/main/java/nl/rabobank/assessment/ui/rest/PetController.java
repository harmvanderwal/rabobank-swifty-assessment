package nl.rabobank.assessment.ui.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import nl.rabobank.assessment.ui.rest.model.request.PetRequest;
import nl.rabobank.assessment.ui.rest.model.response.PetResponse;
import nl.rabobank.assessment.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(PetController.BASE_URL)
@RequiredArgsConstructor
public class PetController {

	protected static final String CREATING_NEW_PET = "Creating new Pet: {}";

	protected static final String DELETING_PET = "Deleting pet {}";

	protected static final String RETRIEVING_ALL_PETS = "Retrieving all pets {}";

	protected static final String RETRIEVING_PET = "Retrieving pet {}";

	protected static final String UPDATING_PET = "Updating pet {}: {}";

	private static final Logger log = LoggerFactory.getLogger(PetController.class);

	public static final String BASE_URL = "/v1/pet";

	private static final String NO_PET_FOUND_WITH_ID = "No pet found with id: %s";

	private final PetService petService;

	@Operation(description = "Create a new pet.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<ResponseEntity<Void>> createPet(@Valid @NotNull @RequestBody PetRequest petRequest) {
		log.debug(CREATING_NEW_PET, petRequest);
		return petService.createPet(petRequest)
				.map(uuid -> ResponseEntity.created(URI.create(String.format("%s/%s", BASE_URL, uuid)))
						.build());
	}

	@Operation(description = "Delete pet.")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@ApiResponse(responseCode = "404", description = "No pet found.")
	@DeleteMapping("/{id}")
	public Mono<Void> deletePet(@Parameter(description = "petId", required = true)
	                            @PathVariable UUID id) {
		log.debug(DELETING_PET, id);
		return petService.deletePetById(id);
	}

	@Operation(description = "Get all pets (filterable by personId).")
	@ApiResponse(responseCode = "200", description = "Ok")
	@GetMapping
	public Flux<PetResponse> getAllPets(@Parameter(description = "personId")
	                                    @RequestParam @Nullable UUID personId) {
		log.debug(RETRIEVING_ALL_PETS, personId == null ? "" : String.format("matching personId %s", personId));
		return personId == null ? petService.getAllPets() : petService.getPetsByPersonId(personId);
	}

	@Operation(description = "Get pet by id.")
	@ApiResponse(responseCode = "200", description = "Ok")
	@ApiResponse(responseCode = "404", description = "No pet found.")
	@GetMapping("/{id}")
	public Mono<PetResponse> getPet(@Parameter(description = "petId", required = true)
	                                @PathVariable UUID id) {
		log.debug(RETRIEVING_PET, id);
		return petService.getPetById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(NO_PET_FOUND_WITH_ID, id))));
	}

	@Operation(description = "Update pet.")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@ApiResponse(responseCode = "400", description = "Bad request.")
	@ApiResponse(responseCode = "404", description = "No pet found.")
	@PutMapping("/{id}")
	public Mono<Void> updatePet(@Parameter(description = "petId", required = true)
	                            @PathVariable UUID id,
	                            @Valid @NotNull @RequestBody PetRequest petRequest) {
		log.debug(UPDATING_PET, id, petRequest);
		return petService.updatePet(id, petRequest);
	}
}
