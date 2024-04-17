package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val gitHubService: GitHubService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Adds a new Submission.
     * @param userEmail the email of the user who made the submission
     */
    fun submitCode(submitSolutionRequestDTO: SubmitSolutionRequestDTO, userEmail: String) {
        val submission = this.submissionRepository.findByUserEmail(userEmail)
            ?:throw ResourceNotFoundException("Submission with email $userEmail was not found");

        if(submission.status.matchesAny(SubmissionStates.IN_REVIEW, SubmissionStates.REVIEWED)) {
            throw IllegalStateException("Submission is in state ${submission.status} and can not be submitted")
        }

        val submissionExpirationDate = submission.expirationDate
        val newTurnInDate = Timestamp(System.currentTimeMillis())

        if(TimeUnit.MICROSECONDS.toDays(submissionExpirationDate.time - newTurnInDate.time) <= 0) {
            // TODO: handle too late submission
        }

        CoroutineScope(Dispatchers.IO).launch {
            gitHubService.pushToRepo(submitSolutionRequestDTO, userEmail)
        }

        // TODO: check linting with github api

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
}
