package com.vidbox.backend.controllers

import com.google.gson.JsonParser
import com.vidbox.backend.repos.MovieLikesRepository
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.MovieLikes
import com.vidbox.backend.repos.MovieInfoTopRatedRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import com.vidbox.backend.services.SearchService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.math.BigDecimal
import java.math.MathContext
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/movies")
class MovieController(private val movieRepository: MovieInfoTopRatedRepository,
                      private val movieLikesRepository: MovieLikesRepository,
                      private val userRepository: UserRepository,
                      private val firebaseService: FirebaseService,
                      private val searchService: SearchService) {

    @PostMapping("/like-movie")
    fun likeMovie(@RequestBody like: MovieLikes, request: HttpServletRequest): ResponseEntity<Any> {
        val movieId = like.movieId ?: throw IllegalArgumentException("Movie ID is null")
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
            val existingLike = movieLikesRepository.findByUserIdAndMovieId(userId, movieId)
            if (existingLike == null) {
                val movieLike = MovieLikes(
                    userId = userId,
                    movieId = movieId)
                val savedLike = movieLikesRepository.save(movieLike)
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
        val likedMovies = movieLikesRepository.findLikedMoviesByUserId4(userId)
        println("reviews for liked movies: ${likedMovies.map { "${it.title} ${it.reviewContent}" }}")
        return ResponseEntity.ok(likedMovies)
    }

    @GetMapping("/search-movies")
    fun searchMovies(@RequestParam query: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") size: Int,
                     request: HttpServletRequest): ResponseEntity<Page<MovieInfoTopRatedProjection>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val pinceoneQueryResult = searchService.pineconeSearchQuery(query)
        val movies = movieRepository.findByIdsAndUser(userId, pinceoneQueryResult, PageRequest.of(page, size))
        return ResponseEntity.ok(movies)
    }

    @GetMapping("/recommendations")
    fun getRecommendations(request: HttpServletRequest): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val likedMovies = movieLikesRepository.findLikedMoviesByUserId4(userId)
        val recommendedMovieIds = searchService.pineconeRecommendations(likedMovies)
        val recommendedMovies = movieRepository.findByIdsAndUser(userId, recommendedMovieIds, PageRequest.of(0, 100))
        println("recommended movies: ${recommendedMovies.content.map { it.title }}")
        return ResponseEntity.ok(recommendedMovies.content)
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
