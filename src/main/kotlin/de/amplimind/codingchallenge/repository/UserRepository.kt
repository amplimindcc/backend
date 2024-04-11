package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for the user entity
 */
interface UserRepository : JpaRepository<User, String> {
    /**
     * Finds a user by its email
     * @param email the email to search for
     * @return the user if found, null otherwise
     */
    fun findByEmail(email: String): User?
}
