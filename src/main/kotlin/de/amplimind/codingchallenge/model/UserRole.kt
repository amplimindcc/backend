package de.amplimind.codingchallenge.model

/**
 * The different roles a user can have
 */
enum class UserRole {
    /**
     * The user is in the initialization state:
     * - User has been invited to the platform
     * - User did not react to the invitation and thereby has not set a password yet
     */
    INIT,

    /**
     * The has set a password
     */
    USER,

    /**
     * The user is an admin
     */
    ADMIN,
}
