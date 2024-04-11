package de.amplimind.codingchallenge.dto.request

/**
 * Data transfer object for the request to creating a project.
 */
data class CreateProjectRequestDTO(
    val title: String,
    val description: String,
    val active: Boolean,
)
