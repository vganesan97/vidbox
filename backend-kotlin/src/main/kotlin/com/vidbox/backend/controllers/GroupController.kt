package com.vidbox.backend.controllers

import com.vidbox.backend.entities.*
import com.vidbox.backend.models.GroupInfo
import com.vidbox.backend.repos.*
import com.vidbox.backend.services.FirebaseService
import com.vidbox.backend.services.GCSService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.full.memberProperties

@RestController
@RequestMapping("/group")
class GroupController(private val groupInfoRepository: GroupInfoRepository,
                      private val userRepository: UserRepository,
                      private val firebaseService: FirebaseService,
                      private val groupMemberRepository: GroupMemberRepository,
                      private val gcsService: GCSService) {

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
}
