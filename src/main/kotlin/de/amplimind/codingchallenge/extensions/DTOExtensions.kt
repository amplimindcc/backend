package de.amplimind.codingchallenge.extensions

import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.model.Submission

/**
 * Extension functions for DTOs
 */
object DTOExtensions {
    /**
     * Converts a [Submission] to a [SubmissionInfoDTO]
     * @return the [SubmissionInfoDTO] with the information of the [Submission]
     */
    fun Submission.toSumbissionInfoDTO() =
        SubmissionInfoDTO(
            userEmail = this.userEmail,
            expirationDate = this.expirationDate,
            projectID = this.projectID,
            turnInDate = this.turnInDate,
            status = this.status,
        )
}
