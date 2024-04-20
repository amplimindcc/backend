package de.amplimind.codingchallenge.exceptions

/**
 * Custom exception when a User tries to submit more than once
 */
class SolutionAlreadySubmittedException(msg: String): Exception(msg)
