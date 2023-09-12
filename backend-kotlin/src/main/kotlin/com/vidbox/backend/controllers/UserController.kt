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

@RestController
@RequestMapping("/user")
class UserController(private val userRepository: UserRepository,
                     private val firebaseService: FirebaseService) {

    @PostMapping("/login")
    fun login(request: HttpServletRequest, @RequestBody(required = false) newUserCreds: NewUserCreds? = null): ResponseEntity<LoginResponse> {
        return try {
            val uid: String = try {
                firebaseService.getUidFromFirebaseToken(request = request)
            } catch (e: Exception) {
                newUserCreds?.let {
                    createUser2(it)
                } ?: throw Exception("Unable to create user.")
            }

            val user = userRepository.findByFirebaseUid(uid)
            println("user ${user.username} ${user.firstName}")

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

    private fun createUser2(newUserCreds: NewUserCreds): String {
        val username = newUserCreds.username
        val firstName = newUserCreds.firstName
        val lastName = newUserCreds.lastName
        val dob = LocalDate.parse(newUserCreds.dob)
        val uid = firebaseService.getUidFromFirebaseToken(idToken = newUserCreds.idToken)

        val user = User(
            username = username,
            firstName = firstName,
            lastName = lastName,
            dob = dob,
            firebaseUid = uid
        )
        userRepository.save(user)
        println("uid $uid")
        return uid
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

    @GetMapping("/public-users")
    fun getPublicUsers(): ResponseEntity<List<User>> {
        val users = userRepository.findByPrivacyLevel("public")
        return ResponseEntity.ok(users)
    }

    @PostMapping("/friends")
    fun getFriends(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
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
            firstName = "unknown",
            lastName = "unknown",
            uid = user.firebaseUid!!,
            profilePic = if (user.profilePic != null) user.profilePic!! else ""
        ))
    }
}
