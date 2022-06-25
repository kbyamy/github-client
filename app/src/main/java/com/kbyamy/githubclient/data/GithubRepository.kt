package com.kbyamy.githubclient.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kbyamy.githubclient.api.GithubApiService
import com.kbyamy.githubclient.data.model.Repository
import com.kbyamy.githubclient.data.model.User
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class GithubRepository(
    private val service: GithubApiService
) {

    fun getSearchUserResultStream(query: String): Flow<PagingData<User>> {
        Timber.d("New query: $query")
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { UserPagingSource(service, query) }
        ).flow
    }

//    fun getUserRepositoryResultStream(query: String): Flow<PagingData<Repository>> {
//        Timber.d("New query: $query")
//    }

    companion object {
        const val NETWORK_PAGE_SIZE = 30
    }
}