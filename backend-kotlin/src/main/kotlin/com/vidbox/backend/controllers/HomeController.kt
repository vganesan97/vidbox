package com.vidbox.backend.controllers


import com.vidbox.backend.entities.User
import com.vidbox.backend.models.LoginCreds
import com.vidbox.backend.models.LoginResponse
import com.vidbox.backend.models.NewUserCreds
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest
import kotlin.random.Random

@RestController
class HomeController(private val userRepository: UserRepository,
                     private val firebaseService: FirebaseService) {

    fun validateInputs(creds: NewUserCreds): Boolean {
        return true
    }

    @PostMapping("/create-user")
    fun createUser(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        if (!validateInputs(userCreds)) return ResponseEntity.status(500).body(
            LoginResponse(
                message = "Invalid username or password",
                username = userCreds.username,
                uid = "null"
            ))

        val username = userCreds.username
        val password = userCreds.password
        val firstName = userCreds.firstName
        val lastName = userCreds.lastName
        val dob = LocalDate.parse(userCreds.dob)
        val uid = firebaseService.getUidFromFirebaseToken(userCreds.idToken)

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
            username = userCreds.username,
            uid = user.firebaseUid!!
        ))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginCreds: LoginCreds, request: HttpServletRequest): ResponseEntity<LoginResponse> {
        try {
            val idToken = request.getHeader("Authorization").substring(7)
            val uid = firebaseService.getUidFromFirebaseToken(idToken)
            val user = userRepository.findByFirebaseUid(uid)
            return ResponseEntity.ok(LoginResponse(
                message = "Successfully logged in. " +
                        "User exists and their name is ${user.firstName} ${user.lastName}",
                username = user.username!!,
                uid = user.firebaseUid!!
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse(
                message = "An error occurred: ${e.message}",
                username = "unknown",
                uid = "null"
            ))
        }
    }
}