package com.kbyamy.githubclient.ui.userrepositories

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.kbyamy.githubclient.common.util.Injection
import com.kbyamy.githubclient.data.model.Repository
import com.kbyamy.githubclient.data.model.User
import com.kbyamy.githubclient.databinding.FragmentUserRepositoriesBinding
import com.kbyamy.githubclient.ui.common.LoadStateAdapter
import com.kbyamy.githubclient.ui.searchusers.BUNDLE_KEY_USER_ID
import com.kbyamy.githubclient.ui.searchusers.UserRepositoriesUiAction
import com.kbyamy.githubclient.ui.searchusers.UserRepositoriesUiState
import com.kbyamy.githubclient.ui.searchusers.UserRepositoriesViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class UserRepositoriesFragment : Fragment() {

    private val args: UserRepositoriesFragmentArgs by navArgs()
    private var _binding: FragmentUserRepositoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserRepositoriesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("::: navigation args.userId is ${args.userId}")
        _binding = FragmentUserRepositoriesBinding.inflate(inflater, container, false)

        val bundle = Bundle()
        bundle.putString(BUNDLE_KEY_USER_ID, args.userId)

        viewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(this, bundle)
        )[UserRepositoriesViewModel::class.java]

        val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.recyclerView.addItemDecoration(decoration)

        Observer<User> {
            updateUserDetailView(it)
            binding.bindState(
                uiState = viewModel.state,
                pagingData = viewModel.pagingDataFlow,
                uiActions = viewModel.accept
            )
        }.also {
            viewModel.userDetail.observe(viewLifecycleOwner, it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUserDetailView(user: User) {
        Picasso.get().load(user.avatarUrl).into(binding.userDetailView.iconImageView)
        binding.userDetailView.userNameTextView.text = user.userId
        binding.userDetailView.fullNameTextView.text = user.name
        binding.userDetailView.followingTextView.text = user.following.toString()
        binding.userDetailView.followerTextView.text = user.followers.toString()
    }

    private fun FragmentUserRepositoriesBinding.bindState(
        uiState: StateFlow<UserRepositoriesUiState>,
        pagingData: Flow<PagingData<Repository>>,
        uiActions: (UserRepositoriesUiAction) -> Unit
    ) {
        val adapter = RepositoriesAdapter(RepositoryViewHolder.OnItemClickEvent {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(requireContext(), Uri.parse(it.repositoryUrl))
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

    private fun FragmentUserRepositoriesBinding.bindSearch(
        uiState: StateFlow<UserRepositoriesUiState>,
        onQueryChanged: (UserRepositoriesUiAction.Search) -> Unit
    ) {
        updateRepoListFromInput(onQueryChanged)
    }

    private fun FragmentUserRepositoriesBinding.updateRepoListFromInput(
        onQueryChanged: (UserRepositoriesUiAction.Search) -> Unit
    ) {
        recyclerView.scrollToPosition(0)
        val query = viewModel.userDetail.value?.userId
        query?.let {
            onQueryChanged(UserRepositoriesUiAction.Search(query = it))
        }
    }

    private fun FragmentUserRepositoriesBinding.bindList(
        adapter: RepositoriesAdapter,
        uiState: StateFlow<UserRepositoriesUiState>,
        pagingData: Flow<PagingData<Repository>>,
        onScrollChanged: (UserRepositoriesUiAction.Scroll) -> Unit
    ) {
        retryButton.setOnClickListener { adapter.retry() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(
                    UserRepositoriesUiAction.Scroll(
                        currentQuery = uiState.value.query
                    )
                )
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

}