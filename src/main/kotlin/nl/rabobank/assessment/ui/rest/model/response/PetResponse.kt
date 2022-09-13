package nl.rabobank.assessment.ui.rest.model.response

import java.util.*

data class PetResponse(
    val id: UUID,
    val name: String,
    val age: Int,
    val personId: UUID?
)
