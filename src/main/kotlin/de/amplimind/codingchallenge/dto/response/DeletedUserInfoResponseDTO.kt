package de.amplimind.codingchallenge.dto.response

/**
 * Data Transfer Object for deleted user information
 * @param email the email of the user
 * @param isAdmin if the user is a admin
 */
data class DeletedUserInfoResponseDTO(
    val email: String,
    val isAdmin: Boolean,
)
