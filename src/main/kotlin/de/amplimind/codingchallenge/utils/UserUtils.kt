package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.NoAuthenticationException
import de.amplimind.codingchallenge.model.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import kotlin.jvm.Throws

/**
 * Utility class for user related operations.
 */
object UserUtils {
    /**
     * Fetches the currently logged in user.
     * @return the currently logged in [UserDetails]
     * @throws NoAuthenticationException if no authentication is present
     */
    @Throws(NoAuthenticationException::class)
    fun fetchLoggedInUser(): UserDetails {
        SecurityContextHolder.getContext().authentication?.let {
            if (it.principal is UserDetails) {
                return it.principal as UserDetails
            }
        }

        // No authentication found
        throw NoAuthenticationException("No authentication for current user was found")
    }
}
