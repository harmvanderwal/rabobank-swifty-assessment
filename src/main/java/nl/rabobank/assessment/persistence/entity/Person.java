package nl.rabobank.assessment.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Person implements Persistable<UUID> {

	@Id
	@Setter(AccessLevel.NONE)
	private UUID id;

	private String firstName;

	private String lastName;

	private LocalDate dateOfBirth;

	private String street;

	private Integer houseNumber;

	private String houseNumberAdditions;

	private String postalCode;

	private String city;

	private String country;

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
		Person that = (Person) other;
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
