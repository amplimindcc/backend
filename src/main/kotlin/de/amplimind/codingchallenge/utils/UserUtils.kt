package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.NoAuthenticationException
import de.amplimind.codingchallenge.model.User
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.jvm.Throws

/**
 * Utility class for user related operations.
 */
object UserUtils {
    /**
     * Fetches the currently logged in user.
     * @return the currently logged in [User]
     * @throws NoAuthenticationException if no authentication is present
     */
    @Throws(NoAuthenticationException::class)
    fun fetchLoggedInUser(): User {
        SecurityContextHolder.getContext().authentication?.let {
            if (it.principal is User) {
                return it.principal as User
            }
        }

        // No authentication found
        throw NoAuthenticationException("No authentication for current user was found")
    }
}
