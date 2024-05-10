package de.amplimind.codingchallenge.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.Version
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Entity which represents a user of this platform
 */
@Entity
@Table(name = "users")
class User(
    @Id
    val email: String,
    private val password: String? = null,
    @Enumerated(EnumType.STRING)
    val role: UserRole,
    @Version
    var version: Long? = null,
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return listOf("ROLE_${role.name}").map { GrantedAuthority { it } }.toMutableList()
    }

    override fun getPassword(): String? {
        return this.password
    }

    override fun getUsername(): String {
        return this.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
