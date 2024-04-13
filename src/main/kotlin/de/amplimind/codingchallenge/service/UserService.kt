package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.UserInfoDTO
import de.amplimind.codingchallenge.dto.UserStatus
import de.amplimind.codingchallenge.dto.request.ChangeUserRoleRequestDTO
import de.amplimind.codingchallenge.dto.request.RegisterRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.UserAlreadyExistsException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.jwt.JWTUtils
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence


/**
 * Service for managing users.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
) {

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
     * Changes the role of a user.
     * @param changeUserRoleRequestDTO the request to change the role of a user
     * @return the [UserInfoDTO] of the changed user
     */
    fun changeUserRole(changeUserRoleRequestDTO: ChangeUserRoleRequestDTO): UserInfoDTO {
        if (changeUserRoleRequestDTO.newRole.matchesAny(UserRole.INIT)) {
            // Cannot change user role to INIT
            throw IllegalArgumentException("Cannot change user role to INIT")
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

    fun handleRegister(registerRequest: RegisterRequestDTO){
        val email: String = JWTUtils.getClaimItem(registerRequest.token, JWTUtils.MAIL_KEY) as String
        setPassword(email, registerRequest.password)
    }

    /**
     * handle the Invite of a new applicant
     * @param email The email of the applicant which should be created and where the email should be sent to
     */

    fun handleInvite(email: String): UserInfoDTO {
        val user = createUser(email)
        emailService.sendEmail(email)
        return UserInfoDTO(
            email = user.email,
            isAdmin = user.role.matchesAny(UserRole.ADMIN),
            status = extractUserStatus(user),
        )
    }


    /**
     * Create a new User
     * @param email The email of the user which should be created
     */
    fun createUser(email: String): User{

        // check if user already exists
        val foundUser: User? =
            this.userRepository.findByEmail(email)

        if (foundUser != null){
            throw UserAlreadyExistsException("User with email $email already exists")
        }

        // create Random initial Password
        val STRING_LENGTH: Long = 20
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        val randomPwd: CharSequence = ThreadLocalRandom.current()
            .ints(STRING_LENGTH.toLong(), 0, charPool.size)
            .asSequence()
            .map(charPool::get)
            .joinToString("")

        // create new User
        val newUser = User(
            email = email,
            password = passwordEncoder.encode(randomPwd),
            role = UserRole.INIT,
        )
        this.userRepository.save(newUser)

        return newUser
    }



    /**
     * Update password for User
     * @param email The email of the user which will be updated
     * @param password The new password that will be set
     */
    fun setPassword(email: String, password: String) {
        val userObject = this.userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User with email ${email} was not found")

        val updatedUser =
            userObject.let {
                User(
                    email = it.email,
                    role = UserRole.USER,
                    password = passwordEncoder.encode(password)
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
        if (user.role.matchesAny(UserRole.ADMIN)) {
            // An admin should not submit anything
            return UserStatus.REGISTERED
        }

        // The user should have a submission if its not the admin

        val submission =
            this.submissionRepository.findByUserEmail(user.email)
                ?: throw IllegalStateException("User has no submission but is not in init state")

        if (user.role.matchesAny(UserRole.INIT)) {
            return UserStatus.UNREGISTERED
        }

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
        return submission.status.matchesAny(SubmissionStates.REVIEWED, SubmissionStates.IN_REVIEW, SubmissionStates.SUBMITTED)
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
}
