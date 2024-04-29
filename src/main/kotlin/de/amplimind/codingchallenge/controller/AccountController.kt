package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.request.ChangePasswordRequestDTO
import de.amplimind.codingchallenge.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    @ApiResponse(responseCode = "404", description = "User does not exist")
    @PostMapping("request-password-change/{email}")
    fun requestPasswordReset(
        @PathVariable email: String,
    ): ResponseEntity<Unit> {
        this.userService.requestPasswordChange(email)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Changes the password of the user")
    @ApiResponse(responseCode = "200", description = "Password has been changed successfully")
    @ApiResponse(responseCode = "409", description = "Token has already been used")
    @ApiResponse(responseCode = "400", description = "Token is not valid")
    @ApiResponse(responseCode = "412", description = "Password doesnt fulfill the Requirements")
    @PostMapping("change-password")
    fun changePassword(
        @RequestBody changePasswordRequestDTO: ChangePasswordRequestDTO,
    ): ResponseEntity<Unit> {
        this.userService.changePassword(changePasswordRequestDTO)
        return ResponseEntity.ok().build()
    }
}
