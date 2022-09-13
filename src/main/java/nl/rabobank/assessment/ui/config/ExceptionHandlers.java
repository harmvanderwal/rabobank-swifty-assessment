package nl.rabobank.assessment.ui.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.stream.Stream;

@ControllerAdvice
public class ExceptionHandlers {

	private static final String EXCEPTION_MESSAGE_FORMAT = "%s value '%s' %s";

	/**
	 * Handles the {@linkplain WebExchangeBindException}.
	 * Binds them together and returns them as 1 error.
	 *
	 * @param exception The exception to be handled.
	 * @return A map with all {@linkplain WebExchangeBindException}
	 */
	@ExceptionHandler(WebExchangeBindException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionBody handle(final ServerWebExchange exchange, final WebExchangeBindException exception) {

		HttpStatus badRequest = HttpStatus.BAD_REQUEST;
		List<String> messages = exception.getBindingResult().getFieldErrors()
				.stream()
				.map(x -> String.format(EXCEPTION_MESSAGE_FORMAT, x.getField(), x.getRejectedValue(), x.getDefaultMessage()))
				.toList();
		String path = exchange.getRequest().getURI().toString();

		return new ExceptionBody(badRequest.value(), badRequest.getReasonPhrase(), messages, exception.toString(), path);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ExceptionBody> handle(final ServerWebExchange exchange,
	                                            final ResponseStatusException exception) {
		String path = exchange.getRequest().getURI().toString();
		return ResponseEntity.status(exception.getStatus()).body(new ExceptionBody(exception.getRawStatusCode(),
				exception.getStatus().getReasonPhrase(),
				Stream.of(exception.getReason()).toList(),
				exception.toString(),	path));
	}
}
