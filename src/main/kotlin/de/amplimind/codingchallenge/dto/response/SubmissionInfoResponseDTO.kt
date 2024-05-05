package de.amplimind.codingchallenge.dto.response

import de.amplimind.codingchallenge.model.SubmissionStates
import java.sql.Timestamp

/**
 * Data Transfer Object for submission information which are relevant for the frontend
 * @param userEmail the email of the user
 * @param expirationDate the expiration date of the submission
 * @param projectID the id of the project
 * @param turnInDate the date when the submission was turned in
 * @param status the status of the submission
 */
data class SubmissionInfoResponseDTO(
    val userEmail: String,
    val expirationDate: Timestamp?,
    val projectID: Long,
    val turnInDate: Timestamp?,
    var status: SubmissionStates,
)
