package de.amplimind.codingchallenge.dto.request

/**
 * DTO for the login request
 * @param email the email of the user
 * @param password the password of the user
 */
data class LoginRequestDTO(
    val email: String,
    val password: String,
)
