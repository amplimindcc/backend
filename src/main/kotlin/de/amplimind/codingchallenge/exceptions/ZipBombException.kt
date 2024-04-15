package de.amplimind.codingchallenge.exceptions

/**
 * Custom exception for when a potential zip bomb was found
 */
class ZipBombException(msg: String) : Exception(msg)
