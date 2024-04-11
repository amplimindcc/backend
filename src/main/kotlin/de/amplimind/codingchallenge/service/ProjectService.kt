package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.model.Project
import de.amplimind.codingchallenge.repository.ProjectRepository
import org.springframework.stereotype.Service

/**
 * Service for managing projects.
 */
@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
) {
    /**
     * Adds a new project.
     * @param createProjectRequest the request to create a project
     */
    fun addProject(createProjectRequest: CreateProjectRequestDTO) {
        val project =
            Project(
                description = createProjectRequest.description,
                title = createProjectRequest.title,
                active = createProjectRequest.active,
            )

        this.projectRepository.save(project)
    }
}
