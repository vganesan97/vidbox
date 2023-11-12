package com.vidbox.backend.repos

import com.vidbox.backend.entities.Friend
import com.vidbox.backend.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRepository : JpaRepository<Friend, Int> {

//    @Query(value = """
//        SELECT * FROM users WHERE id IN (
//            SELECT frienda FROM friends WHERE friendb = :userId
//            UNION
//            SELECT friendb FROM friends WHERE frienda = :userId
//        )
//    """, nativeQuery = true)
//    fun findAllFriendsByUserId(userId: Int): List<User>
//
//    @Query(value = """
//        SELECT * FROM users
//        WHERE privacy_level = 'public'
//        AND id <> :userId
//        AND id NOT IN (
//            SELECT frienda FROM friends WHERE friendb = :userId
//            UNION
//            SELECT friendb FROM friends WHERE frienda = :userId
//        )
//    """, nativeQuery = true)
//    fun findPublicUsersNotFriends(userId: Int): List<User>


}
