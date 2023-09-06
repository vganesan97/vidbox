package com.vidbox.backend.controllers

import com.vidbox.backend.repos.MovieLikesRepository
import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.MovieLikes
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.LoginCreds
import com.vidbox.backend.models.LoginResponse
import com.vidbox.backend.models.NewUserCreds
import com.vidbox.backend.repos.MovieInfoTopRatedRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
    fun login(@RequestBody loginCreds: LoginCreds, request: HttpServletRequest): ResponseEntity<LoginResponse> {
        try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            println(user.firstName)
            return ResponseEntity.ok(LoginResponse(
                    message = "Successfully logged in. " +
                            "User exists and their name is ${user.firstName} ${user.lastName}",
                    username = user.username.toString(),
                    firstName = user.firstName.toString(),
                    lastName = user.lastName.toString(),
                    uid = user.firebaseUid!!,
                    profilePic = if (user.profilePic != null) user.profilePic!! else ""
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse(
                    message = "An error occurred: ${e.message}",
                    username = "unknown",
                    firstName = "unknown",
                    lastName = "unknown",
                    uid = "null",
                    profilePic = "null"
            ))
        }
    }

    @PostMapping("/create-user")
    fun createUser(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        val username = userCreds.username
        val password = userCreds.password
        val firstName = userCreds.firstName
        val lastName = userCreds.lastName
        val dob = LocalDate.parse(userCreds.dob)
        val uid = firebaseService.getUidFromFirebaseToken(idToken = userCreds.idToken)

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
                firstName = "unknown",
                lastName = "unknown",
                uid = user.firebaseUid!!,
                profilePic = if (user.profilePic != null) user.profilePic!! else ""
        ))
    }

    @PostMapping("/public-users")
    fun getPublicUsers(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        val username = userCreds.username
        val password = userCreds.password
        val firstName = userCreds.firstName
        val lastName = userCreds.lastName
        val dob = LocalDate.parse(userCreds.dob)
        val uid = firebaseService.getUidFromFirebaseToken(idToken = userCreds.idToken)

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
            firstName = "unknown",
            lastName = "unknown",
            uid = user.firebaseUid!!,
            profilePic = if (user.profilePic != null) user.profilePic!! else ""
        ))
    }

    @PostMapping("/friends")
    fun getFriends(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        val username = userCreds.username
        val password = userCreds.password
        val firstName = userCreds.firstName
        val lastName = userCreds.lastName
        val dob = LocalDate.parse(userCreds.dob)
        val uid = firebaseService.getUidFromFirebaseToken(idToken = userCreds.idToken)

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
            firstName = "unknown",
            lastName = "unknown",
            uid = user.firebaseUid!!,
            profilePic = if (user.profilePic != null) user.profilePic!! else ""
        ))
    }
}
