package com.vidbox.backend.repos

import com.vidbox.backend.entities.GroupInfos
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupInfoRepository : JpaRepository<GroupInfos, Int> {
    @Query(
        value = """SELECT g.*
               FROM group_infos g 
               WHERE g.privacy = 'public' 
               AND to_tsvector('english', g.group_name) @@ to_tsquery('english', :query || ':*')""",
        countQuery = """SELECT count(*) 
                    FROM group_infos g 
                    WHERE g.privacy = 'public' 
                    AND to_tsvector('english', g.group_name) @@ to_tsquery('english', :query || ':*')""",
        nativeQuery = true)
    fun findPublicGroupsByName(query: String, pageable: Pageable): Page<GroupInfos>

    @Query(
        value = """SELECT g.*
           FROM group_infos g 
           WHERE g.group_admin_id = :userId 
           ORDER BY g.created_at DESC 
           LIMIT 1""",
        nativeQuery = true)
    fun findLastGroupInfoByUserId(userId: Int): GroupInfos?

    @Query(
        value = """SELECT g.*
           FROM group_infos g 
           INNER JOIN group_members gm ON g.id = gm.group_id
           WHERE gm.user_id = :userId""",
        countQuery = """SELECT count(*) 
                FROM group_infos g 
                INNER JOIN group_members gm ON g.id = gm.group_id
                WHERE gm.user_id = :userId""",
        nativeQuery = true)
    fun findGroupsByUserId(userId: Int, pageable: Pageable): Page<GroupInfos>

    @Query(
        value = """SELECT EXISTS (
               SELECT 1 
               FROM group_members gm 
               WHERE gm.user_id = :userId 
               AND gm.group_id = :groupId)""",
        nativeQuery = true)
    fun isUserInGroup(userId: Int, groupId: Int): Boolean


}
