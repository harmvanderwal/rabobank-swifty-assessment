package nl.rabobank.assessment.ui.rest.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import java.time.ZonedDateTime
import java.util.stream.Stream

@ControllerAdvice
class ExceptionHandlers {

    private val EXCEPTION_MESSAGE_FORMAT = "%s value '%s' %s"

    /**
     * Handles the [WebExchangeBindException].
     * Binds them together and returns them as 1 error.
     *
     * @param exception The exception to be handled.
     * @return A map with all [WebExchangeBindException]
     */
    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handle(exchange: ServerWebExchange, exception: WebExchangeBindException): ExceptionBody? {
        val badRequest = HttpStatus.BAD_REQUEST
        val messages = exception.bindingResult.fieldErrors
            .stream()
            .map { x: FieldError ->
                String.format(
                    EXCEPTION_MESSAGE_FORMAT,
                    x.field,
                    x.rejectedValue,
                    x.defaultMessage
                )
            }
            .toList()
        val path = exchange.request.uri.toString()
        return ExceptionBody(badRequest.value(), badRequest.reasonPhrase, messages, ZonedDateTime.now(), exception.toString(), path)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handle(
        exchange: ServerWebExchange,
        exception: ResponseStatusException
    ): ResponseEntity<ExceptionBody?>? {
        val path = exchange.request.uri.toString()
        return ResponseEntity.status(exception.status).body(
            ExceptionBody(
                exception.rawStatusCode,
                exception.status.reasonPhrase,
                listOf(exception.reason!!),
                ZonedDateTime.now(),
                exception.toString(),
                path))
    }
}