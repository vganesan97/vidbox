package com.vidbox.backend.entities

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
    var releaseDate: Int? = null,

    @Column(name = "movie_id")
    var movieId: Int? = null
)
