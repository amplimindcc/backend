package de.amplimind.codingchallenge.model

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
}
