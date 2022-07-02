package com.kbyamy.githubclient.data.model

import com.squareup.moshi.Json

data class User(
    val avatar_url: String?,
    @Json(name = "login")
    val userId: String,
    val name: String?,
    val followers: Int = 0,
    val following: Int = 0
)
