package com.vidbox.backend.controllers

import com.vidbox.backend.repos.MovieLikesRepository
import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieLikes
import com.vidbox.backend.repos.MovieInfoTopRatedRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/movies")
class MovieController(private val movieRepository: MovieInfoTopRatedRepository,
                      private val movieLikesRepository: MovieLikesRepository,
                      private val userRepository: UserRepository,
                      private val firebaseService: FirebaseService) {

    @PostMapping("/like-movie")
    fun likeMovie(@RequestBody like: MovieLikes, request: HttpServletRequest): ResponseEntity<Any> {
        val movieId = like.movieId ?: throw IllegalArgumentException("Movie ID is null")

        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)

            val existingLike = movieLikesRepository.findByUserIdAndMovieId(userId, movieId)

            if (existingLike == null) {
                val savedLike = MovieLikes(
                    userId = userId,
                    movieId = movieId
                )
                val saved = movieLikesRepository.save(savedLike)
                println("saved like $savedLike")
                ResponseEntity.ok(mapOf("liked" to true))
            } else {
                movieLikesRepository.deleteById(existingLike.id!!)
                println("deleted like $existingLike")
                ResponseEntity.ok(mapOf("liked" to false))
            }
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
    }

    @GetMapping("/liked-movies")
    fun likedMovies(request: HttpServletRequest): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val likedMovies = movieLikesRepository.findLikedMoviesByUserId(userId)
        return ResponseEntity.ok(likedMovies)
    }

    @GetMapping("/search-movies")
    fun searchMovies(@RequestParam query: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") size: Int): ResponseEntity<Page<MovieInfoTopRated>> {
        val pageable = PageRequest.of(page, size)
        val resultsPage = movieRepository.findByTitleContains(query, pageable)
        return ResponseEntity.ok(resultsPage)
    }

    @GetMapping("/all_movies")
    fun getAllMovies(): Any {
        val pageable = PageRequest.of(0, 10)
        val page = movieRepository.findAll(pageable)
        val titles = page.content.map { it.title }
        return """
            <h1> ${page.size} , $titles </h1>
        """.trimIndent()
    }
}
