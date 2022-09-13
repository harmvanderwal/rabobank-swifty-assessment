package nl.rabobank.assessment.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Pet implements Persistable<UUID> {

	@Id
	@Setter(AccessLevel.NONE)
	private UUID id;

	private String name;

	private int age; // Waarom is het age en niet date of birth?

	private UUID personId;

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null || getClass() != other.getClass())
			return false;
		Pet that = (Pet) other;
		return Objects.equals(id, that.id);
	}

	@Override
	public boolean isNew() {
		if (id == null) {
			id = UUID.randomUUID();
			return true;
		}
		return false;
	}
}
