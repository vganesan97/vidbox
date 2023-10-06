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
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/friends")
class FriendController(private val userRepository: UserRepository,
                       private val friendRequestRepository: FriendRequestRepository,
                       private val friendRepository: FriendRepository,
                       private val firebaseService: FirebaseService) {

    @PostMapping("/send-friend-request")
    fun sendFriendRequest(request: HttpServletRequest, @RequestBody friendRequest: FriendRequest): ResponseEntity<FriendRequestEntity> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        if (userId != friendRequest.requesterId) return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val friendRequestEntity = FriendRequestEntity(requester = friendRequest.requesterId, requested = friendRequest.requestedId)
        friendRequestRepository.save(friendRequestEntity)
        return ResponseEntity.ok(friendRequestEntity)
    }

    @GetMapping("/get-friend-requests")
    fun getFriendRequests(request: HttpServletRequest): ResponseEntity<List<FriendRequestEntity>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val friendRequests = friendRequestRepository.findByStatusAndRequested("PENDING", userId)
        return ResponseEntity.ok(friendRequests)
    }

    @PostMapping("/accept-friend-request")
    fun acceptFriendRequest(request: HttpServletRequest, @RequestBody friendRequest: FriendRequest): ResponseEntity<Friend> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        if (friendRequest.requestedId != userId) return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val friendRequestEntity = friendRequestRepository.findById(friendRequest.id).get()
        friendRequestEntity.status = "ACCEPTED"
        friendRequestRepository.save(friendRequestEntity)
        val friendEntity = Friend(friendAId = friendRequestEntity.requester, friendBId = friendRequestEntity.requested)
        friendRepository.save(friendEntity)
        return ResponseEntity.ok(friendEntity)
    }

    @GetMapping("/get-friends")
    fun getFriends(request: HttpServletRequest): ResponseEntity<List<User>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val friends = friendRepository.findAllFriendsByUserId(userId)
        return ResponseEntity.ok(friends)
    }
}
