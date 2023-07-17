package com.vidbox.backend.controllers


import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.*
import com.google.cloud.storage.StorageException
import com.vidbox.backend.entities.GroupInfos
import com.vidbox.backend.entities.GroupMembers
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.GroupInfo
import com.vidbox.backend.models.LoginCreds
import com.vidbox.backend.models.LoginResponse
import com.vidbox.backend.models.NewUserCreds
import com.vidbox.backend.repos.GroupInfoRepository
import com.vidbox.backend.repos.GroupMemberRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.FileInputStream
import java.net.URL
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest


data class AvatarGroupOrUserId(val groupId: Int?, val userId: Int?)
data class GCSSignedURL(val signedUrl: String?)
enum class GCSRequestType { }

@RestController
class HomeController(private val userRepository: UserRepository,
                     private val groupInfoRepository: GroupInfoRepository,
                     private val groupMemberRepository: GroupMemberRepository,
                     private val firebaseService: FirebaseService) {


//    @PostMapping("/avatar/groups/{groupId}")
//    fun updateGroupAvatar(@PathVariable groupId: String, request: HttpServletRequest): ResponseEntity<String> {
//        return try {
//            val uid = firebaseService.getUidFromFirebaseToken(request = request)
//            val user = userRepository.findByFirebaseUid(uid)
//            ResponseEntity.ok(generateV4PutObjectSignedUrl(isUser = false, user).toString())
//        } catch (e: Exception) {
//            ResponseEntity.badRequest().body("problem occurred, probably unauthorized")
//        }
//    }

    fun signedUrlHelper(objectName: String): Pair<Storage, BlobInfo> {
        val projectId = "vidbox-7d2c1"
        val bucketName = "avatar_pictures"
        val filePath = "/Users/vishaalganesan/downloads/vidbox-7d2c1-firebase-adminsdk-akp4p-f90c0efd75.json"
        val serviceAccount = FileInputStream(filePath)
        val storage = StorageOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setProjectId(projectId)
            .build().service
        return Pair(storage, BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build())
    }
    @Throws(StorageException::class)
    fun signedGetUrlDetails(objectName: String): URL {
        val (storage, blobInfo) = signedUrlHelper(objectName)
        return storage.signUrl(blobInfo, 3, TimeUnit.HOURS, Storage.SignUrlOption.withV4Signature())
    }

    @Throws(StorageException::class)
    fun signedPutUrlDetails(objectName: String): URL {
        val (storage, blobInfo) = signedUrlHelper(objectName)
        val extensionHeaders: MutableMap<String, String> = HashMap()
        extensionHeaders["Content-Type"] = "image/jpeg"
        return storage.signUrl(
            blobInfo,
            15,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withExtHeaders(extensionHeaders),
            Storage.SignUrlOption.withV4Signature()
        )
    }

    fun updateProfileAvatar(user: User): URL {
        val profileAvatarAddress = if (user.profilePic != null) user.profilePic!! else "profiles/${UUID.randomUUID()}"
        val signedUrlDetails = signedPutUrlDetails(profileAvatarAddress)
        // do after getting urlDetails in case getting the signed url throws an exception
        if (user.profilePic == null) createProfileAvatarInDB(user, profileAvatarAddress)
        return signedUrlDetails
    }

    fun refreshProfileAvatarSignedURL(user: User): URL? {
        if (user.profilePic == null) return null
        val profileAvatarAddress = user.profilePic!!
        return signedGetUrlDetails(profileAvatarAddress)
    }

    fun createProfileAvatarInDB(user: User, objectName: String) {
        try {
            user.profilePic = objectName
            userRepository.save(user)
        } catch (e: Exception) {
            throw e
        }
    }

    @GetMapping("/avatar/user/put-signed-url")
    fun getPutProfileAvatarSignedURL(request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            val signedUrl = updateProfileAvatar(user)
            ResponseEntity.ok(GCSSignedURL(signedUrl.toString()))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

    @GetMapping("/avatar/user/get-signed-url")
    fun getGetProfileAvatarSignedURL(request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            val signedUrl = refreshProfileAvatarSignedURL(user)
            ResponseEntity.ok(
                if (signedUrl != null) GCSSignedURL(signedUrl.toString()) else GCSSignedURL("")
            )
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
            return ResponseEntity.ok(mapOf(
                "groupName" to group.groupName,
                "groupDescription" to group.groupDescription,
                "groupAdmin" to group.groupAdminId,
                "groupPrivacy" to group.privacy,
                "groupAvatar" to group.groupAvatar,
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    @GetMapping("/search-groups")
    fun searchGroups(@RequestParam query: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") size: Int,
                     request: HttpServletRequest): ResponseEntity<Page<GroupInfos>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val pageable = PageRequest.of(page, size)
        val resultsPage = groupInfoRepository.findPublicGroupsByName(query, pageable)
        return ResponseEntity.ok(resultsPage)
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