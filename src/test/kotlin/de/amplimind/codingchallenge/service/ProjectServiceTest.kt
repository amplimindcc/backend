package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.model.Project
import de.amplimind.codingchallenge.repository.ProjectRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.springframework.test.context.ActiveProfiles

/**
 * Test class for [ProjectService].
 */
@ActiveProfiles("test")
internal class ProjectServiceTest {
    @MockK
    lateinit var projectRepository: ProjectRepository

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
}
