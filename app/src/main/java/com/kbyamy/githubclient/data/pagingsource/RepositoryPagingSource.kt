package com.kbyamy.githubclient.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kbyamy.githubclient.api.GithubApiService
import com.kbyamy.githubclient.data.GithubApiRepository
import com.kbyamy.githubclient.data.model.Repository
import okio.IOException
import retrofit2.HttpException

private const val GITHUB_STARTING_PAGE_INDEX = 1

class RepositoryPagingSource(
    private val service: GithubApiService,
    private val query: String
) : PagingSource<Int, Repository>() {

    override fun getRefreshKey(state: PagingState<Int, Repository>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repository> {
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query
        return try {
            val response = service.searchRepositories(
                apiQuery,
                position,
                params.loadSize)
            val repositories = response.items
            val nextKey = if (repositories.isEmpty()) {
                null
            } else {
                position + (params.loadSize / GithubApiRepository.NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = repositories,
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