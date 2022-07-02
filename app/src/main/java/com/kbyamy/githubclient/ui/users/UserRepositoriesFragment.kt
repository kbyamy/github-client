package com.kbyamy.githubclient.ui.users

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import com.kbyamy.githubclient.common.util.Injection
import com.kbyamy.githubclient.data.model.Repository
import com.kbyamy.githubclient.data.model.User
import com.kbyamy.githubclient.databinding.FragmentSearchUsersBinding
import com.kbyamy.githubclient.databinding.FragmentUserRepositoriesBinding
import com.kbyamy.githubclient.ui.ViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class UserRepositoriesFragment : Fragment() {

    private val args: UserRepositoriesFragmentArgs by navArgs()
    private var _binding: FragmentUserRepositoriesBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = SearchUsersFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("::: navigation args.userId is ${args.userId}")
        _binding = FragmentUserRepositoriesBinding.inflate(inflater, container, false)

        val bundle = Bundle()
        bundle.putString("bundle_key_userId", args.userId)

        val viewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(this, bundle)
        )[UserRepositoriesViewModel::class.java]

        val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.recyclerView.addItemDecoration(decoration)

        viewModel.userDetail.observe(viewLifecycleOwner, Observer {
            binding.userDetailView.userNameTextView.text = it.userId
            binding.userDetailView.fullNameTextView.text = it.name
        })

//        binding.bindState(
//            uiState = viewModel.state,
//            pagingData = viewModel.pagingDataFlow,
//            uiActions = viewModel.accept
//        )

        Observer<User> {
            updateUserDetailView(it)
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
        Picasso.get().load(user.avatar_url).into(binding.userDetailView.iconImageView)
        binding.userDetailView.userNameTextView.text = user.userId
        binding.userDetailView.fullNameTextView.text = user.name
        binding.userDetailView.followingTextView.text = user.following.toString()
        binding.userDetailView.followerTextView.text = user.followers.toString()
    }

//    private fun FragmentUserRepositoriesBinding.bindState(
//        uiState: StateFlow<UserRepositoriesUiState>,
//        pagingData: Flow<PagingData<Repository>>,
//        uiActions: (UserRepositoriesUiAction) -> Unit
//    ) {
//        val adapter = RepositoriesAdapter()
//        recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
//            header = UsersLoadStateAdapter { adapter.retry() },
//            footer = UsersLoadStateAdapter { adapter.retry() }
//        )
//
//        bindSearch(
//            uiState = uiState,
//            onQueryChanged = uiActions
//        )
//
//        bindList(
//            adapter = adapter,
//            uiState = uiState,
//            pagingData = pagingData,
//            onScrollChanged = uiActions
//        )
//    }

//    private fun FragmentUserRepositoriesBinding.bindLoad(
//        uiState: StateFlow<UserRepositoriesUiState>,
//    ) {
//
//    }

    private fun FragmentUserRepositoriesBinding.bindSearch(
        uiState: StateFlow<UserRepositoriesUiState>,
        onQueryChanged: (UserRepositoriesUiAction.Search) -> Unit
    ) {
//        editText.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_GO) {
//                updateRepoListFromInput(onQueryChanged)
//                true
//            } else {
//                false
//            }
//        }

//        editText.setOnKeyListener { _, keyCode, event ->
//            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                updateRepoListFromInput(onQueryChanged)
//                hideKeyboard()
//                true
//            } else {
//                false
//            }
//        }

        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(userDetailView.userNameTextView::setText)
        }
    }

//    private fun FragmentUserRepositoriesBinding.updateRepoListFromInput(
//        onQueryChanged: (UserRepositoriesUiAction.Search) -> Unit
//    ) {
//        recyclerView.scrollToPosition(0)
//        onQueryChanged(UserRepositoriesUiAction.Search(query = it.toString()))
//        editText.text.trim().let {
//            if (it.isNotEmpty()) {
//                recyclerView.scrollToPosition(0)
//                onQueryChanged(SearchUsersUiAction.Search(query = it.toString()))
//            }
//        }
//    }

}