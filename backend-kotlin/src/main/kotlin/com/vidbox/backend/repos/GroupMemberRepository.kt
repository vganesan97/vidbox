package com.vidbox.backend.repos

import com.vidbox.backend.entities.GroupMembers
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupMemberRepository : JpaRepository<GroupMembers, Int> {

//    @Query(
//        value = """SELECT m.*, CASE WHEN l.movie_id IS NOT NULL THEN true ELSE false END AS liked
//                   FROM movie_infos_top_rated m
//                   LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId
//                   WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')""",
//        countQuery = """SELECT count(*)
//                        FROM movie_infos_top_rated m
//                        LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId
//                        WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')""",
//        nativeQuery = true)
//    fun findByTitleContains2(query: String, pageable: Pageable, userId: Int): Page<MovieInfoTopRatedProjection>




}
