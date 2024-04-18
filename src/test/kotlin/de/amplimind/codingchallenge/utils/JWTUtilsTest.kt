package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.InvalidTokenException
import io.jsonwebtoken.ExpiredJwtException
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Tests for the [JWTUtils]
 */
class JWTUtilsTest {

    @Test
    fun test_is_token_expired() {
        assert(!JWTUtils.isTokenExpired(notExpiredToken))
        assert(JWTUtils.isTokenExpired(expiredToken))
    }

    @Test
    fun test_validate_Token() {
        assertThrows<ExpiredJwtException> { JWTUtils.validateToken(expiredToken) }
        assertThrows<InvalidTokenException> { JWTUtils.validateToken(TRASH_TOKEN) }
        assertDoesNotThrow { JWTUtils.validateToken(notExpiredToken) }
    }

    companion object {
        private const val TRASH_TOKEN: String = "BlaBlaBla"
        private lateinit var expiredToken: String
        private lateinit var notExpiredToken: String

        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            this.notExpiredToken = JWTUtils.createToken(mapOf(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
            this.expiredToken = JWTUtils.createToken(mapOf(), Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))
        }
    }
}