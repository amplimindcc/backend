package de.amplimind.codingchallenge.exceptions

/**
 * Exception which is thrown when a token is used which was already used before
 */
class TokenAlreadyUsedException(msg: String) : Exception(msg)
