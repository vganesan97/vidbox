package com.vidbox.backend.models

data class FriendRequest(
    val requesterId: Int,
    val requestedId: Int,
    val id: Int,
)

