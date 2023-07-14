package com.vidbox.backend.controllers


import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.*
import com.google.cloud.storage.StorageException
import com.vidbox.backend.entities.User
import com.vidbox.backend.models.GroupInfo
import com.vidbox.backend.models.LoginCreds
import com.vidbox.backend.models.LoginResponse
import com.vidbox.backend.models.NewUserCreds
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.FileInputStream
import java.net.URL
import java.nio.file.Paths
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest


data class AvatarGroupOrUserId(val groupId: Int?, val userId: Int?)
data class GCSSignedUrl(val signedUrl: String?)

@RestController
class HomeController(private val userRepository: UserRepository,
                     private val firebaseService: FirebaseService) {

    fun validateInputs(creds: NewUserCreds): Boolean {
        return true
    }

    fun uploadObject(projectId: String, bucketName: String, objectName: String, filePath: String) {
        val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
        val blobId = BlobId.of(bucketName, objectName)
        val blobInfo = BlobInfo.newBuilder(blobId)

            .setContentType("image/jpeg")
            .build()

        // Optional: set a generation-match precondition to avoid potential race
        // conditions and data corruptions. The request returns a 412 error if the
        // preconditions are not met.
        val precondition: Storage.BlobWriteOption = if (storage.get(bucketName, objectName) == null) {
            // For a target object that does not yet exist, set the DoesNotExist precondition.
            // This will cause the request to fail if the object is created before the request runs.
            Storage.BlobWriteOption.doesNotExist()
        } else {
            // If the destination already exists in your bucket, instead set a generation-match
            // precondition. This will cause the request to fail if the existing object's generation
            // changes before the request runs.
            Storage.BlobWriteOption.generationMatch(
                storage.get(bucketName, objectName).generation
            )
        }
        storage.createFrom(blobInfo, Paths.get(filePath), precondition)

        println("File $filePath uploaded to bucket $bucketName as $objectName")
    }



    @GetMapping("/test-upload")
    fun testUpload(): ResponseEntity<Any> {
        uploadObject(
            "vidbox-7d2c1",
            "avatar_pictures",
            "test pic",
            "/Users/vishaalganesan/downloads/unnamed.jpg"
        )
        return ResponseEntity.ok(3)
    }

    @GetMapping("/avatar/user")
    fun updateUserAvatar(request: HttpServletRequest): ResponseEntity<GCSSignedUrl> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            val signedUrl = generateV4PutObjectSignedUrl(true)

            ResponseEntity.ok(GCSSignedUrl(signedUrl.toString()))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedUrl("error"))
        }
    }

    @PostMapping("/avatar/groups/{groupId}")
    fun updateGroupAvatar(@PathVariable groupId: String, request: HttpServletRequest): ResponseEntity<String> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            ResponseEntity.ok(generateV4PutObjectSignedUrl(false).toString())
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("problem occurred, probably unauthorized")
        }
    }

    /**
     * Signing a URL requires Credentials which implement ServiceAccountSigner. These can be set
     * explicitly using the Storage.SignUrlOption.signWith(ServiceAccountSigner) option. If you don't,
     * you could also pass a service account signer to StorageOptions, i.e.
     * StorageOptions().newBuilder().setCredentials(ServiceAccountSignerCredentials). In this example,
     * neither of these options are used, which means the following code only works when the
     * credentials are defined via the environment variable GOOGLE_APPLICATION_CREDENTIALS, and those
     * credentials are authorized to sign a URL. See the documentation for Storage.signUrl for more
     * details.
     */
    @Throws(StorageException::class)
    fun generateV4PutObjectSignedUrl(isUser: Boolean): URL {
        val projectId = "vidbox-7d2c1";
        val bucketName = "avatar_pictures";
        val blobRoot = if (isUser) "profiles" else "groups"
        val blobUUID = UUID.randomUUID().toString()
        val objectName = "${blobRoot}/${blobUUID}";

        val filePath = "/Users/vishaalganesan/downloads/vidbox-7d2c1-firebase-adminsdk-akp4p-f90c0efd75.json"
        val serviceAccount = FileInputStream(filePath)
        val storage = StorageOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setProjectId(projectId)
            .build().service

        // Define Resource
        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build()

        // Generate Signed URL
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

    @PostMapping("/create-group")
    fun createGroup(@RequestBody groupInfo: GroupInfo, request: HttpServletRequest): ResponseEntity<Any> {

        return ResponseEntity.ok(3)
    }

    @PostMapping("/create-user")
    fun createUser(@RequestBody userCreds: NewUserCreds): ResponseEntity<LoginResponse> {
        if (!validateInputs(userCreds)) return ResponseEntity.status(500).body(
            LoginResponse(
                message = "Invalid username or password",
                username = userCreds.username,
                uid = "null"
            ))

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
            uid = user.firebaseUid!!
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
                uid = user.firebaseUid!!
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse(
                message = "An error occurred: ${e.message}",
                username = "unknown",
                uid = "null"
            ))
        }
    }
}