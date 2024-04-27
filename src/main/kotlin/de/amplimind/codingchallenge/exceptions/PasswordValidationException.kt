package de.amplimind.codingchallenge.exceptions

/**
 * exception to be thrown if a password does not comply with the validation rules
 */

class PasswordValidationException(msg: String) : Exception(msg)