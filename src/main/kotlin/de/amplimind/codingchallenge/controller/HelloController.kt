package de.amplimind.codingchallenge.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// TODO delete this later
@RestController
@RequestMapping("/v1/hello")
class HelloController {
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, World!"
    }
}
