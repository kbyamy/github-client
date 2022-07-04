package com.kbyamy.githubclient.data.response

import com.kbyamy.githubclient.data.model.Repository

data class RepositoriesResponse(
    val total_count: Int = 0,
    val items: List<Repository> = emptyList(),
    val nextPage: Int? = null
)
