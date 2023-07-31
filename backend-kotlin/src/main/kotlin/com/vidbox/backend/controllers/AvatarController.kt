package com.vidbox.backend.controllers

import com.vidbox.backend.repos.GroupInfoRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import com.vidbox.backend.services.GCSService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

data class GCSSignedURL(val signedUrl: String?)

@RestController
@RequestMapping("/avatar")
class AvatarController(private val gcsService: GCSService,
                       private val groupInfoRepository: GroupInfoRepository,
                       private val userRepository: UserRepository,
                       private val firebaseService: FirebaseService) {

    @GetMapping("/user/put-signed-url")
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

    @GetMapping("/group/{groupInfoId}/put-signed-url")
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

    @GetMapping("/user/get-signed-url")
    fun getGetProfileAvatarSignedURL(request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val user = userRepository.findByFirebaseUid(uid)
            //val signedUrl = refreshProfileAvatarSignedURL(user)
            val signedUrl = gcsService.refreshProfileAvatarSignedURL(user)
            ResponseEntity.ok(if (signedUrl != null) GCSSignedURL(signedUrl) else GCSSignedURL(""))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

    @GetMapping("/group/{groupInfoId}/get-signed-url")
    fun getGetGroupAvatarSignedURL(@PathVariable groupInfoId: Int, request: HttpServletRequest): ResponseEntity<GCSSignedURL> {
        return try {
            // val uid = firebaseService.getUidFromFirebaseToken(request = request)
            // val user = userRepository.findByFirebaseUid(uid)
            val group = groupInfoRepository.findById(groupInfoId)
            if (group.isPresent) {
                //val signedUrl = refreshGroupAvatarSignedURL(group.get())
                val signedUrl = gcsService.refreshGroupAvatarSignedURL(group.get())
                ResponseEntity.ok(if (signedUrl != null) GCSSignedURL(signedUrl) else GCSSignedURL(""))
            } else {
                ResponseEntity.badRequest().body(GCSSignedURL("group for groupId: $groupInfoId does not exist"))
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(GCSSignedURL("error"))
        }
    }

}
