package com.vidbox.backend.entities

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "movie_infos_top_rated")
data class MovieInfoTopRated(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    @Column(name = "poster_path")
    var posterPath: String? = null,

    @Column(name = "backdrop_path")
    var backdropPath: String? = null,

    @Column(name = "overview")
    var overview: String? = null,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "release_date")
    var releaseDate: Date? = null,

    @Column(name = "movie_id")
    var movieId: Int? = null,

    @Column(name = "adult")
    var adult: String? = null,

    @Column(name = "original_language")
    var originalLanguage: String? = null,

    @Column(name = "original_title")
    var originalTitle: String? = null,

    @Column(name = "popularity")
    var popularity: Float? = null,

    @Column(name = "video")
    var video: Boolean? = null,

    @Column(name = "vote_average")
    var voteAverage: Float? = null,

    @Column(name = "vote_count")
    var voteCount: Int? = null
)
