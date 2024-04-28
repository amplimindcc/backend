package de.amplimind.codingchallenge.dto

/**
 * Data Transfer Object for deleted user information
 * @param email the email of the user
 * @param isAdmin if the user is a admin
 */
data class DeletedUserInfoDTO(
    val email: String,
    val isAdmin: Boolean,
)
