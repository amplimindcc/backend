package de.amplimind.codingchallenge.dto

/**
 * Data transfer object for the full user information
 * @param email the email of the user
 * @param isAdmin if the user is a admin
 * @param status the status of the user
 * @param canBeReinvited if the user can be reinvited
 * @param inviteTokenExpiration the expiration date of the user`s invite token (dd.MM.yyyy hh:mm)
 */
data class FullUserInfoDTO(
    val email: String,
    val isAdmin: Boolean,
    val status: UserStatus,
    val canBeReinvited: Boolean,
    val inviteTokenExpiration: String = ""
)
