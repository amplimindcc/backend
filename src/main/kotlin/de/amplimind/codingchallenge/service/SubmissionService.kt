package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.extensions.DTOExtensions.toSumbissionInfoDTO
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.createGitHubApiClient
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.concurrent.TimeUnit

/**
 * Service for managing submissions.
 */
@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val gitHubService: GitHubService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    val accessToken = ""

    /**
     * Adds a new Submission.
     * @param userEmail the email of the user who made the submission
     */
    fun submitCode(submitSolutionRequestDTO: SubmitSolutionRequestDTO, userEmail: String) {
        val submission = this.submissionRepository.findByUserEmail(userEmail)
            ?:throw ResourceNotFoundException("Submission with email $userEmail was not found");

        if((submission.status == SubmissionStates.IN_IMPLEMENTATION).not()) {
            throw IllegalStateException("Submission is in state ${submission.status} and can not be submitted")
        }

        val submissionExpirationDate = submission.expirationDate
        val newTurnInDate = Timestamp(System.currentTimeMillis())

        if(TimeUnit.MICROSECONDS.toDays(submissionExpirationDate.time - newTurnInDate.time) <= 0) {
            // TODO: handle too late submission
        }

        val gitHubApiClient = createGitHubApiClient(accessToken)
        val repoName = userEmail.replace('@', '.')
        runBlocking {
            gitHubService.createRepo(gitHubApiClient, repoName)
            gitHubService.pushToRepo(gitHubApiClient, submitSolutionRequestDTO, repoName)
            delay(5000)
            gitHubService.triggerWorkflow(gitHubApiClient, repoName)
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            if(!submissionGitRepositoryExists(userEmail)) {
//            }
//        }

        val updatedSubmission =
            submission.let {
                Submission(
                    userEmail = it.userEmail,
                    expirationDate = it.expirationDate,
                    projectID = it.projectID,
                    turnInDate = newTurnInDate,
                    status = SubmissionStates.SUBMITTED,
                )
            }

        this.submissionRepository.save(updatedSubmission)
    }

    /**
     * Changes the submission state to reviewed.
     * @param email the email of the user which will be used to find the submission
     * @return the [SubmissionInfoDTO] of the updated submission
     */
    fun changeSubmissionStateReviewed(email: String): SubmissionInfoDTO {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("If the submission for the provided $email was not found.")

        if ((submission.status == SubmissionStates.SUBMITTED).not()) {
            // Submission has to be submitted
            throw IllegalStateException("Submission is in state ${submission.status} and can not be changed reviewed.")
        }

        val updatedSubmission =
            submission.let {
                Submission(
                    userEmail = it.userEmail,
                    status = SubmissionStates.REVIEWED,
                    turnInDate = it.turnInDate,
                    projectID = it.projectID,
                    expirationDate = it.expirationDate,
                )
            }

        this.submissionRepository.save(updatedSubmission)

        return updatedSubmission.toSumbissionInfoDTO()
    }

    fun getProjectIdOfUser(email: String): Long {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("Submission for user with email $email was not found.")
        return submission.projectID
    }
}
