package com.kbyamy.githubclient.api

import com.kbyamy.githubclient.data.UserSearchResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GithubApiService {

    @GET("search/users?")
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): UserSearchResponse

    companion object {
        private const val BASE_URL = "https://api/github.com/"

        fun create(): GithubApiService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = Level.BASIC

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GithubApiService::class.java)
        }
    }

}