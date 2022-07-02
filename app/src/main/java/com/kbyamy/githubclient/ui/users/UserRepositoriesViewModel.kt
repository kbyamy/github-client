package com.kbyamy.githubclient.ui.users

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kbyamy.githubclient.data.GithubRepository
import com.kbyamy.githubclient.data.model.Repository
import com.kbyamy.githubclient.data.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class UserRepositoriesViewModel(
    private val repository: GithubRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    lateinit var userDetail: LiveData<User>

    val state: StateFlow<UserRepositoriesUiState>

    val pagingDataFlow: Flow<PagingData<Repository>>

    val accept: (UserRepositoriesUiAction) -> Unit

    init {
        val userId = savedStateHandle.get<String>(key = "bundle_key_userId")
        Timber.d("::: bundle_key_userId is $userId")
        userId?.let {
            repository.getUserDetail(userId).asLiveData().let {
                userDetail = it
            }
        }

        val initialQuery: String = savedStateHandle[LAST_REPOSITORY_QUERY] ?: DEFAULT_QUERY
        val lastQueryScrolled: String = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<UserRepositoriesUiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UserRepositoriesUiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UserRepositoriesUiAction.Search(query = initialQuery)) }

        val queriesScrolled = actionStateFlow
            .filterIsInstance<UserRepositoriesUiAction.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(UserRepositoriesUiAction.Scroll(currentQuery = lastQueryScrolled)) }

        pagingDataFlow = searches
            .flatMapLatest { searchRepository(it.query) }
            .cachedIn(viewModelScope)

        state = combine(
            searches,
            queriesScrolled,
            ::Pair
        ).map { (search, scroll) ->
            UserRepositoriesUiState(
                query = search.query,
                lastQueryScrolled = scroll.currentQuery,
                hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UserRepositoriesUiState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun searchRepository(query: String): Flow<PagingData<Repository>> =
        repository.getSearchRepositoryStream(query)
}

sealed class UserRepositoriesUiAction {
    data class Search(val query: String) : SearchUsersUiAction()
    data class Scroll(val currentQuery: String) : UserRepositoriesUiAction()
}

data class UserRepositoriesUiState(
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false
)

private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
private const val LAST_REPOSITORY_QUERY: String = "last_repository_query"
private const val DEFAULT_QUERY = ""