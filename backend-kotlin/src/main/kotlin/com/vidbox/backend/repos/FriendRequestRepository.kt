package com.vidbox.backend.repos

import com.vidbox.backend.entities.FriendRequest
import com.vidbox.backend.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRequestRepository : JpaRepository<FriendRequest, Int> {

    // Finds User entities who made the friend requests to the requested userId with a specific status
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT fr.requester FROM FriendRequest fr WHERE fr.status = :status AND fr.requested = :requestedUserId)")
    fun findUsersByFriendReqStatusAndRequested(status: String, requestedUserId: Int): List<User>

    fun findByRequesterAndRequested(requesterId: Int, requestedId: Int): FriendRequest?

}
