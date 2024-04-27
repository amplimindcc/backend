package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.DTOExtensions.toSumbissionInfoDTO
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
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
) {
    /**
     * Changes the submission state to reviewed.
     * @param email the email of the user which will be used to find the submission
     * @return the [SubmissionInfoDTO] of the updated submission
     */
    fun changeSubmissionStateReviewed(email: String): SubmissionInfoDTO {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("If the submission for the provided $email was not found.")

        if (submission.status.matchesAny(SubmissionStates.SUBMITTED).not()) {
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

    /**
     * sets the expiration date of a submission when the user fetches it for the first time
     * @param email email of the user
     */
    fun setExpirationIfNotSet(email: String) {
        val submission: Submission = this.submissionRepository.findByUserEmail(email) ?: throw ResourceNotFoundException("If the submission for the provided $email was not found.")
        if (submission.expirationDate != null) {
            return
        }
        val updatedSubmission =
            submission.let {
                Submission(
                    userEmail = it.userEmail,
                    status = it.status,
                    turnInDate = it.turnInDate,
                    projectID = it.projectID,
                    id = it.id,
                    expirationDate = Timestamp.from(Instant.now().plus(DAYS_TILL_DEADLINE, ChronoUnit.DAYS)),
                    version = it.version,
                )
            }
        this.submissionRepository.save(updatedSubmission)
    }

    companion object {
        private const val DAYS_TILL_DEADLINE: Long = 3L
    }
}
