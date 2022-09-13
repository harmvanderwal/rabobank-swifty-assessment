package nl.rabobank.assessment.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ResourceHelper {
	private static ObjectMapper objectMapper;

	static {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper = mapper;
	}

	public static void setObjectMapper(ObjectMapper objectMapper) {
		ResourceHelper.objectMapper = Objects.requireNonNullElse(objectMapper, new ObjectMapper());
	}

	public static <T> T getResourceAsType(String fileLocation, Class<T> returnType) {
		try (InputStream input =
				     Objects.requireNonNull(ResourceHelper.class.getClassLoader().getResourceAsStream(fileLocation))) {
			return objectMapper.readValue(input, returnType);
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	public static <T> T getResourceAsType(String fileLocation, TypeReference<T> returnType) {
		try (InputStream input =
				     Objects.requireNonNull(ResourceHelper.class.getClassLoader().getResourceAsStream(fileLocation))) {
			return objectMapper.readValue(input, returnType);
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	public static String getResourceAsString(String fileLocation) {
		try (InputStream input =
				     Objects.requireNonNull(ResourceHelper.class.getClassLoader().getResourceAsStream(fileLocation))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}
}
