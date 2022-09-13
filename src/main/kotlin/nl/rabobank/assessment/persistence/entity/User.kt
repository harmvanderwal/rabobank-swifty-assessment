package nl.rabobank.assessment.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class User(

    @Id
    private var id: UUID? = null,
    private var password: String,
    var roles: List<Role>,
    private var username: String,
    val enabled: Boolean,
    var locked: Boolean = false

) : Persistable<UUID>, UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return roles.stream().map { role: Role ->
            SimpleGrantedAuthority(
                role.name
            )
        }.toList()
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return !locked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    enum class Role {
        ROLE_USER, ROLE_ADMIN
    }

    override fun getId(): UUID? {
        return id;
    }

    override fun isNew(): Boolean {
        return if (id == null) {
            id = UUID.randomUUID()
            true
        } else {
            false
        }
    }
}
