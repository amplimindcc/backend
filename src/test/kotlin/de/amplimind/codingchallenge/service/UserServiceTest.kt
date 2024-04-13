package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.ChangeUserRoleRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles

/**
 * Test class for [UserService].
 */
@ActiveProfiles("test")
internal class UserServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var submissionRepository: SubmissionRepository

    @InjectMockKs
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    companion object {
        val adminUser =
            User(
                email = "adminUser@web.de",
                password = "password",
                role = UserRole.ADMIN,
            )
    }

    /**
     * Test that a [IllegalArgumentException] is thrown when trying to change a user role to [UserRole.INIT].
     */
    @Test
    fun test_change_user_role_fail() {
        val changeUserRoleRequest =
            ChangeUserRoleRequestDTO(
                email = "ignore@web.de",
                newRole = UserRole.INIT,
            )

        assertThrows<IllegalArgumentException> { this.userService.changeUserRole(changeUserRoleRequest) }
    }

    /**
     * Test that a [ResourceNotFoundException] is thrown when trying to change the role of a user that does not exist.
     */
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun test_change_user_role_not_found_user() {
        val changeUserRoleRequest =
            ChangeUserRoleRequestDTO(
                email = "doesnotexists@web.de",
                newRole = UserRole.ADMIN,
            )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(adminUser, null)

        every { userRepository.findByEmail(changeUserRoleRequest.email) } returns null

        assertThrows<ResourceNotFoundException> { this.userService.changeUserRole(changeUserRoleRequest) }
    }

    /**
     * Test a successful role change.
     */
    @Test
    fun test_successful_role_change() {
        val changeUserRoleRequest =
            ChangeUserRoleRequestDTO(
                email = "user@web.de",
                newRole = UserRole.ADMIN,
            )

        val storedUser =
            User(
                email = changeUserRoleRequest.email,
                password = "password",
                role = UserRole.USER,
            )

        val updatedUser =
            User(
                email = changeUserRoleRequest.email,
                password = "password",
                role = changeUserRoleRequest.newRole,
            )

        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(adminUser, null)

        val userSlot = slot<User>()

        every { userRepository.findByEmail(changeUserRoleRequest.email) } returns storedUser

        every { userRepository.save(capture(userSlot)) } returns updatedUser

        val result = this.userService.changeUserRole(changeUserRoleRequest)

        assert(userSlot.captured.email == updatedUser.email)
        assert(userSlot.captured.role == updatedUser.role)

        assert(result.email == updatedUser.email)
        assert(result.isAdmin)
    }

    /**
     * Test a successful fetch of all stored user infos
     */
    @Test
    fun test_fetch_all_userInfos() {
        val storedUsers =
            listOf(
                User(
                    email = "a@web.de",
                    password = "password",
                    role = UserRole.USER,
                ),
                User(
                    email = "b@web.de",
                    password = "password",
                    role = UserRole.ADMIN,
                ),
                User(
                    email = "c@web.de",
                    password = "password",
                    role = UserRole.USER,
                ),
            )

        every { userRepository.findAll() } returns storedUsers
        every { submissionRepository.findByUserEmail(any()) } returns
            Submission(
                userEmail = "example@web.de",
                projectID = 1L,
                status = SubmissionStates.SUBMITTED,
                expirationDate = java.sql.Timestamp(System.currentTimeMillis()),
                turnInDate = java.sql.Timestamp(System.currentTimeMillis()),
            )

        val result = this.userService.fetchAllUserInfos()

        assert(result.size == storedUsers.size)
        assert(result.map { it.email }.toList().containsAll(storedUsers.map { it.email }))
    }
}
