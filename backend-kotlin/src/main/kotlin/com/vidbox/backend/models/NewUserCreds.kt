package com.vidbox.backend.models

import java.time.LocalDate

data class NewUserCreds(
    val username: String,
    val firstName: String,
    val lastName: String,
    val dob: String,
    val idToken: String)
