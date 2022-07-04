package com.kbyamy.githubclient.data.model

import com.squareup.moshi.Json

data class Repository(
    val name: String,
    val language: String?,
    val stargazers_count: Int,
    val description: String?,
    @Json(name = "html_url")
    val repositoryUrl: String
)
