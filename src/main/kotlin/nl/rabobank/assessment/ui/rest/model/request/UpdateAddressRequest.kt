package nl.rabobank.assessment.ui.rest.model.request

import javax.validation.constraints.Pattern

data class UpdateAddressRequest(
    val street: String,
    val houseNumber: Int,
    val houseNumberAdditions: String?,
    @Pattern(regexp = "(?i)^\\d{4}\\s?[A-Z]{2}$")
    val postalCode: String,
    val city: String,
    val country: String
)
