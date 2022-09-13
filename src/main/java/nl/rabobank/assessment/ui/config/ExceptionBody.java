package nl.rabobank.assessment.ui.config;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * A class that represents the default body structure that is returned by Spring Boot when throwing a custom exception
 * with a @ResponseStatus.
 *
 * It is used in {@linkplain ExceptionHandlers}.
 */
public class ExceptionBody {
	private final String error;

	private final List<String> messages;

	private final String path;

	private final int status;

	private final ZonedDateTime timestamp = ZonedDateTime.now();

	private final String trace;

	public ExceptionBody(int status, String error, List<String> messages, String trace, String path) {
		this.status = status;
		this.error = error;
		this.messages = messages;
		this.trace = trace;
		this.path = path;
	}

	public String getError() {
		return error;
	}

	public List<String> getMessages() {
		return messages;
	}

	public String getPath() {
		return path;
	}

	public int getStatus() {
		return status;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public String getTrace() {
		return trace;
	}
}