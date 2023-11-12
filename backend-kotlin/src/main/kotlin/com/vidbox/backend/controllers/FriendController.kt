package com.vidbox.backend.controllers

import com.vidbox.backend.entities.Friend
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.FriendRequest
import com.vidbox.backend.entities.FriendRequest as FriendRequestEntity
import com.vidbox.backend.models.*
import com.vidbox.backend.repos.FriendRepository
import com.vidbox.backend.repos.FriendRequestRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import com.vidbox.backend.services.GCSService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/friends")
class FriendController(private val userRepository: UserRepository,
                       private val friendRequestRepository: FriendRequestRepository,
                       private val friendRepository: FriendRepository,
                       private val firebaseService: FirebaseService,
                       private val gcsService: GCSService
) {

    @PostMapping("/send-friend-request/{friendId}")
    fun sendFriendRequest(request: HttpServletRequest, @PathVariable friendId: Int): ResponseEntity<FriendRequestEntity> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val friendRequestEntity = FriendRequestEntity(
            requester = userId,
            requested = friendId)
        friendRequestRepository.save(friendRequestEntity)
        return ResponseEntity.ok(friendRequestEntity)
    }

    @GetMapping("/get-friend-requests")
    fun getFriendRequests(request: HttpServletRequest): ResponseEntity<List<User>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val friendRequests = friendRequestRepository.findUsersByFriendReqStatusAndRequested("PENDING", userId)
        friendRequests.map { it.profilePic = gcsService.refreshProfileAvatarSignedURL(it) }
        return ResponseEntity.ok(friendRequests)
    }

    @PostMapping("/accept-friend-request/{friendId}")
    fun acceptFriendRequest(request: HttpServletRequest, @PathVariable friendId: Int): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        // Use the custom method to find the friend request
        val friendRequestEntity = friendRequestRepository.findByRequesterAndRequested(friendId, userId)
        if (friendRequestEntity != null) {
            // If the friend request exists, accept it
            friendRequestEntity.status = "ACCEPTED"
            friendRequestRepository.save(friendRequestEntity)

            // Create the Friend entity
            val friendEntity = Friend(friendAId = friendRequestEntity.requester, friendBId = userId)
            friendRepository.save(friendEntity)

            return ResponseEntity.ok(friendEntity)
        } else {
            // If the friend request does not exist, return an error response
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend request not found.")
        }
    }


    @GetMapping("/get-friends")
    fun getFriends(request: HttpServletRequest): ResponseEntity<List<User>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val friends = userRepository.findAllFriendsByUserId(userId)
        friends.map { it.profilePic = gcsService.refreshProfileAvatarSignedURL(it) }
        return ResponseEntity.ok(friends)
    }

    @GetMapping("/get-public-users-not-friends")
    fun getPublicUsersNotFriends(request: HttpServletRequest): ResponseEntity<List<User>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val users = userRepository.findPublicUsersNotFriends(userId)
        users.map { it.profilePic = gcsService.refreshProfileAvatarSignedURL(it) }
        return ResponseEntity.ok(users)
    }
}
