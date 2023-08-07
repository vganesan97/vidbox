package com.vidbox.backend.models

data class LoginResponse(
    val message: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val uid: String,
    val profilePic: String)
