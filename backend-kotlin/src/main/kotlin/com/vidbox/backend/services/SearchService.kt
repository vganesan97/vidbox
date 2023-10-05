package com.vidbox.backend.services

import com.google.gson.JsonParser
import com.vidbox.backend.entities.MovieInfoTopRatedProjection
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.*
import java.math.BigDecimal
import java.math.MathContext

@Service
class SearchService(@Value("\${pinecone-db-url}") private val pineconeDbUrl: String,
                    @Value("\${pinecone-api-key}") private val pineconeApiKey: String,
                    @Value("\${openai-secret-key}") private val openaiSecret: String) {

    fun pineconeRecommendations(likedMovies: List<MovieInfoTopRatedProjection>): List<Int> {
        val likedMovieIds = likedMovies.map { it.id }.toSet() // Collect IDs of liked movies
        val queryParamIds = likedMovies.map { it.id }.joinToString(separator = "&") { "ids=$it" }
        println("id string: $queryParamIds")

        // find the vectors corresponding to the ids of the liked movies
        val pineconeRequest = Request.Builder()
            .url("$pineconeDbUrl/vectors/fetch?${queryParamIds}")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Api-Key", pineconeApiKey)
            .build()

        OkHttpClient().newCall(pineconeRequest).execute().use { pineconeResponse ->
            if (!pineconeResponse.isSuccessful) throw IOException("Unexpected code $pineconeResponse")
            val res = pineconeResponse.body?.string() ?: "No response body"
            val pineconeJsonObject = JsonParser.parseString(res).asJsonObject["vectors"].asJsonObject

            // average the vectors of liked movies
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
            val avgLikedMoviesVector = sumArray!!.map { it.divide(BigDecimal(vectorCount), MathContext.DECIMAL64) }

            println("Average Vector: $avgLikedMoviesVector")

            // find the most similar vectors to the averaged liked movies vector
            val pineconeRequestBodyJson = """{
                "topK": 100,
                "vector": $avgLikedMoviesVector
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



}