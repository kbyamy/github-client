package com.kbyamy.githubclient.ui.searchusers

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.kbyamy.githubclient.common.util.Injection
import com.kbyamy.githubclient.data.model.User
import com.kbyamy.githubclient.databinding.FragmentSearchUsersBinding
import com.kbyamy.githubclient.ui.common.LoadStateAdapter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchUsersFragment : Fragment() {

    private var _binding: FragmentSearchUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)

        val viewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(this)
        )[SearchUsersViewModel::class.java]

        val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.recyclerView.addItemDecoration(decoration)

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun FragmentSearchUsersBinding.bindState(
        uiState: StateFlow<SearchUsersUiState>,
        pagingData: Flow<PagingData<User>>,
        uiActions: (SearchUsersUiAction) -> Unit
    ) {
        val adapter = UsersAdapter(UserViewHolder.OnItemClickEvent {
            Timber.d("::: OnItemClickEvent user is ${it.userId}")
            val action = SearchUsersFragmentDirections
                .actionSearchUsersFragmentToUserRepositoriesFragment(it.userId)
            findNavController().navigate(action)
        })

        recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadStateAdapter { adapter.retry() },
            footer = LoadStateAdapter { adapter.retry() }
        )

        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions
        )

        bindList(
            adapter = adapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions
        )
    }

    private fun FragmentSearchUsersBinding.bindSearch(
        uiState: StateFlow<SearchUsersUiState>,
        onQueryChanged: (SearchUsersUiAction.Search) -> Unit
    ) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput(onQueryChanged)
                true
            } else {
                false
            }
        }

        editText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput(onQueryChanged)
                hideKeyboard()
                true
            } else {
                false
            }
        }

        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(editText::setText)
        }
    }

    private fun FragmentSearchUsersBinding.updateRepoListFromInput(
        onQueryChanged: (SearchUsersUiAction.Search) -> Unit
    ) {
        editText.text.trim().let {
            if (it.isNotEmpty()) {
                recyclerView.scrollToPosition(0)
                onQueryChanged(SearchUsersUiAction.Search(query = it.toString()))
            }
        }
    }

    private fun FragmentSearchUsersBinding.bindList(
        adapter: UsersAdapter,
        uiState: StateFlow<SearchUsersUiState>,
        pagingData: Flow<PagingData<User>>,
        onScrollChanged: (SearchUsersUiAction.Scroll) -> Unit
    ) {
        retryButton.setOnClickListener { adapter.retry() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(SearchUsersUiAction.Scroll(
                    currentQuery = uiState.value.query
                ))
            }
        })

        val notLoading = adapter.loadStateFlow.distinctUntilChangedBy {
            it.source.refresh
        }.map {
            it.source.refresh is LoadState.NotLoading
        }

        val hasNotScrolledForCurrentSearch = uiState.map {
            it.hasNotScrolledForCurrentSearch
        }.distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentSearch,
            Boolean::and
        ).distinctUntilChanged()

        lifecycleScope.launch {
            pagingData.collectLatest(adapter::submitData)
        }

        lifecycleScope.launch {
            shouldScrollToTop.collect { shouldScroll ->
                if (shouldScroll) recyclerView.scrollToPosition(0)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collect {
                // show empty message when search result is empty.
                val isEmpty = it.refresh is LoadState.NotLoading && adapter.itemCount == 0
                emptyTextView.isVisible = isEmpty
                recyclerView.isVisible = !isEmpty

                // show loading indicator.
                progressBar.isVisible = it.source.refresh is LoadState.Loading

                // show retry when search result is error.
                val hasError = it.source.refresh is LoadState.Error
                retryButton.isVisible = hasError
                recyclerView.isVisible = !hasError
            }
        }
    }

    private fun hideKeyboard() {
        if (activity?.currentFocus != null) {
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(
                activity?.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

}