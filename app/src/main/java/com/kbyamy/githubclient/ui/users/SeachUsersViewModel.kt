package com.kbyamy.githubclient.ui.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kbyamy.githubclient.data.GithubRepository
import com.kbyamy.githubclient.data.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchUsersViewModel(
    private val repository: GithubRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<SearchUsersUiState>

    val pagingDataFlow: Flow<PagingData<User>>

    val accept: (SearchUsersUiAction) -> Unit

    init {
        val initialQuery: String = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
        val lastQueryScrolled: String = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<SearchUsersUiAction>()

        val searches = actionStateFlow
            .filterIsInstance<SearchUsersUiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(SearchUsersUiAction.Search(query = initialQuery)) }

        val queriesScrolled = actionStateFlow
            .filterIsInstance<SearchUsersUiAction.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(SearchUsersUiAction.Scroll(currentQuery = lastQueryScrolled)) }

        pagingDataFlow = searches
            .flatMapLatest {
                if (it.query.isNotEmpty()) {
                    searchUsers(it.query)
                } else {
                    emptyFlow()
                }
            }
            .cachedIn(viewModelScope)

        state = combine(
            searches,
            queriesScrolled,
            ::Pair
        ).map { (search, scroll) ->
            SearchUsersUiState(
                query = search.query,
                lastQueryScrolled = scroll.currentQuery,
                hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = SearchUsersUiState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

    }

    private fun searchUsers(query: String): Flow<PagingData<User>> =
        repository.getSearchUserResultStream(query)
}

sealed class SearchUsersUiAction {
    data class Search(val query: String) : SearchUsersUiAction()
    data class Scroll(val currentQuery: String) : SearchUsersUiAction()
}

data class SearchUsersUiState(
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false
)

private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val DEFAULT_QUERY = ""