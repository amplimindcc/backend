package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.DTOExtensions.toSumbissionInfoDTO
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import org.springframework.stereotype.Service

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
}