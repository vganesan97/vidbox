package com.vidbox.backend.controllers

import com.vidbox.backend.entities.Review as ReviewEntity
import com.vidbox.backend.repos.ReviewRepository
import com.vidbox.backend.models.Review as ReviewModel
import com.vidbox.backend.repos.UserRepository
import com.vidbox.backend.services.FirebaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/review")
class ReviewController(private val userRepository: UserRepository,
                       private val reviewRepository: ReviewRepository,
                       private val firebaseService: FirebaseService) {

    @PostMapping("/create")
    fun createReview(@RequestBody reviewModel: ReviewModel, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val uid = firebaseService.getUidFromFirebaseToken(request = request)
            val userId = userRepository.findByFirebaseUid(uid).id ?: return ResponseEntity(HttpStatus.NOT_FOUND)
            val existingReview = reviewRepository.findByUserIdAndMovieId(userId, reviewModel.movieId)
            val savedReview: ReviewEntity
            if (existingReview != null) {
                // Update the existing review
                existingReview.reviewContent = reviewModel.reviewContent
                savedReview = reviewRepository.save(existingReview)
                println("updated review $savedReview")
            } else {
                // Create a new review
                val review = ReviewEntity(
                    userId = userId,
                    movieId = reviewModel.movieId,
                    reviewContent = reviewModel.reviewContent)
                savedReview = reviewRepository.save(review)
                println("saved review $savedReview")
            }
            ResponseEntity.ok(savedReview)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(reviewModel)
        }
    }


}
