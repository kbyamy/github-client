package com.kbyamy.githubclient.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kbyamy.githubclient.api.GithubApiService
import com.kbyamy.githubclient.data.GithubRepository.Companion.NETWORK_PAGE_SIZE
import com.kbyamy.githubclient.data.model.User
import okio.IOException
import retrofit2.HttpException

private const val GITHUB_STARTING_PAGE_INDEX = 1

class UserPagingSource(
    private val service: GithubApiService,
    private val query: String
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query

        return try {
            val response = service.searchUsers(
                apiQuery,
                position,
                params.loadSize)
            val users = response.items
            val nextKey = if (users.isEmpty()) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = users,
                prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) null else position -1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

}