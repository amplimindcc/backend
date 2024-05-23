package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.ChangeProjectActiveStatusRequestDTO
import de.amplimind.codingchallenge.dto.request.ChangeProjectTitleRequestDTO
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.dto.response.UserProjectResponseDTO
import de.amplimind.codingchallenge.exceptions.ProjectInUseException
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.Project
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import org.springframework.stereotype.Service

/**
 * Service for managing projects.
 */
@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val submissionRepository: SubmissionRepository,
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

    /**
     * Fetches all projects.
     * @return a list of all [Project]s
     */
    fun fetchAllProjects(): List<Project> = this.projectRepository.findAll()

    /**
     * Changes the active status of a project.
     * @param changeProjectActiveStatusRequestDTO the request to change the active status of a project
     * @return the updated [Project]
     */
    fun changeProjectActive(changeProjectActiveStatusRequestDTO: ChangeProjectActiveStatusRequestDTO): Project {
        val storedProject =
            this.projectRepository.findById(changeProjectActiveStatusRequestDTO.projectId)
                .orElseThrow { ResourceNotFoundException("Project with id ${changeProjectActiveStatusRequestDTO.projectId} not found.") }

        storedProject.active = changeProjectActiveStatusRequestDTO.active
        return this.projectRepository.save(storedProject)
    }

    /**
     * Changes the title of a project.
     * @param changeProjectTitleRequestDTO the request to change the title of a project
     * @return the updated [Project]
     */
    fun changeProjectTitle(changeProjectTitleRequestDTO: ChangeProjectTitleRequestDTO): Project {
        val storedProject =
            this.projectRepository.findById(changeProjectTitleRequestDTO.projectId)
                .orElseThrow { ResourceNotFoundException("Project with id ${changeProjectTitleRequestDTO.projectId} not found.") }

        storedProject.title = changeProjectTitleRequestDTO.newTitle
        return this.projectRepository.save(storedProject)
    }

    /**
     * Deletes a project based on the supplied id
     *
     * @param id the id of the project to be deleted
     */
    fun deleteProject(id: Long) {

        this.projectRepository.findById(id).orElseThrow()
        if (this.submissionRepository.findByProjectID(id).isNotEmpty()) {
            throw ProjectInUseException("Project is still in use!")
        }
        this.projectRepository.deleteById(id)
    }

    /**
     * fetches the project by its id
     * @param id of the project fetched
     */
    fun fetchProjectById(id: Long): UserProjectResponseDTO {
        val allProjects = projectRepository.findAll()
        val project =
            this.projectRepository.findById(id)
                .orElseThrow { ResourceNotFoundException("Project with id $id not found.") }
        return UserProjectResponseDTO(title = project.title, description = project.description)
    }
}
