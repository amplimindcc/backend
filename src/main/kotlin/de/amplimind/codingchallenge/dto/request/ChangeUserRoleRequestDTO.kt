package de.amplimind.codingchallenge.dto.request

import de.amplimind.codingchallenge.model.UserRole

/**
 * Data transfer object for the request to change the role of a user.
 * @param email the email of the user
 * @param newRole the new role of the user
 */
data class ChangeUserRoleRequestDTO(
    val email: String,
    val newRole: UserRole,
)
