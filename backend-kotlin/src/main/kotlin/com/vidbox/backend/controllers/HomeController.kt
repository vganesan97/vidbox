package com.vidbox.backend.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @GetMapping("/home")
    fun getHome(): String {

        return """
            <h1> this is home </h1>
        """.trimIndent()
    }
}