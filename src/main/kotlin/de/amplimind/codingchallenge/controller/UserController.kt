package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.IsAdminDTO
import de.amplimind.codingchallenge.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for user checkup related tasks
 */
@RestController
@RequestMapping("/v1/user")
class UserController(private val userService: UserService) {
    @Operation(summary = "Entry point to check if a user is an admin")
    @ApiResponse(responseCode = "200", description = "Successfully received the result if the user is an admin")
    @GetMapping("/check-admin")
    fun requestIsAdmin(): ResponseEntity<IsAdminDTO> {
        return ResponseEntity.ok(userService.fetchLoggedInUserAdminStatus())
    }
}
