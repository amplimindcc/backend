package de.amplimind.codingchallenge.exceptions

/**
 * exception for when a user is already registered when a resend invite was prompted
 */
class UserAlreadyRegisteredException(msg: String) : Exception(msg)
