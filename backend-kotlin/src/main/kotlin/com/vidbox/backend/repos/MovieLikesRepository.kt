package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.MovieLikes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MovieLikesRepository : JpaRepository<MovieLikes, Int> {

    fun findByUserIdAndMovieId(userId: Int, movieId: Int): MovieLikes?

    @Query("SELECT m FROM MovieInfoTopRated m JOIN MovieLikes l ON m.id = l.movieId WHERE l.userId = :userId")
    fun findLikedMoviesByUserId1(userId: Int): List<MovieInfoTopRatedProjection>

    @Query("SELECT m.id as id, m.posterPath as poster_path, m.backdropPath as backdrop_path, m.overview as overview, m.title as title, m.releaseDate as release_date, m.movieId as movie_id, TRUE as liked FROM MovieInfoTopRated m JOIN MovieLikes l ON m.id = l.movieId WHERE l.userId = :userId")
    fun findLikedMoviesByUserId(userId: Int): List<MovieInfoTopRatedProjection>

    fun findAllByUserId(userId: Int): List<MovieLikes>

}

