package de.amplimind.codingchallenge.dto.response

import de.amplimind.codingchallenge.model.SubmissionStates

/**
 * Data Transfer Object for submission active information
 * @param isStarted if the submission has been started
 * @param isExpired if the submission is expired
 * @param submissionStates the submission states
 */
class SubmissionActiveInfoDTO(
    val isStarted: Boolean,
    val isExpired: Boolean,
    private val submissionStates: SubmissionStates,
)
