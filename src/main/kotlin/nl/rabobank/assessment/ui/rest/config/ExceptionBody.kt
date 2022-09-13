package nl.rabobank.assessment.ui.rest.config

import java.time.ZonedDateTime

class ExceptionBody(
    val status: Int,
    val error: String,
    val messages: List<String>,
    private val timestamp: ZonedDateTime,
    val trace: String,
    val path: String,
)