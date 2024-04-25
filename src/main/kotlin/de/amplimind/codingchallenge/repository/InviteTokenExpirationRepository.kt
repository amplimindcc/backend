package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.InviteTokenExpiration
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for invite tokens.
 */
interface InviteTokenExpirationRepository : JpaRepository<InviteTokenExpiration, Long> {
    fun deleteByEmail(email: String)

    fun findByEmail(email: String): InviteTokenExpiration?
}
