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

}
