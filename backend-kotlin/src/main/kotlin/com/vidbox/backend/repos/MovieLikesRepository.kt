package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieLikes
import org.springframework.data.jpa.repository.JpaRepository

interface MovieLikesRepository : JpaRepository<MovieLikes, Long> {

    fun findByUserIdAndMovieId(userId: Int, movieId: Int): MovieLikes?

    fun deleteByUserIdAndMovieId(userId: Long, movieId: Int): Long
}

