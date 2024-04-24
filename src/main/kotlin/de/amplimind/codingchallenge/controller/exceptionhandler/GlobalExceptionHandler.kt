package de.amplimind.codingchallenge.controller.exceptionhandler

import de.amplimind.codingchallenge.exceptions.EmailFormatException
import de.amplimind.codingchallenge.exceptions.InvalidTokenException
import de.amplimind.codingchallenge.exceptions.NoAuthenticationException
import de.amplimind.codingchallenge.exceptions.ProjectInUseException
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.TokenAlreadyUsedException
import de.amplimind.codingchallenge.exceptions.UserAlreadyExistsException
import de.amplimind.codingchallenge.exceptions.UserAlreadyRegisteredException
import de.amplimind.codingchallenge.exceptions.UserSelfDeleteException
import io.jsonwebtoken.ExpiredJwtException
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

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity("Illegal argument: ${ex.message}", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UserSelfDeleteException::class)
    fun handleException(ex: Exception): ResponseEntity<String> {
        return ResponseEntity("UserSelfDeleteException occurred: ${ex.message}", HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(ex: UserAlreadyExistsException): ResponseEntity<String> {
        return ResponseEntity("User already exists: ${ex.message}", HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(ex: InvalidTokenException): ResponseEntity<String> {
        return ResponseEntity("Token is invalid!", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<String> {
        return ResponseEntity("Token is expired!", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(NoAuthenticationException::class)
    fun handleNoAuthenticationException(ex: NoAuthenticationException): ResponseEntity<String> {
        return ResponseEntity("No authentication: ${ex.message}", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(EmailFormatException::class)
    fun handleNoAuthenticationException(ex: EmailFormatException): ResponseEntity<String> {
        return ResponseEntity("Email is not an email: ${ex.message}", HttpStatus.UNPROCESSABLE_ENTITY)
    }

    @ExceptionHandler(ProjectInUseException::class)
    fun handleProjectIsUseException(ex: ProjectInUseException): ResponseEntity<String> {
        return ResponseEntity("Error whilst deleting project: ${ex.message}", HttpStatus.CONFLICT)
    }

    @ExceptionHandler(TokenAlreadyUsedException::class)
    fun handleTokenAlreadyUsedException(ex: TokenAlreadyUsedException): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UserAlreadyRegisteredException::class)
    fun handleUserAlreadyRegisteredException(ex: UserAlreadyRegisteredException): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }
}
