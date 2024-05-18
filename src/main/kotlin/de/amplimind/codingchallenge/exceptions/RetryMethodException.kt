package de.amplimind.codingchallenge.exceptions

/**
 * Custom exception when a method call fails even after retrying
 */
class RetryMethodException(msg: String) : Exception(msg)
