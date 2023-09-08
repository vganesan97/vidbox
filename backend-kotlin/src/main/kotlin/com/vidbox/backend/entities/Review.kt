package com.vidbox.backend.entities

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Int? = null,

    @Column(name = "movie_id", nullable = false)
    var movieId: Int? = null,

    @Column(name = "review_content", nullable = false, columnDefinition = "TEXT")
    var reviewContent: String? = null
)
