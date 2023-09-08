package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MovieInfoTopRatedRepository : JpaRepository<MovieInfoTopRated, Int> {

    @Query("SELECT m.*, CASE WHEN l.movie_id IS NOT NULL THEN true ELSE false END AS liked " +
            "FROM movie_infos_top_rated m " +
            "LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId " +
            "WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')",
        countQuery = "SELECT count(*) FROM movie_infos_top_rated m WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')",
        nativeQuery = true)
    fun findByTitleContains1(query: String, pageable: Pageable, userId: Int): Page<MovieInfoTopRatedProjection>

    @Query(
        value = """SELECT m.*, CASE WHEN l.movie_id IS NOT NULL THEN true ELSE false END AS liked
                   FROM movie_infos_top_rated m 
                   LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId 
                   WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')""",
        countQuery = """SELECT count(*) 
                        FROM movie_infos_top_rated m 
                        LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId 
                        WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')""",
        nativeQuery = true)
    fun findByTitleContains2(query: String, pageable: Pageable, userId: Int): Page<MovieInfoTopRatedProjection>

    @Query(
        value = """
        SELECT m.*, CASE WHEN l.movie_id IS NOT NULL THEN true ELSE false END as liked
        FROM movie_infos_top_rated m 
        LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId
        WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')
    """,
        countQuery = """
        SELECT count(*) 
        FROM movie_infos_top_rated m 
        LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId
        WHERE to_tsvector('english', m.title) @@ to_tsquery('english', :query || ':*')
    """,
        nativeQuery = true
    )
    fun findMoviesWithLikedFlag(query: String, userId: Int, pageable: Pageable): Page<MovieInfoTopRated>

    @Query(
        value = """
            SELECT m.*, CASE WHEN l.movie_id IS NOT NULL THEN true ELSE false END AS liked
            FROM movie_infos_top_rated m 
            LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId
            LEFT JOIN reviews r ON m.id = r.movie_id AND r.user_id = :userId
            WHERE m.id IN :ids
        """,
        countQuery = """
            SELECT count(*) 
            FROM movie_infos_top_rated m 
            LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId 
            LEFT JOIN reviews r ON m.id = r.movie_id AND r.user_id = :userId
            WHERE m.id IN :ids
        """,
        nativeQuery = true)
    fun findByIdsAndUser0(userId: Int, ids: List<Int>, pageable: Pageable): Page<MovieInfoTopRatedProjection>

    @Query(
        value = """
            SELECT m.id AS id,
                   m.poster_path AS poster_path,
                   m.backdrop_path AS backdrop_path,
                   m.overview AS overview,
                   m.title AS title,
                   m.release_date AS release_date,
                   m.movie_id AS movie_id,
                   r.review_content AS reviewContent,
                   CASE WHEN l.movie_id IS NOT NULL THEN true ELSE false END AS liked
            FROM movie_infos_top_rated m 
            LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId 
            LEFT JOIN reviews r ON m.id = r.movie_id AND r.user_id = :userId
            WHERE m.id IN :ids
        """,
        countQuery = """
            SELECT count(*) 
            FROM movie_infos_top_rated m 
            LEFT JOIN movie_likes l ON m.id = l.movie_id AND l.user_id = :userId 
            LEFT JOIN reviews r ON m.id = r.movie_id AND r.user_id = :userId
            WHERE m.id IN :ids
        """,
        nativeQuery = true)
    fun findByIdsAndUser(userId: Int, ids: List<Int>, pageable: Pageable): Page<MovieInfoTopRatedProjection>

}
