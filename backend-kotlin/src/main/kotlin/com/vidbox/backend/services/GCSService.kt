package com.vidbox.backend.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.vidbox.backend.entities.GroupInfos
import com.vidbox.backend.entities.User
import com.vidbox.backend.repos.GroupInfoRepository
import com.vidbox.backend.repos.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.FileInputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class GCSService(private val userRepository: UserRepository,
                 private val groupInfoRepository: GroupInfoRepository) {

    fun signedUrlHelper(objectName: String): Pair<Storage, BlobInfo> {
        val projectId = "vidbox-7d2c1"
        val bucketName = "avatar_pictures"
        val filePath = "vidbox-7d2c1-firebase-adminsdk-akp4p-f90c0efd75.json"
        val serviceAccount = FileInputStream(filePath)
        val storage = StorageOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setProjectId(projectId)
            .build().service
        return Pair(
            storage,
            BlobInfo.newBuilder(BlobId.of(bucketName, objectName))
                .setContentType("image/jpeg")
                .setCacheControl("public, max-age=3600")
                .build()
        )
    }

    @Throws(StorageException::class)
    fun signedPutUrlDetails(objectName: String): URL {
        val (storage, blobInfo) = signedUrlHelper(objectName)
        val extensionHeaders: MutableMap<String, String> = HashMap()
        extensionHeaders["Content-Type"] = "image/jpeg"
        return storage.signUrl(
            blobInfo,
            7,
            TimeUnit.DAYS,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withExtHeaders(extensionHeaders),
            Storage.SignUrlOption.withV4Signature()
        )
    }

    fun updateProfileAvatar(user: User): URL {
        val profileAvatarAddress = if (user.profilePic != null) user.profilePic!! else "profiles/${UUID.randomUUID()}"
        val signedUrlDetails = signedPutUrlDetails(profileAvatarAddress)
        println("signed url details: ${signedUrlDetails.toString()}")
        // do after getting urlDetails in case getting the signed url throws an exception
        if (user.profilePic == null) createProfileAvatarInDB(user, profileAvatarAddress)
        return signedUrlDetails
    }

    fun updateGroupAvatar(group: GroupInfos): URL {
        val groupAvatarAddress = if (group.groupAvatar != null) group.groupAvatar!! else "groups/${UUID.randomUUID()}"
        val signedUrlDetails = signedPutUrlDetails(groupAvatarAddress)
        // do after getting urlDetails in case getting the signed url throws an exception
        if (group.groupAvatar == null) createGroupAvatarInDB(group, groupAvatarAddress)
        return signedUrlDetails
    }

    fun refreshProfileAvatarSignedURL(user: User): String? {
        if (user.profilePic == null) return null
        val profileAvatarAddress = user.profilePic!!
        return signedGetURL(profileAvatarAddress)!!
    }

    fun refreshGroupAvatarSignedURL(group: GroupInfos): String? {
        if (group.groupAvatar == null) return null
        val groupAvatarAddress = group.groupAvatar!!
        return signedGetURL(groupAvatarAddress)
    }

    fun createProfileAvatarInDB(user: User, objectName: String) {
        try {
            user.profilePic = objectName
            userRepository.save(user)
        } catch (e: Exception) {
            throw e
        }
    }

    fun createGroupAvatarInDB(group: GroupInfos, objectName: String) {
        try {
            group.groupAvatar = objectName
            groupInfoRepository.save(group)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Creates a signed URL for a Cloud CDN endpoint with the given key
     * URL must start with http:// or https://, and must contain a forward
     * slash (/) after the hostname.
     *
     * @param url the Cloud CDN endpoint to sign
     * @param key url signing key uploaded to the backend service/bucket, as a 16-byte array
     * @param keyName the name of the signing key added to the back end bucket or service
     * @param expirationTime the date that the signed URL expires
     * @return a properly formatted signed URL
     * @throws InvalidKeyException when there is an error generating the signature for the input key
     * @throws NoSuchAlgorithmException when HmacSHA1 algorithm is not available in the environment
     */
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    fun signedGetURL(objectPath: String): String? {
        val keyName = "vidbox-avatarss"
        val keyPath = "cdn-signing-key.txt"
        //val requestUrl = "http://34.149.119.30/groups/2d4e9a7d-1f48-483a-9afd-3c5740c9c0bc"
        val requestUrl = "http://34.149.119.30/${objectPath}"


        //val unixTime = expirationTime.time / 1000
        val urlToSign = (requestUrl
                + (if (requestUrl.contains("?")) "&" else "?")
                + "Expires=" + ZonedDateTime.now().plusDays(1).toEpochSecond()
                + "&KeyName=" + keyName)
        val base64String = String(
            Files.readAllBytes(Paths.get(keyPath)),
            StandardCharsets.UTF_8
        )
        val keyBytes = Base64.getUrlDecoder().decode(base64String)
        val encoded: String = getSignature(keyBytes, urlToSign)!!
        val signedUrl = "$urlToSign&Signature=$encoded"
        println("signed url: $signedUrl")
        return signedUrl
    }

    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    fun getSignature(privateKey: ByteArray, input: String): String? {
        val algorithm = "HmacSHA1"
        val offset = 0
        val key: Key = SecretKeySpec(privateKey, offset, privateKey.size, algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(key)
        return Base64.getUrlEncoder().encodeToString(mac.doFinal(input.toByteArray()))
    }

}
