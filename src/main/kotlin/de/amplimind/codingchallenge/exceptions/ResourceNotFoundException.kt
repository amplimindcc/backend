package de.amplimind.codingchallenge.exceptions

/**
 * Custom exception when a requested resource was not found
 */
class ResourceNotFoundException(msg: String) : Exception(msg)
