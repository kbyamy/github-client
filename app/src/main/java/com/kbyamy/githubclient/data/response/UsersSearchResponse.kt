package com.kbyamy.githubclient.data.response

import com.kbyamy.githubclient.data.model.User

data class UsersSearchResponse(
    val total_count: Int = 0,
    val items: List<User> = emptyList(),
    val nextPage: Int? = null
)
