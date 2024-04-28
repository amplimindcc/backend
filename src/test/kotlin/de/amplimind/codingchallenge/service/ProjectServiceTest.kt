package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.ChangeProjectActiveStatusRequestDTO
import de.amplimind.codingchallenge.dto.request.ChangeProjectTitleRequestDTO
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.Project
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.springframework.test.context.ActiveProfiles
import java.util.Optional

/**
 * Test class for [ProjectService].
 */
@ActiveProfiles("test")
internal class ProjectServiceTest {
    @MockK
    lateinit var projectRepository: ProjectRepository

    @MockK
    private lateinit var submissionRepository: SubmissionRepository

    @InjectMockKs
    lateinit var projectService: ProjectService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    /**
     * Test that a project is added correctly.
     */
    @Test
    fun test_add_project() {
        val createProjectRequest =
            CreateProjectRequestDTO(
                title = "Test Project",
                description = "This is a test description",
                active = true,
            )

        val project =
            Project(
                title = createProjectRequest.title,
                description = createProjectRequest.description,
                active = createProjectRequest.active,
            )

        val projectSlot = slot<Project>()

        every { projectRepository.save(capture(projectSlot)) } returns any()

        projectService.addProject(createProjectRequest)

        verify(exactly = 1) { projectRepository.save(any()) }

        // Make sure the project is the same as the one we expect
        assert(
            projectSlot.captured.let {
                it.title == project.title &&
                    it.description == project.description &&
                    it.active == project.active
            },
        )
    }

    /**
     * Test that all projects are fetched correctly.
     */
    @Test
    fun fetch_all_projects_test() {
        val exampleProjects: List<Project> =
            listOf(
                Project(
                    title = "Test Project 1",
                    description = "This is a test description",
                    active = true,
                ),
                Project(
                    title = "Test Project 2",
                    description = "This is a test description",
                    active = false,
                ),
            )

        every { projectRepository.findAll() } returns exampleProjects

        val fetchedProjects = projectService.fetchAllProjects()

        assert(fetchedProjects == exampleProjects)
    }

    /**
     * Test that a project's title is changed correctly.
     */
    @Test
    fun test_change_project_active() {
        val projectId = 1L
        val active = false

        val project =
            Project(
                id = projectId,
                title = "Test Project",
                description = "This is a test description",
                active = true,
            )

        val changeProjectActiveStatusRequestDTO =
            ChangeProjectActiveStatusRequestDTO(
                projectId = projectId,
                active = active,
            )

        every { projectRepository.findById(projectId) } returns Optional.of(project)
        every { projectRepository.save(project) } returns project

        val updatedProject = projectService.changeProjectActive(changeProjectActiveStatusRequestDTO)

        assert(updatedProject.active == active)
    }

    /**
     * Test that an exception is thrown when trying to change the active state of a project that does not exist.
     */
    @Test
    fun change_project_active_state_failure() {
        val changeProjectActiveStatusRequestDTO =
            ChangeProjectActiveStatusRequestDTO(
                projectId = -1,
                active = true,
            )

        every { projectRepository.findById(changeProjectActiveStatusRequestDTO.projectId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            projectService.changeProjectActive(changeProjectActiveStatusRequestDTO)
        }
    }

    /**
     * Test that a project's title is changed correctly.
     */
    @Test
    fun test_change_project_title() {
        val projectId = 1L
        val newTitle = "New Title"

        val project =
            Project(
                id = projectId,
                title = "Test Project",
                description = "This is a test description",
                active = true,
            )

        val changeProjectTitleRequestDTO =
            ChangeProjectTitleRequestDTO(
                projectId = projectId,
                newTitle = newTitle,
            )

        every { projectRepository.findById(projectId) } returns Optional.of(project)
        every { projectRepository.save(project) } returns project

        val updatedProject = projectService.changeProjectTitle(changeProjectTitleRequestDTO)

        assert(updatedProject.title == newTitle)
    }

    /**
     * Test that an exception is thrown when trying to change the title of a project that does not exist.
     */
    @Test
    fun change_project_title_failure() {
        val changeProjectTitleRequestDTO =
            ChangeProjectTitleRequestDTO(
                projectId = -1,
                newTitle = "New Title",
            )

        every { projectRepository.findById(changeProjectTitleRequestDTO.projectId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            projectService.changeProjectTitle(changeProjectTitleRequestDTO)
        }
    }
}
