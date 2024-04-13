package de.amplimind.codingchallenge.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.AeadAlgorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Utils object for creating and validating a JWT as well as reading out certain claim keys
 */
object JWTUtils {
    const val MAIL_KEY = "email"
    const val EXPIRATION_FROM_CREATION: Long = 5

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
     *
     * @return the token
     */
    fun createToken(claims: Map<String, String>): String {
        return Jwts.builder().claims().expiration(
            Date.from(Instant.now().plus(EXPIRATION_FROM_CREATION, ChronoUnit.DAYS)),
        ).add(claims).and().encryptWith(key, enc).compact()
    }

    /**
     * validates a token and optionally can return its claims
     *
     * @param token the toke to validate
     *
     * @return the claims of that token if it is valid
     *
     * @throws SecurityException when the token is invalid
     */
    fun validateToken(token: String): Claims {
        val parsedClaims: Claims
        try {
            parsedClaims = Jwts.parser().decryptWith(key).build().parseEncryptedClaims(token).payload
        } catch (e: Exception) {
            throw SecurityException("Invalid JWT token")
        }
        if (Date.from(Instant.now()).after(parsedClaims.expiration)) {
            throw SecurityException("Token is expired")
        }
        return parsedClaims
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
     * @throws SecurityException if the token is invalid
     */
    fun getClaimItem(
        token: String,
        claimKey: String,
    ): Any? {
        // TODO return type to be specific
        return validateToken(token).getOrElse(claimKey) {
            throw IllegalArgumentException("Key is empty")
        }
    }
}
