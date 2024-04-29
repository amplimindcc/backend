package de.amplimind.codingchallenge.exceptions

/**
 * Custom exception when a User exceeds his submission date
 */
class TooLateSubmissionException(msg: String) : Exception(msg)
