package de.amplimind.codingchallenge.dto.request

/**
 * Data transfer object for the request to creating a project.
 * @param title the title of the project
 * @param description the description of the project
 * @param active the active status of the project
 */
data class CreateProjectRequestDTO(
    val title: String,
    val description: String,
    val active: Boolean,
)
