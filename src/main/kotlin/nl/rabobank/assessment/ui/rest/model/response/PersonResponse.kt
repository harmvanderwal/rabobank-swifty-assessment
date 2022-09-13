package nl.rabobank.assessment.ui.rest.model.response

import java.time.LocalDate
import java.util.*
import javax.validation.constraints.Pattern

data class PersonResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val street: String,
    val houseNumber: Int,
    val houseNumberAdditions: String?,
    @Pattern(regexp = "(?i)^\\d{4}\\s?[A-Z]{2}$")
    val postalCode: String,
    val city: String,
    val country: String
)