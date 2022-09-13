package nl.rabobank.assessment.ui.rest.model.response;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public record PetResponse(

		UUID id,

		@NotNull
		String name,

		int age,

		UUID personId
){}
