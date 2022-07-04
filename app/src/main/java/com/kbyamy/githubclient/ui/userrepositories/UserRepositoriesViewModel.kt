package com.kbyamy.githubclient.ui.searchusers

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kbyamy.githubclient.data.GithubApiRepository
import com.kbyamy.githubclient.data.model.Repository
import com.kbyamy.githubclient.data.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class UserRepositoriesViewModel(
    private val repository: GithubApiRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    lateinit var userDetail: LiveData<User>

    val state: StateFlow<UserRepositoriesUiState>

    val pagingDataFlow: Flow<PagingData<Repository>>

    val accept: (UserRepositoriesUiAction) -> Unit

    init {
        val userId = savedStateHandle.get<String>(BUNDLE_KEY_USER_ID)
        Timber.d("::: bundle_key_userId is $userId")

        userId?.let {
            repository.getUserDetail(userId).asLiveData().let {
                userDetail = it
            }
        }

        val initialQuery: String = ("user:$userId")
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
            .flatMapLatest {
                Timber.d("::: pagingDataFlow searches query = ${it.query}")
                searchRepositories(it.query)
            }
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

    private fun searchRepositories(query: String): Flow<PagingData<Repository>> =
        repository.getSearchRepositoryStream(query)
}

sealed class UserRepositoriesUiAction {
    data class Search(val query: String) : UserRepositoriesUiAction()
    data class Scroll(val currentQuery: String) : UserRepositoriesUiAction()
}

data class UserRepositoriesUiState(
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false
)

const val BUNDLE_KEY_USER_ID: String = "bundle_key_userId"
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
private const val DEFAULT_QUERY = ""