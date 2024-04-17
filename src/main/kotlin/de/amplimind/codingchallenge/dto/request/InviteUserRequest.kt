package de.amplimind.codingchallenge.dto.request

/**
 * Data Transfer Object for inviting a user
 * @param email the email of the user
 * @param isAdmin if the user is a admin
 */
data class InviteUserRequest(
    val email: String,
    val isAdmin: Boolean,
)
