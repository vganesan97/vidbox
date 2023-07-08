package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieInfoTopRated
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MovieInfoTopRatedRepository : JpaRepository<MovieInfoTopRated, Long> {
    //@Query("SELECT m FROM MovieInfoTopRated m WHERE lower(m.title) LIKE lower(concat('%', :query, '%'))")
    @Query(
        value = "SELECT * FROM movie_infos_top_rated m WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')",
        countQuery = "SELECT count(*) FROM movie_infos_top_rated m WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')",
        nativeQuery = true
    )
    fun findByTitleContains(query: String, pageable: Pageable): Page<MovieInfoTopRated>
}
