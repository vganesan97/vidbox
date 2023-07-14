package com.vidbox.backend.entities

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "username", nullable = false)
    var username: String? = null,

    @Column(name = "password", nullable = false)
    var password: String? = null,

    @Column(name = "first_name", nullable = false)
    var firstName: String? = null,

    @Column(name = "last_name", nullable = false)
    var lastName: String? = null,

    @Column(name = "dob", nullable = false)
    var dob: LocalDate? = null,

    @Column(name = "firebase_uid", nullable = false)
    var firebaseUid: String? = null,

    @Column(name = "profile_pic", nullable = true)
    var profilePic: String? = null

)
