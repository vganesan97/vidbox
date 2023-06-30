package com.vidbox.backend.controllers

import com.google.firebase.FirebaseApp
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class LoginCreds(val username: String, val password: String)
data class LoginResponse(val message: String, val username: String)

@RestController
class HomeController {

    val firebaseApp = FirebaseApp.initializeApp()

    @GetMapping("/home")
    fun getHome(): String {

        return """
            <h1>jkjjjkh1>
        """.trimIndent()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginCreds: LoginCreds): ResponseEntity<LoginResponse> {
        // Replace this with your own authentication logic
        return if (loginCreds.username == "admin" && loginCreds.password == "pass") {
            ResponseEntity.ok(LoginResponse(
                message = "Successfully logged in",
                username = loginCreds.username
            ))
        } else {
            ResponseEntity.status(401).body(
                LoginResponse(
                message = "Unauthorized",
                username = loginCreds.username
            )
            )
        }
    }

}