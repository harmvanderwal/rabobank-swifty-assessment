package nl.rabobank.assessment.ui.rest.model.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

public record PersonRequest(

		@NotNull
		String firstName,

		@NotNull
		String lastName,

		@NotNull
		LocalDate dateOfBirth,

		@NotNull
		String street,

		@NotNull
		Integer houseNumber,

		String houseNumberAdditions,

		@NotNull
		@Pattern(regexp = "(?i)^\\d{4}\\s?[A-Z]{2}$")
		String postalCode,

		@NotNull
		String city,

		@NotNull
		String country
) {}
