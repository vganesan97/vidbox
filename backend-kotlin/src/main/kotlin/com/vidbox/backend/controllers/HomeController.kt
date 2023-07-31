package com.vidbox.backend.controllers

import com.google.cloud.storage.*
import com.vidbox.backend.repos.GroupInfoRepository
import com.vidbox.backend.repos.GroupMemberRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import com.vidbox.backend.services.GCSService
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
class HomeController(private val userRepository: UserRepository,
                     private val groupInfoRepository: GroupInfoRepository,
                     private val groupMemberRepository: GroupMemberRepository,
                     private val firebaseService: FirebaseService,
                     private val gcsService: GCSService) {

    @GetMapping("/")
    fun home(): Any {
        return """
            <h1> the backend service has started </h1>
        """.trimIndent()
    }

    @GetMapping("/test")
    fun getAllMovies(): Any {
        return """
            <h1> hi hows it goin </h1>
        """.trimIndent()
    }

}