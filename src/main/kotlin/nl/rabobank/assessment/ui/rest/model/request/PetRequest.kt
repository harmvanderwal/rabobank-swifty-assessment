package nl.rabobank.assessment.ui.rest.model.request

import java.util.*
import javax.validation.constraints.NotNull

data class PetRequest(
    val name: String,
    @get:NotNull
    val age: Int?,
    val personId: UUID?
)
