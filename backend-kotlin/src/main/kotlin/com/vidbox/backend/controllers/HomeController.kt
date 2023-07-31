package com.vidbox.backend.controllers

import com.google.cloud.storage.*
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
class HomeController() {

    @GetMapping("/")
    fun home(): Any {
        return """
        <h1 style="color: green;"> The backend service has started 😊 </h1>
    """.trimIndent()
    }


    @GetMapping("/test")
    fun getAllMovies(): Any {
        return """
            <h1> hi hows it goin </h1>
        """.trimIndent()
    }

}