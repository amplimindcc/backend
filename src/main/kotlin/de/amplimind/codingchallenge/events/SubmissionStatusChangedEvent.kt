package de.amplimind.codingchallenge.events

import de.amplimind.codingchallenge.model.Submission
import org.springframework.context.ApplicationEvent

/**
 * Event which is triggered when the status of a submission has changed
 * @param changedSubmission the submission which [SubmissionState] has changed
 */
data class SubmissionStatusChangedEvent(val changedSubmission: Submission) : ApplicationEvent(changedSubmission)
