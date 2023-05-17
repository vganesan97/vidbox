package com.vidbox.backend.controllers

import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.repos.MovieInfoTopRatedRepository
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/movies")
class MovieController(private val movieRepository: MovieInfoTopRatedRepository) {

    @GetMapping("/home")
    fun getHome(): String {
        return """
            <h1> this is home </h1>
        """.trimIndent()
    }

    @GetMapping("/all_movies")
    fun getAllMovies(): Any {
        val pageable = PageRequest.of(0, 10)
        val cont = movieRepository.findAll(pageable).content.map { it.title }
        return """
            <h1> ${cont.size} , $cont </h1>
        """.trimIndent()
    }
}
