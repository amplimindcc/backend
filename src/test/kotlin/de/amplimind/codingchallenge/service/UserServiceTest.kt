package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.InviteRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.UserAlreadyExistsException
import de.amplimind.codingchallenge.exceptions.UserAlreadyRegisteredException
import de.amplimind.codingchallenge.exceptions.UserSelfDeleteException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import de.amplimind.codingchallenge.storage.ResetPasswordTokenStorage
import de.amplimind.codingchallenge.utils.UserUtils
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
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
    private lateinit var projectRepository: ProjectRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var emailService: EmailService

    @MockK
    private lateinit var authenticationProvider: AuthenticationProvider

    @MockK
    private lateinit var resetPasswordTokenStorage: ResetPasswordTokenStorage

    @MockK
    private lateinit var inviteTokenExpirationService: InviteTokenExpirationService

    @InjectMockKs
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
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
        val user =
            User(
                email = emailToUse,
                password = "password",
                role = UserRole.USER,
            )
        val submission =
            Submission(
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
        every { inviteTokenExpirationService.deleteEntryForUser(any()) } just Runs

        val result = userService.deleteUserByEmail(emailToUse)

        assertEquals(emailToUse, result.email)
        assertFalse(result.isAdmin)
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
        val user =
            User(
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

    /**
     * Test the handle invite method in the [UserService].
     */
    @Test
    fun test_handleInvite() {
        val emailToUse = "1a1new_user@web.de"

        val inviteRequestDTO =
            InviteRequestDTO(
                email = emailToUse,
                isAdmin = true,
            )

        every { userRepository.findByEmail(emailToUse) } returns null
        every { passwordEncoder.encode(any()) } returns "password"
        every { emailService.sendEmail(any(), any(), any()) } just Runs
        every { userRepository.save(any()) } returns User(emailToUse, "password", UserRole.USER)
        every { inviteTokenExpirationService.updateExpirationToken(any(), any()) } just Runs
        every { inviteTokenExpirationService.fetchExpirationDateForUser(any()) } returns anyString()

        val response = userService.handleInvite(inviteRequestDTO)

        assert(response.email == inviteRequestDTO.email)
        assert(response.isAdmin)
    }

    /**
     * Makes sure a exception is thrown when trying to invite a user that already exists.
     */
    @Test
    fun test_handleInvite_fail() {
        val emailToUse = "user@web.de"

        val inviteRequestDTO =
            InviteRequestDTO(
                email = emailToUse,
                isAdmin = true,
            )

        every { userRepository.findByEmail(emailToUse) } returns User(emailToUse, "password", UserRole.USER)
        every { passwordEncoder.encode(any()) } returns "password"
        every { emailService.sendEmail(any(), any(), any()) } just Runs
        every { userRepository.save(any()) } returns User(emailToUse, "password", UserRole.USER)

        assertThrows<UserAlreadyExistsException> { userService.handleInvite(inviteRequestDTO) }
    }

    /**
     * Test the handle resend invite method in the [UserService] when a unknown email is used.
     */
    @Test
    fun test_handle_resend_invite_not_found() {
        every { userRepository.findByEmail(any()) } returns null

        assertThrows<ResourceNotFoundException> { userService.handleResendInvite(InviteRequestDTO("unknown", true)) }
    }

    /**
     * Test the handle resend invite method in the [UserService] when a user is already registered.
     */
    @Test
    fun test_handle_resend_invite_already_registered() {
        every { userRepository.findByEmail(any()) } returns User("email", "password", UserRole.USER)

        assertThrows<UserAlreadyRegisteredException> { userService.handleResendInvite(InviteRequestDTO("email", true)) }
    }
}
