package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieLikes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MovieLikesRepository : JpaRepository<MovieLikes, Int> {

    fun findByUserIdAndMovieId(userId: Int, movieId: Int): MovieLikes?

    @Query("SELECT m FROM MovieInfoTopRated m JOIN MovieLikes l ON m.id = l.movieId WHERE l.userId = :userId")
    fun findLikedMoviesByUserId(userId: Int): List<MovieInfoTopRated>

    fun findAllByUserId(userId: Int): List<MovieLikes>

}

