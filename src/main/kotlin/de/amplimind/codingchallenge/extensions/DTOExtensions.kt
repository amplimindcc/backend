package de.amplimind.codingchallenge.extensions

import de.amplimind.codingchallenge.dto.response.SubmissionInfoResponseDTO
import de.amplimind.codingchallenge.model.Submission

/**
 * Extension functions for DTOs
 */
object DTOExtensions {
    /**
     * Converts a [Submission] to a [SubmissionInfoResponseDTO]
     * @return the [SubmissionInfoResponseDTO] with the information of the [Submission]
     */
    fun Submission.toSumbissionInfoDTO() =
        SubmissionInfoResponseDTO(
            userEmail = this.userEmail,
            expirationDate = this.expirationDate,
            projectID = this.projectID,
            turnInDate = this.turnInDate,
            status = this.status,
        )
}
