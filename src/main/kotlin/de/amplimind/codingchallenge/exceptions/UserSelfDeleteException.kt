package de.amplimind.codingchallenge.exceptions

/**
 * Custom exception when a user tries to delete themselves
 */
class UserSelfDeleteException(msg: String) : Exception(msg)