package de.amplimind.codingchallenge.dto

/**
 * Enum class for the status of a user
 */
enum class UserStatus {
    /**
     * The user did not set his password
     */
    UNREGISTERED,

    /**
     * User is registered but has not started implementing his submission
     */
    REGISTERED,

    /**
     * User is currently implementing his submission
     */
    IMPLEMENTING,

    /**
     * User has submitted his submission
     */
    SUBMITTED,

    /**
     * User has been deleted
     */
    DELETED,
}
