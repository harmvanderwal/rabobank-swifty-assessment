package nl.rabobank.assessment.ui.rest.model.response;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.UUID;

public record PersonResponse(

		UUID id,

		@NotNull
		String firstName,

		@NotNull
		String lastName,

		@NotNull
		LocalDate dateOfBirth,

		@NotNull
		String street,

		@NotNull
		int houseNumber,

		String houseNumberAdditions,

		@NotNull
		@Pattern(regexp = "(?i)^\\d{4}\\s?[A-Z]{2}$")
		String postalCode,

		@NotNull
		String city,

		@NotNull
		String country
) {}
