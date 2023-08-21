package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.MovieLikes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MovieLikesRepository : JpaRepository<MovieLikes, Int> {

    fun findByUserIdAndMovieId(userId: Int, movieId: Int): MovieLikes?

    @Query("""
        SELECT m.id as id,
               m.posterPath as poster_path,
               m.backdropPath as backdrop_path,
               m.overview as overview,
               m.title as title,
               m.releaseDate as release_date,
               m.movieId as movie_id,
               TRUE as liked
        FROM MovieInfoTopRated m
        JOIN MovieLikes l ON m.id = l.movieId
        WHERE l.userId = :userId
    """)
    fun findLikedMoviesByUserId(userId: Int): List<MovieInfoTopRatedProjection>

    @Query(value = """
        SELECT m.id AS id,
               m.poster_path AS poster_path,
               m.backdrop_path AS backdrop_path,
               m.overview AS overview,
               m.title AS title,
               m.release_date AS release_date,
               m.movie_id AS movie_id,
               r.review_content AS reviewContent,
               TRUE AS liked
        FROM movie_infos_top_rated m
        JOIN movie_likes l ON m.id = l.movie_id
        LEFT JOIN reviews r ON m.id = r.movie_id
        WHERE l.user_id = :userId
    """, nativeQuery = true)
    fun findLikedMoviesByUserId4(userId: Int): List<MovieInfoTopRatedProjection>

    fun findAllByUserId(userId: Int): List<MovieLikes>

}

