package com.vidbox.backend.models

import java.time.LocalDate

data class NewUserCreds(
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val dob: LocalDate,
    val idToken: String)
