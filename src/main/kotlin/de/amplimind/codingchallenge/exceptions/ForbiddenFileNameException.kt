package de.amplimind.codingchallenge.exceptions

/**
 * Exception to be throws when a readme is present in the repository
 */
class ForbiddenFileNameException(msg: String) : Exception(msg)
