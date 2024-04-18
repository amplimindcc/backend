package de.amplimind.codingchallenge.storage

import de.amplimind.codingchallenge.utils.JWTUtils
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Storage for reset password tokens which are used but did not expire yet
 */
@Component
class ResetPasswordTokenStorage {
    /**
     * Set of used tokens which did not expire yet but were sent to the user
     */
    private val usedNonExpiredToken: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    init {
        // Schedule the check for expired tokens to run every 15 minutes
        scheduler.scheduleAtFixedRate({
            checkAndRemoveExpiredTokens()
        }, 0, 15, TimeUnit.MINUTES)
    }

    private fun checkAndRemoveExpiredTokens() {
        val currentTokens = usedNonExpiredToken.toList()
        for (token in currentTokens) {
            if (JWTUtils.isTokenExpired(token)) {
                usedNonExpiredToken.remove(token)
            }
        }
    }

    fun addToken(token: String) {
        this.usedNonExpiredToken.add(token)
    }
}
