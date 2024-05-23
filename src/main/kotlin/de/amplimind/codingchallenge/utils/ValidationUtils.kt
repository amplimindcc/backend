package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.EmailFormatException
import de.amplimind.codingchallenge.exceptions.PasswordValidationException
import org.apache.commons.validator.routines.EmailValidator

/**
 * Utils class containing validation methods for email and password
 */
object ValidationUtils {
    private val EMAIL_VALIDATOR = EmailValidator.getInstance()
    private val SPECIAL_CHARACTERS = Regex(pattern = "[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")

    /**
     * validates a supplied string to check if it is an actual email address
     *
     * @param email the string to be checked if it is an email address
     *
     * @throws EmailFormatException if the supplied string is not an email throws this exception
     */
    @Throws(EmailFormatException::class)
    fun validateEmail(email: String) {
        if (!EMAIL_VALIDATOR.isValid(email)) {
            throw EmailFormatException("Email is not valid")
        }
    }

    /**
     * validates a supplied string to check if it is a valid password
     *
     * @param password the string to be checked if it is a valid password
     *
     * @throws PasswordValidationException if the supplied string is not a valid password throws this exception
     */
    @Throws(PasswordValidationException::class)
    fun validatePassword(password: String) {
        if (password.length < 8) {
            throw PasswordValidationException("Password must be at least 8 characters long")
        }

        if (!SPECIAL_CHARACTERS.containsMatchIn(password)) {
            throw PasswordValidationException("Password must contain at least one special character")
        }
    }
}
