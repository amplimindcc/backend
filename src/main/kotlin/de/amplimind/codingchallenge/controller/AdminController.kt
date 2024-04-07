package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.config.SecurityConfig
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(SecurityConfig.ADMIN_PATH)
class AdminController {
    @GetMapping("test")
    fun test(): String {
        return "Hello, Admin!"
    }
}
