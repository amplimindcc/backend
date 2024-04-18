package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to handle account related requests
 */
@RestController
@RequestMapping("/v1/account/")
class AccountController(
    private val userService: UserService,
) {
    @Operation(summary = "Sends a email to the user with a link to reset the password")
    @ApiResponse(responseCode = "200", description = "Email has been sent. This will always return 200, even if the email does not exist.")
    @ApiResponse(responseCode = "422", description = "Email is not a valid email address")
    @PostMapping("reset-password/{email}")
    fun requestPasswordReset(
        @PathVariable email: String,
    ): ResponseEntity<Unit> {
        this.userService.requestPasswordChange(email)
        return ResponseEntity.ok().build()
    }
}
