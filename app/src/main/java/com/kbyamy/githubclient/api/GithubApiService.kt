package com.kbyamy.githubclient.api

import com.kbyamy.githubclient.BuildConfig
import com.kbyamy.githubclient.data.response.RepositoriesResponse
import com.kbyamy.githubclient.data.response.UsersSearchResponse
import com.kbyamy.githubclient.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApiService {

    @GET("search/users?")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): UsersSearchResponse

    @GET("users/{user}")
    suspend fun getUserDetail(
        @Path("user") user: String
    ): Response<User>

    @GET("search/repositories?")
    suspend fun searchRepositories(
        @Query("q", encoded = true) query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): RepositoriesResponse

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GithubApiService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = Level.BASIC

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            val client = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val request: Request = chain
                        .request()
                        .newBuilder()
                        .addHeader("Accept", "application/vnd.github.v3+json")
                        .addHeader("Authorization", BuildConfig.GITHUB_API_TOKEN)
                        .build()
                    chain.proceed(request)
                })
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