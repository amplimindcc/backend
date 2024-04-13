package de.amplimind.codingchallenge.dto.request

/**
 * Data transfer object for the request to change the title of a project.
 * @param projectId the id of the project
 * @param newTitle the new title of the project
 */
data class ChangeProjectTitleRequestDTO(
    val projectId: Long,
    val newTitle: String,
)
