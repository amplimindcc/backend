package de.amplimind.codingchallenge.exceptions

/**
 * Exception to be thrown when rate limit is reached
 */
class RateLimitException(msg: String) : Exception(msg)
