package com.kbyamy.githubclient.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.kbyamy.githubclient.data.GithubRepository
import com.kbyamy.githubclient.ui.users.SearchUsersViewModel
import com.kbyamy.githubclient.ui.users.UserRepositoriesViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: GithubRepository
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(SearchUsersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchUsersViewModel(repository, handle) as T
        }
        if (modelClass.isAssignableFrom(UserRepositoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserRepositoriesViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unexpected ViewModel class")
    }

}