package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.EmailFormatException
import org.apache.commons.validator.routines.EmailValidator

object ValidationUtils {
    private val EMAIL_VALIDATOR = EmailValidator.getInstance()

    /**
     * validates a supplied string to check if it is an actual email address
     *
     * @param the string to be checked if it is an email address
     *
     * @throws EmailFormatException if the supplied string is not an email throws this exception
     */
    fun validateEmail(email: String) {
        if (!EMAIL_VALIDATOR.isValid(email)) {
            throw EmailFormatException("Email is not valid")
        }
    }
}
