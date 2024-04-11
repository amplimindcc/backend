package de.amplimind.codingchallenge.model

enum class SubmissionStates {
    /**
     * The user has not even requested his submission
     */
    INIT,
    IN_IMPLEMENTATION,
    SUBMITTED,
    IN_REVIEW,
    REVIEWED,
}
