package de.amplimind.codingchallenge.model

/**
 * The states a submission can be in
 */
enum class SubmissionStates {
    /**
     * The user has not even requested his submission
     */
    INIT,

    // Will be set by the user when he fetches his project for the first time
    IN_IMPLEMENTATION,

    // Will be set by the user when he submits his project
    SUBMITTED,

    // TODO maybe not needed
    IN_REVIEW,

    // Will be set by the admin when he reviewed the project
    REVIEWED,
}
