package de.amplimind.codingchallenge.controller.exceptionhandler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(ex: NullPointerException): ResponseEntity<String> {
        return ResponseEntity("NullPointerException occurred: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
