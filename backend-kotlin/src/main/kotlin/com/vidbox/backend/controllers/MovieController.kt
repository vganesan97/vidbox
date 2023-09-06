package com.vidbox.backend.controllers

import com.google.gson.JsonParser
import com.vidbox.backend.repos.MovieLikesRepository
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.MovieLikes
import com.vidbox.backend.repos.MovieInfoTopRatedRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
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
                      @Value("\${openai-secret-key}") private val openaiSecret: String,
                      @Value("\${pinecone-api-key}") private val pineconeApiKey: String,
                      @Value("\${pinecone-db-url}") private val pineconeDbUrl: String) {

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
        val pinceoneQueryResult = pineconeSearchQuery(query)
        val movies = movieRepository.findByIdsAndUser(userId, pinceoneQueryResult, PageRequest.of(page, size))
        return ResponseEntity.ok(movies)
    }

    @GetMapping("/recommendations")
    fun getRecommendations(request: HttpServletRequest): ResponseEntity<Any> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val likedMovies = movieLikesRepository.findLikedMoviesByUserId4(userId)
        val recommendedMovieIds = pineconeRecommendations(likedMovies)
        val recommendedMovies = movieRepository.findByIdsAndUser(userId, recommendedMovieIds, PageRequest.of(0, 100))
        println("recommended movies: ${recommendedMovies.content.map { it.title }}")
        return ResponseEntity.ok(recommendedMovies.content)
    }

    fun pineconeRecommendations(likedMovies: List<MovieInfoTopRatedProjection>): List<Int> {
        val likedMovieIds = likedMovies.map { it.id }.toSet() // Collect IDs of liked movies

        val ids = likedMovies.map { it.id }.joinToString(separator = "&") { "ids=$it" }
        println("id string: $ids")

        val pineconeRequest = Request.Builder()
            .url("$pineconeDbUrl/vectors/fetch?${ids}")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Api-Key", pineconeApiKey)
            .build()

        OkHttpClient().newCall(pineconeRequest).execute().use { pineconeResponse ->
            if (!pineconeResponse.isSuccessful) throw IOException("Unexpected code $pineconeResponse")
            val res = pineconeResponse.body?.string() ?: "No response body"
            val pineconeJsonObject = JsonParser.parseString(res).asJsonObject["vectors"].asJsonObject

            var sumArray: Array<BigDecimal>? = null
            var vectorCount = 0
            for ((key, value) in pineconeJsonObject.entrySet()) {
                val vector = value.asJsonObject["values"].asJsonArray.map { it.asBigDecimal }
                println("Vector for key = $key: $vector")
                if (sumArray == null) {
                    sumArray = Array(vector.size) { BigDecimal.ZERO }
                }
                for (i in vector.indices) {
                    sumArray[i] = sumArray[i].add(vector[i])
                }
                vectorCount++
            }
            val avgArray = sumArray!!.map { it.divide(BigDecimal(vectorCount), MathContext.DECIMAL64) }
            println("Average Vector: $avgArray")

            val pineconeRequestBodyJson = """{
                "topK": 100,
                "vector": $avgArray
            }"""
            val pineconeRequest = Request.Builder()
                .url("$pineconeDbUrl/query")
                .post(pineconeRequestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("Api-Key", pineconeApiKey)
                .build()

            OkHttpClient().newCall(pineconeRequest).execute().use { pineconeResponse ->
                if (!pineconeResponse.isSuccessful) throw IOException("Unexpected code $pineconeResponse")
                val res = pineconeResponse.body?.string() ?: "No response body"
                val pineconeJsonObject = JsonParser.parseString(res).asJsonObject["matches"].asJsonArray
                val movieIds = pineconeJsonObject.map { it.asJsonObject.get("id").asString.toInt() }
                return movieIds.filterNot { it in likedMovieIds }
            }
        }
    }

    fun pineconeSearchQuery(query: String): List<Int> {
        val client = OkHttpClient()
        // Specify the model and input text
        val searchQueryJsonString = """{
            "model": "text-embedding-ada-002",
            "input": "$query"
        }"""
        val openAIEmbeddingRequestBody: okhttp3.RequestBody = searchQueryJsonString.toRequestBody("application/json".toMediaTypeOrNull())
        val openAIEmbeddingRequest = Request.Builder()
            .url("https://api.openai.com/v1/embeddings")
            .post(openAIEmbeddingRequestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $openaiSecret") // Replace with your actual API key
            .build()
        client.newCall(openAIEmbeddingRequest).execute().use { embeddingResponse ->
            if (!embeddingResponse.isSuccessful) throw IOException("Unexpected code $embeddingResponse")
            val embeddingResponseBody = embeddingResponse.body?.string() ?: "No response body"
            val embeddingJsonObject = JsonParser.parseString(embeddingResponseBody).asJsonObject
            val embeddedVector = embeddingJsonObject["data"].asJsonArray[0].asJsonObject["embedding"].asJsonArray
            val pineconeRequestBodyJson = """{
                "topK": 10,
                "vector": $embeddedVector
            }"""

            val pineconeRequest = Request.Builder()
                .url("$pineconeDbUrl/query")
                .post(pineconeRequestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("Api-Key", pineconeApiKey)
                .build()

            client.newCall(pineconeRequest).execute().use { pineconeResponse ->
                if (!pineconeResponse.isSuccessful) throw IOException("Unexpected code $pineconeResponse")
                val res = pineconeResponse.body?.string() ?: "No response body"
                val pineconeJsonObject = JsonParser.parseString(res).asJsonObject["matches"].asJsonArray
                val movieIds = pineconeJsonObject.map { it.asJsonObject.get("id").asString.toInt() }
                return movieIds
            }
        }
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
