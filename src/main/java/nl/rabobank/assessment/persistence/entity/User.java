package nl.rabobank.assessment.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
public class User implements Persistable<UUID>, UserDetails {

	@Id
	@Setter(AccessLevel.NONE)
	private UUID id;

	private String password;

	private List<Role> roles;

	private String username;

	private boolean enabled;

	private boolean locked;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).toList();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !locked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

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
		User that = (User) other;
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

	enum Role {
		ROLE_USER,
		ROLE_ADMIN
	}
}
