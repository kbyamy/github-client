package com.kbyamy.githubclient.data

import com.kbyamy.githubclient.data.model.User

data class UserSearchResponse(
    val total_count: Int = 0,
    val items: List<User> = emptyList(),
    val nextPage: Int? = null
)
