package de.amplimind.codingchallenge.dto

/**
 * Data Transfer Object for the linting results of a submission
 * @param result the linting artifact
 */
data class LintResultDTO(
    val result: String,
)
