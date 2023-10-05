package com.vidbox.backend.repos

import com.vidbox.backend.entities.FriendRequest
import com.vidbox.backend.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRequestRepository : JpaRepository<FriendRequest, Int> {

    fun findByStatusAndRequested(status: String, userId: Int): List<FriendRequest>
}
