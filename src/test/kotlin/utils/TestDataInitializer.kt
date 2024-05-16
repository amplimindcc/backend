package utils

import de.amplimind.codingchallenge.model.Project
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.sql.Timestamp

/**
 * Class to initialize test data.
 * @param userRepository [UserRepository] to save users.
 * @param submissionRepository [SubmissionRepository] to save submissions.
 * @param projectRepository [ProjectRepository] to save projects.
 * @param passwordEncoder [BCryptPasswordEncoder] to encode passwords.
 */
class TestDataInitializer(
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
    private val projectRepository: ProjectRepository,
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder(),
    private val jdbcTemplate: JdbcTemplate? = null,
) {
    fun initTestData() {
        reset()
        init()
    }

    private fun reset() {
        listOf(userRepository, submissionRepository, projectRepository).forEach {
            it.deleteAll()
        }

        jdbcTemplate?.execute("ALTER TABLE projects ALTER COLUMN id RESTART WITH 1;")
    }

    private fun init() {
        val admin =
            User(
                email = "admin@web.de",
                password = passwordEncoder.encode("admin"),
                role = UserRole.ADMIN,
            )

        val user =
            User(
                email = "user@web.de",
                password = passwordEncoder.encode("user"),
                role = UserRole.USER,
            )

        val initUser =
            User(
                email = "init@web.de",
                password = null,
                role = UserRole.INIT,
            )

        val initUser1 =
            User(
                email = "init1@web.de",
                password = null,
                role = UserRole.INIT,
            )

        val initUser2 =
            User(
                email = "init2@web.de",
                password = null,
                role = UserRole.INIT,
            )

        val initUser3 =
            User(
                email = "init3@web.de",
                password = null,
                role = UserRole.INIT,
            )

        val implementingUser =
            User(
                email = "impl@web.de",
                password = passwordEncoder.encode("impl"),
                role = UserRole.USER,
            )

        val submittedUser =
            User(
                email = "submitted@web.de",
                password = passwordEncoder.encode("submitted"),
                role = UserRole.USER,
            )

        this.userRepository.saveAll(listOf(admin, user, initUser, initUser1, initUser2, initUser3, implementingUser, submittedUser))

        val userSubmission =
            Submission(
                userEmail = user.email,
                status = SubmissionStates.INIT,
                projectID = 1L,
            )

        val initUserSubmission =
            Submission(
                userEmail = initUser.email,
                status = SubmissionStates.INIT,
                projectID = 1L,
            )

        val inImplementationSubmission =
            Submission(
                userEmail = implementingUser.email,
                status = SubmissionStates.IN_IMPLEMENTATION,
                turnInDate = Timestamp(System.currentTimeMillis()),
                projectID = 1L,
                expirationDate = Timestamp(System.currentTimeMillis()),
            )

        val submittedSubmission =
            Submission(
                userEmail = submittedUser.email,
                status = SubmissionStates.SUBMITTED,
                turnInDate = Timestamp(System.currentTimeMillis()),
                projectID = 1L,
                expirationDate = Timestamp(System.currentTimeMillis()),
            )

        this.submissionRepository.saveAll(listOf(inImplementationSubmission, submittedSubmission, initUserSubmission, userSubmission))

        val project1 =
            Project(
                title = "Test Project",
                description = "This is a test description",
                active = true,
                id = 1L,
            )

        val project2 =
            Project(
                title = "Test Project 2",
                description = "This is a test description",
                active = false,
                id = 2L,
            )

        this.projectRepository.saveAll(listOf(project1, project2))
    }
}
