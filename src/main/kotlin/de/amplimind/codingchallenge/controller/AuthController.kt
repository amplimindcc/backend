package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.request.LoginRequestDTO
import de.amplimind.codingchallenge.dto.request.RegisterRequestDTO
import de.amplimind.codingchallenge.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth/")
class AuthController(
    private val authenticationProvider: AuthenticationProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
) {
    @Operation(summary = "Entry point for user login")
    @ApiResponse(responseCode = "200", description = "User logged in successfully, and the session id has been supplied successfully")
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequestDTO,
        session: HttpSession,
    ) {
        val authentication: Authentication =
            authenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password,
                ),
            )

        SecurityContextHolder.getContext().authentication = authentication
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext())
    }

    @Operation(summary = "Entry point for invite")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Token is invalid. Repeated requests with the same token will also fail!")
    @ApiResponse(responseCode = "403", description = "Token is expired. Repeated requests with the same token will also fail!")
    @PostMapping("/register")
    fun invite(
        @RequestBody registerRequest: RegisterRequestDTO,
        session: HttpSession,
    ) {
        this.userService.handleRegister(registerRequest, session)
    }

    @Operation(summary = "Entry point to check if a user is logged in")
    @ApiResponse(responseCode = "200", description = "User is logged in")
    @ApiResponse(responseCode = "401", description = "User is not logged in")
    @GetMapping("/check-login")
    fun checkLogin(): ResponseEntity<String> {
        val isAuthenticated =
            SecurityContextHolder.getContext().authentication != null &&
                "anonymousUser" != SecurityContextHolder.getContext().authentication.name

        if (isAuthenticated) {
            return ResponseEntity.ok("User is authenticated")
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated")
    }
}
