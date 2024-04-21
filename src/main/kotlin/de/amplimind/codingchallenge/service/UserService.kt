package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.IsAdminDTO
import de.amplimind.codingchallenge.dto.UserInfoDTO
import de.amplimind.codingchallenge.dto.UserStatus
import de.amplimind.codingchallenge.dto.request.ChangePasswordRequestDTO
import de.amplimind.codingchallenge.dto.request.ChangeUserRoleRequestDTO
import de.amplimind.codingchallenge.dto.request.InviteRequestDTO
import de.amplimind.codingchallenge.dto.request.RegisterRequestDTO
import de.amplimind.codingchallenge.exceptions.InvalidTokenException
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.TokenAlreadyUsedException
import de.amplimind.codingchallenge.exceptions.UserAlreadyExistsException
import de.amplimind.codingchallenge.exceptions.UserSelfDeleteException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import de.amplimind.codingchallenge.storage.ResetPasswordTokenStorage
import de.amplimind.codingchallenge.utils.JWTUtils
import de.amplimind.codingchallenge.utils.UserUtils
import de.amplimind.codingchallenge.utils.ValidationUtils
import jakarta.servlet.http.HttpSession
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.streams.asSequence

/**
 * Service for managing users.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
    private val projectRepository: ProjectRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val authenticationProvider: AuthenticationProvider,
    private val resetPasswordTokenStorage: ResetPasswordTokenStorage,
) {
    companion object {
        private const val RESET_PASSWORD_SUBJECT = "Password Reset Requested"
        private const val RESET_PASSWORD_TEXT =
            "You have requested to reset your password for your Amplimind Coding Challenge account." +
                " Please follow the link below to set up a new password:"
        private const val RESET_LINK_PREFIX = "http://localhost:5174/reset-password/"
    }

    private val checkResetPasswordLock = Any()

    /**
     * Fetches all user infos [UserInfoDTO]
     */
    fun fetchAllUserInfos(): List<UserInfoDTO> {
        return this.userRepository.findAll().map {
            UserInfoDTO(
                email = it.email,
                isAdmin = it.role.matchesAny(UserRole.ADMIN),
                status = extractUserStatus(it),
            )
        }
    }

    // TODO maybe remove later, might not be needed
    @Throws(ResourceNotFoundException::class)
    fun fetchUserInfosForEmail(email: String): UserInfoDTO {
        return this.userRepository.findByEmail(email)?.let {
            return UserInfoDTO(
                email = it.email,
                isAdmin = it.role.matchesAny(UserRole.ADMIN),
                status = extractUserStatus(it),
            )
        } ?: throw ResourceNotFoundException("User with email $email was not found")
    }

    /**
     * Deletes a user by its email
     * @param email the email of the user to delete
     * @return the [UserInfoDTO] of the deleted user
     */
    fun deleteUserByEmail(email: String): UserInfoDTO {
        // Check if the user is trying to delete himself
        val auth = SecurityContextHolder.getContext().authentication
        val authenticatedUserEmail = auth?.name
        if (authenticatedUserEmail == email) {
            throw UserSelfDeleteException("User with email $email cannot delete himself")
        }

        // Delete the submissions of the user
        val submissions = this.submissionRepository.findByUserEmail(email)
        if (submissions != null) {
            this.submissionRepository.delete(submissions)
        }

        // Find the user & delete the user
        val user = this.userRepository.findByEmail(email) ?: throw ResourceNotFoundException("User with email $email was not found")
        this.userRepository.delete(user)
        return UserInfoDTO(
            email = user.email,
            isAdmin = user.role.matchesAny(UserRole.ADMIN),
            status = UserStatus.DELETED,
        )
    }

    /**
     * Changes the role of a user.
     * @param changeUserRoleRequestDTO the request to change the role of a user
     * @return the [UserInfoDTO] of the changed user
     */
    fun changeUserRole(changeUserRoleRequestDTO: ChangeUserRoleRequestDTO): UserInfoDTO {
        if (changeUserRoleRequestDTO.newRole.matchesAny(UserRole.INIT)) {
            // Cannot change user role to INIT
            throw IllegalArgumentException("Cannot change user role to INIT")
        }

        val user = UserUtils.fetchLoggedInUser()

        if (user.username == changeUserRoleRequestDTO.email) {
            throw IllegalArgumentException("Cannot change own role")
        }

        val foundUser =
            this.userRepository.findByEmail(changeUserRoleRequestDTO.email)
                ?: throw ResourceNotFoundException("User with email ${changeUserRoleRequestDTO.email} was not found")

        val updatedUser =
            foundUser.let {
                User(
                    email = it.email,
                    password = it.password,
                    role = changeUserRoleRequestDTO.newRole,
                )
            }

        // save the updated user
        this.userRepository.save(updatedUser)

        return UserInfoDTO(
            email = updatedUser.email,
            isAdmin = updatedUser.role.matchesAny(UserRole.ADMIN),
            status = extractUserStatus(updatedUser),
        )
    }

    /**
     * overrride random password with password set by user
     * @param registerRequest
     */

    fun handleRegister(
        registerRequest: RegisterRequestDTO,
        session: HttpSession,
    ) {
        val email: String = JWTUtils.getClaimItem(registerRequest.token, JWTUtils.MAIL_KEY) as String
        val isAdmin: Boolean = JWTUtils.getClaimItem(registerRequest.token, JWTUtils.ADMIN_KEY) as Boolean

        val user =
            userRepository.findByEmail(email)
                ?: throw ResourceNotFoundException("User with email $email was not found")

        if (user.role.matchesAny(UserRole.ADMIN, UserRole.USER)) {
            throw InvalidTokenException("Token was already used")
        }

        setPassword(email, registerRequest.password, isAdmin)

        val authentication: Authentication =
            authenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken(
                    email,
                    registerRequest.password,
                ),
            )

        SecurityContextHolder.getContext().authentication = authentication
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext())
    }

    /**
     * handle the Invite of a new applicant
     * @param email The email of the applicant which should be created and where the email should be sent to
     */
    fun handleInvite(inviteRequest: InviteRequestDTO): UserInfoDTO {
        val user = createUser(inviteRequest)
        emailService.sendEmail(inviteRequest)
        return UserInfoDTO(
            email = user.email,
            isAdmin = inviteRequest.isAdmin,
            status = if (inviteRequest.isAdmin) UserStatus.UNREGISTERED else extractUserStatus(user),
        )
    }

    /**
     * Create a new User
     * @param email The email of the user which should be created
     */
    @Transactional
    fun createUser(inviteRequest: InviteRequestDTO): User {
        val foundUser: User? =
            this.userRepository.findByEmail(inviteRequest.email)

        if (foundUser != null) {
            throw UserAlreadyExistsException("User with email $inviteRequest.email already exists")
        }

        val newUser =
            User(
                email = inviteRequest.email,
                password = passwordEncoder.encode(createPassword(20)),
                role = UserRole.INIT,
            )
        this.userRepository.save(newUser)

        if (!inviteRequest.isAdmin) {
            generateSubmission(newUser)
        }

        return newUser
    }

    fun createPassword(length: Long): String {
        // create Random initial Password
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return ThreadLocalRandom.current()
            .ints(length, 0, charPool.size)
            .asSequence()
            .map(charPool::get)
            .joinToString("")
    }

    fun generateSubmission(user: User) {
        // create new Submission
        // TODO maybe write specific method for this
        val activeProjectIds = projectRepository.findByActive().filter { it.id != null }.filter { it.active }.map { it.id as Long }
        val newSubmission =
            Submission(
                userEmail = user.email,
                expirationDate = Timestamp(0),
                projectID = activeProjectIds[Random.nextInt(activeProjectIds.size)],
                turnInDate = Timestamp(0),
                status = SubmissionStates.INIT,
            )
        this.submissionRepository.save(newSubmission)
    }

    /**
     * Update password for User
     * @param email The email of the user which will be updated
     * @param password The new password that will be set
     */
    fun setPassword(
        email: String,
        password: String,
        isAdmin: Boolean,
    ) {
        val userObject =
            this.userRepository.findByEmail(email)
                ?: throw ResourceNotFoundException("User with email $email was not found")

        val userRole = if (isAdmin) UserRole.ADMIN else UserRole.USER

        val updatedUser =
            userObject.let {
                User(
                    email = it.email,
                    role = userRole,
                    password = passwordEncoder.encode(password),
                )
            }
        this.userRepository.save(updatedUser)
    }

    /**
     * Extracts the [UserStatus] for a provided [User]
     * @param user the user to extract the status from
     * @return the [UserStatus]
     */
    private fun extractUserStatus(user: User): UserStatus {
        // TODO we might have to change this method
        if (user.role.matchesAny(UserRole.ADMIN)) {
            // An admin should not submit anything
            return UserStatus.REGISTERED
        }

        if (user.role.matchesAny(UserRole.INIT)) {
            return UserStatus.UNREGISTERED
        }

        // The user should have a submission if its not the admin
        val submission =
            this.submissionRepository.findByUserEmail(user.email)
                ?: throw IllegalStateException("User has no submission but is not in init state")

        if (hasUserCompletedSubmission(submission)) {
            return UserStatus.SUBMITTED
        }

        if (isUserAlreadyImplementing(submission)) {
            return UserStatus.IMPLEMENTING
        }

        if (hasUserStartedImplementing(submission).not() || isUserRegistered(user)) {
            // User did not start implementing the submission
            return UserStatus.REGISTERED
        }

        // This should never happen
        throw IllegalStateException("The userstatus does not match any criteria")
    }

    /**
     * Checks if the user has completed the submission.
     * @param submission the submission to check
     * @return true if the user has completed the submission
     */
    private fun hasUserCompletedSubmission(submission: Submission): Boolean {
        return submission.status.matchesAny(SubmissionStates.REVIEWED, SubmissionStates.SUBMITTED)
    }

    /**
     * Checks if the user is already implementing the submission.
     * @param submission the submission to check
     * @return true if the user is already implementing the submission
     */
    private fun isUserAlreadyImplementing(submission: Submission): Boolean {
        return submission.status.matchesAny(SubmissionStates.IN_IMPLEMENTATION)
    }

    /**
     * Checks if the user has started implementing the submission.
     * @param submission the submission to check
     * @return true if the user has started implementing the submission
     */
    private fun hasUserStartedImplementing(submission: Submission): Boolean {
        return submission.status.matchesAny(SubmissionStates.INIT).not()
    }

    /**
     * Checks if the user is registered (not init anymore)
     * @param user the user to check
     * @return true if the user is registered
     */
    private fun isUserRegistered(user: User): Boolean {
        return user.role.matchesAny(UserRole.USER, UserRole.ADMIN)
    }

    /**
     * Send a reset password link to the user with the provided email
     * @param email The email to which the reset password link should be sent
     */
    fun requestPasswordChange(email: String) {
        ValidationUtils.validateEmail(email)
        val token =
            JWTUtils.createToken(
                mapOf(JWTUtils.MAIL_KEY to email),
                Date.from(
                    Instant.now().plus(
                        JWTUtils.RESET_PASSWORD_EXPIRATION_MIN,
                        ChronoUnit.MINUTES,
                    ),
                ),
            )
        this.emailService.sendEmail(email, RESET_PASSWORD_SUBJECT, RESET_PASSWORD_TEXT + RESET_LINK_PREFIX + token)
    }

    /**
     * Change the password of the user with the provided token
     * @param changePasswordRequestDTO The request to change the password
     * @throws ResourceNotFoundException if the user with the email from the token was not found
     * @throws InvalidTokenException if the token is invalid
     */
    fun changePassword(changePasswordRequestDTO: ChangePasswordRequestDTO) {
        synchronized(checkResetPasswordLock) {
            this.resetPasswordTokenStorage.isTokenUsed(changePasswordRequestDTO.token)
                .takeIf { it }
                ?.let { throw TokenAlreadyUsedException("Token has already be used") }

            val email = JWTUtils.getClaimItem(changePasswordRequestDTO.token, JWTUtils.MAIL_KEY) as String

            ValidationUtils.validateEmail(email)

            val user = userRepository.findByEmail(email) ?: throw ResourceNotFoundException("User with email $email was not found")

            val updatedUser =
                user.let {
                    User(
                        email = it.email,
                        role = it.role,
                        password = passwordEncoder.encode(changePasswordRequestDTO.newPassword),
                    )
                }
            userRepository.save(updatedUser)
            this.resetPasswordTokenStorage.addToken(changePasswordRequestDTO.token)
        }
    }

    /**
     * checks if the current user is an admin
     * @return if the current user is an admin
     */
    fun fetchLoggedInUserAdminStatus(): IsAdminDTO {
        return IsAdminDTO(fetchUserInfosForEmail(UserUtils.fetchLoggedInUser().username).isAdmin)
    }
}
