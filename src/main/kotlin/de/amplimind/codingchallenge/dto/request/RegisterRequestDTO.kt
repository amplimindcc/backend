package de.amplimind.codingchallenge.dto.request

/**
 * Data transfer object for the request to set password for applicant.
 * @param token the token of the applicant
 * @param newRole newly set password of applicant
 */

data class RegisterRequestDTO(
    val token: String,
    val password: String,
)
