package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.UserStatus
import de.amplimind.codingchallenge.dto.request.ChangeUserRoleRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.UserSelfDeleteException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
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

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    private lateinit var userService: UserService

    @MockK
    private lateinit var emailService: EmailService

    @InjectMockKs


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

    /**
     * Test a successful deletion of a user by email
     */
    @Test
    fun test_successfullyDeletesUser() {
        val emailToUse = "user@web.de"
        val user = User(
                email = emailToUse,
                password = "password",
                role = UserRole.USER,
        )
        val submission = Submission(
                userEmail = emailToUse,
                projectID = 1L,
                status = SubmissionStates.SUBMITTED,
                expirationDate = java.sql.Timestamp(System.currentTimeMillis()),
                turnInDate = java.sql.Timestamp(System.currentTimeMillis()),
        )

        every { userRepository.findByEmail(emailToUse) } returns user
        every { submissionRepository.findByUserEmail(emailToUse) } returns submission
        every { submissionRepository.delete(submission) } just Runs
        every { userRepository.delete(user) } just Runs

        val result = userService.deleteUserByEmail(emailToUse)

        assertEquals(emailToUse, result.email)
        assertFalse(result.isAdmin)
        assertEquals(UserStatus.DELETED, result.status)
    }

    /**
     * Test that a [ResourceNotFoundException] is thrown when trying to delete a user that does not exist.
     */
    @Test
    fun test_userNotFound() {
        val emailToUse = "unknown@web.de"

        every { userRepository.findByEmail(emailToUse) } returns null
        every { submissionRepository.findByUserEmail(emailToUse) } returns null

        assertThrows<ResourceNotFoundException> { userService.deleteUserByEmail(emailToUse) }
    }

    /**
     * Test that a [UserSelfDeleteException] is thrown when the user tries to delete himself.
     */
    @Test
    fun test_selfDelete() {
        val emailToUse = "adminUser@web.de"
        val user = User(
                email = emailToUse,
                password = "password",
                role = UserRole.ADMIN,
        )
        every { userRepository.findByEmail(emailToUse) } returns user
        every { submissionRepository.findByUserEmail(emailToUse) } returns null
        every { userRepository.delete(user) } just Runs

        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(emailToUse, null)

        assertThrows<UserSelfDeleteException> { userService.deleteUserByEmail(emailToUse) }
    }
}
