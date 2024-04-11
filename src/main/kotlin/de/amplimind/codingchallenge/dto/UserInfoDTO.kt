package de.amplimind.codingchallenge.dto

import de.amplimind.codingchallenge.model.UserRole

/**
 * Data Transfer Object for user information which are relevant for the frontend
 * @param email the email of the user
 * @param role the role of the user
 * @param status the status of the user
 */
data class UserInfoDTO(
    val email: String,
    val role: UserRole,
    val status: UserStatus,
)
