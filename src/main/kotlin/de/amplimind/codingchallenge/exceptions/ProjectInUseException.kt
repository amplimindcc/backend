package de.amplimind.codingchallenge.exceptions

/**
 * Exception is thrown if project is selected to be deleted but is still in use
 */
class ProjectInUseException(msg: String) : Exception(msg)
