package com.vidbox.backend.controllers

import com.vidbox.backend.models.FriendRequest
import com.vidbox.backend.entities.FriendRequest as FriendRequestEntity
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.*
import com.vidbox.backend.repos.FriendRequestRepository
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
                     private val friendRequestRepository: FriendRequestRepository,
                     private val firebaseService: FirebaseService) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)


    @PostMapping("/login")
    fun login(request: HttpServletRequest, @RequestBody(required = false) newUserCreds: NewUserCreds? = null): ResponseEntity<LoginResponse> {
        logger.warn("this is the login endpoint")
        return try {
            val uid: String = try {
                firebaseService.getUidFromFirebaseToken(request = request)
            } catch (e: Exception) {
                newUserCreds?.let {
                    createUser2(it)
                } ?: throw Exception("Unable to create user.")
            }

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
        logger.warn("uid $uid")
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

//    @PostMapping("/friends")
//    fun getFriends(request: HttpServletRequest): ResponseEntity<Any> {
//        val uid = firebaseService.getUidFromFirebaseToken(request = request)
//        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
//            ..val friends= userRepository.
//
//    }

    @PostMapping("/send-friend-request")
    fun sendFriendRequest(request: HttpServletRequest, @RequestBody friendRequest: FriendRequest): ResponseEntity<FriendRequestEntity> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        if (userId != friendRequest.requester) return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val friendRequestEntity = FriendRequestEntity(requester = friendRequest.requester, requested = friendRequest.requested)
        friendRequestRepository.save(friendRequestEntity)
        return ResponseEntity.ok(friendRequestEntity)
    }

    @GetMapping("/get-friend-requests")
    fun getFriendRequests(request: HttpServletRequest): ResponseEntity<List<FriendRequestEntity>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val friendRequests = friendRequestRepository.findByStatusAndRequested("PENDING", userId)
        return ResponseEntity.ok(friendRequests)


        // Get friends
        //        val friends = userRepository.findById(userId)?.friends?.map { it.id } ?: emptyList()
        //        return ResponseEntity.ok(friends)

    }

//    @PostMapping("/accept-friend-request")
//    fun acceptFriendRequest(request: HttpServletRequest, @RequestBody friendRequest: FriendRequest): ResponseEntity<FriendRequest> {
//        val uid = firebaseService.getUidFromFirebaseToken(request = request)
//        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
//        // accept friend request
//        val friendRequestEntity = friendRequestRepository.findById(friendRequest.id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
//        // check if request is for current user
//        if(friendRequestEntity.requested != userId) return ResponseEntity(HttpStatus.UNAUTHORIZED)
//        // update status to accepted
//        friendRequestEntity.status = "ACCEPTED"
//        // add friends to each other
//        val requesterUser = userRepository.findById(friendRequestEntity.requester) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
//        val requestedUser = userRepository.findById(friendRequestEntity.requested) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
//        // add friends to each other
//        requesterUser.friends.add(requestedUser)
//
//
//
//    }
}
