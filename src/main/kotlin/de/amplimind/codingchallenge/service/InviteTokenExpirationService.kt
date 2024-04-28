package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.InviteTokenExpiration
import de.amplimind.codingchallenge.repository.InviteTokenExpirationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Service for managing the expiration of invite tokens.
 */
@Service
class InviteTokenExpirationService(
    private val inviteTokenExpirationRepository: InviteTokenExpirationRepository,
) {
    companion object {
        /**
         * The formatter for the expiration date.
         */
        val EXPIRATION_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }

    /**
     * Deletes an entry for a user.
     * @param email the email of the user
     */
    @Transactional
    fun deleteEntryForUser(email: String) = this.inviteTokenExpirationRepository.deleteByEmail(email)

    /**
     * Updates the expiration time for a user.
     * @param email the email of the user
     * @param expirationInMillis the new expiration time in milliseconds
     */
    fun updateExpirationToken(
        email: String,
        expirationInMillis: Long,
    ) {
        this.inviteTokenExpirationRepository.findByEmail(email)?.let {
            it.expirationInMillis = expirationInMillis
            inviteTokenExpirationRepository.save(it)
            return
        }

        inviteTokenExpirationRepository.save(InviteTokenExpiration(email = email, expirationInMillis = expirationInMillis))
    }

    /**
     * Fetches the expiration date for a user.
     * @param email the email of the user
     * @return the expiration date as a string
     */
    fun fetchExpirationDateForUser(email: String): String {
        val inviteToken =
            inviteTokenExpirationRepository.findByEmail(email)
                ?: throw ResourceNotFoundException("No expiration date found for user with email $email")

        return Instant.ofEpochMilli(inviteToken.expirationInMillis)
            .atZone(ZoneId.systemDefault())
            .format(EXPIRATION_DATE_TIME_FORMATTER)
    }
}
