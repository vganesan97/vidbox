package com.vidbox.backend.entities

import javax.persistence.*

//@Entity
//@Table(name = "movie_infos_top_rated")
interface MovieInfoTopRatedProjection {
    val id: Int?
    val poster_path: String?
    val backdrop_path: String?
    val overview: String?
    val title: String?
    val release_date: Int?
    val movie_id: Int?
    val liked: Boolean?
}
