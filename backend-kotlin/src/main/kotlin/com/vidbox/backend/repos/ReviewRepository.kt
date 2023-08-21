package com.vidbox.backend.repos

import com.vidbox.backend.entities.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Int> {

    fun findByUserIdAndMovieId(userId: Int, movieId: Int): Review?

}
