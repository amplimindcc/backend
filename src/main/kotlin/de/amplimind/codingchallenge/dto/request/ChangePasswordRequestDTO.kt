package de.amplimind.codingchallenge.dto.request

/**
 * Data transfer object for changing the password.
 * @param token the token to change the password
 * @param newPassword the new password
 */
data class ChangePasswordRequestDTO(
    val token: String,
    val newPassword: String,
)
