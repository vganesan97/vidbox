package com.vidbox.backend.models

data class LoginResponse(
    val message: String,
    val username: String,
    val uid: String,
    val profilePic: String)
