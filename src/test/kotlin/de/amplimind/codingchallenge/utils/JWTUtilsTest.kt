package de.amplimind.codingchallenge.utils

import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Tests for the [JWTUtils]
 */
class JWTUtilsTest {

    @Test
    fun test_is_token_expired() {
        val notExpiredToken = JWTUtils.createToken(mapOf(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
        val expiredToken = JWTUtils.createToken(mapOf(), Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))

        assert(!JWTUtils.isTokenExpired(notExpiredToken))
        assert(JWTUtils.isTokenExpired(expiredToken))
    }
}