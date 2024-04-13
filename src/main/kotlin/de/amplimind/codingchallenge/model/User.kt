package de.amplimind.codingchallenge.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
class User(
    @Id
    val email: String,
    private val password: String? = null,
    @Enumerated(EnumType.STRING)
    val role: UserRole,
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
