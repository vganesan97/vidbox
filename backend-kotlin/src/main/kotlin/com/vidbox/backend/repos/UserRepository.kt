package com.vidbox.backend.repos

import com.vidbox.backend.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    // Define additional query methods if needed

}