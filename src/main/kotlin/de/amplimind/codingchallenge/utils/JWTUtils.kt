package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.AeadAlgorithm
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Utils object for creating and validating a JWT as well as reading out certain claim keys
 */
object JWTUtils {
    const val MAIL_KEY = "email"
    const val ADMIN_KEY = "admin"

    // TODO move to own configfile
    const val INVITE_LINK_EXPIRATION_DAYS: Long = 5
    const val RESET_PASSWORD_EXPIRATION_MIN: Long = 30

    private val enc: AeadAlgorithm = Jwts.ENC.A256GCM
    private val key: SecretKey =
        SecretKeySpec(
            "70f75b3c5evd0a51294058e0e454b724".toByteArray(),
            "AES",
        ) // TODO move key to config file and use a new key when moving

    /**
     * creates a token with the payload of the supplied claims
     *
     * @param claims map of String for additional properties of the token
     * @param expiration the expiration date of the token
     * @return the token
     */
    fun createToken(
        claims: Map<String, Any>,
        expiration: Date,
    ): String {
        return Jwts.builder().claims().expiration(expiration).add(claims).and().encryptWith(key, enc).compact()
    }

    /**
     * validates a token and optionally can return its claims
     *
     * @param token the toke to validate
     *
     * @return the claims of that token if it is valid
     *
     * @throws ExpiredJwtException when the token is expired
     * @throws InvalidTokenException when the token is invalid
     */
    @Throws(InvalidTokenException::class, ExpiredJwtException::class)
    fun validateToken(token: String): Claims {
        val parsedClaims: Claims
        try {
            parsedClaims = Jwts.parser().decryptWith(key).build().parseEncryptedClaims(token).payload
        } catch (expirationException: ExpiredJwtException) {
            throw expirationException
        } catch (e: Exception) {
            throw InvalidTokenException("Token is invalid!")
        }

        return parsedClaims
    }

    /**
     * checks if a token is expired
     * @param token the token to check
     * @return true if the token is expired, false otherwise
     */
    fun isTokenExpired(token: String): Boolean {
        try {
            Date.from(Instant.now()).after(Jwts.parser().decryptWith(key).build().parseEncryptedClaims(token).payload.expiration)

            return false
        } catch (expirationException: ExpiredJwtException) {
            return true
        }
    }

    /**
     * gets an item that is under the key supplied from the supplied token if that token is valid
     *
     * @param token the token to get the item from
     * @param claimKey the key of the item
     *
     * @return the item beneath the claim key
     *
     * @throws IllegalArgumentException if the key does not contain any items
     * @throws InvalidTokenException if the token is invalid
     * @throws ExpiredJwtException if the token is expired
     */
    @Throws(IllegalArgumentException::class, InvalidTokenException::class, Exception::class)
    fun getClaimItem(
        token: String,
        claimKey: String,
    ): Any? {
        return validateToken(token).getOrElse(claimKey) {
            throw IllegalArgumentException("Key is empty")
        }
    }
}
