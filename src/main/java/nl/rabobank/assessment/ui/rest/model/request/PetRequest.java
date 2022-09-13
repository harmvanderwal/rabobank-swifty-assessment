package nl.rabobank.assessment.ui.rest.model.request;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public record PetRequest(

		@NotNull
		String name,

		@NotNull
		Integer age,

		UUID personId
){}
