package com.vidbox.backend.models

data class GroupInfo(
    val group_name: String,
    val group_description: String,
    val group_admin_id: Int,
    val privacy: String? = null,
    val group_avatar: String? = null,
)

