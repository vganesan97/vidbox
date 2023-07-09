package com.vidbox.backend.repos

import com.vidbox.backend.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {

    fun findByFirebaseUid(firebaseUid: String): User

}