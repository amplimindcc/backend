package de.amplimind.codingchallenge.dto.response

/**
 * Data Transfer Object for the linting results of a submission
 * @param result the linting artifact
 */
data class LintResultResponseDTO(
    val result: String,
)
