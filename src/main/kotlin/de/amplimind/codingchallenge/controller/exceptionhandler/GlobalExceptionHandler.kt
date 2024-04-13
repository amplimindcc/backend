package de.amplimind.codingchallenge.controller.exceptionhandler

import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.UserSelfDeleteException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Globally used Advice for controller to unify the handling of exceptions and their responses.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(ex: NullPointerException): ResponseEntity<String> {
        return ResponseEntity("NullPointerException occurred: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<String> {
        return ResponseEntity("Resource not found: ${ex.message}", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity("Illegal argument: ${ex.message}", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UserSelfDeleteException::class)
    fun handleException(ex: Exception): ResponseEntity<String> {
        return ResponseEntity("UserSelfDeleteException occurred: ${ex.message}", HttpStatus.CONFLICT)
    }
}
