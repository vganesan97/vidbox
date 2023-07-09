package com.vidbox.backend.entities

import javax.persistence.*

@Entity
@Table(name = "movie_likes")
data class MovieLikes(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "user_id")
    val userId: Int? = null,

    @Column(name = "movie_id")
    val movieId: Int? = null

)
