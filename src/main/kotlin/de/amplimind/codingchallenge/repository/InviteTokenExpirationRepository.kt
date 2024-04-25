package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.InviteTokenExpiration
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for invite tokens.
 */
interface InviteTokenExpirationRepository : JpaRepository<InviteTokenExpiration, Long> {

    /**
     * Deletes an entry for a user.
     * @param email the email of the user
     */
    fun deleteByEmail(email: String)

    /**
     * Fetches an entry for a user.
     * @param email the email of the user
     * @return the entry or null if non existing
     */
    fun findByEmail(email: String): InviteTokenExpiration?
}
