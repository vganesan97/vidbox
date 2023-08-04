package com.vidbox.backend.controllers

import com.google.gson.JsonParser
import com.vidbox.backend.repos.MovieLikesRepository
import com.vidbox.backend.entities.MovieInfoTopRated
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import com.vidbox.backend.entities.MovieLikes
import com.vidbox.backend.repos.MovieInfoTopRatedRepository
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
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
        //println("liked movies: $likedMovies")
        //likedMovies.forEach { movie -> movie.liked = true }
        return ResponseEntity.ok(likedMovies)
    }

    @GetMapping("/search-movies")
    fun searchMovies(@RequestParam query: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") size: Int,
                     request: HttpServletRequest): ResponseEntity<Page<MovieInfoTopRatedProjection>> {
        val uid = firebaseService.getUidFromFirebaseToken(request = request)
        val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val pageable = PageRequest.of(page, size)
        //val resultsPage = movieRepository.findByTitleContains2(query, pageable, userId)
        val pc = pineconeQuery(query)
        val rp1 = movieRepository.findByIdsAndUser(userId, pc, pageable)
       // val resultsPage = movieRepository.findByTitleContains(query, pageable)

//        val likedMovies = movieLikesRepository.findLikedMoviesByUserId(userId)
//        resultsPage.content.forEach { movie ->
//            if (movie in likedMovies) movie.liked = true
//        }
        //val resultsPage = movieRepository.findAllWithLiked(userId)
        //val resultsPage = movieRepository.findMoviesWithLikedFlag(query, userId, pageable)
        //println(rp1)
        return ResponseEntity.ok(rp1)
    }

    //@GetMapping("/pinecone-query")
    fun pineconeQuery(query: String): List<Int> {
        val client = OkHttpClient()
        // Specify the model and input text
        println("query: $query")
        val jsonString = """{
            "model": "text-embedding-ada-002",
            "input": "$query"
        }"""
        val requestBody: okhttp3.RequestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
        val request0 = Request.Builder()
                .url("https://api.openai.com/v1/embeddings")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer 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") // Replace with your actual API key
                .build()
        client.newCall(request0).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body?.string() ?: "No response body"
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            val data = jsonObject["data"].asJsonArray[0].asJsonObject["embedding"].asJsonArray
            val requestBodyJson = """{
                    "topK": 10,
                    "vector": $data
            }"""
            val request3 = Request.Builder()
                    .url("https://vidbox-movie-search-6b646aa.svc.us-west1-gcp-free.pinecone.io/query")
                    .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("Api-Key", "eb741bc8-c5a9-49a5-9037-5d65a9fbd465")
                    .build()
            client.newCall(request3).execute().use { response1 ->
                if (!response1.isSuccessful) throw IOException("Unexpected code $response1")
                val res = response1.body?.string() ?: "No response body"
                val jsonObject1 = JsonParser.parseString(res).asJsonObject["matches"].asJsonArray
                val ids = jsonObject1.map { it.asJsonObject.get("id").asString.toInt() }
                return ids
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
