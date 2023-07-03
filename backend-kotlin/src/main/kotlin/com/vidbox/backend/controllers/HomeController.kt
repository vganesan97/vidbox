package com.vidbox.backend.controllers

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.LoginCreds
import com.vidbox.backend.models.LoginResponse
import com.vidbox.backend.models.NewUserCreds
import com.vidbox.backend.repos.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import kotlin.random.Random

@RestController
class HomeController(private val userRepository: UserRepository) {

    val firebaseApp = FirebaseApp.initializeApp()

    fun validateInputs(creds: NewUserCreds): Boolean {

        return true
    }

    @GetMapping("/home")
    fun getHome(): String {
        return """
            <h1>jkjjjkh1>
        """.trimIndent()
    }

    fun getUidFromFirebaseToken(idToken: String): String {
        val uid: String
        // Extract the token from the Authorization header
        try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            uid = decodedToken.uid
            // The token is valid, and we have the user's UID
            // You can use the UID to identify the user in your database
        } catch (e: FirebaseAuthException) {
            // Handle error: The ID token was invalid
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to authenticate user.")
        }
        return uid
    }

    fun randomDate(): LocalDate {
        val randomYears = Random.nextLong(20, 80)  // Generate a random long between 20 and 80
        val randomMonths = Random.nextLong(0, 12)  // Generate a random long between 0 and 12
        val randomDays = Random.nextLong(0, 31)    // Generate a random long between 0 and 31
        return LocalDate.now()
            .minusYears(randomYears)
            .minusMonths(randomMonths)
            .minusDays(randomDays)
    }

    @PostMapping("/create-user")
    fun createUser(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        if (!validateInputs(userCreds)) return ResponseEntity.status(500).body(
            LoginResponse(message = "Invalid username or password", username = userCreds.username))

        val username = userCreds.username
        val password = userCreds.password
        val firstName = userCreds.firstName
        val lastName = userCreds.lastName
        val dob = randomDate()
        val uid = getUidFromFirebaseToken(userCreds.idToken)

        val user = User(
            username = username,
            password = password,
            firstName = firstName,
            lastName = lastName,
            dob = dob,
            firebaseUid = uid)
        userRepository.save(user)

        return ResponseEntity.ok(LoginResponse(
            message = "Successfully created user",
            username = userCreds.username
        ))
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