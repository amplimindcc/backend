package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.request.LoginRequestDTO
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController {
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequestDTO,
        session: HttpSession,
    ) {
        if (loginRequest.email == "fail") {
            throw RuntimeException("Login failed")
        }

        if (loginRequest.email == "admin" && loginRequest.password == "admin") {
            session.setAttribute("username", "admin")
        }
    }

    @GetMapping("/whoami")
    fun whoAmI(session: HttpSession): String {
        return "${session.getAttribute("username")}, User!"
    }
}
