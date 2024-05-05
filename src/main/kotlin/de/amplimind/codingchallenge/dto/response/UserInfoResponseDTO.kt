package de.amplimind.codingchallenge.dto.response

import de.amplimind.codingchallenge.model.UserStatus

/**
 * Data Transfer Object for user information which are relevant for the frontend
 * @param email the email of the user
 * @param isAdmin if the user is a admin
 * @param status the status of the user
 */
data class UserInfoResponseDTO(
    val email: String,
    val isAdmin: Boolean,
    val status: UserStatus,
)
