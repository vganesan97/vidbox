package com.vidbox.backend.controllers

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.*
import com.google.cloud.storage.StorageException
import com.vidbox.backend.entities.GroupInfos
import com.vidbox.backend.entities.GroupMembers
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.GroupInfo
import com.vidbox.backend.models.LoginCreds
import com.vidbox.backend.models.LoginResponse
import com.vidbox.backend.models.NewUserCreds
import com.vidbox.backend.repos.GroupInfoRepository
import com.vidbox.backend.repos.GroupMemberRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import com.vidbox.backend.services.GCSService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.FileInputStream
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.full.memberProperties


data class AvatarGroupOrUserId(val groupId: Int?, val userId: Int?)
data class GCSSignedURL(val signedUrl: String?)
enum class GCSRequestType { }

@RestController
class HomeController(private val userRepository: UserRepository,
                     private val groupInfoRepository: GroupInfoRepository,
                     private val groupMemberRepository: GroupMemberRepository,
                     private val firebaseService: FirebaseService,
                     private val gcsService: GCSService) {

    @GetMapping("/test")
    fun getAllMovies(): Any {
        return """
            <h1> hi hows it goin </h1>
        """.trimIndent()
    }

    @GetMapping("/avatar/user/put-signed-url")
    fun getPutProfileAvatarSignedURL(request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            val signedUrl = gcsService.updateProfileAvatar(user)
            //val signedUrl = updateProfileAvatar(user)
            ResponseEntity.ok(GCSSignedURL(signedUrl.toString()))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

    @GetMapping("/avatar/group/{groupInfoId}/put-signed-url")
    fun getPutGroupAvatarSignedURL(@PathVariable groupInfoId: Int, request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            //val uid = firebaseService.getUidFromFirebaseToken(request = request)
            //val user = userRepository.findByFirebaseUid(uid)
            val group = groupInfoRepository.findById(groupInfoId)
            if (group.isPresent) {
                val signedUrl = gcsService.updateGroupAvatar(group.get())
                //val signedUrl = updateGroupAvatar(group.get())
                ResponseEntity.ok(GCSSignedURL(signedUrl.toString()))
            } else {
                ResponseEntity.badRequest().body(GCSSignedURL("group for groupId: $groupInfoId does not exist"))
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

    @GetMapping("/avatar/user/get-signed-url")
    fun getGetProfileAvatarSignedURL(request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            //val signedUrl = refreshProfileAvatarSignedURL(user)
            val signedUrl = gcsService.refreshProfileAvatarSignedURL(user)
            ResponseEntity.ok(
                if (signedUrl != null) GCSSignedURL(signedUrl) else GCSSignedURL("")
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

    @GetMapping("/avatar/group/{groupInfoId}/get-signed-url")
    fun getGetGroupAvatarSignedURL(@PathVariable groupInfoId: Int, request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
           // val uid = firebaseService.getUidFromFirebaseToken(request = request)
            // val user = userRepository.findByFirebaseUid(uid)
            val group = groupInfoRepository.findById(groupInfoId)
            if (group.isPresent) {
                //val signedUrl = refreshGroupAvatarSignedURL(group.get())
                val signedUrl = gcsService.refreshGroupAvatarSignedURL(group.get())
                ResponseEntity.ok(
                    if (signedUrl != null) GCSSignedURL(signedUrl) else GCSSignedURL("")
                )
            } else {
                ResponseEntity.badRequest().body(GCSSignedURL("group for groupId: $groupInfoId does not exist"))
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

    @PostMapping("/create-group")
    fun createGroup(@RequestBody groupInfo: GroupInfo, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)

            val group = GroupInfos(
                groupName = groupInfo.group_name,
                groupDescription = groupInfo.group_description,
                groupAdminId = user.id!!,
                privacy = groupInfo.privacy
            )
            groupInfoRepository.save(group)
            val groupMember = GroupMembers(
                groupId = group.id,
                userId = user.id
            )
            groupMemberRepository.save(groupMember)
            println("group: ${group}")
            return ResponseEntity.ok(group)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    @GetMapping("/search-groups")
    fun searchGroups(@RequestParam query: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") size: Int,
                     request: HttpServletRequest): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val pageable = PageRequest.of(page, size)
        val resultsPage = groupInfoRepository.findPublicGroupsByName(query, pageable)

        val x = resultsPage.map { pg ->
            if (pg.groupAvatar != null) {
                //pg.groupAvatar = refreshGroupAvatarSignedURL(pg).toString()
                pg.groupAvatar = gcsService.refreshGroupAvatarSignedURL(pg).toString()
            } else {
                pg.groupAvatar = "https://cdn.britannica.com/39/7139-050-A88818BB/Himalayan-chocolate-point.jpg"
            }

            val groupInfosMap = pg.javaClass.kotlin.memberProperties.associateBy { it.name }.mapValues { it.value.get(pg) }
            val response = groupInfosMap.toMutableMap()
            response["isMember"] = groupInfoRepository.isUserInGroup(userId, pg.id!!) // add your additional field here
            response
        }
        return ResponseEntity.ok(x)
    }

    @GetMapping("/search-groups-get-last")
    fun searchGroupsGetLast(@RequestParam query: String,
                            @RequestParam(defaultValue = "0") page: Int,
                            @RequestParam(defaultValue = "10") size: Int,
                            request: HttpServletRequest): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val lastCreatedGroup = groupInfoRepository.findLastGroupInfoByUserId(userId)
        val x = lastCreatedGroup?.let {
            if (it.groupAvatar != null) {
                //it.groupAvatar = refreshGroupAvatarSignedURL(it).toString()
                it.groupAvatar = gcsService.refreshGroupAvatarSignedURL(it).toString()
            } else {
                it.groupAvatar = "https://cdn.britannica.com/39/7139-050-A88818BB/Himalayan-chocolate-point.jpg"
            }
            it
        } ?: throw NullPointerException()

        // Convert the GroupInfos object to a map
        val groupInfosMap = x.javaClass.kotlin.memberProperties.associateBy { it.name }.mapValues { it.value.get(x) }

        // Create a new map with the additional field
        val response = groupInfosMap.toMutableMap()
        response["isMember"] = groupInfoRepository.isUserInGroup(userId, x.id!!) // add your additional field here

        return ResponseEntity.ok(response)
    }

    @PostMapping("/join-group/{groupId}")
    fun joinGroup(@PathVariable groupId: Int, request: HttpServletRequest): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        var gm: GroupMembers
        try {
            println("group id: $groupId, user id: $userId")
            val groupMember = GroupMembers(
                groupId = groupId,
                userId = userId
            )
            groupMemberRepository.save(groupMember)
            return ResponseEntity.ok(groupMember)
        } catch (e: Exception) {
            throw e
        }
    }

    @GetMapping("/get-groups")
    fun getGroups(request: HttpServletRequest,
                  @RequestParam(defaultValue = "0") page: Int,
                  @RequestParam(defaultValue = "15") size: Int): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        try {
            val pageable = PageRequest.of(page, size)
            val resultsPage = groupInfoRepository.findGroupsByUserId(userId, pageable)

            val x = resultsPage.map { pg ->
                if (pg.groupAvatar != null) {
                    //pg.groupAvatar = refreshGroupAvatarSignedURL(pg).toString()
                    pg.groupAvatar = gcsService.refreshGroupAvatarSignedURL(pg).toString()
                } else {
                    pg.groupAvatar = "https://cdn.britannica.com/39/7139-050-A88818BB/Himalayan-chocolate-point.jpg"
                }

                val groupInfosMap = pg.javaClass.kotlin.memberProperties.associateBy { it.name }.mapValues { it.value.get(pg) }
                val response = groupInfosMap.toMutableMap()
                response["isMember"] = groupInfoRepository.isUserInGroup(userId, pg.id!!) // add your additional field here
                response
            }
            return ResponseEntity.ok(x)
        } catch (e: Exception) {
            throw e
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
            uid = user.firebaseUid!!,
            profilePic = if (user.profilePic != null) user.profilePic!! else ""
        ))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginCreds: LoginCreds, request: HttpServletRequest): ResponseEntity<LoginResponse> {
        try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            return ResponseEntity.ok(LoginResponse(
                message = "Successfully logged in. " +
                        "User exists and their name is ${user.firstName} ${user.lastName}",
                username = user.username!!,
                uid = user.firebaseUid!!,
                profilePic = if (user.profilePic != null) user.profilePic!! else ""
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse(
                message = "An error occurred: ${e.message}",
                username = "unknown",
                uid = "null",
                profilePic = "null"
            ))
        }
    }
}