package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.dto.response.LintResultResponseDTO
import de.amplimind.codingchallenge.dto.response.SubmissionActiveInfoDTO
import de.amplimind.codingchallenge.dto.response.SubmissionInfoResponseDTO
import de.amplimind.codingchallenge.events.SubmissionStatusChangedEvent
import de.amplimind.codingchallenge.exceptions.ForbiddenFileNameException
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.SolutionAlreadySubmittedException
import de.amplimind.codingchallenge.exceptions.SubmissionException
import de.amplimind.codingchallenge.exceptions.TooLateSubmissionException
import de.amplimind.codingchallenge.exceptions.ZipBombException
import de.amplimind.codingchallenge.extensions.DTOExtensions.toSumbissionInfoDTO
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.utils.UserUtils
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Service for managing submissions.
 */
@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val gitHubService: GitHubService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val DAYS_TILL_DEADLINE: Long = 3L
    }

    /**
     * Adds a new Submission.
     * @param submitSolutionRequestDTO the zip file of the code and the description the user sent
     * @return the [SubmissionInfoResponseDTO] of the updated submission
     */
    fun submitCode(submitSolutionRequestDTO: SubmitSolutionRequestDTO): SubmissionInfoResponseDTO {
        val userEmail = UserUtils.fetchLoggedInUser().username
        val submission =
            this.submissionRepository.findByUserEmail(userEmail)
                ?: throw ResourceNotFoundException("Submission with email $userEmail was not found")

        if ((submission.status == SubmissionStates.IN_IMPLEMENTATION).not()) {
            throw IllegalStateException("Submission is in state ${submission.status} and can not be submitted")
        }

        val submissionExpirationDate = submission.expirationDate
        val newTurnInDate = Timestamp(System.currentTimeMillis())

        if (submissionExpirationDate != null) {
            if (newTurnInDate.after(submissionExpirationDate)) {
                throw TooLateSubmissionException("Too late Submission. Submission was due $submissionExpirationDate")
            }
        }

        val repoName = userEmail.replace('@', '.')
        runBlocking {
            if (gitHubService.submissionGitRepositoryExists(repoName)) {
                throw SolutionAlreadySubmittedException("Submission Repository already exists")
            } else {
                gitHubService.createRepo(userEmail)
            }
            try {
                gitHubService.pushToRepo(submitSolutionRequestDTO, userEmail)
                gitHubService.triggerLintingWorkflow(repoName)
            } catch (e: ForbiddenFileNameException) {
                gitHubService.deleteSubmissionRepository(repoName)
                throw e
            } catch (e: ZipBombException) {
                gitHubService.deleteSubmissionRepository(repoName)
                throw e
            } catch (e: Exception) {
                gitHubService.deleteSubmissionRepository(repoName)
                throw SubmissionException("Submission failed: ${e.message}")
            }
        }

        submission.turnInDate = newTurnInDate
        submission.status = SubmissionStates.SUBMITTED

        this.submissionRepository.save(submission)

        this.eventPublisher.publishEvent(SubmissionStatusChangedEvent(submission))

        return submission.toSumbissionInfoDTO()
    }

    /**
     * Changes the submission state to 'reviewed'.
     * @param email the email of the user which will be used to find the submission
     * @return the [SubmissionInfoResponseDTO] of the updated submission
     */
    fun changeSubmissionStateReviewed(email: String): SubmissionInfoResponseDTO {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("If the submission for the provided $email was not found.")

        if (submission.status.matchesAny(SubmissionStates.SUBMITTED).not()) {
            // Submission has to be submitted
            throw IllegalStateException("Submission is in state ${submission.status} and can not be changed reviewed.")
        }
        submission.status = SubmissionStates.REVIEWED

        this.submissionRepository.save(submission)
        this.eventPublisher.publishEvent(SubmissionStatusChangedEvent(submission))
        return submission.toSumbissionInfoDTO()
    }

    fun getProjectIdOfUser(email: String): Long {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("Submission for user with email $email was not found.")
        return submission.projectID
    }

    /**
     * sets the expiration date of a submission when the user fetches it for the first time
     * @param email email of the user
     */
    fun setExpirationIfNotSet(email: String) {
        val submission: Submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("If the submission for the provided $email was not found.")
        if (submission.expirationDate != null) {
            return
        }

        submission.expirationDate = Timestamp.from(Instant.now().plus(DAYS_TILL_DEADLINE, ChronoUnit.DAYS))
        submission.status = SubmissionStates.IN_IMPLEMENTATION
        this.submissionRepository.save(submission)
        this.eventPublisher.publishEvent(SubmissionStatusChangedEvent(submission))
    }

    fun fetchSubmissionActiveInfo(): SubmissionActiveInfoDTO {
        val userEmail = UserUtils.fetchLoggedInUser().username
        val submission =
            this.submissionRepository.findByUserEmail(userEmail)
                ?: throw ResourceNotFoundException("Submission with email $userEmail was not found")

        val expirationDate = submission.expirationDate

        if (expirationDate == null || submission.status == SubmissionStates.INIT) {
            return SubmissionActiveInfoDTO(
                isStarted = false,
                isExpired = false,
                SubmissionStates.INIT,
            )
        }

        return SubmissionActiveInfoDTO(
            isStarted = true,
            isExpired = expirationDate.before(Timestamp.from(Instant.now())),
            submission.status,
        )
    }

    fun fetchAllSubmissions(): List<SubmissionInfoResponseDTO> {
        return this.submissionRepository.findAll().map { it.toSumbissionInfoDTO() }
    }

    /**
     * Fetches the linting result of the user's submission
     * @param mail the email of the user
     * @return the [LintResultResponseDTO] of the linting result
     */
    fun getLinterResponse(mail: String): LintResultResponseDTO {
        return runBlocking {
            val linterResponse = gitHubService.getLintingResult(mail)
            linterResponse
        }
    }
}
