package com.vidbox.backend.repos

import com.vidbox.backend.entities.MovieInfoTopRated
import org.springframework.data.jpa.repository.JpaRepository

interface MovieInfoTopRatedRepository : JpaRepository<MovieInfoTopRated, Long> {

    // Define additional query methods if needed

}
