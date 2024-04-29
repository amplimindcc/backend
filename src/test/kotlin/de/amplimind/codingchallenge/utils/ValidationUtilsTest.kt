package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.EmailFormatException
import de.amplimind.codingchallenge.exceptions.PasswordValidationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for the [ValidationUtils]
 */
class ValidationUtilsTest {
    /**
     * Test to validate if a password is too short
     */
    @Test
    fun validateShortPassword() {
        val password = "short"
        Assertions.assertThrows(PasswordValidationException::class.java) {
            ValidationUtils.validatePassword(password)
        }
    }

    /**
     * Test to validate if a password does not contain a special character
     */
    @Test
    fun validateSpecialCharacterPassword() {
        val password = "NoSpecialCharacters"
        Assertions.assertThrows(PasswordValidationException::class.java) {
            ValidationUtils.validatePassword(password)
        }
    }

    /**
     * Test to validate if a password is valid
     */
    @Test
    fun validatePasswordSuccess() {
        val password = "Valid@Password"
        Assertions.assertDoesNotThrow {
            ValidationUtils.validatePassword(password)
        }
    }

    /**
     * Test to validate if an email is invalid
     */
    @Test
    fun validateEmailError() {
        val email = "invalidEmail"
        Assertions.assertThrows(EmailFormatException::class.java) {
            ValidationUtils.validateEmail(email)
        }
    }

    /**
     * Test to validate if an email is valid
     */
    @Test
    fun validateEmailSuccess() {
        val email = "valid@email.com"
        Assertions.assertDoesNotThrow {
            ValidationUtils.validateEmail(email)
        }
    }
}
