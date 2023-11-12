package com.vidbox.backend.controllers

import com.vidbox.backend.entities.User
import com.vidbox.backend.models.*
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/user")
class UserController(private val userRepository: UserRepository,
                     private val firebaseService: FirebaseService) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @PostMapping("/login")
    fun login(request: HttpServletRequest, @RequestBody(required = false) newUserCreds: NewUserCreds? = null): ResponseEntity<LoginResponse> {
        logger.warn("this is the login endpoint")
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            logger.warn("user ${user.username} ${user.firstName}")
            ResponseEntity.ok(
                LoginResponse(
                    message = "Successfully logged in. User exists and their name is ${user.firstName} ${user.lastName}",
                    username = user.username.toString(),
                    firstName = user.firstName.toString(),
                    lastName = user.lastName.toString(),
                    uid = user.firebaseUid!!,
                    profilePic = user.profilePic ?: ""
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                LoginResponse(
                    message = "An error occurred: ${e.message}",
                    username = "unknown",
                    firstName = "unknown",
                    lastName = "unknown",
                    uid = "null",
                    profilePic = "null"
                )
            )
        }
    }

    @PostMapping("/create-user")
    fun createUser(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        println("hi")
        println("user creds: $userCreds")
        val username = userCreds.username
        val firstName = userCreds.firstName
        val lastName = userCreds.lastName
        val dob = LocalDate.parse(userCreds.dob)
        val uid = firebaseService.getUidFromFirebaseToken(idToken = userCreds.idToken)

        val user = User(
                username = username,
                firstName = firstName,
                lastName = lastName,
                dob = dob,
                firebaseUid = uid)
        userRepository.save(user)

        return ResponseEntity.ok(LoginResponse(
                message = "Successfully created user",
                username = userCreds.username,
                firstName = user.firstName.toString(),
                lastName = user.lastName.toString(),
                uid = user.firebaseUid!!,
                profilePic = if (user.profilePic != null) user.profilePic!! else ""
        ))
    }
}
