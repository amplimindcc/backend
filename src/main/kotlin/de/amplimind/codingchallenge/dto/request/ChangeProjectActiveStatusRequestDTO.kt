package de.amplimind.codingchallenge.dto.request

/**
 * Data transfer object for the request to change the active status of a project.
 * @param projectId the id of the project
 * @param active the new active status of the project
 * @param version the version of the project
 */
data class ChangeProjectActiveStatusRequestDTO(
    val projectId: Long,
    val active: Boolean,
    val version: Long
)
